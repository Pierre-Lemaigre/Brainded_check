package org.brainded.check;

import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.*;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.utils.CtlUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    public boolean satisfyFormulae() {
        this.validatingStates.addAll(marking(this.ctrlFormulae));
        Set<State> validInitialStates = this.validatingStates.stream().filter(State::isInitialState).collect(Collectors.toSet());
        return validInitialStates.equals(this.kripkeStructure.getInitialStates());
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
            for (Iterator<State> it = sp2.iterator(); it.hasNext(); ) {
                currentState = it.next();
                to_mark.add(currentState);
                it.remove();

                parents = this.kripkeStructure.getParentState(currentState);
                for (State parent : parents) {
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

        for (State current : this.kripkeStructure.getStates()) {
            successors.put(current.getStateName(), current.getSuccessors().size());
        }

        do {
            for (Iterator<State> it = sp2.iterator(); it.hasNext(); ) {
                currentState = it.next();
                to_mark.add(currentState);
                it.remove();

                parents = this.kripkeStructure.getParentState(currentState);
                for (State parent : parents) {
                    String name = parent.getStateName();
                    Integer val = successors.get(name);
                    val--;
                    successors.replace(name, val);
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
        Operand operand = formulae.stream().findFirst().orElseThrow();
        if (operand instanceof CtlFormulae) {
            return computeSubFormulae(formulae, (CtlFormulae) operand);
        } else if (operand instanceof Operator) {
            return computeOperator(formulae, (Operator) operand);
        } else if (operand instanceof Atom) {
            return computeAtom(formulae, (Atom) operand);
        }
        return new HashSet<>();
    }

    private Set<State> computeAtom(List<Operand> formulae, Atom operand) {
        if (formulae.size() > 1) {
            if (formulae.get(1) instanceof Operator operator && operator == Operator.And) {
                return this.AND(CtlUtils.uniqueAtIndex(formulae, 0), CtlUtils.minusXIndex(formulae, 2));
            }
            Set<State> states = marking(operand);
            states.addAll(marking(CtlUtils.minusFirstIndex(formulae)));
            return states;
        }
        return marking(operand);
    }

    private Set<State> computeSubFormulae(List<Operand> formulae, CtlFormulae operand) {
        Set<State> states = marking(operand.getOperands());
        if (formulae.size() > 1) states.addAll(marking(CtlUtils.minusFirstIndex(formulae)));
        return states;
    }

    private Set<State> computeOperator(List<Operand> formulae, Operator operand) {
        switch (operand) {
            case Not -> {
                return computeNotOperator(formulae);
            }
            case All -> {
                return computeAllOperator(formulae);
            }
            case Exist -> {
                return computeExistOperator(formulae);
            }
            case True -> {
                return computeTrueOperator(formulae);
            }
            default -> throw new RuntimeException(
                    String.format("Operand %s is not suported", operand));
        }
    }

    private Set<State> computeNotOperator(List<Operand> formulae) {
        if (formulae.size() >= 2) {
            if (formulae.size() > 2) {
                return this.NOT(CtlUtils.minusFirstIndex(formulae));
            }
            if (formulae.get(1) instanceof CtlFormulae operand) {
                return this.NOT(operand.getOperands());
            }
        }
        throw new RuntimeException("CTL Syntax error");
    }

    private Set<State> computeExistOperator(List<Operand> formulae) {
        if (formulae.size() >= 2) {
            if (formulae.size() > 2) {
                switch ((Operator) formulae.get(2)) {
                    case Until -> {
                        return this.EU(CtlUtils.uniqueAtIndex(formulae, 1),
                                CtlUtils.minusXIndex(formulae, 3));
                    }
                    case Next -> {
                        return this.EX(formulae);
                    }
                    default -> throw new RuntimeException("Exist operator cannot be follow by anything but U and X");
                }
            }
            if (formulae.get(1) instanceof CtlFormulae subCtl) {
                List<Operand> subCtlOperands = subCtl.getOperands();
                if (subCtlOperands.size() > 1 && subCtlOperands.get(1) instanceof Operator operator) {
                    switch (operator) {
                        case Next -> {
                            return this.EX(subCtlOperands);
                        }
                        case Until -> {
                            return this.EU(CtlUtils.uniqueAtIndex(subCtlOperands, 0),
                                    CtlUtils.minusXIndex(subCtlOperands, 2));
                        }
                        default -> throw new RuntimeException("Operand E must be folowed by U and X");
                    }
                }
            }
        }
        throw new RuntimeException("Operand E must be followed by U and X");
    }

    private Set<State> computeAllOperator(List<Operand> formulae) {
        if (formulae.size() >= 2) {
            if (formulae.size() > 2 && formulae.get(2) == Operator.Until) {
                return this.AU(CtlUtils.uniqueAtIndex(formulae, 1),
                        CtlUtils.minusXIndex(formulae, 3));
            } else if (formulae.get(1) instanceof CtlFormulae subCtl) {
                List<Operand> sub_formulae = subCtl.getOperands();
                if (sub_formulae.size() > 1 && sub_formulae.get(1) == Operator.Until) {
                    return this.AU(CtlUtils.uniqueAtIndex(sub_formulae, 0),
                            CtlUtils.minusXIndex(sub_formulae, 2));
                }
            } else {
                throw new RuntimeException("Operand A must be followed by U");
            }
        }
        throw new RuntimeException("Operand A must be followed by U");
    }

    private Set<State> computeTrueOperator(List<Operand> formulae) {
        Set<State> states = new HashSet<>(this.kripkeStructure.getStates());
        if (formulae.size() > 1)
            states.addAll(marking(CtlUtils.minusFirstIndex(formulae)));
        return states;
    }
}
