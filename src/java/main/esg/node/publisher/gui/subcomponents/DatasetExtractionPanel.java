/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;

import esg.node.publisher.gui.util.ExceptionDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import llnl.gnem.util.gui.SpringUtilities;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class DatasetExtractionPanel  extends JPanel implements MouseListener, ActionListener {

   
    JPanel mainPanel = new JPanel(new SpringLayout());
    private static String name = "Data Extraction";

    public DatasetExtractionPanel() {
        this.setLayout(new SpringLayout());
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        this.setBorder(border);
        buildPanel();
        this.setPreferredSize(new Dimension(200, 220));
    }

    public static String getPanelName() {
        return name;
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void buildPanel() {
        //Panel 1 - Specify Project and dataset


        // Panels names reflect their line number on the panel
        JPanel one = new JPanel(new SpringLayout());
        JPanel one_a = new JPanel(new FlowLayout());
        JPanel two = new JPanel(new SpringLayout());
        JPanel three = new JPanel(new SpringLayout());
        
        JLabel dataset_Label = new JLabel("Dataset");

        // prepare one line at a time
        dataset_Label.setForeground(new Color(0, 0, 128)); //change color
        one.add(dataset_Label);
        
        JButton one_Button_a = new JButton("Select All");
        one_Button_a.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                } catch (Exception ex) {
                    ExceptionDialog.displayError(ex);
                }
            }
        });
        JButton one_Button_b = new JButton("Unselect All");
        one_Button_b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                } catch (Exception ex) {
                    ExceptionDialog.displayError(ex);
                }
            }
        });
        one_a.add(one_Button_a);
        one_a.add(one_Button_b);        
        one.add(one_a);
        SpringUtilities.makeCompactGrid(one,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

        
        two.add(new JLabel("Data extraction"));
        JButton two_b = new JButton("Create/Replace");
        two_b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                } catch (Exception ex) {
                    ExceptionDialog.displayError(ex);
                }
            }
        });

        two.add(two_b);
        SpringUtilities.makeCompactGrid(two,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

        three.add(new JLabel("Update extraction"));
        JButton three_b = new JButton("Create/Replace");
        three_b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                } catch (Exception ex) {
                    ExceptionDialog.displayError(ex);
                }
            }
        });

        three.add(three_b);
        SpringUtilities.makeCompactGrid(three,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad
      




        add(one);
        add(two);
        add(three);
      
        SpringUtilities.makeCompactGrid(this,
                3, 1,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

    }

    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /*
     * //Panel 2 - Data Extraction
        add(new JLabel("Dataset"));
        gbc = new GridBagConstraints(0,2,1,1,0.1,0,GridBagConstraints.WEST,
        GridBagConstraints.BOTH,new Insets(5,5,5,0),0,0);
        p2.add(new JLabel("Data extraction"), gbc);
        gbc = new GridBagConstraints(0,4,1,1,0.1,0,GridBagConstraints.WEST,
        GridBagConstraints.BOTH,new Insets(5,5,5,0),0,0);
        p2.add(new JLabel("Update extraction"), gbc);

        gbc = new GridBagConstraints(1,0,1,1,0.1,0,GridBagConstraints.WEST,
        GridBagConstraints.NONE,new Insets(5,40,5,5),0,0);
        selectAll.setForeground(new Color(25,25,112)); //change color
        selectAll.setMargin(new Insets(3,3,3,3));
        p2.add(selectAll, gbc);
        selectAll.addActionListener(this); //ActionListener
        gbc = new GridBagConstraints(2,0,1,1,0.1,0,GridBagConstraints.WEST,
        GridBagConstraints.NONE,new Insets(5,-70,5,5),0,0);
        unselectAll.setMargin(new Insets(3,3,3,3));
        p2.add(unselectAll, gbc);
        unselectAll.addActionListener(this); //ActionListener
        gbc = new GridBagConstraints(1,2,1,1,0.1,0,GridBagConstraints.WEST,
        GridBagConstraints.NONE,new Insets(5,40,5,5),0,0);
        p2.add(new JButton("Create/Replace"), gbc);
        gbc = new GridBagConstraints(1,4,1,1,0.1,0,GridBagConstraints.WEST,
        GridBagConstraints.NONE,new Insets(5,40,5,5),0,0);
        p2.add(new JButton("Append/Update"), gbc);

     */


}
