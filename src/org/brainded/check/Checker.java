package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;

import java.util.ArrayList;
import java.util.List;

public class Checker {

    private KripkeStructure kripkeStructure;
    private List<Operand> ctrlFormulae;
    private List<State> validatingStates;

    public Checker(KripkeStructure kripkeStructure, List<Operand> ctrlFormulae){
        this.kripkeStructure = kripkeStructure;
        this.ctrlFormulae = ctrlFormulae;
        this.validatingStates = new ArrayList<>();
    }

    public List<State> getValidatingStates() {
        return this.validatingStates;
    }

    public void marking(Operand operand){
        if(operand instanceof Atom){
            for (State state: this.kripkeStructure.getStates()) {
                if(state.getLabels().contains(((Atom) operand).getAtomicName()))
                    this.validatingStates.add(state);
            }
        }
    }









}
