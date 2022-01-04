package org.brainded.check.model;

import java.util.ArrayList;
import java.util.List;

public class KripkeStructure {
    private List<State> states;

    KripkeStructure() {
        this.states = new ArrayList<>();
    }

    KripkeStructure(List<State> states) {
        this.states = states;
    }
}
