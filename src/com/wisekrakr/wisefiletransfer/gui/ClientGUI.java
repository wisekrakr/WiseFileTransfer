package com.wisekrakr.wisefiletransfer.gui;

import com.wisekrakr.wisefiletransfer.ClientInstance;
import com.wisekrakr.wisefiletransfer.gui.tools.ListView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The ClientGUI extends Java's JFrame and implements a GUI Interface with show and close methods
 * Search for files and send them with this simple GUI. The Files are shown in a {@link ListView}
 */
public class ClientGUI extends JFrame implements GUI {

    private JPanel buttonPanel;
    private JFileChooser fc;
    private final Map<String, File>filesToSend = new HashMap<>();
    private ListView listView;
    private final ClientInstance clientInstance;

    public ClientGUI(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
        setTitle("WiseFileTransfer");
    }

    @Override
    public void showGUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(true);
        setLayout(new BorderLayout());

        setBounds(screenSize.width + DESIRED_WIDTH, screenSize.height, DESIRED_WIDTH, DESIRED_HEIGHT);
        setFocusable(true);
        setLocationRelativeTo(null);

        buttonPanel = new JPanel(new GridLayout(0,1));
        buttonPanel.setBackground(DARK_CYAN);

        listView = new ListView();
        listView.setOpaque(true);
        setContentPane(listView);

        //search for the files to send and add them to a hashmap
        fileSearching();
        //start the client and send the hashmap filled with files
        sendFiles();
        //terminating the connection
        terminate();

        add(new JLabel(clientInstance.username()), BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.EAST);
        setVisible(true);
    }

    @Override
    public void closeGUI() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }


    private void fileSearching(){
        JButton fileSearch = new JButton("Search for Files", null);
        fileSearch.addActionListener(e -> {
            fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if((fc.showOpenDialog(ClientGUI.this) == JFileChooser.APPROVE_OPTION)){
                putFilesInMap();
            }
        });

        buttonPanel.add(fileSearch);
    }

    private void putFilesInMap(){
        File[] selectedFilesList = fc.getSelectedFiles();

        for (int i = 0; i < selectedFilesList.length; i++) {
            filesToSend.put(selectedFilesList[i].getAbsolutePath(), selectedFilesList[i]);
            listView.getListModel().add(i, selectedFilesList[i].getAbsolutePath());
        }
        clientInstance.fileTransferSession().queueFiles(filesToSend);
    }


    private void sendFiles() {
        JButton sendButton = new JButton("Send Files");
        sendButton.addActionListener(e -> clientInstance.fileTransferSession().sendFiles());

        buttonPanel.add(sendButton);
    }

    private void terminate(){
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> clientInstance.exit());
        buttonPanel.add(exitButton);
    }
}
