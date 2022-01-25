package org.brainded.check.model.exceptions;

public class CtlException extends RuntimeException {

    public CtlException(){
        super("Invalid CTL formulae");
    }

    public CtlException(String msg) {
        super(msg);
    }
    
}
