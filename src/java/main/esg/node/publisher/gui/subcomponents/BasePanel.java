/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.*;
/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class BasePanel extends JPanel  {

    	private static final long serialVersionUID = 1L;
	String text;
    Font font;
    private boolean selected;
    BufferedImage open, closed;
    Rectangle target;
    final int
        OFFSET = 30,
        PAD    =  5;

    
    /**
     * Constructor
     * @param text  the panel label
     * @param ml  the mouse listener
     */
    public BasePanel(String text) {
        this.text = text;
        //addMouseListener(ml);
        font = new Font("sans-serif", Font.PLAIN, 12);
        selected = false;
        setBackground(new Color(185, 211, 238));//200,200,200
        setPreferredSize(new Dimension(330,20));
        setBorder(BorderFactory.createRaisedBevelBorder());
        setPreferredSize(new Dimension(330,20));
        createImages();
        setRequestFocusEnabled(true);
        setLayout(new SpringLayout());
    }

    /**
     * Toggle action panel selection
     */
    public void toggleSelection() {
        selected = !selected;
        repaint();
    }

    /**
     * Overrides the paintComponent Class of JComponent to edit elements
     * KEY_ANTIALIASING blends the existing colors of the pixels along
     * the boundary of a shape. Achieves a smooth image.
     * LINEMETRICS allows access to the metrics needed to layout characters
     * along a line and to layout of a set of lines
     */
    @Override
    protected void paintComponent(Graphics g) { // this failed on line 88 after I added a panel to options
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth();
        int h = getHeight();
        if (selected) {
            g2.drawImage(open, PAD, 0, this);//PAD moves the triangle along the x-axis
        }
        else {
            g2.drawImage(closed, PAD, 0, this);//PAD moves the triangle along the x-axis
        }
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics(text, frc);
        float height = lm.getAscent() + lm.getDescent();
        float x = OFFSET;
        float y = (h + height)/2 - lm.getDescent();
        g2.drawString(text, x, y);
    }

    /**
     * Creates the triangles inside the action panels
     */
    private void createImages() {
        int w = 20;
        int h = getPreferredSize().height;
        target = new Rectangle(2, 0, 20, 18);

        //triangle facing down (open)
        open = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = open.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0,0,w,h);
        int[] x = { 2, w/2, 18 };
        int[] y = { 4, 15,   4 };
        Polygon p = new Polygon(x, y, 3); //triangle
        g2.setPaint(new Color(250,250,250)); //open arrow
        g2.fill(p);
        g2.setPaint(new Color(84,84,84)); //border
        g2.draw(p);
        g2.dispose();

        //triangle facing right (closed)
        closed = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g2 = closed.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0,0,w,h);
        x = new int[] { 3, 13,   3 };
        y = new int[] { 4, h/2, 16 };
        p = new Polygon(x, y, 3);
        g2.setPaint(new Color(34,24,130)); //closed arrow848484
        g2.fill(p);
        g2.setPaint(new Color(34,24,130)); //border
        g2.draw(p);
        g2.dispose();
    }

        /**
     * Expands the panel with a press of the mouse
     * Uses togglePanelVisibility(ActionPanel ap) method
     */
    public void mousePressed(MouseEvent e) {
        BasePanel ap = (BasePanel)e.getSource();
        if(ap.contains(e.getPoint())) { //ap.target.contains(e.getPoint())
            ap.toggleSelection();
            togglePanelVisibility();
        }
    }

    /**
     * Makes the panels visible/not visible.
     * Uses getPanelIndex(ActionPanel ap)
     * @param ap  the action panel
     */
//    public void togglePanelVisibility() {
//
//        if(this.isShowing())
//            this.setVisible(false);
//        else
//            this.setVisible(true);
//        this.getParent().validate();
//    }
        public void togglePanelVisibility() {

        if(this.isShowing())
            this.getParent().setVisible(false);
        else
            this.getParent().setVisible(true);
        this.getParent().validate();
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
