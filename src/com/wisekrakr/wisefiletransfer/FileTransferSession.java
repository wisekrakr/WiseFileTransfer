package com.wisekrakr.wisefiletransfer;

import java.io.File;
import java.util.Map;

public interface FileTransferSession {
    void queueFiles(Map<String, File> filesToBeSend);
    void sendFiles();
}
