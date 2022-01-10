package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Checker {

    private final KripkeStructure kripkeStructure;
    private List<Operand> ctrlFormulae;
    private final Set<State> validatingStates;

    public Checker(KripkeStructure kripkeStructure, List<Operand> ctrlFormulae){
        this.kripkeStructure = kripkeStructure;
        this.ctrlFormulae = ctrlFormulae;
        this.validatingStates = new HashSet<>();
    }

    public Set<State> getValidatingStates() {
        return this.validatingStates;
    }

    public Set<State> marking(Atom proposition){
        Set<State> marked = new HashSet<>();
        for (State state: this.kripkeStructure.getStates()) {
            if(state.getLabels().contains(proposition.getAtomicName()))
                marked.add(state);
        }
        return marked;
    }

    public Set<State> reverseMarking(Atom proposition) {
        Set<State> marked = new HashSet<>();
        for (State state: this.kripkeStructure.getStates()) {
            if(!state.getLabels().contains(proposition.getAtomicName()))
                marked.add(state);
        }
        return marked;
    }

    public Set<State> AND(Atom p1, Atom p2) {
        Set<State> marked = new HashSet<>();
        for (State state: this.kripkeStructure.getStates()) {
            if (state.getLabels().contains(p1.getAtomicName()) &&
                state.getLabels().contains(p2.getAtomicName())) {
                marked.add(state);
            }
        }
        return marked;
    }
}
