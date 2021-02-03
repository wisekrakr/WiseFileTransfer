package com.wisekrakr.wisefiletransfer.gui;


import com.wisekrakr.wisefiletransfer.ClientInstance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * StartUp GUI. The user will see this GUI first. Enter a username and connect.
 * Extends Java's JFrame and a GUI interface with show and close methods attached
 */
public class ConnectionGUI extends JFrame implements GUI {

    private JPanel panel;
    private final ClientInstance clientInstance;

    public ConnectionGUI(ClientInstance clientInstance){
        this.clientInstance = clientInstance;
    }

    @Override
    public void showGUI(){
        setResizable(false);
        setLayout(new BorderLayout());

        setBounds(screenSize.width + DESIRED_WIDTH, screenSize.height, DESIRED_WIDTH, DESIRED_HEIGHT);
        setFocusable(true);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setBackground(DARK_CYAN);

        connect();

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void closeGUI() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void connect(){
        JTextField textField = new JTextField("username/nickname");

        JButton connectButton = new JButton("Connect to server");
        connectButton.addActionListener(e -> {
            clientInstance.connect(textField.getText());

            SwingUtilities.invokeLater(() -> new ClientGUI(clientInstance).showGUI());

            closeGUI();
        });

        panel.add(textField);
        panel.add(connectButton);
    }
}
