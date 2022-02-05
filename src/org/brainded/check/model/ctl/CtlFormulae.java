package org.brainded.check.model.ctl;

import java.util.ArrayList;
import java.util.List;

public class CtlFormulae implements Operand {

    private int nbParenthesis;

    private final List<Operand> operands;

    public CtlFormulae() {
        this.operands = new ArrayList<>();
    }

    public CtlFormulae(List<Operand> operands, int nbParenthesis) {
        this.operands = operands;
        this.nbParenthesis = nbParenthesis;
    }

    public List<Operand> getOperands() {
        return this.operands;
    }

    public int getNbOperandsRecursive() {
        int size = 0;

        for (Operand operand : this.operands) {
            if (operand instanceof CtlFormulae)
                size += ((CtlFormulae) operand).getNbOperandsRecursive();
            else
                size++;
        }

        return size;
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

    public int getNbParenthesis() {
        return this.nbParenthesis;
    }

    @Override
    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        for (Operand operand : this.operands)
            if (operand instanceof CtlFormulae)
                stringRepresentation.append(Parenthesis.Open.value).append(operand).append(Parenthesis.Close.value);
            else if (operand instanceof Operator operator)
                stringRepresentation.append(operator.value);
            else
                stringRepresentation.append(operand.toString());

        return stringRepresentation.toString();
    }
}
