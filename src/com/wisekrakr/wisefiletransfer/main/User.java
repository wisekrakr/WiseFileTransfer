package com.wisekrakr.wisefiletransfer.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private static int nbUser = 0;
    private int userId;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private Socket clientSocket;

    // constructor
    public User(Socket clientSocket, String name) throws IOException {
        this.out = new PrintWriter(clientSocket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientSocket = clientSocket;
        this.nickname = name;
        this.userId = nbUser;

        nbUser += 1;
    }


    public PrintWriter getOutStream(){
        return this.out;
    }

    public BufferedReader getInputStream(){
        return this.in;
    }

    public String getNickname(){ return this.nickname;    }


}
