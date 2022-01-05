package org.brainded.check.parser;

import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CtlParser {

    public static List<Operand> Parse(String ctlFormulaeString) {

        List<Operand> ctlFormulae = new ArrayList<>();

        for (char character : ctlFormulaeString.toCharArray()) {
            ctlFormulae.add(
                    Objects.requireNonNullElseGet(
                            Operator.valueOfOperator(character),
                            () -> new Atom(character)
                    )
            );
        }

        return ctlFormulae;

    }

}
