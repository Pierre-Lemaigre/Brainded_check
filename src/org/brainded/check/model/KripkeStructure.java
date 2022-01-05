package org.brainded.check.model;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class KripkeStructure {

    private final List<State> states;

    public KripkeStructure() {
        this.states = new ArrayList<State>();
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

    public List<State> getStates() {
        return this.states;
    }
}
