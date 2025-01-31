import java.sql.*;
import java.util.Scanner;

public class ATMSimulation {
    private static final String URL = "jdbc:mysql://localhost:3306/atm_smln";
    private static final String USER = "root";  // MySQL username
    private static final String PASSWORD = "Maddheshiya1@";  // MySQL password

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;

        try {
            // Connect to the MySQL database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the ATM simulation database.");

            // ATM Simulation loop
            while (true) {
                System.out.println("\nWelcome to the ATM");
                System.out.print("Enter account number: ");
                int accountNumber = scanner.nextInt();
                System.out.print("Enter PIN: ");
                String pin = scanner.next();

                if (validateUser(connection, accountNumber, pin)) {
                    System.out.println("Login successful.");
                    boolean exit = false;

                    // ATM Menu
                    while (!exit) {
                        System.out.println("\nATM Menu:");
                        System.out.println("1. Check Balance");
                        System.out.println("2. Deposit");
                        System.out.println("3. Withdraw");
                        System.out.println("4. Exit");
                        System.out.print("Choose an option: ");
                        int choice = scanner.nextInt();

                        switch (choice) {
                            case 1:
                                checkBalance(connection, accountNumber);
                                break;
                            case 2:
                                depositMoney(connection, accountNumber, scanner);
                                break;
                            case 3:
                                withdrawMoney(connection, accountNumber, scanner);
                                break;
                            case 4:
                                exit = true;
                                System.out.println("Thank you for using the ATM.");
                                break;
                            default:
                                System.out.println("Invalid option. Try again.");
                        }
                    }
                } else {
                    System.out.println("Invalid account number or PIN. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Validate user login (account number and PIN)
    private static boolean validateUser(Connection connection, int accountNumber, String pin) {
        String query = "SELECT * FROM accounts WHERE account_number = ? AND pin = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountNumber);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check balance
    private static void checkBalance(Connection connection, int accountNumber) {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Your balance is: Rs." + rs.getBigDecimal("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Deposit money
    private static void depositMoney(Connection connection, int accountNumber, Scanner scanner) {
        System.out.print("Enter amount to deposit: Rs.");
        double amount = scanner.nextDouble();
        if (amount > 0) {
            String query = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountNumber);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Deposited Rs." + amount + " successfully.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    // Withdraw money
    private static void withdrawMoney(Connection connection, int accountNumber, Scanner scanner) {
        System.out.print("Enter amount to withdraw: Rs.");
        double amount = scanner.nextDouble();
        if (amount > 0) {
            String checkBalanceQuery = "SELECT balance FROM accounts WHERE account_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkBalanceQuery)) {
                stmt.setInt(1, accountNumber);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");
                    if (currentBalance >= amount) {
                        String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setDouble(1, amount);
                            updateStmt.setInt(2, accountNumber);
                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                System.out.println("Withdrew Rs." + amount + " successfully.");
                            }
                        }
                    } else {
                        System.out.println("Insufficient balance.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid withdrawal amount.");
        }
    }
}
