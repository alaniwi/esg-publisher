/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class MyMouseAdapter extends MouseAdapter implements ActionListener{
    BasePanel panel;
    public MyMouseAdapter(BasePanel panel){
    this.panel = panel;

    }
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mousePressed(MouseEvent e) {
        JPanel ap = (JPanel)e.getSource();
        if(panel.contains(e.getPoint())) { //ap.target.contains(e.getPoint())
            panel.toggleSelection();
            panel.togglePanelVisibility();
        }
    }
}
