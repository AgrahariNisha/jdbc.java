import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Stationery Item class
class StationeryItem {
    private String name;
    private double price;
    private int quantity;

    public StationeryItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void reduceQuantity(int amount) {
        if (amount <= quantity) {
            this.quantity -= amount;
        }
    }
}

// Main Class with Database Interaction and User Interface
public class StationaryShop {
    // Database URL, username, and password (adjust as needed)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database"; // Update with your DB name
    private static final String USER = "your_username"; // Update with your DB username
    private static final String PASS = "your_password"; // Update with your DB password

    public static void main(String[] args) {
        // Initialize a list for managing inventory
        List<StationeryItem> inventory = new ArrayList<>();
        double totalSales = 0.0;

        // Load JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBC Driver
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        // Connect to the database and fetch inventory
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT name, price, quantity FROM stationery_items";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                inventory.add(new StationeryItem(name, price, quantity));
            }
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
            return;
        }

        // Create a Scanner object to read user input
        Scanner scanner = new Scanner(System.in);

        // Display menu
        System.out.println("Welcome to the Stationary Shop!");
        System.out.println("-------------------------------");
        for (int i = 0; i < inventory.size(); i++) {
            StationeryItem item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - $" + item.getPrice());
        }
        System.out.println("-------------------------------");

        // Loop until user chooses to exit
        while (true) {
            System.out.print("Enter the number of the product you'd like to purchase (or 0 to exit): ");
            int choice = scanner.nextInt();

            if (choice == 0) {
                break;
            } else if (choice > 0 && choice <= inventory.size()) {
                System.out.print("Enter the quantity you'd like to purchase: ");
                int quantity = scanner.nextInt();

                StationeryItem selectedItem = inventory.get(choice - 1);

                if (quantity > 0 && quantity <= selectedItem.getQuantity()) {
                    double subtotal = selectedItem.getPrice() * quantity;
                    totalSales += subtotal;
                    selectedItem.reduceQuantity(quantity);
                    System.out.println("You purchased " + quantity + " " + selectedItem.getName() + "(s) for $" + subtotal);

                    // Update the quantity in the database
                    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                        String updateQuery = "UPDATE stationery_items SET quantity = ? WHERE name = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setInt(1, selectedItem.getQuantity());
                        updateStmt.setString(2, selectedItem.getName());
                        updateStmt.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println("Error updating the inventory.");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Sorry, we don't have enough stock.");
                }
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("Thank you for shopping at the Stationary Shop!");
        System.out.println("Total sales: $" + totalSales);
    }
}
