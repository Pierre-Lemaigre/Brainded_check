package org.brainded.check.model.ctl;

public enum Operator implements Operand {
    Exist('E'),
    All('A'),
    Or('+'),
    And('*'),
    Next('X'),
    Not('-'),
    True('T'),
    F('F'),
    G('G');

    public final char value;

    Operator(char value) {
        this.value = value;
    }

    public static Operator valueOfOperator(char operandChar) {
        for (Operator o : values()) {
            if (o.value == operandChar)
                return o;
        }
        return null;
    }


}
