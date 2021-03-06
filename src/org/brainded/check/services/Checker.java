package org.brainded.check.services;

import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.*;
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

    public List<String> getValidatingStates() {
        return this.validatingStates
                .stream()
                .map(State::getStateName)
                .sorted()
                .toList();
    }

    public List<String> getInitialStates() {
        return this.kripkeStructure
                .getInitialStates()
                .stream()
                .map(State::getStateName)
                .sorted()
                .toList();
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
        Set<State> mark = new HashSet<>();
        for (State state : this.kripkeStructure.getStates()) {
            if (this.verify(state, proposition))
                mark.add(state);
        }
        return mark;
    }

    private Set<State> NOT(List<Operand> formulae) {
        Set<State> atomic_mark = marking(formulae);
        Set<State> mark = new HashSet<>();
        for (State current : this.kripkeStructure.getStates()) {
            if (!atomic_mark.contains(current)) {
                mark.add(current);
            }
        }
        return mark;
    }

    private Set<State> AND(List<Operand> formulae_1, List<Operand> formulae_2) {
        Set<State> sfp1 = marking(formulae_1);
        Set<State> sfp2 = marking(formulae_2);
        Set<State> mark = new HashSet<>();
        for (State current : this.kripkeStructure.getStates()) {
            if (sfp1.contains(current) && sfp2.contains(current)) {
                mark.add(current);
            }
        }
        return mark;
    }

    private Set<State> OR(List<Operand> formulae_1, List<Operand> formulae_2) {
        Set<State> sfp1 = marking(formulae_1);
        Set<State> sfp2 = marking(formulae_2);
        Set<State> mark = new HashSet<>();
        for (State current : this.kripkeStructure.getStates()) {
            if (sfp1.contains(current) || sfp2.contains(current)) {
                mark.add(current);
            }
        }
        return mark;
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
        if (formulae.size() == 2 && formulae.get(1) instanceof Atom atom) {
            return this.NOT(CtlUtils.minusFirstIndex(formulae));
        } else if (formulae.size() > 2) {
            return this.NOT(CtlUtils.minusFirstIndex(formulae));
        } else if (formulae.get(1) instanceof CtlFormulae operand) {
            return this.NOT(operand.getOperands());
        }
        throw new RuntimeException("Operand Not must be followed by something");
    }

    private Set<State> computeAllOperator(List<Operand> formulae) {
        if (formulae.size() < 2) {
            throw new RuntimeException("Operand A must be followed by U");
        }
        if (formulae.size() > 2 && formulae.get(2) == Operator.Until) {
            return this.AU(CtlUtils.uniqueAtIndex(formulae, 1),
                    CtlUtils.minusXIndex(formulae, 3));
        } else if (formulae.get(1) instanceof CtlFormulae subCtl) {
            List<Operand> sub_formulae = subCtl.getOperands();
            if (sub_formulae.size() > 1 && sub_formulae.get(1) == Operator.Until) {
                return this.AU(CtlUtils.uniqueAtIndex(sub_formulae, 0),
                        CtlUtils.minusXIndex(sub_formulae, 2));
            }
        }
        throw new RuntimeException("Operand A must be followed by U");
    }

    private Set<State> computeExistOperator(List<Operand> formulae) {
        if (formulae.size() < 2) {
            throw new RuntimeException("Operand E must be followed by U or X");
        }
        if (formulae.size() == 2) {
            if (formulae.get(1) instanceof CtlFormulae subCtl) {
                List<Operand> subCtlOperands = subCtl.getOperands();
                if (subCtlOperands.size() > 1 && subCtlOperands.get(1) instanceof Operator operator) {
                    return operandAfterE(subCtlOperands, operator, true);
                }
            }
        } else {
            if (formulae.get(2) instanceof Operator operand) {
                return operandAfterE(formulae, operand, false);
            }
            if (formulae.get(1) instanceof Operator operand) {
                return operandAfterE(formulae, operand, false);
            }
        }
        throw new RuntimeException("Operand E must be followed by U or X");
    }

    private Set<State> operandAfterE(List<Operand> formulae, Operator operand, boolean subCtl) {
        int offsetFormulae = subCtl ? 1 : 0;
        switch (operand) {
            case Next -> {
                return this.EX(CtlUtils.minusXIndex(formulae, 2 - offsetFormulae));
            }
            case Until -> {
                return this.EU(CtlUtils.uniqueAtIndex(formulae, 1 - offsetFormulae),
                        CtlUtils.minusXIndex(formulae, 3 - offsetFormulae));
            }
            default -> throw new RuntimeException("Exist operator cannot be follow by anything but U and X");
        }
    }

    private Set<State> computeTrueOperator(List<Operand> formulae) {
        Set<State> states = new HashSet<>(this.kripkeStructure.getStates());
        if (formulae.size() > 1)
            states.addAll(marking(CtlUtils.minusFirstIndex(formulae)));
        return states;
    }

    private Set<State> computeAtom(List<Operand> formulae, Atom operand) {
        if (formulae.size() <= 1) {
            return marking(operand);
        }
        if (formulae.get(1) instanceof Operator operator) {
            switch (operator) {
                case And -> {
                    return this.AND(CtlUtils.uniqueAtIndex(formulae, 0), CtlUtils.minusXIndex(formulae, 2));
                }
                case Or -> {
                    return this.OR(CtlUtils.uniqueAtIndex(formulae, 0), CtlUtils.minusXIndex(formulae, 2));
                }
            }
        }
        Set<State> states = marking(operand);
        states.addAll(marking(CtlUtils.minusFirstIndex(formulae)));
        return states;
    }
}
