package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import com.google.gson.*;
import java.sql.*;

public class ParseThread extends Thread {
    private static final String INPUT_FILE = "C:/Users/LENOVO/OneDrive/Bureau/Order_management/src/main/resources/Data/input.json";
    private static final String OUTPUT_FILE = "C:/Users/LENOVO/OneDrive/Bureau/Order_management/src/main/resources/Data/output.json";
    private static final String ERROR_FILE = "C:/Users/LENOVO/OneDrive/Bureau/Order_management/src/main/resources/Data/errors.json";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/order_management";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private static final int SLEEP_TIME = 3600 * 1000; // 5 seconds

    private Connection conn;

    public ParseThread() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            System.err.println("Error creating database connection: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                processOrders();
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                System.err.println("Error sleeping: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private synchronized void processOrders() {
        File inputDir = new File("C:/Users/LENOVO/OneDrive/Bureau/Order_management/src/main/resources/Data/input");
        File outputDir = new File("C:/Users/LENOVO/OneDrive/Bureau/Order_management/src/main/resources/Data/output");
        File errorDir = new File("C:/Users/LENOVO/OneDrive/Bureau/Order_management/src/main/resources/Data/errors");

        ensureDirectoryExists(inputDir.getAbsolutePath());
        ensureDirectoryExists(outputDir.getAbsolutePath());
        ensureDirectoryExists(errorDir.getAbsolutePath());

        File[] files = inputDir.listFiles();
        System.out.println("Processing " + files.length + " files ...");
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".json")) {
                    System.out.println("Processing json file : " +file.getAbsolutePath().replace("\\", "/"));
                    List<Order> orders = readOrdersFromFile(file.toPath().toString().replace("\\", "/"));
                    List<Order> validOrders = new ArrayList<>();
                    List<Order> invalidOrders = new ArrayList<>();
                    try {
                        for (Order order : orders) {
                            System.out.println("Processing order " + order + " ...");
                            if (customerExists(order.getCustomerId())) {
                                addOrderToDatabase(order);
                                validOrders.add(order);
                            } else {
                                invalidOrders.add(order);
                                System.err.println("Invalid order: " + order);
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error processing orders: " + e.getMessage());
                        // Déplacer le fichier dans le répertoire des erreurs
                        moveFile(file, errorDir);
                    }
                    if (!invalidOrders.isEmpty()) {
                        System.err.println("Invalid orders: " + invalidOrders);
                        moveFile(file, errorDir);
                        System.err.println("File "+file.getName()+" moved to the errors directory ");
                        System.exit(1);
                    }else {
                        System.out.println("Orders processed successfully");
                        moveFile(file, outputDir);
                        System.out.println("File "+file.getName()+" moved to the output directory");

                    }
                }
            }
        }
    }

    private List<Order> readOrdersFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("The file " + filePath + " does not exist. Please ensure it is available.");
            return Collections.emptyList();
        }
        try (Reader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            return Arrays.asList(gson.fromJson(reader, Order[].class));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void writeOrdersToFile(String filePath, List<Order> orders) {
        try (Writer writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(orders, writer);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    private boolean customerExists(int customerId) throws SQLException {
        String query = "SELECT 1 FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Customer " + customerId + " exists");
                return true;
            }else{
                System.out.println("Customer " + customerId + " does not exist");
                return false;
            }
        }
    }

    private void addOrderToDatabase(Order order) throws SQLException {
        String query = "INSERT INTO orders (date, amount, customer_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, order.getDate());
            stmt.setDouble(2, order.getAmount());
            stmt.setInt(3, order.getCustomerId());
            if (stmt.executeUpdate() == 1) {
                System.out.println("Order "+order.getId()+" added to the database");
            }
        }
    }

    private void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }
    private void moveFile(File file, File dir) {
        try {
            File newFile = new File(dir, file.getName());
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error moving file: " + e.getMessage());
        }
    }
}