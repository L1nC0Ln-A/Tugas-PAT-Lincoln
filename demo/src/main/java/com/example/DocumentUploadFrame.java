package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DocumentUploadFrame extends JFrame {

    private JTextField ownerField;
    private JTextArea contentArea;

    public DocumentUploadFrame(String username) {
        setTitle("Upload New Document");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel ownerLabel = new JLabel("Owner:");
        ownerField = new JTextField(username);
        ownerField.setEditable(false); // Set owner field to read-only

        JLabel contentLabel = new JLabel("Content:");
        contentArea = new JTextArea();

        JButton uploadButton = new JButton("Upload");
        JButton cancelButton = new JButton("Cancel");

        panel.add(ownerLabel);
        panel.add(ownerField);
        panel.add(contentLabel);
        panel.add(new JScrollPane(contentArea));
        panel.add(uploadButton);
        panel.add(cancelButton);

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String owner = ownerField.getText();
                String content = contentArea.getText();
                try {
                    uploadDocument(owner, content);
                    JOptionPane.showMessageDialog(null, "Document uploaded successfully!");
                    dispose(); // Close the upload frame after successful upload
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error uploading document.");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the upload frame if cancel button is clicked
            }
        });

        add(panel, BorderLayout.CENTER);
    }

    private void uploadDocument(String owner, String content) throws Exception {
        String url = "http://localhost:8000/api/uploadDocument";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set up the connection properties
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        // Create the JSON request payload
        String jsonInputString = "{\"owner\": \"" + owner + "\", \"content\": \"" + content + "\"}";

        // Send the request
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Check response code
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to upload document. HTTP error code: " + responseCode);
        }
    }
}
