package com.bank;

/**
 * Application Entry Point for Bank Security & Account Management System.
 * Developed with Core Java 21, adhering strictly to OOP principles.
 */
public class Main {

    public static final String APP_NAME = "BANK SECURITY & ACCOUNT MANAGEMENT SYSTEM";
    public static final String APP_VERSION = "1.0.0";

    public static void main(String[] args) {
        printBanner();
        System.out.println("System initialized successfully on Java " + System.getProperty("java.version") + ".");
        System.out.println("Phase 1: Project structure and core packages loaded.");
    }

    private static void printBanner() {
        System.out.println("================================================================================");
        System.out.println("       " + APP_NAME);
        System.out.println("                         Version: " + APP_VERSION);
        System.out.println("================================================================================");
    }
}
