package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;
import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.parser.CtlParser;
import org.brainded.check.model.exceptions.KripkeException;
import org.brainded.check.parser.KripkeParser;
import org.brainded.check.services.Checker;
import org.brainded.check.services.KripkeGenerator;
import org.brainded.check.utils.KripkeSerializer;

import javax.management.InstanceNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HieroglyphsChecker {

    //region Variables

    private enum Action {
        LOAD_KRIPKE,
        ENTER_CTL,
        QUIT,
        RANDOM_KRIPKE,
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
            case RANDOM_KRIPKE -> randomKripke();
            case QUIT -> quit();
        }
    }

    private static Action displayMenu() {
        Action actionAsked = Action.DEFAULT;

        do {
            System.out.println("\n-- Hieroglyphs checker menu -- \n");
            System.out.println("1. Load Kripke structure file");
            System.out.println("2. Enter CTL state formulae");
            System.out.println("3. Generate a Random Kripke Structure");
            System.out.println("4. Quit");
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
                case 3 -> actionAsked = Action.RANDOM_KRIPKE;
                case 4 -> actionAsked = Action.QUIT;
                default -> printError("Invalid choice");
            }
        }
        return actionAsked;
    }

    //endregion

    //region Actions
    private static void loadKripke() {
        System.out.println("\n-- Load Kripke structure file -- \n");
        overloadingKripke();

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

    private static void randomKripke() {
        overloadingKripke();
        System.out.println("\n-- Enter a number of state --\n");
        int ksStatesNumber = readIntInput();
        Set<Character> set = new HashSet<>(Arrays.asList('p', 'q', 'r', 's', 'v', 'w'));
        KripkeGenerator kripkeGenerator = new KripkeGenerator(ksStatesNumber, set);
        try {
            ks = kripkeGenerator.generateKripkeStructure();
            KripkeSerializer serializer = new KripkeSerializer(ks, true);
            serializer.saveKsInFile();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            printError("Error saving random Kripke Structure");
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
        } catch (NumberFormatException | IOException | NullPointerException e) {
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

    private static void overloadingKripke() {
        if (ks != null) {
            System.out.println("Kripke structure already loaded!");
            System.out.println("Are you sure to overload Kripke Structure ? (y/N)");
            String overload = readStringInput();
            if (!(overload.toLowerCase(Locale.ROOT).equals("y") || overload.toLowerCase(Locale.ROOT).equals("yes"))) {
                act();
            }
        }
    }

    //endregion

    public static void main(String[] args) {
        act();
    }
}