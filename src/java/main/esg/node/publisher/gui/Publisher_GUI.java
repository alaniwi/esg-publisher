/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esg.node.publisher.gui;

import esg.node.publisher.gui.subcomponents.*;
import esg.node.publisher.gui.subcomponents.DatasetProjectSpecifyPanel;
import esg.node.publisher.gui.subcomponents.DatasetWindow;
import esg.node.publisher.gui.subcomponents.ErrorPanel;
import esg.node.publisher.gui.subcomponents.OutputPanel;
import esg.node.publisher.gui.subcomponents.TabbedPaneManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import llnl.gnem.util.gui.SpringUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class Publisher_GUI {

    JSplitPane spTop;
    JTabbedPane tpTop, tabbedPaneTop, tabbedPaneBottom;
    JFileChooser fileChooser;
    FileExtensionFilter fileExtensionFilter;
    LinkedList<Object> tabList;
    JFrame frame;
    JSplitPane splitPaneTop, splitPaneBottom, splitPaneMain;
    JPanel mainPanel = new JPanel();
    JPanel topPanel = new JPanel();
    DatasetWindow datasetWindow = new DatasetWindow();

   // JButton leftComponent, rightComponent,topComponent, bottomComponent;
    private ErrorPanel errorPanel = new ErrorPanel();
    private OutputPanel outputPanel = new OutputPanel();
    public Publisher_GUI() {
    }

    public void buildPublisher() {
        // test
        //JButton leftComponent=new JButton("left");


        frame = new JFrame("ESG Data Node: Publisher's Graphical User Interface");
        tabbedPaneTop = new JTabbedPane();
        tabbedPaneBottom = new JTabbedPane();
        buildOutputTab();
        buildErrorTab();
   
      
        log.info("Starting ESG Publisher Graphical User Interface...");

       // GridLayout experimentLayout = new GridLayout(5,1);
       // JPanel leftPanel_main = new JPanel(new BorderLayout());
       // JPanel leftPanel = new JPanel(experimentLayout);

        JPanel leftPanel = new JPanel();
        //JPanel leftPanel_main = new JPanel(new BorderLayout());

        

        DatasetProjectSpecifyPanel dp1 = new DatasetProjectSpecifyPanel();
        DatasetExtractionPanel dp2 = new DatasetExtractionPanel();
        DatasetPublicationPanel dp3 = new DatasetPublicationPanel();
        DatasetExtractionPanel dp4 = new DatasetExtractionPanel();
        DatasetDeletionPanel dp5 = new DatasetDeletionPanel();
        CollapsablePanel panelLeft = new CollapsablePanel(DatasetProjectSpecifyPanel.getPanelName(), (JPanel)dp1);
        CollapsablePanel panelLeft2 = new CollapsablePanel(DatasetExtractionPanel.getPanelName(), (JPanel)dp2);
        CollapsablePanel panelLeft3 = new CollapsablePanel(DatasetPublicationPanel.getPanelName(), (JPanel)dp3);
        CollapsablePanel panelLeft4 = new CollapsablePanel(DatasetExtractionPanel.getPanelName(), (JPanel)dp4);
        CollapsablePanel panelLeft5 = new CollapsablePanel(DatasetDeletionPanel.getPanelName(), (JPanel)dp5);
        JPanel[] aps = new JPanel[5];
        aps[0] = panelLeft;
        aps[1] = panelLeft2;
        aps[2] = panelLeft3;
        aps[3] = panelLeft4;
        aps[4] = panelLeft5;
        JPanel leftPanel_main = getComponent( aps);
        //leftPanel_main.add(leftPanel, BorderLayout.NORTH);

        panelLeft.setVisible(true);
        panelLeft2.setVisible(true);
        // Create a left-right split pane
        splitPaneTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel_main, datasetWindow);
    
        // Create a top-bottom split pane
        splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop, tabbedPaneBottom);
        mainPanel.setLayout(new SpringLayout());
        mainPanel.add(splitPaneMain);
    
        SpringUtilities.makeCompactGrid(mainPanel,
                1, 1,
                10, 5,  //initX, initY
                10, 3); //xPad, yPad
        frame.add(mainPanel);
        splitPaneTop.setContinuousLayout(true);
        splitPaneMain.setContinuousLayout(true);
        splitPaneTop.setOneTouchExpandable(true);
        splitPaneMain.setOneTouchExpandable(true);
        buildFrame();

        TabbedPaneManager tbm = TabbedPaneManager.getInstance();
        tbm.setPanel(datasetWindow);
        tbm.initialize(splitPaneTop, tabbedPaneTop, 0);

    }
    public void buildFrame() {
        // Closes application when close button is clicked
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add MenuBar to Frame
        MenuBarCreator mb = new MenuBarCreator(frame);
        JMenuBar jmb = mb.createMenu();
        frame.setJMenuBar(jmb);

        // Add the split panels to the frame
       // frame.getContentPane().add(splitPaneBottom);

        // Display the frame
        frame.pack();
        frame.setSize(980, 650);
        frame.setVisible(true);
    }
    private static final Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        //UIManager.put("swing.boldMetal", Boolean.FALSE);

        Publisher_GUI ESG = new Publisher_GUI();
        ESG.buildPublisher();
        System.out.println("done");
    }


protected JPanel getComponent(JPanel aps[]) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1,3,0,3);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        for(int j = 0; j < aps.length; j++) {
            panel.add(aps[j], gbc);
            //panel.add(panels[j], gbc);
            //panels[j].setVisible(false);
        }
        JLabel padding = new JLabel();
        gbc.weighty = 1.0;
        panel.add(padding, gbc);
        return panel;
    }

    /**
     * Edits Bottom 'Output' tab and Adds it to panel.
     * Turns editable feature off
     */
    public void buildOutputTab(){

        tabbedPaneBottom.addTab("Output", outputPanel);
    }

    /**
     * Edits Bottom 'Error' tab and Adds it to panel.
     * Turns editable feature off
     */
    public void buildErrorTab(){

        tabbedPaneBottom.addTab("Error", errorPanel);

    }
}
