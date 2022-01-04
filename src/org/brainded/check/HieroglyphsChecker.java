package org.brainded.check;

import org.brainded.check.parser.KripkeParser;

import java.util.Scanner;

public class HieroglyphsChecker {

    private enum Action {
        LOAD_KRIPKE,
        ENTER_CTL,
        QUIT,
        DEFAULT
    }

    private static final Scanner keyboard = new Scanner(System.in);

    private static Action displayMenu() {
        Action actionAsked;
        int answer = -1;

        do {
            System.out.println("\n-- Hieroglyphs checker menu -- \n");
            System.out.println("1. Load Kripke structure file");
            System.out.println("2. Enter CTL state formulae");
            System.out.println("3. Quit");
            System.out.print("Enter your choice : ");
            answer = keyboard.nextInt();
            System.out.println("------------------------------");

            actionAsked = switch (answer) {
                case 1 -> Action.LOAD_KRIPKE;
                case 2 -> Action.ENTER_CTL;
                case 3 -> Action.QUIT;
                default -> Action.DEFAULT;
            };

        } while (actionAsked == Action.DEFAULT);

        return actionAsked;
    }

    private static void loadKripke(){
        System.out.println("\n-- Load Kripke structure file -- \n");
        System.out.print("Enter the path to the Kripke structure file : ");

        KripkeParser.parse(keyboard.next());

        act();
    }

    private static void enterCtl(){
        System.out.println("\n-- Enter CTL state formulae -- \n");
        System.out.print("Enter the CTL state formulae to check : ");

        String ctlFormulae = keyboard.next();

        System.out.println(ctlFormulae);

        act();
    }

    private static void quit(){
        System.out.println("\nBye !");
    }

    private static void act(){
        switch (displayMenu()){
            case LOAD_KRIPKE -> loadKripke();
            case ENTER_CTL -> enterCtl();
            case QUIT -> quit();
        }
    }

    public static void main(String[] args) {
        act();
    }
}
