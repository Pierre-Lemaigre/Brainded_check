package org.brainded.check.parser;

import jdk.jshell.spi.ExecutionControl;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;
import org.brainded.check.model.ctl.Parenthesis;
import org.brainded.check.model.exceptions.CtlException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CtlParser {

    public static List<Operand> parse(String ctlFormulaeString) throws ExecutionControl.NotImplementedException {

        List<Operand> ctlFormulae = new ArrayList<>();
        char[] ctlFormulaeCharArray = ctlFormulaeString.toCharArray();

        for (char character : ctlFormulaeCharArray)
            parseAndVerify(ctlFormulae, character);

        verifyParenthesis(ctlFormulae);

        return ctlFormulae;
    }

    private static void parseAndVerify(List<Operand> ctlFormulae, char character) throws ExecutionControl.NotImplementedException {
        int ctlFormulaeSize = ctlFormulae.size();
        Operand lastOperator;
        Operand operand = parseCharToOperand(character);

        ctlFormulae.add(operand);

        if (ctlFormulaeSize > 0) {
            lastOperator = ctlFormulae.get(ctlFormulaeSize - 1);

            if (operand instanceof Operator)
                verifyOperator(operand, lastOperator);
            else if (operand instanceof Atom)
                verifyAtomicProposition(lastOperator);
        }
    }

    private static Operand parseCharToOperand(char character) {
        return Objects.requireNonNullElseGet(
                Operator.valueOfOperator(character),
                () -> Objects.requireNonNullElseGet(
                        Parenthesis.valueOfOperator(character),
                        () -> new Atom(character)
                )
        );
    }

    private static void verifyExistOperator(Operand lastOperator) {
        if (lastOperator != Operator.Or &&
                lastOperator != Operator.And &&
                lastOperator != Operator.Imply)
            throw new CtlException("verifyAllOperator");
    }

    private static void verifyAllOperator(Operand lastOperator) {
        if (lastOperator != Operator.Or &&
                lastOperator != Operator.And &&
                lastOperator != Operator.Imply)
            throw new CtlException("verifyAllOperator");
    }

    private static void verifyNextOperator(Operand lastOperator) {
        if (lastOperator != Operator.Or &&
                lastOperator != Operator.And &&
                lastOperator != Operator.Imply &&
                lastOperator != Operator.Exist &&
                lastOperator != Operator.All)
            throw new CtlException("verifyNextOperator");
    }



    private static void verifyAndOperator(Operand lastOperator) {
        if (lastOperator != Operator.Or &&
                lastOperator != Operator.And &&
                lastOperator != Operator.Imply)
            throw new CtlException("verifyAllOperator");
    }

    private static void verifyOrOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyOrOperator");
    }

    private static void verifyNotOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyNotOperator");
    }

    private static void verifyImplyOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyImplyOperator");
    }

    private static void verifyTrueOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyTrueOperator");
    }

    private static void verifyFinallyOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyFinallyOperator");
    }

    private static void verifyGloballyOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyGloballyOperator");
    }

    private static void verifyUntilOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyUntilOperator");
    }

    private static void verifyWeakOperator(Operand lastOperator) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("verifyWeakOperator");
    }

    private static void verifyOperator(Operand operand, Operand lastOperator) throws ExecutionControl.NotImplementedException {
        switch ((Operator) operand) {
            case And -> verifyAndOperator(lastOperator);
            case Or -> verifyOrOperator(lastOperator);
            case Not -> verifyNotOperator(lastOperator);
            case Imply -> verifyImplyOperator(lastOperator);
            case All -> verifyAllOperator(lastOperator);
            case Exist -> verifyExistOperator(lastOperator);
            case Next -> verifyNextOperator(lastOperator);
            case True -> verifyTrueOperator(lastOperator);
            case Finally -> verifyFinallyOperator(lastOperator);
            case Globally -> verifyGloballyOperator(lastOperator);
            case Until -> verifyUntilOperator(lastOperator);
            case Weak -> verifyWeakOperator(lastOperator);
            default -> throw new CtlException("verifyOperator", "Invalid operator");
        }
    }

    private static void verifyParenthesis(List<Operand> ctlFormulae) {
        int parenthesisCount = 0;

        for (Operand op : ctlFormulae) {
            if (op instanceof Parenthesis) {
                if (op == Parenthesis.Open)
                    parenthesisCount++;
                else if (op == Parenthesis.Close)
                    parenthesisCount--;
                else
                    throw new CtlException("verifyParenthesis", "Invalid parenthesis");
            }
        }
        if (parenthesisCount != 0)
            throw new CtlException("verifyParenthesis");
    }

    private static void verifyAtomicProposition(Operand lastOperator) {
        if (lastOperator instanceof Atom || lastOperator == Operator.And || lastOperator == Operator.Or)
            throw new CtlException("verifyAtomicProposition");
    }

}

