package com.wisekrakr.wisefiletransfer.authentication;

import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.util.Messenger;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

public class NonceAuthenticator {

    private String nonceString;
    private byte[] serverNonce;

    /**
     * Send nonce as the message for the server to encrypt, to make sure no playback attack can take place
     */
    public void sendNonceToServer(PrintWriter out){
        byte[] nonce = new byte[32];
        Random rand;
        try {
            rand = SecureRandom.getInstance(Constants.NONCE_ALGORITHM);

            rand.nextBytes(nonce);
            nonceString = new String(nonce, StandardCharsets.UTF_8); //todo was UTF-16
        }catch (Exception e){
            e.printStackTrace();
        }
        String sendNonce = DatatypeConverter.printBase64Binary(nonce);
        Messenger.sendMsg(out, sendNonce);
    }

    public String getNonceString() {
        return nonceString;
    }

    public byte[] getServerNonce() {
        return serverNonce;
    }

    /**
     * Receiving the client nonce to prevent replay (nonce is used only once). The server sends the client a secret.     *
     * @param in BufferedReader to receive messages from the client
     * @param out PrintWriter to send messages to the client
     * @param rsaECipher Encryption Cipher to encrypt the nonce with
     */
    public void receiveNonceFromClient(BufferedReader in, PrintWriter out, Cipher rsaECipher){
        byte[] clientNonceInBytes;
        try {
            String clientNonce = in.readLine();

            System.out.println("server received nonce from client: " + clientNonce);

            clientNonceInBytes = DatatypeConverter.parseBase64Binary(clientNonce);

            byte[] encryptedNonce = rsaECipher.doFinal(clientNonceInBytes);

            sendSecret(out, encryptedNonce);

        }catch (Throwable t){
            throw new IllegalStateException("Error receiving nonce from client",t);
        }
    }

    /**
     * Client method; receiving the nonce from the server
     * @param rsaECipherPrivate Encryption Cipher for private key encryption
     * @param in BufferedReader to receive messages from the server
     * @return Encrypted nonce data in a single-part operation
     */
    public byte[] receiveNonceFromServer(Cipher rsaECipherPrivate, BufferedReader in){
        byte[] serverNonceInBytes;

        try{
            String serverNonce = in.readLine();
            serverNonceInBytes = DatatypeConverter.parseBase64Binary(serverNonce);
            System.out.println("client received nonce from server: " + serverNonce);

            return rsaECipherPrivate.doFinal(serverNonceInBytes);
        }catch (Throwable t){
            throw new IllegalStateException("Error receiving nonce from server",t);
        }

    }

    /**
     * Generate nonce to ensure that client is a valid requester, and not a playback attacker
     * @return random nonce in string form
     */
    public String generateNonceToValidateClient(){
        serverNonce = new byte[32];
        try {
            Random randGen = SecureRandom.getInstanceStrong();
            randGen.nextBytes(serverNonce);
            return new String(serverNonce, StandardCharsets.UTF_8);
        }catch (Throwable t){
            throw new IllegalArgumentException("Could not generate random nonce",t);
        }
    }

    /**
     * Method used by the server to send the client a secret "message" (nonce) for authentication purposes.
     * @param out PrintWriter that sends the message
     * @param encryptedNonce by Cipher encrypted byte array with the client's nonce
     */
    private void sendSecret(PrintWriter out, byte[] encryptedNonce){
        String secret = DatatypeConverter.printBase64Binary(encryptedNonce);
        Messenger.sendMsg(out, secret);
        System.out.println("server send secret to client: " + secret);
    }
}
