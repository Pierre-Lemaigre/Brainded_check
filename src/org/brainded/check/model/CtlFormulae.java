package org.brainded.check.model;

import org.brainded.check.model.ctl.Operand;

import java.util.List;

public class CtlFormulae implements Operand{
    private final List<Operand> operands;

    CtlFormulae(List<Operand> operands) {
        this.operands = operands;
    }

    public List<Operand> getOperands() {
        return operands;
    }
}
