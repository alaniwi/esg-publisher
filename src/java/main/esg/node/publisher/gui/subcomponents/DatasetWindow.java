/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esg.node.publisher.gui.subcomponents;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import llnl.gnem.util.gui.SpringUtilities;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class DatasetWindow extends JPanel {

    ImageIcon esgLogo;
    JTextPane initialMessagePane;
    JEditorPane outputTextArea, errorTextArea;
    String tabLabel;
    private JPanel initialTopPanel;
    String initialMessageString = "Publisher Software. Version 1.0 Beta @ Copyright LLNL/PCMDI."
                + " Earth Systems Grid Center for Enabling Technologies."
                + " Funded by the US Department Of Energy";
    public DatasetWindow() {
        this.setLayout(new SpringLayout());
        buildInitialMessagePanel();
        add(initialMessagePane);
        SpringUtilities.makeCompactGrid(this,
                1, 1,
                10, 5,  //initX, initY
                10, 3); //xPad, yPad
    }

    private void buildInitialMessagePanel() {
        
        initialTopPanel = new JPanel(new SpringLayout()); //contains initial message displayed
        initialMessagePane = new JTextPane();
        initialMessagePane.setText(initialMessageString);
        initialMessagePane.setPreferredSize(new Dimension(450, 150));

        SimpleAttributeSet attribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(attribute, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attribute, (float) 1.0);
        StyleConstants.setFontFamily(attribute, "Serif");
        StyleConstants.setFontSize(attribute, 14);
        StyleConstants.ColorConstants.setForeground(attribute, Color.DARK_GRAY);

        initialMessagePane.setParagraphAttributes(attribute, true);
        initialMessagePane.setEditable(false);

        esgLogo = new ImageIcon("ESGLogo.png");
        JLabel labelImage = new JLabel();
        labelImage.setIcon(esgLogo);
        initialTopPanel.setBackground(Color.WHITE);
        initialTopPanel.add(labelImage); 

        initialTopPanel.add(initialMessagePane);
        
    }
}
