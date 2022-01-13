package org.brainded.check.model.ctl;

import java.util.ArrayList;
import java.util.List;

public class CtlFormulae implements Operand {

    private final List<Operand> operands;

    public CtlFormulae() {
        this.operands = new ArrayList<>();
    }

    public CtlFormulae(List<Operand> operands) {
        this.operands = operands;
    }

    public List<Operand> getOperands() {
        return this.operands;
    }

    public int getNbOperands() {
        return this.operands.size();
    }

    public Operand getOperand(int index) {
        return this.operands.get(index);
    }

    public void addOperands(Operand operand) {
        this.operands.add(operand);
    }

    @Override
    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        for (Operand op : this.operands) {
            if (op instanceof Operator)
                stringRepresentation.append(((Operator) op).value);
            else if (op instanceof Parenthesis)
                stringRepresentation.append(((Parenthesis) op).value);
            else
                stringRepresentation.append(op.toString());
        }
        return stringRepresentation.toString();
    }
}
