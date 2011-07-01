/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esg.node.publisher.gui.subcomponents;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 15, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
import esg.node.publisher.gui.*;
import java.util.LinkedList;
import javax.swing.JButton;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;


public class TabbedPaneManager {

    LinkedList<Object> tabList;
    JPanel jPanel;
    JTabbedPane tpTop;
    int tabCounter;
    JSplitPane splitPaneTop;
    static TabbedPaneManager _instance = null;


    private TabbedPaneManager() {     
    }
    
    public void initialize(JSplitPane splitPane_Top, JTabbedPane tabbedPane_Top,int counter){
        tpTop = tabbedPane_Top;
        tabCounter = counter;       
        splitPaneTop = splitPane_Top;
        tabList = new LinkedList<Object>();
    }
    public static TabbedPaneManager getInstance(){
        if (_instance==null)
            _instance =  new TabbedPaneManager();

        return _instance;

    }

    public LinkedList getTabList(){
        return tabList;
    }

    public int getCounter(){
        return tabCounter;
    }

    public void incCounter(){
         tabCounter++;
    }

    public void setCounter(int i){
         tabCounter = i;
    }
    public JPanel getPanel(){
        return jPanel;
    }
    public void setPanel(JPanel panel){
        jPanel = panel;
    }
    public JSplitPane getSplitPane(){
        return splitPaneTop;
    }
    // todo this exposes tpTop outside the class, fix this later...

    public JTabbedPane getTabbedPane(){
        return tpTop;
    }

        public void deleteTabs(int index) {
    	if (tabList.isEmpty() == false) {
	    		tabList.remove(index);
    	}
    }

    /**
     * Removes all tabs from the Linked List
     */
    public void clearAllTabs() {
    	if (tabList.isEmpty() == false) {
    		tabList.clear();               
			System.out.println("removing...");
    	}
    }
}
