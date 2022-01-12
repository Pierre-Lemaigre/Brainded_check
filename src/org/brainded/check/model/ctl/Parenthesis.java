package org.brainded.check.model.ctl;

public enum Parenthesis implements Operand {
    Open('('),
    Close(')');

    public final char value;

    Parenthesis(char value) {
        this.value = value;
    }

    public static Parenthesis valueOfOperator(char operandChar) {
        for (Parenthesis o : values()) {
            if (o.value == operandChar)
                return o;
        }
        return null;
    }


}
