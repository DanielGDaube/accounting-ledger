package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;



/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 *

 */
public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) throws IOException {
        loadTransactions(FILE_NAME);


        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    /**
     * Load transactions from FILE_NAME.
     * • If the file doesn’t exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName) {

        try{
            BufferedReader accountingReader = new BufferedReader(new FileReader(fileName));

            String line;
            LocalDateTime dateTime;

            while ((line = accountingReader.readLine()) != null) {
                String[] tokens = line.split("\\|");
                dateTime = LocalDate.parse(tokens[0]).atTime(LocalTime.parse(tokens[1]));
                String description = tokens[2];
                String vendor = tokens[3];
                double amount = Double.parseDouble(tokens[4]);
                transactions.add(new Transaction(dateTime, description, vendor, amount));
            }
        }
        catch (Exception e){
            System.err.println("Something went wrong!");
        }
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) throws IOException {
            while (true) {
                System.out.print("Enter the date and time of the deposit in the following format:\nyyyy-MM-dd HH:mm:ss :");
                LocalDateTime dateTime = LocalDateTime.parse(scanner.nextLine(), DATETIME_FMT);


                System.out.println("Enter the following information:");
                System.out.print("Description:");
                String description = scanner.nextLine();
                System.out.print("Vendor:");
                String vendor = scanner.nextLine();
                System.out.print("Amount:");
                double numAmount = scanner.nextDouble();
                if (numAmount < 0) {
                    System.out.println("Inputted amount cannot be negative or 0!");
                    scanner.nextLine();
                    continue;
                }
                transactions.add(new Transaction(dateTime, description, vendor, numAmount));
                ledgerWriter(transactions);
                break;
            }


    }

    /**
     * Same prompts as addDeposit.
     * Amount must be entered as a positive number,
     * then converted to a negative amount before storing.
     */
    private static void addPayment(Scanner scanner) throws IOException {
        while (true) {
            System.out.print("Enter the date and time of the deposit in the following format:\nyyyy-MM-dd HH:mm:ss :");
            LocalDateTime dateTime = LocalDateTime.parse(scanner.nextLine(), DATETIME_FMT);

            System.out.println("Enter the following information:");
            System.out.print("Description:");
            String description = scanner.nextLine();
            System.out.print("Vendor:");
            String vendor = scanner.nextLine();
            System.out.print("Amount:");
            double numAmount = scanner.nextDouble();
            if (numAmount < 0) {
                System.out.println("Inputted amount cannot be negative or 0!");
                scanner.nextLine();
                continue;
            }
            numAmount = -numAmount;
            transactions.add(new Transaction(dateTime, description, vendor, numAmount));
            ledgerWriter(transactions);
            break;
        }
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    private static void displayLedger() {
        newestSort();
        for (Transaction transaction : transactions){
            columnPrint(transaction);
        }
    }


    private static void displayDeposits() {
        newestSort();
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0){
                columnPrint(transaction);
            }
        }
    }

    private static void displayPayments() {
        newestSort();
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0){
                columnPrint(transaction);
            }
        }
         }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> monthToDateReport();
                case "2" -> previousMonthReport();
                case "3" -> {/* TODO – year-to-date report   */ }
                case "4" -> {/* TODO – previous year report  */ }
                case "5" -> {/* TODO – prompt for vendor then report */ }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static void monthToDateReport(){
        int year = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        newestSort();
        for (Transaction transaction : transactions){
            if (findMonthValue(transaction) == currentMonth && findYear(transaction) == year){
                columnPrint(transaction);
            }
        }
    }

    private static void previousMonthReport(){
        int year = LocalDate.now().getYear();
        int previousMonth = LocalDate.now().getMonthValue()-1;
        newestSort();
        for (Transaction transaction : transactions){
            if (findMonthValue(transaction) == previousMonth && findYear(transaction) == year){
                columnPrint(transaction);
            }
        }

    }

    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        // TODO – iterate transactions, print those within the range
    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static int findYear(Transaction transaction){
        return transaction.getDateTime().getYear();
    }

    private static int findMonthValue(Transaction transaction){
        return transaction.getDateTime().getMonthValue();
    }

    private static Double parseDouble(String s) {
        /* TODO – return Double   or null */
        return null;
    }

    private static void newestSort(){
        Collections.sort(transactions, (transaction1, transaction2) -> transaction2.getDateTime()
                .compareTo(transaction1.getDateTime()));
    }

    private static void ledgerWriter(ArrayList transactions) throws IOException {
        BufferedWriter ledgerWriter = new BufferedWriter(new FileWriter(FILE_NAME, true));
        ledgerWriter.write(transactions.get(transactions.size()-1).toString());
        ledgerWriter.write("\n");
        ledgerWriter.close();
    }
    private static void columnPrint(Transaction transaction){
        System.out.printf("%-10tF|%-8tT|%-30s|%-20s|%.2f%n", transaction.getDate(), transaction.getTime(), transaction.getDescription(),
                transaction.getVendor(), transaction.getAmount());
    }

}
