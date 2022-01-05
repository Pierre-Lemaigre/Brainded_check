package org.brainded.check.model.ctl;

public class Atom implements Operand {

    private final char atomicName;

    public Atom(char atomicName) {
        this.atomicName = atomicName;

    }

    public char getAtomicName() {
        return this.atomicName;
    }

}
