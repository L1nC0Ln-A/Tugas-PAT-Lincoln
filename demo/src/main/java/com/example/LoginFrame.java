package com.example;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LoginFrame extends JFrame {

    private JTextField userText;
    private JPasswordField passText;
    private JButton logoutButton;

    // Constructor
    public LoginFrame() {
        // Set frame properties
        setTitle("Login Page");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create panel to hold components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        // Create components
        JLabel userLabel = new JLabel("Username:");
        userText = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        passText = new JPasswordField(20);

        JButton loginButton = new JButton("Login");
        logoutButton = new JButton("Logout");
        logoutButton.setEnabled(false); // Disable logout button initially

        JButton registerButton = new JButton("Register"); // Register button

        // Add components to panel
        panel.add(userLabel);
        panel.add(userText);
        panel.add(passLabel);
        panel.add(passText);
        panel.add(loginButton);
        panel.add(logoutButton);
        panel.add(registerButton); // Add register button to panel

        // Add panel to frame
        add(panel, BorderLayout.CENTER);

        // Add action listener to login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passText.getPassword());
                try {
                    String response = sendLoginRequest(username, password);
                    JSONObject jsonResponse = new JSONObject(response);
                    int status = jsonResponse.getInt("status");
                    if (status == 200) {
                        JOptionPane.showMessageDialog(null, "Login successful!");
                        logoutButton.setEnabled(true); // Enable logout button on successful login
                        DocumentDisplayFrame documentDisplayFrame = new DocumentDisplayFrame(username);
                        documentDisplayFrame.setVisible(true); // Open document display window
                    } else if (status == 400 && "User is already logged in".equals(jsonResponse.getString("error"))) {
                        JOptionPane.showMessageDialog(null, "User is already logged in.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error connecting to server.");
                }
            }
        });
        

        // Add action listener to logout button
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                try {
                    String response = sendLogoutRequest(username);
                    JSONObject jsonResponse = new JSONObject(response);
                    int status = jsonResponse.getInt("status");
                    if (status == 200) {
                        JOptionPane.showMessageDialog(null, "Logout successful!");
                        logoutButton.setEnabled(false); // Disable logout button on successful logout
                        userText.setText(""); // Clear username field
                        passText.setText(""); // Clear password field
                    } else {
                        JOptionPane.showMessageDialog(null, "Logout failed.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error connecting to server.");
                }
            }
        });

        // Add action listener to register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame().setVisible(true); // Open registration frame
            }
        });
    }

    private String sendLoginRequest(String username, String password) throws Exception {
        String url = "http://localhost:8000/api/login";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set up the connection properties
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        // Create the JSON request payload
        String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        // Send the request
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private String sendLogoutRequest(String username) throws Exception {
        String url = "http://localhost:8000/api/logout";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set up the connection properties
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        // Create the JSON request payload
        String jsonInputString = "{\"username\": \"" + username + "\"}";

        // Send the request
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static void main(String[] args) {
        // Create and display the frame
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
