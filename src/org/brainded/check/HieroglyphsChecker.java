package org.brainded.check;

import jdk.jshell.spi.ExecutionControl;
import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.parser.CtlParser;
import org.brainded.check.model.exceptions.KripkeException;
import org.brainded.check.parser.KripkeParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

public class HieroglyphsChecker {

    //region Variables

    private enum Action {
        LOAD_KRIPKE,
        ENTER_CTL,
        QUIT,
        DEFAULT
    }

    private static final BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    private static KripkeStructure ks;

    private static CtlFormulae ctlFormulae;

    //endregion

    //region Menu

    private static void act() {
        switch (displayMenu()) {
            case LOAD_KRIPKE -> loadKripke();
            case ENTER_CTL -> enterCtl();
            case QUIT -> quit();
        }
    }

    private static Action displayMenu() {
        Action actionAsked = Action.DEFAULT;

        do {
            System.out.println("\n-- Hieroglyphs checker menu -- \n");
            System.out.println("1. Load Kripke structure file");
            System.out.println("2. Enter CTL state formulae");
            System.out.println("3. Quit");
            System.out.print("Enter your choice : ");
            actionAsked = getActionAsked(actionAsked);
            System.out.println("------------------------------");
        } while (actionAsked == Action.DEFAULT);

        return actionAsked;
    }

    private static Action getActionAsked(Action actionAsked) {
        Integer answer;
        answer = readIntInput();

        if (answer != null) {
            switch (answer) {
                case 1 -> actionAsked = Action.LOAD_KRIPKE;
                case 2 -> actionAsked = Action.ENTER_CTL;
                case 3 -> actionAsked = Action.QUIT;
                default -> printError("Invalid choice");
            }
        }
        return actionAsked;
    }

    //endregion

    //region Actions
    private static void loadKripke() {
        System.out.println("\n-- Load Kripke structure file -- \n");
        if (ks != null) {
            System.out.println("Kripke structure already loaded!");
            System.out.println("Are you sure to overload Kripke Structure ? (y/N)");
            String overload = readStringInput();
            if (overload.toLowerCase(Locale.ROOT).equals("n") || overload.toLowerCase(Locale.ROOT).equals("no")) {
                act();
            }
        }

        System.out.print("Enter the path to the Kripke structure file : ");
        String kripkeFilePath = readStringInput();
        ks = KripkeParser.parse(kripkeFilePath);
        try {
            ks.validateKripkeStruct();
            System.out.println("Loaded this Kripke Structure :\n" + ks);
        } catch (KripkeException e) {
            printError(e.getMessage());
            ks = null;
        }
        act();
    }

    private static void enterCtl() {
        System.out.println("\n-- Enter CTL state formulae -- \n");
        System.out.print("Enter the CTL state formulae to check : ");

        try {
            CtlParser ctlParser = new CtlParser();
            ctlFormulae = ctlParser.parse(readStringInput());
            /**CtlFormulae form1 = new CtlFormulae();
            form1.addOperands(new Atom('m'));
            form1.addOperands(Operator.And);
            form1.addOperands(new Atom('n'));

            CtlFormulae form2 = new CtlFormulae();
            form2.addOperands(Operator.Exist);
            form2.addOperands(Operator.True);
            form2.addOperands(Operator.Until);
            form2.addOperands(form1);

            CtlFormulae form3 = new CtlFormulae();
            form3.addOperands(Operator.Not);
            form3.addOperands(form2);

            CtlFormulae form4 = new CtlFormulae();
            form4.addOperands(Operator.Exist);
            form4.addOperands(Operator.True);
            form4.addOperands(Operator.Until);
            form4.addOperands(form3);

            CtlFormulae form5 = new CtlFormulae();
            form5.addOperands(Operator.Not);
            form5.addOperands(form4);*/

            Checker checker = new Checker(ks, ctlFormulae.getOperands());
            System.out.println(ctlFormulae);
            if (checker.satisfyFormulae()) {
                for (State state : checker.getValidatingStates()) {
                    System.out.println(state.minimalPrint());
                }
            }
        } catch (CtlException e) {
            printError(e.getMessage());
        }


        act();
    }

    private static void quit() {
        try {
            keyboard.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nBye !");
    }

    //endregion

    //region Utils

    //region Input

    private static Integer readIntInput() {
        Integer intInput = null;
        try {
            intInput = Integer.parseInt(keyboard.readLine());
        } catch (NumberFormatException | IOException e) {
            printError("Please enter en number");
        }
        return intInput;
    }

    private static String readStringInput() {
        String stringInput = null;
        try {
            stringInput = keyboard.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringInput;
    }

    //endregion

    //region Output

    private static void printError(String errorMsg) {
        System.out.println("\u001B[31m" + errorMsg + "\u001B[0m");
    }

    //endregion

    //endregion

    public static void main(String[] args) {
        act();
    }
}