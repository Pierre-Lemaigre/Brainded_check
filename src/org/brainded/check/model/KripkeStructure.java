package org.brainded.check.model;

import java.util.ArrayList;
import java.util.List;

public class KripkeStructure {
    private final List<State> states;

    KripkeStructure() {
        this.states = new ArrayList<>();
    }

    KripkeStructure(List<State> states) {
        this.states = states;
    }

    public void addState(State state) {
        if (!states.contains(state)) {
            states.add(state);
        }
    }
}
