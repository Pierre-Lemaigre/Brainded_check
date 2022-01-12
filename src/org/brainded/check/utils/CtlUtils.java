package org.brainded.check.utils;

import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Parenthesis;

import java.util.List;

public class CtlUtils {
    public static List<Operand> subtractParenthesis(List<Operand> formulae, Parenthesis firstOperator) {
        switch (firstOperator) {
            case Open -> {
                for (int index = formulae.size() - 1; index > 0; index--) {
                    if (formulae.get(index) == Parenthesis.Close)
                        return formulae.subList(0, index - 1);
                }
            }
            case Close -> throw new RuntimeException("Error parenthesis");
            default -> throw new RuntimeException("Unkown Parenthesis");
        }
        return formulae;
    }

    public static List<Operand> minusFirstIndex(List<Operand> formulae) {
        if (formulae.size() > 1) {
            return formulae.subList(1, formulae.size() - 1);
        }
        return formulae;
    }
}
