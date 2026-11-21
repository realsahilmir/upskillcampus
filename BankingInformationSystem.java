import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BankingInformationSystem {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<Long, BankAccount> accounts = new LinkedHashMap<>();
    private static long nextAccountNumber = 100001L;

    static class BankAccount {
        private final long accountNumber;
        private final String accountHolder;
        private final String pin;
        private double balance;
        private final List<String> transactions = new ArrayList<>();

        BankAccount(long accountNumber, String accountHolder, String pin, double openingBalance) {
            this.accountNumber = accountNumber;
            this.accountHolder = accountHolder;
            this.pin = pin;
            this.balance = openingBalance;
            transactions.add("Account opened with " + money(openingBalance));
        }

        boolean verifyPin(String enteredPin) {
            return pin.equals(enteredPin);
        }

        void deposit(double amount) {
            balance += amount;
            transactions.add("Deposited " + money(amount) + " | Balance: " + money(balance));
        }

        boolean withdraw(double amount) {
            if (amount <= 0 || amount > balance) {
                return false;
            }
            balance -= amount;
            transactions.add("Withdrawn " + money(amount) + " | Balance: " + money(balance));
            return true;
        }

        void recordTransfer(String details) {
            transactions.add(details + " | Balance: " + money(balance));
        }

        void showDetails() {
            System.out.println("\n------------- ACCOUNT DETAILS -------------");
            System.out.println("Account Number : " + accountNumber);
            System.out.println("Account Holder : " + accountHolder);
            System.out.println("Available Balance: " + money(balance));
            System.out.println("--------------------------------------------");
        }

        void showTransactions() {
            System.out.println("\n----------- TRANSACTION HISTORY ------------");
            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
            } else {
                for (int i = 0; i < transactions.size(); i++) {
                    System.out.println((i + 1) + ". " + transactions.get(i));
                }
            }
            System.out.println("--------------------------------------------");
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("       BANKING INFORMATION SYSTEM");
        System.out.println("============================================");

        while (true) {
            showMainMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    withdrawMoney();
                    break;
                case 4:
                    checkBalance();
                    break;
                case 5:
                    transferMoney();
                    break;
                case 6:
                    viewAccountDetails();
                    break;
                case 7:
                    viewTransactionHistory();
                    break;
                case 8:
                    System.out.println("\nThank you for using Banking Information System!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1 to 8.");
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--------------- MAIN MENU ------------------");
        System.out.println("1. Create New Account");
        System.out.println("2. Deposit Money");
        System.out.println("3. Withdraw Money");
        System.out.println("4. Check Balance");
        System.out.println("5. Transfer Money");
        System.out.println("6. View Account Details");
        System.out.println("7. View Transaction History");
        System.out.println("8. Exit");
        System.out.println("--------------------------------------------");
    }

    private static void createAccount() {
        System.out.println("\n--------------- CREATE ACCOUNT -------------");
        String name = readNonEmptyLine("Enter account holder name: ");
        String pin = readPin();

        double openingBalance = readAmount("Enter opening balance (minimum Rs. 0): ", true);
        long accountNumber = nextAccountNumber++;

        BankAccount account = new BankAccount(accountNumber, name, pin, openingBalance);
        accounts.put(accountNumber, account);

        System.out.println("\nAccount created successfully!");
        System.out.println("Your account number is: " + accountNumber);
        System.out.println("Please remember your account number and PIN.");
    }

    private static void depositMoney() {
        System.out.println("\n---------------- DEPOSIT -------------------");
        BankAccount account = authenticateAccount();
        if (account == null) return;

        double amount = readAmount("Enter amount to deposit: ", false);
        account.deposit(amount);
        System.out.println("Deposit successful. New balance: " + money(account.balance));
    }

    private static void withdrawMoney() {
        System.out.println("\n---------------- WITHDRAW ------------------");
        BankAccount account = authenticateAccount();
        if (account == null) return;

        double amount = readAmount("Enter amount to withdraw: ", false);
        if (account.withdraw(amount)) {
            System.out.println("Withdrawal successful. Remaining balance: " + money(account.balance));
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    private static void checkBalance() {
        System.out.println("\n-------------- CHECK BALANCE ---------------");
        BankAccount account = authenticateAccount();
        if (account == null) return;

        System.out.println("Available balance: " + money(account.balance));
    }

    private static void transferMoney() {
        System.out.println("\n-------------- MONEY TRANSFER --------------");
        BankAccount sender = authenticateAccount();
        if (sender == null) return;

        long receiverNumber = readLong("Enter receiver account number: ");
        BankAccount receiver = accounts.get(receiverNumber);

        if (receiver == null) {
            System.out.println("Receiver account not found.");
            return;
        }
        if (receiver == sender) {
            System.out.println("You cannot transfer money to the same account.");
            return;
        }

        double amount = readAmount("Enter amount to transfer: ", false);
        if (amount > sender.balance) {
            System.out.println("Insufficient balance. Transfer cancelled.");
            return;
        }

        sender.balance -= amount;
        receiver.balance += amount;

        sender.recordTransfer("Transferred " + money(amount) + " to account " + receiverNumber);
        receiver.recordTransfer("Received " + money(amount) + " from account " + sender.accountNumber);

        System.out.println("Transfer successful!");
        System.out.println("Your new balance: " + money(sender.balance));
    }

    private static void viewAccountDetails() {
        System.out.println("\n------------ ACCOUNT INFORMATION -----------");
        BankAccount account = authenticateAccount();
        if (account != null) account.showDetails();
    }

    private static void viewTransactionHistory() {
        System.out.println("\n------------- TRANSACTION LOG --------------");
        BankAccount account = authenticateAccount();
        if (account != null) account.showTransactions();
    }

    private static BankAccount authenticateAccount() {
        long accountNumber = readLong("Enter account number: ");
        BankAccount account = accounts.get(accountNumber);

        if (account == null) {
            System.out.println("Account not found. Please create an account first.");
            return null;
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            String pin = readLine("Enter your 4-digit PIN: ");
            if (account.verifyPin(pin)) return account;

            int remaining = 3 - attempt;
            if (remaining > 0) {
                System.out.println("Incorrect PIN. " + remaining + " attempt(s) remaining.");
            }
        }

        System.out.println("Authentication failed. Please try again later.");
        return null;
    }

    private static String readPin() {
        while (true) {
            String pin = readLine("Set a 4-digit PIN: ");
            if (pin.matches("\\d{4}")) return pin;
            System.out.println("PIN must contain exactly 4 digits.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(input);
                if (value > 0) return value;
                System.out.println("Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid account number.");
            }
        }
    }

    private static double readAmount(String prompt, boolean allowZero) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double amount = Double.parseDouble(input);
                if (Double.isFinite(amount) && (allowZero ? amount >= 0 : amount > 0)) {
                    return Math.round(amount * 100.0) / 100.0;
                }
                System.out.println(allowZero
                        ? "Amount cannot be negative."
                        : "Amount must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private static String readNonEmptyLine(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String money(double amount) {
        return String.format("Rs. %.2f", amount);
    }
}
