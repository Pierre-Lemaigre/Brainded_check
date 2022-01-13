package org.brainded.check.parser;

import org.brainded.check.model.ctl.*;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.utils.CtlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CtlParser {

    private CtlFormulae ctlFormulae = new CtlFormulae();
    private int parenthesisCount = 0;

    public CtlFormulae parse(String ctlFormulaeString) {

        char[] ctlFormulaeCharArray = ctlFormulaeString.toCharArray();

        for (char character : ctlFormulaeCharArray)
            this.verifyAndAddToCtlFormulae(this.parseCharToOperand(character));

        this.verifyParenthesis();

        this.ctlFormulae = this.translate(this.ctlFormulae);


        return ctlFormulae;
    }

    // region Utils

    private void verifyAndAddToCtlFormulae(Operand operand) {
        int ctlFormulaeSize = this.ctlFormulae.getNbOperands();
        Operand lastOperator;

        this.ctlFormulae.addOperands(operand);

        if (ctlFormulaeSize > 0) {
            lastOperator = this.ctlFormulae.getOperand(ctlFormulaeSize - 1);

            if (operand instanceof Operator)
                this.verifyOperator(operand, lastOperator);
            else if (operand instanceof Parenthesis)
                this.countParenthesis(operand);
            else
                this.verifyAtomicProposition(lastOperator);
        }
    }

    private Operand parseCharToOperand(char character) {
        return Objects.requireNonNullElseGet(
                Operator.valueOfOperator(character),
                () -> Objects.requireNonNullElseGet(
                        Parenthesis.valueOfOperator(character),
                        () -> new Atom(character)
                )
        );
    }

    //endregion

    // region Translators

    private CtlFormulae translate(CtlFormulae ctlFormulae) {
        CtlFormulae translated = null;

        for (int i = 0; i < ctlFormulae.getNbOperands() - 1; i++) {

            Operand operand = ctlFormulae.getOperand(i);
            Operand nextOperand = ctlFormulae.getOperand(i + 1);

            if (operand instanceof Operator) {
                switch ((Operator) operand) {
                    case All -> {

                        // AX
                        if (nextOperand == Operator.Next)
                            translated = this.translateAX(ctlFormulae, i);

                        // AG
                        if (nextOperand == Operator.Globally)
                            translated = this.translateAG(ctlFormulae, i);

                        // AF
                        if (nextOperand == Operator.Finally)
                            translated = this.translateAF(ctlFormulae, i);
                    }
                    case Exist -> {
                        // EF
                        if (nextOperand == Operator.Finally)
                            translated = this.translateEF(ctlFormulae, i);
                    }
                    case Next -> {
                    }
                    case True -> {
                    }
                    case Finally -> {
                    }
                    case Globally -> {
                    }
                    case Until -> {
                    }
                }

                if (translated != null)
                    i = i + translated.getNbOperands() - 1;
            }
        }

        return translated;
    }

    private CtlFormulae translateAX(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Not);

        if (operandPlus2 == Parenthesis.Open)
            translated.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            translated.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "AX error");

        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        return translated;
    }

    private CtlFormulae translateAG(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Not);
        translated.addOperands(Parenthesis.Open);

        if (operandPlus2 == Parenthesis.Open)
            translated.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            translated.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "AX error");

        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        return translated;
    }

    private CtlFormulae translateAF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.Globally);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Not);

        if (operandPlus2 == Parenthesis.Open)
            translated.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            translated.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "AF error");

        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        return translated;
    }

    private CtlFormulae translateEF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Parenthesis.Open);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);

        if (operandPlus2 == Parenthesis.Open)
            translated.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            translated.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "EF error");

        translated.addOperands(Parenthesis.Close);
        translated.addOperands(Parenthesis.Close);
        return translated;
    }

    // endregion


    // region Verifiers

    private void verifyAtomicProposition(Operand lastOperator) {
        if (lastOperator == Parenthesis.Close ||
                lastOperator == Operator.True ||
                lastOperator instanceof Atom)
            throw new CtlException("verifyAtomicProposition");
    }

    private void verifyExistOperator(Operand lastOperator) {
        if (lastOperator != Parenthesis.Open &&
                lastOperator != Operator.Or &&
                lastOperator != Operator.And &&
                lastOperator != Operator.Imply)
            throw new CtlException("verifyExistOperator");
    }

    private void verifyAllOperator(Operand lastOperator) {
        if (lastOperator != Parenthesis.Open &&
                lastOperator != Operator.Or &&
                lastOperator != Operator.And &&
                lastOperator != Operator.Imply)
            throw new CtlException("verifyAllOperator");
    }

    private void verifyAndOrImplyOperator(Operand lastOperator) {
        if (!(lastOperator instanceof Atom || lastOperator == Parenthesis.Close))
            throw new CtlException("verifyAndOrImplyOperator");
    }

    private void verifyNotOperator(Operand lastOperator) {
        System.out.println("Wola je sais pas");
    }

    private void verifyTrueOperator(Operand lastOperator) {
        if (lastOperator instanceof Atom
                || lastOperator == Parenthesis.Close)
            throw new CtlException("verifyTrueOperator");
    }

    private void verifyNextOperator(Operand lastOperator) {
        if (lastOperator instanceof Atom
                || lastOperator == Parenthesis.Close
                || lastOperator == Operator.Next
                || lastOperator == Operator.Finally
                || lastOperator == Operator.Globally)
            throw new CtlException("verifyNextOperator");
    }

    private void verifyFinallyOperator(Operand lastOperator) {
        if (lastOperator instanceof Atom
                || lastOperator == Parenthesis.Close
                || lastOperator == Operator.Next
                || lastOperator == Operator.Until
                || lastOperator == Operator.Globally)
            throw new CtlException("verifyFinallyOperator");
    }

    private void verifyGloballyOperator(Operand lastOperator) {
        if (lastOperator instanceof Atom
                || lastOperator == Parenthesis.Close
                || lastOperator == Operator.Next
                || lastOperator == Operator.Until
                || lastOperator == Operator.Finally)
            throw new CtlException("verifyGloballyOperator");
    }

    private void verifyUntilOperator(Operand lastOperator) {
        if (lastOperator == Operator.Next
                || lastOperator == Operator.Globally
                || lastOperator == Operator.Finally)
            throw new CtlException("verifyUntilOperator");
    }

    private void verifyParenthesis() {
        if (this.parenthesisCount != 0)
            throw new CtlException("verifyParenthesis");
    }

    private void countParenthesis(Operand operand) {
        if (operand == Parenthesis.Open)
            this.parenthesisCount++;
        else if (operand == Parenthesis.Close)
            this.parenthesisCount--;
        else
            throw new CtlException("verifyParenthesis", "Invalid parenthesis");
    }

    private void verifyOperator(Operand operand, Operand lastOperator) {
        switch ((Operator) operand) {
            case And, Or, Imply -> verifyAndOrImplyOperator(lastOperator);
            case Not -> verifyNotOperator(lastOperator);
            case All -> verifyAllOperator(lastOperator);
            case Exist -> verifyExistOperator(lastOperator);
            case Next -> verifyNextOperator(lastOperator);
            case True -> verifyTrueOperator(lastOperator);
            case Finally -> verifyFinallyOperator(lastOperator);
            case Globally -> verifyGloballyOperator(lastOperator);
            case Until -> verifyUntilOperator(lastOperator);
            default -> throw new CtlException("verifyOperator", "Invalid operator");
        }
    }

    // endregion


}

