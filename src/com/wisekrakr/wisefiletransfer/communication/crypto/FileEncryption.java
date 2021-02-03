package com.wisekrakr.wisefiletransfer.communication.crypto;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;

public class FileEncryption {
    public static byte[] encryptFile(byte[] fileBytes, Cipher rsaECipher) throws Exception{

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        int start = 0;
        int fileLength = fileBytes.length;
        while (start < fileLength) {
            byte[] tempBuff;
            if (fileLength - start >= 117) { //todo why 117?
                tempBuff = rsaECipher.doFinal(fileBytes, start, 117);
            } else {
                tempBuff = rsaECipher.doFinal(fileBytes, start, fileLength - start);
            }
            byteOutput.write(tempBuff, 0, tempBuff.length);
            start += 117;
        }
        byte[] encryptedFileBytes = byteOutput.toByteArray();
        byteOutput.close();
        return encryptedFileBytes;

    }
}
