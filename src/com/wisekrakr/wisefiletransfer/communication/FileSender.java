package com.wisekrakr.wisefiletransfer.communication;


import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.communication.crypto.FileEncryption;
import com.wisekrakr.wisefiletransfer.util.Messenger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

public class FileSender {

    public void sendFiles(HashMap<String, File> filesToBeSend, PrintWriter out, SecretKey key){
        //Inform server about number of files sent
        Messenger.sendMsg(out, String.valueOf(filesToBeSend.size()));

        File fileToSend;
        Iterator<File>iterator = filesToBeSend.values().iterator();
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
}
