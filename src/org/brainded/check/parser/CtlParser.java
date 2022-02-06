package org.brainded.check.parser;

import org.brainded.check.model.ctl.*;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.utils.CtlUtils;

import java.util.Objects;

public class CtlParser {

    private final CtlFormulae ctlFormulae = new CtlFormulae();
    private int parenthesisCount = 0;
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
        Operand lastOperand = null;

        this.ctlFormulae.addOperands(operand);

        if (ctlFormulaeSize > 0)
            lastOperand = this.ctlFormulae.getOperand(ctlFormulaeSize - 1);

        if (operand instanceof Operator)
            this.verifyOperandUsage(operand, lastOperand);
        else if (operand instanceof Parenthesis) {
            this.verifyOperandUsage(operand, lastOperand);
            this.countParenthesis(operand);
        } else
            this.verifyAtomicProposition(lastOperand);
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

                // Throw CtlException if anything else than a sub-formula
                else
                    if(!(nextOperand instanceof CtlFormulae)) throw new CtlException();
            }
            case Exist -> {
                // EF
                if (nextOperand == Operator.Finally) return this.translateEF(ctlFormulae, i);

                // EG
                if (nextOperand == Operator.Globally) return this.translateEG(ctlFormulae, i);

                // EX
                if (nextOperand == Operator.Next) return null;

                // Throw CtlException if anything else than a sub-formula
                else
                    if(!(nextOperand instanceof CtlFormulae)) throw new CtlException();
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
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);

        subFormulae1.addOperands(Operator.Not);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            subFormulae1.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) subFormulae1.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of AG");

        translated.addOperands(subFormulae1);

        return translated;
    }

    private CtlFormulae translateAF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        CtlFormulae subFormulae1 = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.All);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            translated.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) translated.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of AF");

        translated.addOperands(subFormulae1);

        return translated;
    }

    private CtlFormulae translateEF(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Exist);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            translated.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) translated.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of EF");

        return translated;
    }

    private CtlFormulae translateEG(CtlFormulae ctlFormulae, int i) {
        CtlFormulae translated = new CtlFormulae();
        Operand operandPlus2 = ctlFormulae.getOperand(i + 2);

        translated.addOperands(Operator.Not);
        translated.addOperands(Operator.All);
        translated.addOperands(Operator.True);
        translated.addOperands(Operator.Until);
        translated.addOperands(Operator.Not);

        if (operandPlus2 instanceof CtlFormulae subFormulae)
            translated.addOperands(translate(subFormulae));
        else if (operandPlus2 instanceof Atom) translated.addOperands(operandPlus2);
        else throw new CtlException("An error occurs during the translation of EF");

        return translated;
    }

    private CtlFormulae translateImply(CtlFormulae ctlFormulae) {
        CtlFormulae translated = new CtlFormulae();

        CtlFormulae ctlFormulae1 = (CtlFormulae) ctlFormulae.getOperand(0);
        CtlFormulae ctlFormulae2 = (CtlFormulae) ctlFormulae.getOperand(2);

        Operand operand1 = ctlFormulae1.getNbOperands() == 1 && ctlFormulae1.getOperand(0) instanceof Atom ?
                (Atom) ctlFormulae1.getOperand(0) : ctlFormulae1;
        Operand operand2 = ctlFormulae2.getNbOperands() == 1 && ctlFormulae2.getOperand(0) instanceof Atom ?
                (Atom) ctlFormulae2.getOperand(0) : ctlFormulae2;

        translated.addOperands(Operator.Not);
        translated.addOperands(operand1);
        translated.addOperands(Operator.Or);
        translated.addOperands(operand2);

        return translated;
    }

    // endregion

    // region Verifiers


    private void verifyExistAllTrueOperator(Operand operand, Operand lastOperand) {
        if (lastOperand != null || operand == Operator.True) {
            if (lastOperand != Operator.Not && lastOperand != Operator.And && lastOperand != Operator.Or && lastOperand != Operator.Imply && lastOperand != Operator.Until && lastOperand != Parenthesis.Open)
                throw new CtlException("Invalid use of " + operand + " operator");
        }
    }

    private void verifyNextFinallyGloballyOperator(Operand operand, Operand lastOperand) {
        if (lastOperand != Operator.All && lastOperand != Operator.Exist)
            throw new CtlException("Invalid use of " + operand + " operator");
    }

    private void verifyNotOperator(Operand lastOperand) {
        if (lastOperand != Operator.And && lastOperand != Operator.Or && lastOperand != Operator.Imply && lastOperand != Operator.Until && lastOperand != Parenthesis.Open)
            throw new CtlException("Invalid use of Not operator");
    }

    private void verifyAndOrImplyUntilOperator(Operand operand, Operand lastOperand) {
        if(operand == Operator.Until && !(lastOperand instanceof CtlFormulae))
            throw new CtlException("Invalid use of Until operator");
        if (lastOperand != Parenthesis.Close && !(lastOperand instanceof Atom) && lastOperand != Operator.True)
            throw new CtlException("Invalid use of " + operand + " operator");
    }

    private void verifyAtomicProposition(Operand lastOperand) {
        if (lastOperand != Operator.Not && lastOperand != Operator.And && lastOperand != Operator.Or && lastOperand != Operator.Imply && lastOperand != Operator.Until && lastOperand != Parenthesis.Open)
            throw new CtlException("Invalid placement of atomic proposition");
    }

    private void verifyCloseParenthesis(Operand lastOperand) {
        if (lastOperand != Parenthesis.Close && !(lastOperand instanceof Atom) && lastOperand != Operator.True)
            throw new CtlException("Invalid placement of Close parenthesis");
    }

    private void verifyOpenParenthesis(Operand lastOperand) {
        if (lastOperand == Parenthesis.Close || lastOperand instanceof Atom || lastOperand == Operator.True)
            throw new CtlException("Invalid placement of Open parenthesis");
    }

    private void verifyParenthesis() {
        if (this.parenthesisCount != 0) throw new CtlException("Invalid parenthesis combination");
    }

    private void countParenthesis(Operand operand) {
        if (operand == Parenthesis.Open) this.parenthesisCount++;
        else if (operand == Parenthesis.Close) this.parenthesisCount--;
    }

    private void verifyOperandUsage(Operand operand, Operand lastOperand) {
        if (operand instanceof Operator operator) {
            switch (operator) {
                case And, Or, Imply, Until -> this.verifyAndOrImplyUntilOperator(operand, lastOperand);
                case Not -> this.verifyNotOperator(lastOperand);
                case All, Exist, True -> this.verifyExistAllTrueOperator(operand, lastOperand);
                case Next, Finally, Globally -> this.verifyNextFinallyGloballyOperator(operand, lastOperand);
                default -> throw new CtlException("Invalid operator");
            }
        } else if (operand instanceof Parenthesis parenthesis) {
            switch (parenthesis) {
                case Close -> this.verifyCloseParenthesis(lastOperand);
                case Open -> this.verifyOpenParenthesis(lastOperand);
                default -> throw new CtlException("Invalid parenthesis");
            }
        }
    }

    // endregion

}