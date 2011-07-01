/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */


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
public class DatasetPublicationPanel  extends JPanel implements MouseListener, ActionListener {


    JPanel mainPanel = new JPanel(new SpringLayout());
    private static String name = "Data Publication";

    public DatasetPublicationPanel() {
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


        two.add(new JLabel("Release Data"));
        JButton two_b = new JButton("Publish");
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




        add(one);
        add(two);

        SpringUtilities.makeCompactGrid(this,
                2, 1,
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

     */


}
