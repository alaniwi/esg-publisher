/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;

import java.awt.Color;
import javax.swing.JPanel;
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
public class ErrorPanel extends JPanel {
    JTextArea errorTextArea = new JTextArea();
    public ErrorPanel() {
        setLayout(new SpringLayout());
        errorTextArea.setEditable(false);
        //errorTextArea.setSize(600, 300);
	    String errorText = "Error scanning /ipcc/20c3m/atm/da/hfls/gfdl_cm2_0/run1/19655\n" +
	                    	"Error scanning /ipcc/20c3m/atm/da/hfls/gfdl_cm2_0/run1/19656\n" +
	                    	"Error publishing /ipcc/20c3m/atm/da/hfls/gfdl_cm2_0/run1/19656\n" + "...";
	    errorTextArea.setText(errorText);
        errorTextArea.setForeground(new Color(142, 35, 35));
        errorTextArea.setBackground(Color.WHITE);
        add(errorTextArea);
        SpringUtilities.makeCompactGrid(this, // no change?
                1, 1,
                10, 5,  //initX, initY
                10, 3); //xPad, yPad


    }

}
