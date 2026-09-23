package com.bank.ui;

import java.util.Scanner;

/**
 * Robust, exception-safe console input reader.
 * Prevents application crashes from malformed numerical inputs.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader() {
        this.scanner = new Scanner(System.in);
    }

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    public String readString(String prompt) {
        System.out.print(prompt + ": ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        String line = scanner.nextLine();
        return line != null ? line.trim() : "";
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            if (!scanner.hasNextLine()) {
                return 0;
            }
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("! Error: Please enter a valid whole number.");
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val >= min && val <= max) {
                return val;
            }
            if (!scanner.hasNextLine()) {
                return min;
            }
            System.out.printf("! Error: Please enter a choice between %d and %d.%n", min, max);
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            if (!scanner.hasNextLine()) {
                return 0.0;
            }
            String line = scanner.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    System.out.println("! Error: Invalid numeric input.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("! Error: Please enter a valid monetary/decimal amount.");
            }
        }
    }

    public double readPositiveDouble(String prompt) {
        while (true) {
            double val = readDouble(prompt);
            if (val > 0.0) {
                return val;
            }
            if (!scanner.hasNextLine()) {
                return 1.0;
            }
            System.out.println("! Error: Amount must be strictly greater than 0.0.");
        }
    }

    public String readPin(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            if (!scanner.hasNextLine()) {
                return "0000";
            }
            String line = scanner.nextLine();
            if (line != null && line.trim().matches("^[0-9]{4,6}$")) {
                return line.trim();
            }
            System.out.println("! Error: PIN must consist of 4 to 6 numeric digits.");
        }
    }
}
