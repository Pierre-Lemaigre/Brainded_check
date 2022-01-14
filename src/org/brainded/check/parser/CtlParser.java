package org.brainded.check.parser;

import org.brainded.check.model.ctl.*;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.utils.CtlUtils;

import java.util.Collections;
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
                        else if (nextOperand == Operator.Globally)
                            translated = this.translateAG(ctlFormulae, i);

                            // AF
                        else if (nextOperand == Operator.Finally)
                            translated = this.translateAF(ctlFormulae, i);

                            // AU
                        else if (nextOperand == Parenthesis.Open)
                            translated = this.translateAU(ctlFormulae, i);

                        else
                            throw new CtlException("translate");
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

        return translated != null ? translated : ctlFormulae;
    }

    private CtlFormulae translateAX(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.Next);

        subFormulae1.addOperands(Operator.Not);

        if (operandPlus2 == Parenthesis.Open)
            subFormulae1.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            subFormulae1.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "AX error");

        translated.addOperands(subFormulae1);

        return translated;
    }

    private CtlFormulae translateAG(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        CtlFormulae subFormulae2 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);

        subFormulae1.addOperands(Operator.True);
        subFormulae1.addOperands(Operator.Until);

        subFormulae2.addOperands(Operator.Not);

        if (operandPlus2 == Parenthesis.Open)
            subFormulae2.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            subFormulae2.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "AX error");

        subFormulae1.addOperands(subFormulae2);
        translated.addOperands(subFormulae1);
        return translated;
    }

    private CtlFormulae translateAF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.Globally);

        subFormulae1.addOperands(Operator.Not);

        if (operandPlus2 == Parenthesis.Open)
            subFormulae1.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            subFormulae1.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "AF error");

        translated.addOperands(subFormulae1);
        return translated;
    }

    // TODO Finish + Refactor
    private CtlFormulae translateAU(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        CtlFormulae subFormulae2 = new CtlFormulae();
        CtlFormulae subFormulae3 = new CtlFormulae();
        CtlFormulae subFormulae4 = new CtlFormulae();
        CtlFormulae subFormulae5 = new CtlFormulae();

        CtlFormulae translatedSubFormulae1 = null;
        CtlFormulae translatedSubFormulae2;

        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);
        Operand operandPlusX = null;
        Operand operandPlusXPlusOne = null;

        if (operandPlus2 == Parenthesis.Open) {
            translatedSubFormulae1 = translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2));
            operandPlusX = ctlFormulae.getOperand(i + translatedSubFormulae1.getNbOperands());
        } else if (operandPlus2 instanceof Atom) {
            operandPlusX = ctlFormulae.getOperand(i + 3);
            translatedSubFormulae1 = new CtlFormulae(Collections.singletonList(operandPlus2));
        }else
            throw new CtlException("translate", "AU error 1");

        if (operandPlusX == Operator.Until) {
            operandPlusXPlusOne = ctlFormulae.getOperand(i + translatedSubFormulae1.getNbOperands() + 1);
            if (operandPlusXPlusOne == Parenthesis.Open) {
                translatedSubFormulae2 = translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + translatedSubFormulae1.getNbOperands() + 3));
            } else if (operandPlusXPlusOne instanceof Atom)
                translatedSubFormulae2 = new CtlFormulae(Collections.singletonList(operandPlusXPlusOne));
            else
                throw new CtlException("translate", "AU error 1");
        } else
            throw new CtlException("translate", "AU error 2");

        // ¬ψ
        subFormulae3.addOperands(Operator.Not);
        subFormulae3.addOperands(translatedSubFormulae2);

        // φ∨ψ
        subFormulae5.addOperands(translatedSubFormulae1);
        subFormulae5.addOperands(Operator.Or);
        subFormulae5.addOperands(translatedSubFormulae2);

        // ¬(φ∨ψ)
        subFormulae4.addOperands(Operator.Not);
        subFormulae4.addOperands(subFormulae5);

        // (¬ψ)U¬(φ∨ψ)
        subFormulae2.addOperands(subFormulae3);
        subFormulae2.addOperands(Operator.Until);
        subFormulae2.addOperands(subFormulae4);

        // E[(¬ψ)U¬(φ∨ψ)]
        subFormulae1.addOperands(Operator.Exist);
        subFormulae1.addOperands(subFormulae2);

        // ¬( E[(¬ψ)U¬(φ∨ψ)] ∨ EG(¬ψ) )
        translated.addOperands(Operator.Not);
        translated.addOperands(subFormulae1);
        translated.addOperands(Operator.Or);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.Globally);
        translated.addOperands(subFormulae3);

        return translated;
    }

    private CtlFormulae translateEF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Exist);
        subFormulae1.addOperands(Operator.True);
        subFormulae1.addOperands(Operator.Until);

        if (operandPlus2 == Parenthesis.Open)
            subFormulae1.addOperands(
                    translate(CtlUtils.extractNextSubFormulae(ctlFormulae, i + 2))
            );
        else if (operandPlus2 instanceof Atom)
            subFormulae1.addOperands(operandPlus2);
        else
            throw new CtlException("translate", "EF error");

        translated.addOperands(subFormulae1);
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

