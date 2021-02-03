package com.wisekrakr.wisefiletransfer.gui;

import java.awt.*;

public interface GUI {
    void showGUI();
    void closeGUI();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int DESIRED_HEIGHT = 500;
    int DESIRED_WIDTH = 500;
    Color LIGHT_CYAN = new Color(176, 228, 234); // #b0e4ea
    Color DARK_CYAN = new Color(18, 95, 101); //#125f65 even darker #115358
}
