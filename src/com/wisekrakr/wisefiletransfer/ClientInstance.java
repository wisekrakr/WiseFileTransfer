package com.wisekrakr.wisefiletransfer;

public interface ClientInstance {
    void connect(String username);
    String username();
    FileTransferSession fileTransferSession();
    void exit();
}
