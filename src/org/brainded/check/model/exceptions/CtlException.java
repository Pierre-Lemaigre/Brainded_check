package org.brainded.check.model.exceptions;

public class CtlException extends RuntimeException {

    public CtlException(String methodName){
        super(methodName + " : Invdalid CTL syntax");
    }

    public CtlException(String methodName, String msg) {
        super(methodName + " : "+ msg);
    }
    
}
