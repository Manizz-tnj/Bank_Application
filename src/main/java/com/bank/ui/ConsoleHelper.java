package com.bank.ui;

/**
 * Styling and formatting helper for console interface output.
 */
public final class ConsoleHelper {

    private ConsoleHelper() {}

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("   " + title.toUpperCase());
        System.out.println("================================================================================");
    }

    public static void printSubHeader(String subtitle) {
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(" >> " + subtitle);
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void printDivider() {
        System.out.println("--------------------------------------------------------------------------------");
    }
}
