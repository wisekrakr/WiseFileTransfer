package com.wisekrakr.wisefiletransfer.communication;

import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.communication.crypto.FileDecryption;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.security.PrivateKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class FileReceiver {

    public void receiveFiles(BufferedReader in, Cipher rsaDecryptCipherPrivate, Cipher AESCipher, PrivateKey privateKey){
        Map<String,Long> fileUploadTimings = new ConcurrentHashMap<>();
        boolean clientDone;

        final int NCPU = Runtime.getRuntime().availableProcessors();
        final double TCPU = 0.8;
        final double WCRATIO = 0.5;
        final int NTHREADS = (int) (NCPU*TCPU * (1+WCRATIO));
        final Executor threadExec = Executors.newFixedThreadPool(NTHREADS);
        final Phaser phaser = new Phaser();
        // Registers current thread
        phaser.register();

        try {
            int numFilesExpected = Integer.parseInt(in.readLine());
            do{
                if(numFilesExpected==0){
                    break;
                }
                long startTime = System.currentTimeMillis();

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
                                AESCipher, fileUploadTimings, startTime);
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
