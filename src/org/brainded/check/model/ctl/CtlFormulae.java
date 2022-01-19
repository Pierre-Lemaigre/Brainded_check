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
        int size = 0;

        for (Operand operand: this.operands) {
            if(operand instanceof CtlFormulae)
                size+= ((CtlFormulae) operand).getNbOperands();
            else
                size++;
        }

        return size;
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
        for (Operand operand : this.operands)
            if (operand instanceof CtlFormulae)
                stringRepresentation.append(Parenthesis.Open.value).append(operand.toString()).append(Parenthesis.Close.value);
            else if (operand instanceof Operator operator)
                stringRepresentation.append(operator.value);
            else
                stringRepresentation.append(operand.toString());

        return stringRepresentation.toString();
    }
}
