package com.wisekrakr.wisefiletransfer.main;

import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.authentication.CertificateHandler;
import com.wisekrakr.wisefiletransfer.authentication.CryptoClient;
import com.wisekrakr.wisefiletransfer.authentication.NonceAuthenticator;
import com.wisekrakr.wisefiletransfer.communication.FileSender;
import com.wisekrakr.wisefiletransfer.communication.crypto.FileDecryption;
import com.wisekrakr.wisefiletransfer.util.Messenger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Client extends CryptoClient {

    private final String username;
    private final String hostName;
    private final int port;
    private final String privateKeyFilePath;
    private final String certFilePath;
    private Map<String,File> filesToBeSend;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean authenticated;
    private SecretKey secretKey; // key for file transferring
    private Thread chatThread;

    public Client(String username, String hostName, int port, String privateKeyFilePath, String certFilePath){
        super(privateKeyFilePath,certFilePath);
        this.username = username;
        this.hostName = hostName;
        this.port = port;
        this.privateKeyFilePath = privateKeyFilePath;
        this.certFilePath = certFilePath;
    }

    public void setFilesToBeSend(Map<String, File> filesToBeSend) {
        this.filesToBeSend = filesToBeSend;
    }

    private void authenticationProtocol() throws Exception {

        clientSocket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        clientSocket.connect(socketAddress);

        System.out.println("client is connected to " + hostName + " on port: " + port);

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // tell server my identity
        Messenger.sendMsg(out, username);

        // wait for server to accept my send request, terminate if rejected
        String serverAccept = in.readLine();
        System.out.println("Client: server got my username: " + serverAccept);
        // TODO: handle server response
        // send response to user if the server denys connection and stop client
        if(!serverAccept.equals(username)){
            System.out.println("Server rejected connection!");
            authenticated = false;
        }

        CertificateHandler certificateHandler = new CertificateHandler();
        NonceAuthenticator nonceAuthenticator = new NonceAuthenticator();

        nonceAuthenticator.sendNonceToServer(out);

        String serverEncryptedNonceReply = in.readLine();
        System.out.println("client: Server gave me secret message: " + serverEncryptedNonceReply);

        //receive signed cert
        X509Certificate signedCert = certificateHandler.createCertificate(in, Constants.SIGNED_CERT);//todo create self signed cert

        PublicKey certAuthKey = signedCert.getPublicKey();
        signedCert.checkValidity();

        //use public key to decrypt signed certificate to extract public key of server
        String decryptedMessage = FileDecryption.decryptSignedCert(certAuthKey, serverEncryptedNonceReply,null,rsaCryptoCipher());
        System.out.println("DecryptedMessage: " + decryptedMessage);

        if (!decryptedMessage.equals(nonceAuthenticator.getNonceString())){
            Messenger.sendMsg(out, Constants.TERMINATE_MSG);
            terminate();
            System.out.println("authentication failed");
            authenticated = false;
        }

        PrivateKey privateKey = privateKey();

        // read in Client's Certificate in preparation for Server Nonce
        // create encryption cipher
        final Cipher rsaECipherPrivate = rsaCryptoCipher();

        rsaECipherPrivate.init(Cipher.ENCRYPT_MODE, privateKey);

        byte[] certBytes = certificateHandler.certFileInBytes(certFilePath);

        // receive nonce from server
        byte[] encryptedServerNonce = nonceAuthenticator.receiveNonceFromServer(rsaECipherPrivate, in);
        String encryptedNonce = DatatypeConverter.printBase64Binary(encryptedServerNonce);
        Messenger.sendMsg(out, encryptedNonce);

        // wait for server to ask for public key, send public key to server
        receiveServerMsg(Constants.REQUEST_CLIENT_PUBLIC_KEY,"you didn't ask for the public key");

        // send the cert as a string
        String encodedKey = DatatypeConverter.printBase64Binary(certBytes);
        Messenger.sendMsg(out, encodedKey);

        // receive success message and initialise handshake
        receiveServerMsg(Constants.SERVER_READY_TO_RECEIVE,"you didn't tell me you're ready to receive my files");

        Cipher rsaECipherServerPublic;
        try {
            // Read in Server public key
            String serverPublicKeyString = in.readLine();
            rsaECipherServerPublic = rsaCryptoCipher();

            Key serverPublicKey = getPublicKey(serverPublicKeyString);
            rsaECipherServerPublic.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        }catch (Throwable t){
            throw new IllegalStateException("Invalid key",t);
        }

        secretKey = getSecretKey(rsaECipherServerPublic, out);

        authenticated = true;

        System.out.println("client successfully authenticated the server");
    }

    /**
     * Send files encrypted with a secret key
     */
    public void sendOverFiles(){
        Messenger.sendMsg(out, Constants.SECURE_FILE_TRANSFER);

        FileSender fileSender = new FileSender();
        fileSender.sendFiles((HashMap<String, File>) filesToBeSend, out, secretKey);

        System.out.println("told server all encrypted files are sent");
    }

    @Override
    public void run() {
        if(hostName==null){
            System.out.println("INVALID HOSTNAME");
            return;
        }
        try {
            authenticationProtocol();
            //todo what happens if auth failed
            if(!authenticated) {

            }else {

            }


        } catch (Throwable t) {
            throw new IllegalStateException("Error during authentication protocol",t);
        }
    }


    /**
     * Receiving certain server messages to confirm we can continue the operation
     * If the server message does not match the message of the client, we stop the connection
     * @param clientMessage client's message
     * @param errorMessage client's message to server
     */
    private void receiveServerMsg(String clientMessage, String errorMessage){
        try {
            String serverMessage = in.readLine();
            if (!serverMessage.equals(clientMessage)){
                Messenger.sendMsg(out, errorMessage);
                terminate();
            }
        }catch (Throwable t){
            throw new IllegalStateException("Error receiving server message",t);
        }
    }

    public void terminate() {
        try {
            out.close();
            in.close();
            clientSocket.close();

        }catch (Throwable t){
            throw new IllegalStateException("Could not close client successfully",t);
        }
    }
}


