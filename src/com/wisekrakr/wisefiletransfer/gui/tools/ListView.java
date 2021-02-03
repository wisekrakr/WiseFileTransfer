package com.wisekrakr.wisefiletransfer.gui.tools;

import com.wisekrakr.wisefiletransfer.gui.ClientGUI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JPanel with a ListSelectionListener. Creates and shows a list of files that can also be deleted
 * {@link RemoveFromListListener} with a push of a button.
 */
public class ListView extends JPanel implements ListSelectionListener {
    private final JList<String> list;
    private final DefaultListModel<String> listModel;

    private static final String removeString = "Remove";
    private final JButton removeButton;

    public ListView() {
        super(new BorderLayout());

        listModel = new DefaultListModel<>();

        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        list.setSelectionBackground(ClientGUI.LIGHT_CYAN);
        JScrollPane listScrollPane = new JScrollPane(list);

        removeButton = new JButton(removeString);
        removeButton.setActionCommand(removeString);
        removeButton.addActionListener(new RemoveFromListListener());

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,  BoxLayout.LINE_AXIS));
        buttonPane.add(removeButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    public DefaultListModel<String> getListModel() {
        return listModel;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            removeButton.setEnabled(list.getSelectedIndex() != -1);
        }
    }

    class RemoveFromListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();

            if (index != -1){
                listModel.remove(index);

                int size = listModel.getSize();

                if (size == 0) {
                    removeButton.setEnabled(false);

                } else {
                    if (index == listModel.getSize()) {
                        index--;
                    }

                    list.setSelectedIndex(index);
                    list.ensureIndexIsVisible(index);
                }
            }
        }
    }
}
