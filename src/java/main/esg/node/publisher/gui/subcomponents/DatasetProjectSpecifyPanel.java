/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esg.node.publisher.gui.subcomponents;

import esg.node.publisher.gui.CreateTabs;
import esg.node.publisher.gui.FileExtensionFilter;
import esg.node.publisher.gui.TabCreator;
import esg.node.publisher.gui.util.ExceptionDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
public class DatasetProjectSpecifyPanel extends JPanel implements MouseListener, ActionListener {

    private JComboBox projectComboBox, fileSearchComboBox;
    String[] projectList = {"ipcc3", "ipcc4", "ipcc5"};
    String[] filterFileList = {"Search for files *.nc ", "All Files"};
    JRadioButton workOnlineButton, workOfflineButton;
    ButtonGroup radioButtonGroup;
    static String workOnlineString;
    static String workOfflineString;
    JButton directory, file;
    JFileChooser fileChooser;
    FileExtensionFilter fileExtensionFilter;
    JPanel mainPanel = new JPanel(new SpringLayout());
    private static String name = "Specify Project Datasets";

    public DatasetProjectSpecifyPanel() {
        this.setLayout(new SpringLayout());
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        this.setBorder(border);
        buildPanel();
        this.setPreferredSize(new Dimension(200, 220));
        //       this.setSize(new Dimension(200,220));
        //       setVisible(true);
    }

    public static String getPanelName() {
        return name;
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void buildPanel() {
        //Panel 1 - Specify Project and dataset

        workOnlineString = "On-line";
        workOfflineString = "Off-line";

        // Panels names reflect their line number on the panel
        JPanel one = new JPanel(new SpringLayout());
        JPanel two = new JPanel(new SpringLayout());
        JPanel three = new JPanel(new SpringLayout());
        JPanel three_a = new JPanel(new FlowLayout());
        JPanel four = new JPanel(new SpringLayout());
        JPanel five = new JPanel(new SpringLayout());
        JPanel five_a = new JPanel(new FlowLayout());

        workOnlineButton = new JRadioButton(workOnlineString);
        workOfflineButton = new JRadioButton(workOfflineString);
        radioButtonGroup = new ButtonGroup();
        JLabel project = new JLabel("Project");

        // prepare one line at a time
        project.setForeground(new Color(0, 0, 128)); //change color
        one.add(project);
        projectComboBox = new JComboBox(projectList);
        projectComboBox.addActionListener(this); //ActionListener
        one.add(projectComboBox);

        SpringUtilities.makeCompactGrid(one,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

        two.add(new JLabel("Set additional mandatory"));
        JButton two_b = new JButton("Fields");
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

        three.add(new JLabel("Work"));
        workOnlineButton.setSelected(true);
        three_a.add(workOnlineButton);
        three_a.add(workOfflineButton);
        radioButtonGroup.add(workOnlineButton);
        radioButtonGroup.add(workOfflineButton);
        three.add(three_a);
        SpringUtilities.makeCompactGrid(three,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

        four.add(new JLabel("Filter File Search"));
        fileSearchComboBox = new JComboBox(filterFileList);
        fileSearchComboBox.addActionListener(this); //ActionListener
        four.add(fileSearchComboBox);
        fileSearchComboBox.setEditable(true);
        fileSearchComboBox.addActionListener(this); //ActionListener
        SpringUtilities.makeCompactGrid(four,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

        five.add(new JLabel("Generate list from"));

        fileChooser = new JFileChooser();
        fileExtensionFilter = new FileExtensionFilter();
        directory = new JButton("Directory");

        directory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);//DIRECTORIES_ONLY);
    		fileChooser.addChoosableFileFilter(fileExtensionFilter);
                TabbedPaneManager tbm = TabbedPaneManager.getInstance();
    		int returnVal = fileChooser.showOpenDialog(tbm.getSplitPane());
    		if (returnVal == JFileChooser.APPROVE_OPTION) {
                File fileSelected = fileChooser.getSelectedFile();
        		System.out.println("Selected button Directory" + fileSelected);
    		}

                } catch (Exception ex) {
                    ExceptionDialog.displayError(ex);
                }
            }
        });



        file = new JButton("File");
        // directory.addActionListener(this); //ActionListener
        five_a.add(directory);

        // file.addActionListener(this); //ActionListener
        file.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    TabbedPaneManager tbm = TabbedPaneManager.getInstance();

                    if (tbm.getCounter() == 0) {
                        tbm.clearAllTabs();
                        CreateTabs createTabs = new CreateTabs(tbm.getSplitPane(), tbm.getPanel(), tbm.getTabbedPane(), tbm.getCounter());
                        createTabs.createFirstTab();
                        tbm.getTabList().addFirst(createTabs);
                        tbm.setCounter(1);

                    } else {
                        TabCreator tabCreator = new TabCreator(tbm.getTabbedPane(), tbm.getCounter());
                        tabCreator.createRemainingTabs();
                        tbm.tabList.add(tabCreator);
                        System.out.println("\nlinkedList" + tbm.tabList);
                    }
                    tbm.incCounter();


                } catch (Exception ex) {
                    ExceptionDialog.displayError(ex);
                }
            }
        });
        five_a.add(file);
        five.add(five_a);
        SpringUtilities.makeCompactGrid(five,
                1, 2,
                10, 5, //initX, initY
                10, 3); //xPad, yPad

        add(one);
        add(two);
        add(three);
        add(four);
        add(five);
        SpringUtilities.makeCompactGrid(this,
                5, 1,
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
}
