package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;

import java.util.*;

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

    public Set<State> satisfyFormulae(){
        // TODO

        return this.AU(new Atom('p'), false, new Atom('v'), false);
    }

    private Set<State> mark(Atom proposition, boolean reverse) {
        if (reverse) {
            return reverseMarking(proposition);
        } else {
            return marking(proposition);
        }
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

        for (State state : this.kripkeStructure.getStates()) {
            if (state.getSuccessors()
                    .stream()
                    .anyMatch(successor -> this.verify(successor, proposition))) {
                marked.add(state);
            }
        }
        return marked;
    }

    private Set<State> EU(Atom p1, boolean reverse1, Atom p2, boolean reverse2) {
        Set<State> marked = new HashSet<>();
        Set<State> sp2 = mark(p2, reverse2);
        Set<State> nextState = new HashSet<>();
        Set<State> checked_states = new HashSet<>();
        Set<State> parents;
        State currentState;

        do {
            for (Iterator<State> it = sp2.iterator(); it.hasNext();){
                currentState = it.next();
                marked.add(currentState);
                it.remove();

                parents = this.kripkeStructure.getParentState(currentState);
                for (State parent: parents) {
                    if (!checked_states.contains(parent)) {
                        checked_states.add(parent);
                        if (verify(parent, p1)) {
                            nextState.add(parent);
                        }
                    }
                }
            }
            if (!nextState.isEmpty()) {
                sp2.addAll(nextState);
                nextState.clear();
            }
        } while (!sp2.isEmpty());
        return marked;
    }

    private Set<State> AU(Atom p1, boolean rev1, Atom p2, boolean rev2) {
        Set<State> marked = new HashSet<>();
        Set<State> sp2 = mark(p2, rev2);
        Set<State> nextState = new HashSet<>();
        Map<String, Integer> successors = new HashMap<>();
        Set<State> parents;
        State currentState;

        for (State current: this.kripkeStructure.getStates()) {
            successors.put(current.getStateName(), current.getSuccessors().size());
        }

        do {
            for (Iterator<State> it = sp2.iterator(); it.hasNext();) {
                currentState = it.next();
                marked.add(currentState);
                it.remove();

                parents = this.kripkeStructure.getParentState(currentState);
                for (State parent: parents) {
                    String name = parent.getStateName();
                    Integer val = successors.get(name);
                    val--;
                    successors.replace(name,val);
                    if (val == 0 && this.verify(parent, p1) && !marked.contains(parent)) {
                       nextState.add(parent);
                    }
                }
            }
            if (!nextState.isEmpty()) {
                sp2.addAll(nextState);
                nextState.clear();
            }
        } while (!sp2.isEmpty());
        return marked;
    }

    private boolean verify(State state, Atom proposition) {
        return state.getLabels().contains(proposition.getAtomicName());
    }

}
