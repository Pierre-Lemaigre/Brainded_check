package org.brainded.check.parser;

import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;
import org.brainded.check.model.ctl.Parenthesis;
import org.brainded.check.model.exceptions.CtlException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CtlParser {

    private List<Operand> ctlFormulae = new ArrayList<>();
    private int parenthesisCount = 0;

    public List<Operand> parse(String ctlFormulaeString) {

        char[] ctlFormulaeCharArray = ctlFormulaeString.toCharArray();

        for (char character : ctlFormulaeCharArray)
            this.verifyAndAddToCtlFormulae(this.parseCharToOperand(character));

        this.ctlFormulae = this.translate(this.ctlFormulae);

        this.verifyParenthesis();

        return ctlFormulae;
    }

    // region Utils

    private void verifyAndAddToCtlFormulae(Operand operand) {
        int ctlFormulaeSize = this.ctlFormulae.size();
        Operand lastOperator;

        this.ctlFormulae.add(operand);

        if (ctlFormulaeSize > 0) {
            lastOperator = this.ctlFormulae.get(ctlFormulaeSize - 1);

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

    private List<Operand> translate(List<Operand> ctlSubFormulae) {
        List<Operand> translated = new ArrayList<>();

        for (int i = 0; i < ctlSubFormulae.size() - 1; i++) {
            switch ((Operator) ctlSubFormulae.get(i)) {
                case All -> {

                    // AXp
                    if (ctlSubFormulae.get(i + 1) == Operator.Next) {
                        Operand operandPlus2 = ctlSubFormulae.get(i + 2);

                        if (operandPlus2 == Parenthesis.Open) {
                            //axTranslated = translate();
                        } else if (operandPlus2 instanceof Atom) {
                            translated.add(Operator.Not);
                            translated.add(Operator.Exist);
                            translated.add(Parenthesis.Open);
                            translated.add(Operator.True);
                            translated.add(Operator.Until);
                            translated.add(Parenthesis.Open);
                            translated.add(Operator.Not);
                            translated.add(operandPlus2);
                            translated.add(Parenthesis.Close);
                            translated.add(Parenthesis.Close);
                            ctlSubFormulae.removeAll(ctlSubFormulae.subList(i, i + 2));
                            ctlSubFormulae.addAll(i, translated);
                        } else
                            throw new CtlException("translate", "AX error");
                    }


                }

                case Exist -> {
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
        }

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

        if (this.parenthesisCount < -1 || this.parenthesisCount > 1)
            throw new CtlException("verifyParenthesis");
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

