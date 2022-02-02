package org.brainded.check.parser;

import org.brainded.check.model.ctl.*;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.utils.CtlUtils;

import java.util.Objects;

public class CtlParser {

    private final CtlFormulae ctlFormulae = new CtlFormulae();
    private int parenthesisCount = 0;
    private boolean nextShouldBeParenthesis;
    private boolean containsOperator;
    private boolean containsAtom;

    public CtlFormulae parse(String ctlFormulaeString) {

        char[] ctlFormulaeCharArray = ctlFormulaeString.toCharArray();

        for (char character : ctlFormulaeCharArray) {
            Operand operand = this.parseCharToOperand(character);
            if (operand instanceof Operator)
                this.containsOperator = true;
            if (operand instanceof Atom)
                this.containsAtom = true;
            this.verifyAndAddToCtlFormulae(operand);
        }

        if (!this.containsOperator)
            throw new CtlException("CtlFormulae should contain at least one operator.");

        if (!this.containsAtom)
            throw new CtlException("CtlFormulae should contain at least one atomic proposition.");

        this.verifyParenthesis();

        return this.translate(this.removeParenthesis(this.ctlFormulae));
    }

    // region Utils

    private void verifyAndAddToCtlFormulae(Operand operand) {
        int ctlFormulaeSize = this.ctlFormulae.getNbOperandsRecursive();
        Operand lastOperator = null;

        this.ctlFormulae.addOperands(operand);

        if (ctlFormulaeSize > 0)
            lastOperator = this.ctlFormulae.getOperand(ctlFormulaeSize - 1);

        if (operand instanceof Operator operator) {
            if (this.nextShouldBeParenthesis)
                throw new CtlException(lastOperator + " should be followed by an open parenthesis");
            this.verifyOperator(operand, lastOperator);
            this.nextShouldBeParenthesis = (operator == Operator.Finally || operator == Operator.Globally || operator == Operator.Next);
        } else if (operand instanceof Parenthesis) {
            this.countParenthesis(operand);
            this.nextShouldBeParenthesis = false;
        } else {
            if (this.nextShouldBeParenthesis)
                throw new CtlException(lastOperator + " should be followed by an open parenthesis");
            this.verifyAtomicProposition(lastOperator);
        }
    }

    private Operand parseCharToOperand(char character) {
        return Objects.requireNonNullElseGet(
                Operator.valueOfOperator(character),
                () -> Objects.requireNonNullElseGet(
                        Parenthesis.valueOfOperator(character),
                        () -> {
                            if (Character.isLowerCase(character))
                                return new Atom(character);
                            else
                                throw new CtlException("Invalid operator : " + character);
                        }
                ));
    }

    private CtlFormulae removeParenthesis(CtlFormulae ctlFormulae) {
        CtlFormulae translated = new CtlFormulae();

        for (int i = 0; i < ctlFormulae.getNbOperandsRecursive(); i++) {

            Operand operand = ctlFormulae.getOperand(i);

            if (operand instanceof Parenthesis parenthesis && parenthesis == Parenthesis.Open) {
                CtlFormulae nextSubFormulae = CtlUtils.extractNextSubFormulae(ctlFormulae, i);
                CtlFormulae minusParenthesis = this.removeParenthesis(nextSubFormulae);
                translated.addOperands(minusParenthesis);
                i = i + nextSubFormulae.getNbOperandsRecursive() + nextSubFormulae.getNbParenthesis() - 1;
            } else
                translated.addOperands(operand);
        }

        return translated;
    }

    //endregion

    // region Translators

    private CtlFormulae comboOperatorTranslate(CtlFormulae ctlFormulae, Operator operator, Operand nextOperand, int i) {
        switch (operator) {
            case And, Until, Globally, Finally, True, Next, Or, Not -> {
            }
            case All -> {
                // AX
                if (nextOperand == Operator.Next) return this.translateAX(ctlFormulae, i);

                    // AG
                else if (nextOperand == Operator.Globally) return this.translateAG(ctlFormulae, i);

                    // AF
                else if (nextOperand == Operator.Finally) return this.translateAF(ctlFormulae, i);

                else throw new CtlException();
            }
            case Exist -> {
                // EF
                if (nextOperand == Operator.Finally) return this.translateEF(ctlFormulae, i);

                // EG
                if (nextOperand == Operator.Globally) return this.translateEG(ctlFormulae, i);

                // EX
                if (nextOperand == Operator.Next) return null;

                else throw new CtlException();
            }
            case Imply -> {
                // a > b
                if (nextOperand instanceof CtlFormulae) return this.translateImply(ctlFormulae);
            }
        }
        return null;
    }

