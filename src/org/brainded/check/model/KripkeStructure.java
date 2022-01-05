package org.brainded.check.model;

import org.brainded.check.model.exceptions.KripkeException;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class KripkeStructure {
    private Pattern labelsAuthorized;
    private final List<State> states;

    public KripkeStructure() {
        this.states = new ArrayList<>();
        labelsAuthorized = Pattern.compile("[a-z]");
    }

    public KripkeStructure(List<State> states) {
        this.states = states;
    }

    private boolean isStateExist(String stateName) {
        return this.states.stream().anyMatch(state -> state.getStateName().equals(stateName));
    }

    private State getStateByName(String stateName) throws InstanceNotFoundException {
        if (isStateExist(stateName))
            return this.states.stream().filter(state -> state.getStateName().equals(stateName)).findFirst().get();
        else
            throw new InstanceNotFoundException();
    }

    public void addState(State state) {
        if (!states.contains(state)) {
            states.add(state);
        }
    }

    public void markAsInitialState(String stateName) throws InstanceNotFoundException {
        this.getStateByName(stateName).markAsInitialState();
    }

    public void addSuccessors(String stateName, List<String> successors) throws InstanceNotFoundException {
        State state = this.getStateByName(stateName);
        for (String successorString : successors) {
            state.addSuccessor(this.getStateByName(successorString));
        }
    }

    public void addLabeling(String stateName, List<String> labeling) throws InstanceNotFoundException {
        State state = this.getStateByName(stateName);
        for (String labelingString : labeling) {
            state.addLabel(labelingString);
        }
    }

    public void validateKripkeStruct() {
        if (states.isEmpty()) {
            throw new KripkeException("No states in the Kripke Structure.");
        }

        if (states.stream().noneMatch(State::isInitialState)) {
            throw new KripkeException("No initial States in the Kripke Structure");
        }

        states.forEach(state -> {
            if (state.getSuccessors().isEmpty())
                throw new KripkeException("This state has no successor : " + state.getStateName());
        });

        states.forEach(state -> {
            for (String s: state.getLabels()) {
                if (this.labelsAuthorized.matcher(s).find()) {
                    throw new KripkeException("This state has unauthorized label : " + state.getStateName());
                }
            }
        });
    }

    @Override
    public String toString() {
        return states.stream().map(Object::toString).reduce((s, s2) -> s + "\n" + s2).orElse("No States");
    }
}
