package com.wisekrakr.wisefiletransfer;


import com.wisekrakr.wisefiletransfer.gui.ConnectionGUI;
import com.wisekrakr.wisefiletransfer.main.Client;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.File;
import java.io.Serializable;
import java.util.Map;

public class WiseSecureCon implements Serializable {

    public static void main(String[] args) {
        Options options = new Options();

        CommandLine cmd;
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = commandLineParser(options, args);
        } catch (Throwable t) {
            throw new IllegalStateException("Missing option(s) in program arguments", t);
        }

        WiseSecureCon app = new WiseSecureCon();

        System.out.println(" =======>< FileDeliveryApp activated ><======= ");

        optionNotThere(cmd, "h", " Please fill in the host you want to connect to", options, formatter);
        optionNotThere(cmd, "p", "Please fill in the username used on the domain", options, formatter);

        String hostname = cmd.getOptionValue("hostname");
        String port = cmd.getOptionValue("port");

        app.initializeClient(
                hostname,
                Integer.parseInt(port),
                Constants.PRIVATE_KEY_CLIENT,
                Constants.CLIENT_CERT
        );
    }

    private static CommandLine commandLineParser(Options options, String[] strings) throws ParseException {
        options
                .addOption("h", "hostname", true, "The server you are trying to connect to (i.e. localhost)")
                .addOption("p", "port", true, "Port to connect to (i.e. 8080)");

        CommandLineParser parser = new DefaultParser();

        return parser.parse(options, strings);
    }

    /**
     * Looks if there are no options missing in the program arguments.
     * If there are options missing then the app stops.
     * @param cmd Command line with all the options.
     * @param option the specific option to search for
     * @param message string that is shown if the option in missing
     * @param options the options object that holds all the options
     * @param formatter the help formatter that will print a message
     */
    private static void optionNotThere(CommandLine cmd, String option, String message, Options options, HelpFormatter formatter){
        if(!cmd.hasOption(option)){
            formatter.printHelp("The -" + option + " is missing from the arguments", message, options,"Thank you for using a Wisekrakr product");
            System.exit(1);
        }
    }

    private void initializeClient(String hostName, int port, String privateKeyFilePath, String certFilePath){
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }catch (Throwable t){
            throw new IllegalStateException("Could not set look and feel",t);
        }

        SwingUtilities.invokeLater(()-> new ConnectionGUI(new ClientInstance() {
            private Client client;
            private String username;

            @Override
            public void connect(String username) {
                this.username = username;

                this.client = new Client(username, hostName, port, privateKeyFilePath, certFilePath);
                this.client.start();
            }

            @Override
            public String username() {
                return username;
            }

            @Override
            public void exit() {
                if(client != null) client.terminate();
                System.exit(1);
            }

            @Override
            public FileTransferSession fileTransferSession() {
                return new FileTransferSession() {
                    @Override
                    public void queueFiles(Map<String, File> filesToBeSend) {
                        client.setFilesToBeSend(filesToBeSend);
                    }

                    @Override
                    public void sendFiles() {
                        client.sendOverFiles();
                    }

                };
            }
        }).showGUI());
    }
}
