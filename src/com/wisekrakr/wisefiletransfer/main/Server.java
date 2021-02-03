package com.wisekrakr.wisefiletransfer.main;

import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.authentication.CertificateHandler;
import com.wisekrakr.wisefiletransfer.authentication.CryptoClient;
import com.wisekrakr.wisefiletransfer.authentication.NonceAuthenticator;
import com.wisekrakr.wisefiletransfer.communication.FileTransfer;
import com.wisekrakr.wisefiletransfer.communication.crypto.FileDecryption;
import com.wisekrakr.wisefiletransfer.util.Messenger;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Basic server that will receive a connection from a client, and that will trigger the authentication protocol.
 * When the client is authenticated, the client can send files.
 * Authentication through certificates. The server is the CA and will request a signed certificate.
 */
public class Server extends CryptoClient {

    public static void main(String[] args) {
        new Server(Constants.PRIVATE_KEY_SERVER, Constants.SERVER_CERT).start();
    }

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final NonceAuthenticator nonceAuthenticator;
    private final CertificateHandler certificateHandler;
    private final String certFilePath;
    private final List<User> clients;

    public Server(String privateKeyFilePath, String certFilePath){
        super(privateKeyFilePath, certFilePath);

        this.certFilePath = certFilePath;
        this.clients = new ArrayList<>();

        nonceAuthenticator = new NonceAuthenticator();
        certificateHandler = new CertificateHandler();
    }

    private boolean authenticationProtocol(BufferedReader in, PrintWriter out, Cipher rsaECipher) throws Exception{

        nonceAuthenticator.receiveNonceFromClient(in, out, rsaECipher);

        certificateHandler.sendSignedCertToClient(out, certFilePath);

        String serverNonceString = nonceAuthenticator.generateNonceToValidateClient();

        // sending nonce to client
        Messenger.sendMsg(out,DatatypeConverter.printBase64Binary(nonceAuthenticator.getServerNonce()));

        // receiving encrypted nonce with client's private key
        String encryptedNonce = in.readLine();
        byte[] encryptedServerNonce = DatatypeConverter.parseBase64Binary(encryptedNonce);

        // request client's public key
        Messenger.sendMsg(out, Constants.REQUEST_CLIENT_PUBLIC_KEY);

        X509Certificate clientCert = certificateHandler.createCertificate(in, Constants.SIGNED_CERT_REQUEST);

        PublicKey clientPublicKey = clientCert.getPublicKey();
        clientCert.checkValidity();

        String decryptedNonceString = FileDecryption.decryptSignedCert(clientPublicKey, null,
                encryptedServerNonce, rsaCryptoCipher());
        if(!decryptedNonceString.equals(serverNonceString)){
            System.out.println("Client authentication failed!");
            //todo termination method
        }

        System.out.println("Completed authentication protocol");
        Messenger.sendMsg(out, Constants.SERVER_READY_TO_RECEIVE);

        return true;
    }

    protected void handleRequest(Socket clientSocket) throws Exception{

        PrivateKey privateKey = privateKey();

        // Create encryption cipher
        final Cipher rsaECipherPrivate = rsaCryptoCipher();
        rsaECipherPrivate.init(Cipher.ENCRYPT_MODE, privateKey);

        BufferedReader in = new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Create new user for client thread pool
        String username = in.readLine();
        User newUser = new User(clientSocket, username);
        clients.add(newUser);

        Messenger.sendMsg(out, newUser.getNickname());

        boolean proceed = authenticationProtocol(in, out,rsaECipherPrivate);

        if(!proceed){
            System.out.println("Authentication protocol failed!");
            return;
        }

        // Generate keypair here
        KeyPair keyPair = generateKeyPair();
        Key serverPublicKey = keyPair.getPublic();
        Key serverPrivateKey = keyPair.getPrivate();

        // Send client my public key
        Messenger.sendMsg(out,Base64.getEncoder().encodeToString(serverPublicKey.getEncoded()));

        // Waiting for encrypted AES Key from client
        String AESKeyString = in.readLine();
        System.out.println("Waiting for encrypted AES Key from client: " + AESKeyString);
        final Cipher serverPublicDCipher = rsaCryptoCipher();
        serverPublicDCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);

        Key AESKey = getAESKey(AESKeyString,serverPublicDCipher);

        Cipher AESCipher = Cipher.getInstance(Constants.AES);
        AESCipher.init(Cipher.DECRYPT_MODE, AESKey);

        //todo Either go for file transferring or switch to chat
        //get client message of choice
        //start that operation
        String nextOperation = in.readLine();
        System.out.println("Server next operation: " + nextOperation);
        if(nextOperation.equals(Constants.SECURE_FILE_TRANSFER)){
            Messenger.sendMsg(out, "go for files");
            receiveFiles(in, AESCipher, privateKey);

        }else {
            if(clients.isEmpty()){
                in.close();
                out.close();
                serverSocket.close();
            }
        }
    }

    /**
     * Receive encrypted files from a client
     */
    private void receiveFiles(BufferedReader in, Cipher AESCipher, PrivateKey privateKey) {
        try {
            FileTransfer.receiveFiles(in, rsaCryptoCipher(),AESCipher,privateKey);
        }catch (Throwable t){
            throw new IllegalStateException("Could not receive encrypted files",t);
        }
    }


    @Override
    public void run() {
        int portNum = 8080;  // socket address
        try {
            serverSocket = new ServerSocket(portNum);

            System.out.println("Server started on socket: " + serverSocket.getLocalSocketAddress());

            final Executor exec = Executors.newCachedThreadPool();

            while(true){
                System.out.println("Accepting client connections now ...");

                clientSocket = serverSocket.accept();
                System.out.println("Client connection established!");
                Runnable OpenConnections = () -> {
                    try {
                        handleRequest(clientSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };

                exec.execute(OpenConnections);

            }
        }catch (Throwable t){
            throw new IllegalStateException("ERROR: Could not start a new server socket",t);
        }
    }

}




