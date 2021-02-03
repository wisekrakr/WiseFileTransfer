package com.wisekrakr.wisefiletransfer.util;

import java.io.PrintWriter;

public class Messenger {
    /**
     * Send a String message to the other party
     * @param out PrintWriter to send a message with
     * @param msg The message in string form
     */
    public static void sendMsg(PrintWriter out, String msg){
        if(msg.length() < 30){
            System.out.println("MESSENGER SENDING: " + msg);
        }
        out.println(msg);
        out.flush();
    }


}
