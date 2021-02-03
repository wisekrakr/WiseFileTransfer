package com.wisekrakr.wisefiletransfer.communication;

import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.communication.crypto.FileDecryption;
import com.wisekrakr.wisefiletransfer.communication.crypto.FileEncryption;
import com.wisekrakr.wisefiletransfer.util.Messenger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class FileTransfer {

    public static void sendFiles(HashMap<String, File> filesToBeSend, PrintWriter out, SecretKey key){
        //Inform server about number of files sent
        Messenger.sendMsg(out, String.valueOf(filesToBeSend.size()));

        File fileToSend;
        Iterator<File> iterator = filesToBeSend.values().iterator();
        int size = filesToBeSend.size();

        while (iterator.hasNext()){
            fileToSend = iterator.next();

            byte[]fileBytes = new byte[(int)fileToSend.length()];

            try {
                BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(fileToSend));
                while ((fileInput.read(fileBytes, 0, fileBytes.length)) >= 0){
                    //encrypt the file
                    Cipher encryptCipher = Cipher.getInstance(Constants.AES);
                    encryptCipher.init(Cipher.ENCRYPT_MODE, key);
                    byte[]encryptedFile = FileEncryption.encryptFile(fileBytes, encryptCipher);

                    //Send server the file name
                    Messenger.sendMsg(out, fileToSend.getName());
                    //Send server the file length
                    Messenger.sendMsg(out, String.valueOf(encryptedFile.length));
                    //Send server the encrypted file
                    Messenger.sendMsg(out, DatatypeConverter.printBase64Binary(encryptedFile));

                    iterator.remove();
                }


                if(size == filesToBeSend.size() + 1) {
                    size--;
                    out.println(Constants.CLIENT_ONE_FILE_SENT);
                }else{
                    fileInput.close();
                    break;
                }
            }catch (Throwable t){
                throw new IllegalArgumentException("Could not send file: " + fileToSend.getAbsolutePath(),t);
            }
        }
        Messenger.sendMsg(out, Constants.CLIENT_DONE);

    }

    public static void receiveFiles(BufferedReader in, Cipher rsaDecryptCipherPrivate, Cipher AESCipher, PrivateKey privateKey){
        boolean clientDone;

        final int NUMBER_OF_THREADS = (int) ((Runtime.getRuntime().availableProcessors()*0.8) * 1.5);
        final Executor threadExec = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        final Phaser phaser = new Phaser();
        // Registers current thread
        phaser.register();

        try {
            int numFilesExpected = Integer.parseInt(in.readLine());
            do{
                if(numFilesExpected==0){
                    break;
                }
                String clientsFileName = in.readLine();

                if(clientsFileName.equals(Constants.CLIENT_DONE)){
                    break;
                }

                int clientFileSize = Integer.parseInt(in.readLine());

                byte[] encryptedDataFile = new byte[clientFileSize];

                // Read in encrypted file String representation
                String clientEncryptedFileString = in.readLine();
                // Registers a new file to decrypt
                phaser.register();
                Runnable decryptionWorker = () -> {
                    try {
                        rsaDecryptCipherPrivate.init(Cipher.DECRYPT_MODE, privateKey);
                        FileDecryption.handleDecryption(encryptedDataFile, clientEncryptedFileString, clientsFileName,
                                AESCipher);
                    } catch (Throwable t) {
                        throw new IllegalStateException("Could not decrypt file",t);
                    }
                };

                threadExec.execute(decryptionWorker);

                clientDone = Constants.CLIENT_DONE.equals(in.readLine());
            }while(!clientDone);
        }catch (Throwable t){
            throw new IllegalStateException("Error: could not receive file",t);
        }
        phaser.arriveAndAwaitAdvance();
    }
}
