/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import llnl.gnem.util.gui.SpringUtilities;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class OutputPanel extends JPanel{
    JTextArea outputTextArea = new JTextArea();
    public OutputPanel(){
        setLayout(new SpringLayout());
        outputTextArea.setEditable(false);
        outputTextArea.setSize(610, 300);
        String outputText = "Scanning /ipcc/20c3m/atm/da/hfls/gfdl_cm2_0/run1/19651...\n" +
        "Scanning /ipcc/20c3m/atm/da/hfls/gfdl_cm2_0/run1/19652...\n" +
        "Scanning /ipcc/20c3m/atm/da/hfls/gfdl_cm2_0/run1/19653...";
        outputTextArea.setText(outputText);
        JScrollPane outputTextScrollPane = new JScrollPane(outputTextArea);
        outputTextScrollPane.setPreferredSize(new Dimension(1000, 220));//NOT WORKING
        outputTextArea.setBackground(new Color(185, 211, 238));
        add(outputTextScrollPane);
        SpringUtilities.makeCompactGrid(this, // no change?
                1, 1,
                10, 5,  //initX, initY
                10, 3); //xPad, yPad
    }

}
