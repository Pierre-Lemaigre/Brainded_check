package org.brainded.check.model;

import java.util.ArrayList;
import java.util.List;

public class State {
    private boolean initialState;
    private final String stateName;
    private final List<State> successors;
    private final List<String> labels;

    State(boolean initialState, String stateName) {
       this.initialState = initialState;
       this.stateName = stateName;
       this.successors = new ArrayList<>();
       this.labels = new ArrayList<>();
    }

    State(String stateName) {
        this.stateName = stateName;
        this.initialState = false;
        this.successors = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    public boolean isInitialState() {
        return initialState;
    }

    public String getStateName() {
        return stateName;
    }

    public void addSuccesor(State successor) {
        if (!successors.contains(successor)) {
           addSuccesor(successor);
        }
    }

    public List<State> getSuccessors() {
        return successors;
    }

    public List<String> getLabels() {
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return stateName.equals(state.stateName);
    }

    @Override
    public int hashCode() {
        return stateName.hashCode();
    }
}
