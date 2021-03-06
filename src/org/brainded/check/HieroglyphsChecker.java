package org.brainded.check;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.ctl.CtlFormulae;
import org.brainded.check.model.exceptions.CtlException;
import org.brainded.check.model.exceptions.KripkeException;
import org.brainded.check.parser.CtlParser;
import org.brainded.check.parser.KripkeParser;
import org.brainded.check.services.Checker;
import org.brainded.check.services.KripkeGenerator;
import org.brainded.check.utils.DirectoryManager;
import org.brainded.check.utils.KripkeSerializer;

import javax.management.InstanceNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

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
        displayFile();

        System.out.print("Enter the number of the Kripke structure file : ");
        int kripkeFilePath = readIntInput();
        try {
            Path filename = DirectoryManager.getFileFromNumber(kripkeFilePath);
            if (filename.getFileName().toString().equals("empty")) {
                System.out.println("Invalid File chosen");
                act();
            }
            ks = KripkeParser.parse(filename);
            ks.validateKripkeStruct();
            System.out.println("Loaded this Kripke Structure :\n" + ks);
        } catch (KripkeException | IOException e) {
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

            System.out.println("\nFormula to evaluate : " + ctlFormulae + "\n");
            boolean result = checker.satisfyFormulae();
            printResult(checker, result);
        } catch (CtlException e) {
            printError(e.getMessage());
        }

        act();
    }

    private static void randomKripke() {
        overloadingKripke();
        System.out.println("\n-- Enter a number of state --\n");
        int ksStatesNumber = readIntInput();
        Set<Character> set = readLabels();
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
        System.exit(0);
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

    private static Set<Character> readLabels() {
        System.out.println("Enter the number of label");
        Integer labelSize = readIntInput();
        Set<Character> labels = new HashSet<>();
        while(labelSize > 0) {
            System.out.print("Enter a marking label : ");
            Character c = readStringInput().toLowerCase(Locale.ROOT).charAt(0);
            labels.add(c);
            labelSize--;
        }
        return labels;
    }

    //endregion

    //region Output

    private static void printError(String errorMsg) {
        System.out.println("\u001B[31m" + errorMsg + "\u001B[0m");
    }

    private static void displayFile() {
        try {
            DirectoryManager.createResourceDirectory();
            List<String> filenames = DirectoryManager.listFilenames();
            System.out.println("--------------------");
            for (int index = 0; index < filenames.size(); index++)
                System.out.println((1 + index) + ": " + filenames.get(index));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResult(Checker checker, boolean result) {
        String answer = result ?
                "The kripke Structure satisfies the formulae!" :
                "The Kripke Structure doesn't not satisfy the formulae!";
        System.out.println(answer);

        System.out.print("Initial States : |");
        for (String st : checker.getInitialStates()) {
            System.out.print(st + "|");
        }

        System.out.print("\nValidating States : |");
        for (String st : checker.getValidatingStates()) {
            System.out.print(st + "|");
        }

        System.out.println();
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