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

    public Checker(KripkeStructure kripkeStructure, List<Operand> ctrlFormulae) {
        this.kripkeStructure = kripkeStructure;
        this.ctrlFormulae = ctrlFormulae;
        this.validatingStates = new HashSet<>();
    }
    public Set<State> getValidatingStates() {
        return this.validatingStates;
    }

    public Set<State> check(){
        // TODO
        return this.EX((Atom) ctrlFormulae.get(ctrlFormulae.size()-1));
    }



    private Set<State> marking(Atom proposition) {
        Set<State> marked = new HashSet<>();
        for (State state : this.kripkeStructure.getStates()) {
            if (this.verify(state, proposition))
                marked.add(state);
        }
        return marked;
    }

    private Set<State> reverseMarking(Atom proposition) {
        Set<State> marked = new HashSet<>();
        for (State state : this.kripkeStructure.getStates()) {
            if (!this.verify(state, proposition))
                marked.add(state);
        }
        return marked;
    }

    private Set<State> AND(Atom p1, Atom p2) {
        Set<State> marked = new HashSet<>();
        for (State state : this.kripkeStructure.getStates()) {
            if (this.verify(state, p1) && this.verify(state, p2)) {
                marked.add(state);
            }
        }
        return marked;
    }

    private Set<State> EX(Atom proposition) {
        Set<State> marked = new HashSet<>();

        this.marking(proposition);

        for (State state : this.kripkeStructure.getStates()) {
            if (state.getSuccessors()
                    .stream().map(successor -> (State) successor)
                    .allMatch(successor -> this.verify(successor, proposition))) {
                marked.add(state);
            }
        }

        return marked;
    }

    private boolean verify(State state, Atom proposition) {
        return state.getLabels().contains(proposition.getAtomicName());
    }

}
