package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.Atom;
import org.brainded.check.model.ctl.Operand;
import org.brainded.check.model.ctl.Operator;
import org.brainded.check.parser.CtlParser;
import org.brainded.check.model.exceptions.KripkeException;
import org.brainded.check.parser.KripkeParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private static List<Operand> ctlFormulae;

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

        ctlFormulae = CtlParser.Parse(readStringInput());
        Checker checker = new Checker(ks, ctlFormulae);
        System.out.println("State that satisfy the formula: ");
        for (State state: checker.satisfyFormulae()) {
            System.out.println(state.minimalPrint());
        }

        act();
    }

    private static void quit() {
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