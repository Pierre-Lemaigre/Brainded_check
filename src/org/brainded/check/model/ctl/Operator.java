package org.brainded.check.model.ctl;

public enum Operator implements Operand {
    And('*'),
    Or('+'),
    Not('-'),
    All('A'),
    Exist('E'),
    Next('X'),
    True('T'),
    Finally('F'),
    Globally('G'),
    Until('U'),
    Weak('W');

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