    private CtlFormulae translate(CtlFormulae ctlFormulae) {
        CtlFormulae translatedTemp = null;
        CtlFormulae translated = new CtlFormulae();

        for (int i = 0; i < ctlFormulae.getNbOperands(); i++) {
            int nb = ctlFormulae.getNbOperands();

            Operand operand = ctlFormulae.getOperand(i);

            if (operand instanceof CtlFormulae subFormulae) {
                translated.addOperands(this.translate(subFormulae));
            } else if (operand instanceof Operator operator) {
                if ((i + 1) < ctlFormulae.getNbOperands())
                    translatedTemp = this.comboOperatorTranslate(ctlFormulae, operator, ctlFormulae.getOperand(i + 1), i);

                if (translatedTemp != null) {
                    translated = translatedTemp;
                    i += translated.getNbOperandsRecursive() - 1; // -1 because the for loop wil do +1
                } else
                    translated.addOperands(operand);
            } else if (operand instanceof Atom atom) {
                translated.addOperands(atom);
            }

        }


        /*for (int i = 0; i < ctlFormulae.getNbOperands(); i++) {

            Operand operand = ctlFormulae.getOperand(i);
            Operand nextOperand = ctlFormulae.getOperand(i + 1);

            if (nextOperand instanceof CtlFormulae subFormulae) {
                translated = this.translate(subFormulae);
            } else if (operand instanceof Operator operator) {

                switch (operator) {
                    case And, Until, Globally, Finally, True, Next, Or, Not, Imply -> {

                    }
                    case All -> {
                        // AX
                        if (nextOperand == Operator.Next) translated = this.translateAX(ctlFormulae, i);

                            // AG
                        else if (nextOperand == Operator.Globally) translated = this.translateAG(ctlFormulae, i);

                            // AF
                        else if (nextOperand == Operator.Finally) translated = this.translateAF(ctlFormulae, i);
                    }
                    case Exist -> {
                        // EF
                        if (nextOperand == Operator.Finally) translated = this.translateEF(ctlFormulae, i);

                        // EX
                        if (nextOperand == Operator.Next) {
                            translated.addOperands(operand);
                            translated.addOperands(nextOperand);
                        }
                    }
                    default -> throw new CtlException("Miss-formed CTL Formulae");
                }

                if (translated != null) i = i + translated.getNbOperandsRecursive() - 1;
            } else if (operand instanceof CtlFormulae && nextOperand instanceof Operator nextOperator && nextOperator == Operator.Imply) {
                translated = this.translateImply(ctlFormulae);
                i = i + translated.getNbOperands() - 1;
            }
        }*/

        return translated;
    }

    private CtlFormulae translateAX(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.Next);

