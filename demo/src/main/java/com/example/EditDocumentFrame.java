package com.example;

import java.awt.BorderLayout;
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

public class EditDocumentFrame extends JFrame {

    private int docId;
    private String username;
    private JTextArea contentArea;

    public EditDocumentFrame(int docId, String username, String initialContent) {
        this.docId = docId;
        this.username = username;
        setTitle("Edit Document " + docId);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null);

        contentArea = new JTextArea(initialContent);
        JScrollPane scrollPane = new JScrollPane(contentArea);

        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateDocument();
                    dispose(); // Close the edit document frame
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error updating document: " + ex.getMessage());
                }
            }
        });
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateDocument() throws Exception {
        String content = contentArea.getText();
        String url = "http://localhost:8000/api/updateDocument/" + docId + "/" + username;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set up the connection properties
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        // Create the JSON request payload
        String jsonInputString = "{\"content\": \"" + content + "\"}";

        // Send the request
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 200) { // Success
            JOptionPane.showMessageDialog(null, "Document updated successfully!");
        } else {
            // Handle error response
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
            }
            throw new Exception("Failed to update document: HTTP code " + responseCode + " - " + errorResponse.toString());
        }
    }
}
