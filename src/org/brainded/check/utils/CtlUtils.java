package org.brainded.check.utils;

import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Parenthesis;

import java.util.List;

public class CtlUtils {

    public static CtlFormulae extractNextSubFormulae(CtlFormulae ctlFormulae, int firstParenthesisIndex) {

        int subListLength = 0;
        int parenthesisCount = 0;

        for (int i = firstParenthesisIndex; i < ctlFormulae.getNbOperands() - 1; i++) {
            if (ctlFormulae.getOperand(i) == Parenthesis.Open)
                parenthesisCount++;
            else if (ctlFormulae.getOperand(i) == Parenthesis.Close)
                parenthesisCount--;

            if (parenthesisCount == 0) {
                break;
            } else {
                subListLength++;
            }
        }

        // +1 To remove the first parenthesis
        return new CtlFormulae(ctlFormulae.getOperands().subList(firstParenthesisIndex + 1, firstParenthesisIndex + subListLength));
    }

    public static List<Operand> minusFirstIndex(List<Operand> formulae) {
        if (formulae.size() > 1) {
            return formulae.subList(1, formulae.size());
        }
        return formulae;
    }

    public static List<Operand> minusXIndex(List<Operand> formulae, int index) {
        if (formulae.size() > index) {
            return formulae.subList(index, formulae.size());
        }
        return formulae;
    }

    public static List<Operand> uniqueAtIndex(List<Operand> formulae, int index) {
        if (formulae.size() >= index) {
            return formulae.subList(index, index+1);
        }
        return formulae;
    }
}
