package com.wisekrakr.wisefiletransfer.communication.crypto;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Map;

public class FileDecryption {

    public static String decryptSignedCert(PublicKey publicKey, String serverInitialReply, byte[]bytes,Cipher cipher){
        try {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedBytes;
            if(serverInitialReply != null)decryptedBytes = cipher.doFinal(DatatypeConverter.parseBase64Binary(serverInitialReply));
            else decryptedBytes = cipher.doFinal(bytes);
            return new String (decryptedBytes, StandardCharsets.UTF_8); //todo was UTF-16
        }catch (Throwable t){
            throw new IllegalStateException("Could not decrypt certificate: " + serverInitialReply,t);
        }
    }

    public static byte[] decryptFile(byte[] encryptedData, Cipher rsaDecryptionCipher) throws Exception{

        System.out.println("Decrypting files ... ");

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        int start = 0;
        int fileSize = encryptedData.length;
        while (start < fileSize) {
            byte[] tempBuff;
            if (fileSize - start >= 128) {
                tempBuff = rsaDecryptionCipher.doFinal(encryptedData, start, 128);
            } else {
                tempBuff = rsaDecryptionCipher.doFinal(encryptedData, start, fileSize - start);
            }
            byteOutput.write(tempBuff, 0, tempBuff.length);
            start += 128;
        }
        byte[] decryptedFileBytes = byteOutput.toByteArray();
        byteOutput.close();

        System.out.println("Decryption complete");
        return decryptedFileBytes;
    }

    public static void handleDecryption(byte[] encryptedDataFile, String encryptedFileString, String fileName, Cipher AESDCipher) throws Exception{
        encryptedDataFile = DatatypeConverter.parseBase64Binary(encryptedFileString);

        byte[] clientDecryptedFileBytes = decryptFile(encryptedDataFile,AESDCipher);
        fileName = fileName.replace("\\",",");
        String[] temp = fileName.split(",");
        File directory = new File("ReceivedFiles");
        if(!directory.exists()){
            directory.mkdir();
        }

        FileOutputStream fileOutput = new FileOutputStream("ReceivedFiles/" + temp[temp.length-1]);
        fileOutput.write(clientDecryptedFileBytes, 0, clientDecryptedFileBytes.length);
        fileOutput.close();
        System.out.println("successfully saved client's file : " + fileName);
    }

}
