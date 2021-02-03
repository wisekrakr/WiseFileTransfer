package com.wisekrakr.wisefiletransfer;

public class Constants {

    public static final String TERMINATE_MSG = "Adios";
    public static final String CLIENT_DONE = "Done";
    public static final String CLIENT_ONE_FILE_SENT = "Sent one file";
    public static final String REQUEST_CLIENT_PUBLIC_KEY = "Send me your public key";
    public static final String SERVER_READY_TO_RECEIVE = "Ready";
    public static final String SECURE_FILE_TRANSFER = "File Transfer";

    //Authentication
    public static final String PRIVATE_KEY_SERVER = "root/ca/private/ca.key.der";
    public static final String SERVER_CERT = "root/ca/certs/ca.cert.crt";
    public static final String PRIVATE_KEY_CLIENT = "root/ca/intermediate/private/client.key.der";
    public static final String CLIENT_CERT = "root/ca/intermediate/certs/server.cert.crt";
    public static final String SIGNED_CERT_REQUEST = "REQUEST_CERT_FOR_SERVER.crt";
    public static final String SIGNED_CERT = "SIGNED_CERT_FOR_CLIENT.crt";

    //Keys
    public static final int KEY_SIZE = 2048;

    //Cipher
    public static final String AES = "AES";
    public static final String RSA = "RSA";
    public static final String RSA_ECB_PADDING = "RSA/ECB/PKCS1Padding";

    //Certs
    public static final String X509 = "X.509";

    //Nonce
    public static final String NONCE_ALGORITHM = "SHA1PRNG";
}
