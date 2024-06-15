package com.example;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.json.JSONArray;
import org.json.JSONObject;

public class DocumentDisplayFrame extends JFrame {

    private String username;
    private JPanel documentPanel;

    public DocumentDisplayFrame(String username) {
        this.username = username;
        setTitle("Documents of " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null);

        documentPanel = new JPanel();
        documentPanel.setLayout(new GridLayout(0, 1));

        JScrollPane scrollPane = new JScrollPane(documentPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton uploadButton = new JButton("Upload Document");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DocumentUploadFrame uploadFrame = new DocumentUploadFrame(username);
                uploadFrame.setVisible(true); // Open document upload frame
            }
        });
        buttonPanel.add(uploadButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDocuments();
            }
        });
        buttonPanel.add(refreshButton);

        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logoutUser();
                dispose(); // Close the document display frame
                new LoginFrame().setVisible(true); // Open login frame
            }
        });
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Fetch documents from the server
        refreshDocuments();
    }

    private void refreshDocuments() {
        documentPanel.removeAll();
        try {
            String documents = fetchDocuments(username);
            JSONObject jsonResponse = new JSONObject(documents);
            JSONArray jsonArray = jsonResponse.getJSONArray("response");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String docInfo = "Document ID: " + jsonObject.getInt("doc_id") + "\n" +
                                 "Owner: " + jsonObject.getString("owner") + "\n" +
                                 "Content: " + jsonObject.getString("content") + "\n";

                JTextArea docTextArea = new JTextArea(docInfo);
                docTextArea.setEditable(false);

                JButton editButton = new JButton("Edit");
                int docId = jsonObject.getInt("doc_id");
                String docContent = jsonObject.getString("content");
                editButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new EditDocumentFrame(docId, username, docContent).setVisible(true);
                    }
                });

                JPanel docPanel = new JPanel(new BorderLayout());
                docPanel.add(new JScrollPane(docTextArea), BorderLayout.CENTER);
                docPanel.add(editButton, BorderLayout.SOUTH);

                documentPanel.add(docPanel);
            }

            documentPanel.revalidate();
            documentPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching documents: " + e.getMessage());
        }
    }

    private String fetchDocuments(String username) throws Exception {
        String url = "http://localhost:8000/api/documentsByOwner/" + username;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set up the connection properties
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == 200) { // Success
            // Read the response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            // Handle error response
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
            }
            throw new Exception("Failed to fetch documents: HTTP code " + responseCode + " - " + errorResponse.toString());
        }
    }

    private void logoutUser() {
        try {
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

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Failed to logout: HTTP code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error logging out: " + e.getMessage());
        }
    }
}