        subFormulae1.addOperands(Operator.Not);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            subFormulae1.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) subFormulae1.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of AX");

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

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            subFormulae2.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) subFormulae2.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of AG");

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

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            subFormulae1.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) subFormulae1.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of AF");

        translated.addOperands(subFormulae1);
        return translated;
    }

    private CtlFormulae translateEF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Exist);
        subFormulae1.addOperands(Operator.True);
        subFormulae1.addOperands(Operator.Until);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            subFormulae1.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) subFormulae1.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of EF");

        translated.addOperands(subFormulae1);
        return translated;
    }

    private CtlFormulae translateEG(CtlFormulae ctlFormulae, int i) {

        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Finally);
        translated.addOperands(Operator.Not);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            translated.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) translated.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of EF");

        return translated;
    }

    private CtlFormulae translateImply(CtlFormulae ctlFormulae) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();

        CtlFormulae ctlFormulae1 = (CtlFormulae) ctlFormulae.getOperand(0);
        CtlFormulae ctlFormulae2 = (CtlFormulae) ctlFormulae.getOperand(2);

        Operand operand1 = ctlFormulae1.getNbOperands() == 1 && ctlFormulae1.getOperand(0) instanceof Atom ?
                (Atom) ctlFormulae1.getOperand(0) : ctlFormulae1;
        Operand operand2 = ctlFormulae2.getNbOperands() == 1 && ctlFormulae2.getOperand(0) instanceof Atom ?
                (Atom) ctlFormulae2.getOperand(0) : ctlFormulae2;

        subFormulae1.addOperands(Operator.Not);
        subFormulae1.addOperands(operand1);
        translated.addOperands(subFormulae1);
        translated.addOperands(Operator.Or);
        translated.addOperands(operand2);

        return translated;
    }

    // endregion

    // region Verifiers

    private void verifyAtomicProposition(Operand lastOperator) {
        if (lastOperator == Parenthesis.Close || lastOperator == Operator.True || lastOperator instanceof Atom || lastOperator == Operator.Exist || lastOperator == Operator.All)
            throw new CtlException("Invalid placement of atomic proposition");
    }

    private void verifyExistOperator(Operand lastOperator) {
        if (lastOperator != Parenthesis.Open && lastOperator != Operator.Or && lastOperator != Operator.And && lastOperator != Operator.Imply)
            throw new CtlException("Invalid use of Exist operator");
    }

    private void verifyAllOperator(Operand lastOperator) {
        if (lastOperator != Parenthesis.Open && lastOperator != Operator.Or && lastOperator != Operator.And && lastOperator != Operator.Imply)
            throw new CtlException("Invalid use of All operator");
    }

    private void verifyAndOrImplyOperator(Operand operand, Operand lastOperator) {
        if (!(lastOperator instanceof Atom || lastOperator == Parenthesis.Close))
            throw new CtlException("Invalid use of " + operand + " operator");
    }

    private void verifyTrueOperator(Operand lastOperator) {
        if (lastOperator == null || lastOperator instanceof Atom || lastOperator == Parenthesis.Close)
            throw new CtlException("Invalid use of True operator");
    }

    private void verifyNextOperator(Operand lastOperator) {
        if (lastOperator == null || lastOperator instanceof Atom || lastOperator == Parenthesis.Close || lastOperator == Operator.Next || lastOperator == Operator.Finally || lastOperator == Operator.Globally)
            throw new CtlException("Invalid use of Next operator");
    }

    private void verifyFinallyOperator(Operand lastOperator) {
        if (lastOperator instanceof Atom || lastOperator == Parenthesis.Close || lastOperator == Operator.Next || lastOperator == Operator.Until || lastOperator == Operator.Globally)
            throw new CtlException("Invalid use of Finally operator");
    }

    private void verifyGloballyOperator(Operand lastOperator) {
        if (lastOperator instanceof Atom || lastOperator == Parenthesis.Close || lastOperator == Operator.Next || lastOperator == Operator.Until || lastOperator == Operator.Finally)
            throw new CtlException("Invalid use of Globally operator");
    }

    private void verifyUntilOperator(Operand lastOperator) {
        if (lastOperator == null || lastOperator == Operator.Next || lastOperator == Operator.Globally || lastOperator == Operator.Finally)
            throw new CtlException("Invalid use of Until operator");
    }

    private void verifyParenthesis() {
        if (this.parenthesisCount != 0) throw new CtlException("Invalid parenthesis combination");
    }

    private void countParenthesis(Operand operand) {
        if (operand == Parenthesis.Open) this.parenthesisCount++;
        else if (operand == Parenthesis.Close) this.parenthesisCount--;
        else throw new CtlException("Invalid parenthesis combination");
    }

    private void verifyOperator(Operand operand, Operand lastOperator) {
        switch ((Operator) operand) {
            case And, Or, Imply -> verifyAndOrImplyOperator(operand, lastOperator);
            case Not -> {}
            case All -> {
                if (lastOperator != null) verifyAllOperator(lastOperator);
            }
            case Exist -> {
                if (lastOperator != null) verifyExistOperator(lastOperator);
            }
            case Next -> verifyNextOperator(lastOperator);
            case True -> verifyTrueOperator(lastOperator);
            case Finally -> verifyFinallyOperator(lastOperator);
            case Globally -> verifyGloballyOperator(lastOperator);
            case Until -> verifyUntilOperator(lastOperator);
            default -> throw new CtlException("Invalid operator");
        }
    }

    // endregion

}