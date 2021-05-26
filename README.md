# WiseFileTransfer
Java PKI Project with certificate authentication and secure file transfer.

![Master](https://github.com/ipphone/core/workflows/Master/badge.svg)
<img src="https://img.shields.io/badge/Java-build%20with%20Java-blue"/>
![version](https://img.shields.io/badge/version-0.0.3-blue)    

A secure file sharing JavaFx application that allows users to share files the server.
The client side has a simple Swing GUI.

This project was made to learn about public key infrastructure, how to create certificates and private keys and how
to encrypt and decrypt messages, so that the dreadful "Middle Man" can't get to all the secrets.
This study gave me more understanding about what happens behind a secure connection. 

#### The Authentication Protocol -> based of (https://github.com/yinjisheng311/SecureFileTransfer) 
The protocol will authenticate both the server to the client, and client to server.
So that both sides know that the right recipient is getting the data.
 - A nonce request is sent from client to server to prevent playback attacks. 
 - The server will encrypt this nonce with its own private key and sent it back to the client.
 - The client will then request the server's signed certificate to decrypt the encrypted nonce.
 - If the nonce of the client and the server nonce reply matches, the client can be assured of both the identity of the server, 
   as affirmed by the trusted certification authority, and the absence of a playback attack.
 - The Server will repeat this procedure as well, so it can be assured of the client identity.
When a client is authenticated it can send an encrypted file to the server.

<p align="center">
<img src="https://raw.githubusercontent.com/imny94/CSE-Programming-Assignments/master/CSE-Programming-Assignment-2/APFigure.001.jpeg"/>
</p>

#### SPECIFICATION

WiseFileTransfer is compatible with the following specifications:
 - RSA asymmetric encryption, 2048 bit
 - AES symmetric encryption, 128 bit
 - RSA/ECB/PKCS1Padding Cipher cryptography
 - X509 Certificates
 - SHA1PRNG nonce algorithm

#### PREREQUISITES

This software has been developed using Oracle Java Development Kit
version 7.

#### MAVEN DEPENDENCIES


 > *These are the dependencies used in the project:
 ```groovy
    <dependencies>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
        </dependency>
        <dependency>
            <groupId>commons-cli</groupId>
            <artifactId>commons-cli</artifactId>
            <version>1.4</version>
        </dependency>

    </dependencies>
```


#### USAGE

- ** _Enter a username and connect to server_
- ** _The server will start the Authentication Protocol_
- ** _Click on Search Files to search for files on your computer. Multiple files is enabled_
- ** _Click on Send Files to send encrypted files to the server_
- ** _The server will decrypt the files and saves them as a file_




## AUTHOR

David Buendia Cosano davidiscodinghere@gmail.com
