package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;
import org.brainded.check.model.ctl.Parenthesis;
import org.brainded.check.utils.CtlUtils;

import java.util.*;

public class Checker {

    private final KripkeStructure kripkeStructure;
    private final List<Operand> ctrlFormulae;
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
        return marking(this.ctrlFormulae);
    }

    private boolean verify(State state, Atom proposition) {
        return state.getLabels().contains(proposition.getAtomicName());
    }

    private Set<State> marking(Atom proposition) {
        Set<State> marked = new HashSet<>();
        for (State state : this.kripkeStructure.getStates()) {
            if (this.verify(state, proposition))
                marked.add(state);
        }
        return marked;
    }

    private Set<State> markingTrue() {
            return new HashSet<>(this.kripkeStructure.getStates());
    }


    private Set<State> NOT(List<Operand> formulae) {
        Set<State> marked = marking(formulae);
        Set<State> to_mark = new HashSet<>();
        for (State current : this.kripkeStructure.getStates()) {
            if (!marked.contains(current)) {
                to_mark.add(current);
            }
        }
        return to_mark;
    }

    private Set<State> AND(List<Operand> formulae_1, List<Operand> formulae_2) {
        Set<State> sfp1 = marking(formulae_1);
        Set<State> sfp2 = marking(formulae_2);
        Set<State> to_mark = new HashSet<>();
        for (State current : this.kripkeStructure.getStates()) {
            if (sfp1.contains(current) && sfp2.contains(current)) {
                to_mark.add(current);
            }
        }
        return to_mark;
    }

    private Set<State> EX(List<Operand> formulae) {
        Set<State> marked = marking(formulae);
        List<State> successors;
        Set<State> to_mark = new HashSet<>();

        for (State current : this.kripkeStructure.getStates()) {
            successors = current.getSuccessors();
            if (!Collections.disjoint(marked, successors)) {
                to_mark.add(current);
            }
        }
        return to_mark;
    }

    private Set<State> EU(List<Operand> formulae_1, List<Operand> formulae_2) {
        Set<State> sp1 = marking(formulae_1);
        Set<State> sp2 = marking(formulae_2);
        Set<State> to_mark = new HashSet<>();
        Set<State> nextState = new HashSet<>();
        Set<State> checked_states = new HashSet<>();
        Set<State> parents;
        State currentState;

        do {
            for (Iterator<State> it = sp2.iterator(); it.hasNext();){
                currentState = it.next();
                to_mark.add(currentState);
                it.remove();

                parents = this.kripkeStructure.getParentState(currentState);
                for (State parent: parents) {
                    if (!checked_states.contains(parent)) {
                        checked_states.add(parent);
                        if (sp1.contains(parent)) {
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
        return to_mark;
    }

    private Set<State> AU(List<Operand> formulae_1, List<Operand> formulae_2) {
        Set<State> sp1 = marking(formulae_1);
        Set<State> sp2 = marking(formulae_2);
        Set<State> to_mark = new HashSet<>();
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
                to_mark.add(currentState);
                it.remove();

                parents = this.kripkeStructure.getParentState(currentState);
                for (State parent: parents) {
                    String name = parent.getStateName();
                    Integer val = successors.get(name);
                    val--;
                    successors.replace(name,val);
                    if (val == 0 && sp1.contains(parent) && !to_mark.contains(parent)) {
                       nextState.add(parent);
                    }
                }
            }
            if (!nextState.isEmpty()) {
                sp2.addAll(nextState);
                nextState.clear();
            }
        } while (!sp2.isEmpty());
        return to_mark;
    }

    private Set<State> marking(List<Operand> formulae) {
        Operand firstOperator = formulae.stream().findFirst().orElseThrow();
        if (firstOperator instanceof Atom) {
            return resolveAtom(formulae, (Atom) firstOperator);
        } else if (firstOperator instanceof Parenthesis) {
            return marking(CtlUtils.subtractParenthesis(formulae, (Parenthesis) firstOperator));
        } else {
            return resolveOperator(formulae, firstOperator);
        }
    }

    private Set<State> resolveOperator(List<Operand> formulae, Operand firstOperator) {
        switch ((Operator) firstOperator) {
            case Not -> {
                if (formulae.get(1) instanceof Parenthesis) {

                }
            }
            case Exist -> {

            }
            case True -> {
            }
            case Until -> {
            }
            default -> throw new NoSuchElementException(
                    String.format("Operator %s is not implemented, please consider find equivalence Operator", firstOperator));
        }
        return null;
    }

    private Set<State> resolveAtom(List<Operand> formulae, Atom firstOperator) {
        if (formulae.size() == 1) {
            return marking(firstOperator);
        } else {
            Set<State> mark =  marking(firstOperator);
            mark.addAll(marking(CtlUtils.minusFirstIndex(formulae)));
            return mark;
        }
    }

}
