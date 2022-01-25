package org.brainded.check.utils;

import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;
import org.brainded.check.model.ctl.Parenthesis;

import java.util.List;

public class CtlUtils {

    public static CtlFormulae extractNextSubFormulae(CtlFormulae ctlFormulae, int firstParenthesisIndex) {

        int subListLength = 0;
        int parenthesisVerifier = 0;
        int parenthesisCount = 0;

        for (int i = firstParenthesisIndex; i < ctlFormulae.getNbOperandsRecursive(); i++) {
            if (ctlFormulae.getOperand(i) == Parenthesis.Open) {
                parenthesisVerifier++;
                parenthesisCount++;
            } else if (ctlFormulae.getOperand(i) == Parenthesis.Close) {
                parenthesisVerifier--;
                parenthesisCount++;
            }

            if (parenthesisVerifier != 0) {
                subListLength++;
            } else
                break;
        }

        // +1 To remove the first parenthesis
        return subListLength > 0 ?
                new CtlFormulae(
                        ctlFormulae
                                .getOperands()
                                .subList(firstParenthesisIndex + 1, firstParenthesisIndex + subListLength), parenthesisCount)
                : ctlFormulae;
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
            return formulae.subList(index, index + 1);
        }
        return formulae;
    }
}
