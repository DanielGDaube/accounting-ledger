package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;


public class FinancialTracker {
    // ANSI colors for printing with colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\033[1;30m";
    public static final String ANSI_RED = "\033[0;31m";
    public static final String ANSI_GREEN = "\033[0;32m";
    public static final String ANSI_GREEN_BACKGROUND = "\033[0;102m";
    public static final String ANSI_RED_BACKGROUND = "\033[0;101m";

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

    // prints the app logo
    // displays the main menu and lets the user add a transaction or go to the ledger menu
    public static void main(String[] args) throws InterruptedException {
        loadTransactions(FILE_NAME);
        System.out.println(colorLogo(logo));



        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        welcomeScreen(scanner);


        while (running) {
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addTransaction(scanner,1);
                case "P" -> addTransaction(scanner,2);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> menuError();
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    // loads transactions from transactions.csv
    // creates a new Transactions object from the information
    // stores the object in an array list
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

    /* ask the user to enter transaction info
    adds a new Transactions object to the array list then writes the Transaction to transactions.csv
    if the user selected add deposit, transaction is listed with a positive number
    if the user selected add payment, transaction is listed with a negative number */
    private static void addTransaction(Scanner scanner, int transactionType) throws InterruptedException {
        while (true) {
            try {
                System.out.print("\nEnter the date and time of the deposit in the following format\nyyyy-MM-dd HH:mm:ss :");
                LocalDateTime dateTime = LocalDateTime.parse(scanner.nextLine(), DATETIME_FMT);

                System.out.println("Enter the following information:");
                System.out.print("Description:");
                String description = scanner.nextLine();
                System.out.print("Vendor:");
                String vendor = scanner.nextLine();
                System.out.print("Amount:");
                double numAmount = parseDouble(scanner.nextLine());
                if (numAmount <= 0) {
                    System.err.println("Inputted amount cannot be negative or 0!");
                    Thread.sleep(700);
                    continue;
                }
                if (transactionType == 2){
                    numAmount = -Math.abs(numAmount);
                }
                transactions.add(new Transaction(dateTime, description, vendor, numAmount));
                ledgerWriter(transactions);
                break;
            }
            catch (Exception e){
                menuError();
            }

        }
    }


    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */

    // displays ledger menu and lets the user choose what transactions to view or go to the report menu for more filter options
    private static void ledgerMenu(Scanner scanner) throws InterruptedException {
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
                case "A" -> displayLedger("All");
                case "D" -> displayLedger("Deposits");
                case "P" -> displayLedger("Payments");
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> menuError();
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */

    // displays every transaction from newest to oldest

    private static void displayLedger(String displayType){
        int found = 0;
        newestSort();
        for (Transaction transaction : transactions){
            // displays only deposits (transactions with a positive number)
            if (displayType.equals("Deposits")){
                if (transaction.getAmount() > 0){
                    columnPrint(transaction);
                    found++;
                }

                // displays only payments (transactions with a negative number)
            } else if (displayType.equals("Payments")) {
                if (transaction.getAmount() < 0){
                    columnPrint(transaction);
                    found++;
                }

            }
            else{
                columnPrint(transaction);
                found++;
            }
        }
        foundCheck(found);
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */

    // displays reports menu and lets the user select desired report type
    private static void reportsMenu(Scanner scanner) throws InterruptedException {
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
                case "1" -> monthReport(1);
                case "2" -> monthReport(2);
                case "3" -> yearReport(1);
                case "4" -> yearReport(2);
                case "5" -> {
                    System.out.print("Enter vendor name:");
                    vendorReport(scanner.nextLine());
                }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> menuError();
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */

    // if the user selected month-to-date, displays all transactions from the current month
    // if the user selected previous month, displays all transactions from the previous month
    private static void monthReport(int reportType){
        int found = 0;
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (reportType == 2) {
            if (month == 1){
                year -= 1;
                month = 12;
            }
            else {
                month -= 1;
            }
        }
        newestSort();
        for (Transaction transaction : transactions){
            if (transaction.getDate().getMonthValue() == month && transaction.getDate().getYear() == year){
                columnPrint(transaction);
                found++;
            }
        }
        foundCheck(found);
    }

    // if the user selected year-to-date, displays all transactions from the current year
    // if the user selected previous year, displays all transactions from the previous year
    private static void yearReport(int reportType){
        int found = 0;
        int year = LocalDate.now().getYear();
        if (reportType == 2){
            year -= 1;
        }
        oldestSort();
        for (Transaction transaction : transactions){
            if (transaction.getDate().getYear() == year){
                columnPrint(transaction);
                found++;
            }
        }
        foundCheck(found);
    }

    // prints reports matching the user entered vendor name
    private static void vendorReport(String vendor){
        int found = 0;
        newestSort();
        for (Transaction transaction : transactions){
            if (filterTransactionsByVendor(vendor, transaction)) {
                columnPrint(transaction);
            }
        }
        foundCheck(found);
    }

    // methods for filtering by different pieces of user entered data, used with custom search method

    private static boolean filterTransactionsByDate(LocalDate start, LocalDate end, Transaction transaction) {
        if (start != null && transaction.getDate().isBefore(start)) {
            return false;
        }
        if (end != null && transaction.getDate().isAfter(end)) {
            return false;
        }
            return true;
    }

    private static boolean filterTransactionsByDescription(String description, Transaction transaction) {
        if (description.isBlank()) {
            return true;
        }
            return transaction.getDescription().equalsIgnoreCase(description);
    }

    private static boolean filterTransactionsByVendor(String vendor, Transaction transaction) {
        if (vendor.isBlank()) {
            return true;
        }
            return transaction.getVendor().equalsIgnoreCase(vendor) || shortHand(transaction.getVendor()).equalsIgnoreCase(shortHand(vendor));
    }

    private static boolean filterTransactionsByAmount(String amount, Transaction transaction) {
        if (amount.isBlank()) {
            return true;
        }
            return Math.abs(transaction.getAmount()) == parseDouble(amount);
    }

    // combines all the filters into one
    private static boolean customSearchFilter(Transaction transaction, LocalDate start, LocalDate end, String description, String vendor, String amount){
        return filterTransactionsByDate(start, end, transaction)
                && filterTransactionsByDescription(description, transaction)
                && filterTransactionsByVendor(vendor, transaction)
                && filterTransactionsByAmount(amount, transaction);
    }

    // prompts the user for transaction data and runs the data through filters
    // prints only transactions that match all given data
    // if a filter is left blank it is considered valid for all transactions
    private static void customSearch(Scanner scanner) throws InterruptedException {
        try {
            int found = 0;

            System.out.println("Please enter the following filters to narrow search, or leave blank to ignore filter");
            System.out.print("Start Date:");
            LocalDate start = parseDate(scanner.nextLine());
            System.out.print("End Date:");
            LocalDate end = parseDate(scanner.nextLine());
            System.out.print("Description:");
            String description = scanner.nextLine();
            System.out.print("Vendor:");
            String vendor = scanner.nextLine();
            System.out.print("Amount:");
            String amount = scanner.nextLine();

            for (Transaction transaction : transactions) {
                if (customSearchFilter(transaction, start, end,description, vendor, amount)) {
                    columnPrint(transaction);
                    found++;

                }
            }
            foundCheck(found);
        }
        catch (Exception e){
            menuError();
        }
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    // parses a string to LocalDate
    private static LocalDate parseDate(String s) {
        if (s.isEmpty()) {
            return null;
        }
        else {
            return LocalDate.parse(s, DATE_FMT);
        }
    }

    // parses a string to a double
    private static Double parseDouble(String s) {
        if (s.isEmpty()) {
            return null;
        }
        else {
            return Double.parseDouble(s);
        }
    }

    // returns a shortened vendor name for easier filter matching
    private static String shortHand(String vendor){
        if (vendor.contains(" ")){
            int spaceIndex = vendor.indexOf(' ');
            vendor = vendor.substring(0, spaceIndex);
        }
        if (vendor.contains("'")){
            vendor = vendor.replace("'","");
        }
        return vendor;
    }

    // sorts array list from latest date to oldest date
    private static void newestSort(){
        transactions.sort((transaction1, transaction2) -> transaction2.getDateTime()
                .compareTo(transaction1.getDateTime()));
    }

    // sorts array list from oldest date to latest date
    private static void oldestSort(){
        transactions.sort(Comparator.comparing(Transaction::getDateTime));
    }

    // writes most recent array list object to transactions.csv
    private static void ledgerWriter(ArrayList transactions) throws IOException {
        BufferedWriter ledgerWriter = new BufferedWriter(new FileWriter(FILE_NAME, true));
        ledgerWriter.write(transactions.get(transactions.size()-1).toString());
        ledgerWriter.write("\n");
        ledgerWriter.close();
    }
    // prints Transaction objects with formatting to line up on | delimiters
    // deposits print in green, payments print in red
    private static void columnPrint(Transaction transaction) {
        String background;
        if (transaction.getAmount() > 0) {
            background = ANSI_GREEN_BACKGROUND;
        }
        else {
            background = ANSI_RED_BACKGROUND;
        }
        System.out.printf(background + ANSI_BLACK + "%-10tF|%-8tT|%-30s|%-20s|%-8.2f" + ANSI_RESET + "%n",
                transaction.getDate(), transaction.getTime(), transaction.getDescription(), transaction.getVendor(), transaction.getAmount());
    }

    // prints an error message
    private static void menuError() throws InterruptedException {
        System.err.println("\nInvalid option!");
        Thread.sleep(900);
    }

    // prints a message if no transactions were found with the filter
    private static void foundCheck(int found){
        if (found == 0){
            System.out.println("No matching transactions found");
        }
    }

    // Initializes start up logo
    public static String logo = "                                                                                                     \n" + //
            "                                                                                                     \n" + //
            "                                 ██░░░░███                                                           \n" + //
            "                                █░░░░░░░░░░░██                                                       \n" + //
            "                                █░░░░░░░░░░░░░░░██                                                   \n" + //
            "                                █░░░░░░░░░░░░░░░░░░██                                                \n" + //
            "                                █░░░░░░░░░░░░░░░░░░░░██                                              \n" + //
            "                                █░░░░░░░░░░░░░░░░░░░░░░███                                           \n" + //
            "                                █░░░░░░░░░░░░░░░░░░░░░░░░█▓▓▓▓                                       \n" + //
            "                                █░░░░░░░░░░░░░░░░░░░░░░░░░█████▓▓                                    \n" + //
            "                                 █░░░░░░░░░░░░░░░░░░░░░░░░░██████▓▓▓                                  \n" + //
            "                                ▓██░░░░░░░░░░░░░░░░░░░░░░░░█████████▓                                \n" + //
            "                              ▓▓████░░░░██░░░░░░░░█░░░░░░░░░░████████▓▓                              \n" + //
            "                             ▓███████░░░███░░░░░░█▓▓▓█░░░░░▓██████████▓▓                             \n" + //
            "                            ▓██████████░░░██░░░░░░█▓▓▓█░░░░░████████████▓                            \n" + //
            "                           ▓████████████░░█░░░░░░░░░░░░░░░░░░███████████▓▓                           \n" + //
            "                          ▓▓████████████░░░░█░░░░░░░░░░░░░░░░████████████▓▓                          \n" + //
            "                          ▓█████████░░░░░███░░░░░░░░░░░░░░░░░█████████████▓                          \n" + //
            "                         ▓▓██████░░░░░░░████░░░░█░░░░░░░░░░░░░████████████▓                          \n" + //
            "                         ▓▓█████████░░░█████░░░█░░░░░░░░░░░░░░████████████▓▓                         \n" + //
            "                         ▓██████████████████░░░░█░░░░░░░░░░░░░████████████▓▓                         \n" + //
            "                         ▓██████████████████░░░░███░░░░░░░░███████████████▓▓                         \n" + //
            "                         ▓▓████████████████░░░░░░█████░█████░░░░██████████▓                          \n" + //
            "                          ▓████████████████░░░░░░░█████░█░░░░░████████████▓                          \n" + //
            "                          ▓▓███████████████░░░░░░░░░░░░░░░░░░████████████▓▓                          \n" + //
            "                           ▓██████████████░░░░░░░░░░░░░░░░░░█████████████▓                           \n" + //
            "                            ▓████████████░░░░░░░░░░░░░░░░░░░████████████▓                            \n" + //
            "                             ▓██████████████░░░░░░░░░░░░░░░████████████▓                             \n" + //
            "                              ▓███████████████░░░░░░░░░░░░███████████▓▓                              \n" + //
            "   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓███████████████░░░░░░░░████████████▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   \n" + //
            "   ▓▓██████████████████████████████████████████████░░░░███████████████████████████████████████████▓▓   \n" + //
            "   ▓▓█████▓▓▓▓▓▓▓████████████████████████████████████░█████▓▓▓▓▓▓█████████████████████████████████▓▓   \n" + //
            "   ▓▓██████▓▓██▓▓███████████████████████████████████████████▓▓█▓▓█████████████████████████████████▓▓   \n" + //
            "   ▓▓███████▓██▓▓█▓▓██▓▓█▓▓▓▓▓▓█▓▓▓▓▓█▓▓▓█▓▓█▓▓▓▓▓▓█▓▓▓▓▓▓██▓█████▓▓███▓▓▓▓▓██▓▓▓███▓▓█▓▓▓▓▓██████▓▓   \n" + //
            "   ▓▓███████▓▓▓▓███▓██▓▓██▓▓█▓▓███▓████▓██▓▓██▓▓█▓▓██▓▓█████▓▓▓███▓▓████▓██▓███▓▓██▓▓▓█▓▓█████████▓▓   \n" + //
            "   ▓▓███████▓██▓▓██▓██▓▓██▓▓▓▓▓███▓████▓██▓▓██▓▓▓▓███▓▓▓████▓████▓██▓███▓▓▓▓██▓▓▓▓█▓█▓█▓▓▓▓▓██████▓▓   \n" + //
            "   ▓▓███████▓██▓▓██▓██▓▓██▓▓██████▓████▓██▓▓██▓▓█▓▓██▓▓█████▓████▓██▓███▓██▓██▓▓█▓▓██▓████▓▓██████▓▓   \n" + //
            "   ▓▓█████▓▓▓▓█▓▓▓█▓▓▓▓▓█▓▓▓█████▓▓▓███▓▓▓▓▓█▓▓▓█▓▓█▓▓▓▓▓▓█▓▓▓██▓▓██▓▓█▓▓██▓▓█▓▓█▓▓█▓▓█▓▓▓▓▓██████▓▓   \n" + //
            "   ▓▓██████▓▓██▓▓███▓▓▓██▓▓▓█████▓▓▓████▓▓▓██▓▓██▓▓█▓▓▓▓▓▓█▓▓▓██▓▓██▓▓█▓▓██▓▓█▓▓████▓▓█▓▓▓▓███████▓▓   \n" + //
            "   ▓▓█████████████████████████████████████████████████████████████████████████████████████████████▓▓   \n" + //
            "   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   \n" + //
            "                                                                                                    ";

    // converts the logo into color
    public static String colorLogo(String logo){
        logo =logo.replace("▓", ANSI_RED+"▓");
        logo =logo.replace("░", ANSI_GREEN+"░");
        logo=logo.replace("█", ANSI_BLACK+"█");

        return logo+ANSI_RESET;
    }

    // prints a series of blocks based on entered number of prints, blocks per print, and length of pauses between prints
    public static void loadingBar(int prints, int blocks, int millis) throws InterruptedException {
        String loading = "";
        for (int j = 0; j < blocks ; j++) {
            loading += "█";
        }
        for (int i = 0; i < prints; i++) {
            System.out.print(loading);
            Thread.sleep(millis);
        }
    }

    // displays a welcome message and a loading bar
    // when the loading bar is done it waits for the user to press enter before continuing to the home screen
    public static void welcomeScreen(Scanner scanner) throws InterruptedException {
        System.out.print(ANSI_RED+"                       WELCOME TO THE RUPTURE FARMS VIRTUAL LEDGER\n\n"+ANSI_RESET);

        System.out.print("Loading...");
        loadingBar(7,1,90);
        loadingBar(3,1,120);
        loadingBar(7,8,330);
        loadingBar(3,9,330);
        do{ System.out.println("\rPress Enter to continue:");}
        while (!scanner.nextLine().isBlank());
    }
}
