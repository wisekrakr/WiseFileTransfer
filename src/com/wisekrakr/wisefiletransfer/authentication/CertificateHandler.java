package com.wisekrakr.wisefiletransfer.authentication;


import com.wisekrakr.wisefiletransfer.Constants;
import com.wisekrakr.wisefiletransfer.util.Messenger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class CertificateHandler {

    /**
     * Sending a signed certificate file //todo automate
     * @param out PrintWriter will send the file in string form
     * @param serverCertPath the pathname where the certificate file is located
     */
    public void sendSignedCertToClient(PrintWriter out, String serverCertPath){
        File certFile = new File(serverCertPath);
        byte[] certBytes = new byte[(int) certFile.length()];

        try {
            BufferedInputStream certFileInput = new BufferedInputStream(new FileInputStream(certFile));

            while (certFileInput.read(certBytes,0,certBytes.length) >= 0){
                // Sending signed cert of server - includes public key of client
                Messenger.sendMsg(out, DatatypeConverter.printBase64Binary(certBytes));
            }
            certFileInput.close();

        }catch (Throwable t){
            throw new IllegalStateException("Error sending signed certificate client",t);
        }
    }

    /**
     * Create a file for the certificate and generating a X509Certificate to use further
     * @param in BufferReader
     * @return a newly generated X509Certificate
     */
    public X509Certificate createCertificate(BufferedReader in, String certFilePath){
        byte[] certificate;
        try {
            String certificateString = in.readLine();
            certificate = DatatypeConverter.parseBase64Binary(certificateString);

            System.out.println("certificate string to create new cert with: " +  certificateString);

            FileOutputStream fileOutput = new FileOutputStream(certFilePath);
            fileOutput.write(certificate, 0, certificate.length);
            FileInputStream fileInput = new FileInputStream(certFilePath);

            CertificateFactory cf = CertificateFactory.getInstance(Constants.X509);
            return (X509Certificate) cf.generateCertificate(fileInput);
        }catch (Throwable t){
            throw new IllegalArgumentException("Error: did not receive certificate: " + certFilePath,t);
        }
    }

    /**
     * Turns the certificate file into a byte array.
     * Reads the file and returns a byte array
     * @param certFilePath path name where the certificate file is located
     * @return byte array
     */
    public byte[] certFileInBytes(String certFilePath){
        File certFile = new File(certFilePath);
        byte[] certBytes = new byte[(int) certFile.length()];
        try {
            BufferedInputStream clientCertFileInput = new BufferedInputStream(new FileInputStream(certFile));
            clientCertFileInput.read(certBytes,0,certBytes.length);
            clientCertFileInput.close();
        }catch (Throwable t){
            throw new IllegalStateException("Could not read certification file",t);
        }
        return certBytes;
    }
}
