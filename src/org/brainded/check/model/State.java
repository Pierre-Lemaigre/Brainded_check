package org.brainded.check.model;

import java.util.*;

public class State {
    private boolean initialState;
    private final String stateName;
    private final List<State> successors;
    private final List<Character> labels;

    public State(boolean initialState, String stateName) {
        this.initialState = initialState;
        this.stateName = stateName;
        this.successors = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    public State(String stateName) {
        this.stateName = stateName;
        this.initialState = false;
        this.successors = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    public String getStateName() {
        return stateName;
    }

    public List<State> getSuccessors() {
        return successors;
    }

    public List<Character> getLabels() {
        return labels;
    }

    public boolean isInitialState() {
        return initialState;
    }

    public void markAsInitialState() {
        this.initialState = true;
    }

    public void addSuccessor(State successor) {
        if (!successors.contains(successor)) {
            this.successors.add(successor);
        }
    }

    public void addLabel(Character label) {
        this.labels.add(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.stateName.getClass() != o.getClass()) return false;
        String stateName = (String) o;
        return this.stateName.equals(stateName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stateName);
    }

    @Override
    public String toString() {
        return "State{" +
                "stateName='" + stateName + '\'' +
                '}';
    }

}
