package org.brainded.check.model;

import org.brainded.check.model.exceptions.KripkeException;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Set;

public class KripkeStructure {

    private final List<State> states;

    public KripkeStructure() {
        this.states = new ArrayList<>();
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

    public void addLabeling(String stateName, List<Character> labeling) throws InstanceNotFoundException {
        State state = this.getStateByName(stateName);
        for (char labelingChar : labeling) {
            state.addLabel(labelingChar);
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

        Pattern labelMatch = Pattern.compile("[a-z]");

        for (State state : states) {
            for (Character label : state.getLabels()) {
                if (!labelMatch.matcher(label.toString()).find()) {
                    throw new KripkeException(
                            String.format("This state %s has an unauthorized label : %s", state.getStateName(), label)
                    );
                }
            }
        }
    }

    public List<State> getStates() {
        return this.states;
    }

    public Set<State> getParentState(State state) {
        Set<State> parents = new HashSet<>();
        for (State parent: this.getStates()) {
            if (parent.getSuccessors()
                    .stream()
                    .anyMatch(state1 -> state1.equals(state))) {
                parents.add(parent);
            }
        }
        return parents;
    }

    @Override
    public String toString() {
        return states.stream().map(Object::toString).reduce((s, s2) -> s + "\n" + s2).orElse("No States");
    }
}
