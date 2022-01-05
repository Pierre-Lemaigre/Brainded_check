package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.parser.KripkeParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        System.out.print("Enter the path to the Kripke structure file : ");

        String kripkeFilePath = readStringInput();
        ks = KripkeParser.parse(kripkeFilePath);

        act();
    }

    private static void enterCtl() {
        System.out.println("\n-- Enter CTL state formulae -- \n");
        System.out.print("Enter the CTL state formulae to check : ");

        String ctlFormulae = readStringInput();
        System.out.println(ctlFormulae);

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
