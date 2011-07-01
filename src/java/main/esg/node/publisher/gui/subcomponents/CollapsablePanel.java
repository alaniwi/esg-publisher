/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.subcomponents;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 14, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */
public class CollapsablePanel extends JPanel {

     private boolean selected;
     JPanel contentPanel_;
     HeaderPanel headerPanel_;

     Rectangle target;
        final int
        OFFSET = 30,
        PAD    =  5;

     public CollapsablePanel(){}

     public class HeaderPanel extends JPanel implements MouseListener {
         String text_;
         Font font;

         BufferedImage open, closed;
         final int OFFSET = 30, PAD = 5;



         public HeaderPanel(String text) {
             addMouseListener(this);
             text_ = text;
             font = new Font("sans-serif", Font.PLAIN, 12);
             // setRequestFocusEnabled(true);
             setPreferredSize(new Dimension(200, 20));
             int w = getWidth();
             int h = getHeight();


       // addMouseListener(ml);
       // font = new Font("sans-serif", Font.PLAIN, 12);
        selected = false;
        setBackground(new Color(185, 211, 238));//200,200,200
        setPreferredSize(new Dimension(330,20));
        setBorder(BorderFactory.createRaisedBevelBorder());
        setPreferredSize(new Dimension(330,20));
        createImages();
        setRequestFocusEnabled(true);


             /*try {
                 open = ImageIO.read(new File("images/arrow_down_mini.png"));
                 closed = ImageIO.read(new File("images/arrow_right_mini.png"));
             } catch (IOException e) {
                 e.printStackTrace();
             }*/

         }

//         protected void paintComponent(Graphics g) {
//             super.paintComponent(g);
//             Graphics2D g2 = (Graphics2D) g;
//             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                     RenderingHints.VALUE_ANTIALIAS_ON);
//             int h = getHeight();
//             /*if (selected)
//                 g2.drawImage(open, PAD, 0, h, h, this);
//             else
//                 g2.drawImage(closed, PAD, 0, h, h, this);
//                         */ // Uncomment once you have your own images
//             g2.setFont(font);
//             FontRenderContext frc = g2.getFontRenderContext();
//             LineMetrics lm = font.getLineMetrics(text_, frc);
//             float height = lm.getAscent() + lm.getDescent();
//             float x = OFFSET;
//             float y = (h + height) / 2 - lm.getDescent();
//             g2.drawString(text_, x, y);
//         }
 /**
     * Overrides the paintComponent Class of JComponent to edit elements
     * KEY_ANTIALIASING blends the existing colors of the pixels along
     * the boundary of a shape. Achieves a smooth image.
     * LINEMETRICS allows access to the metrics needed to layout characters
     * along a line and to layout of a set of lines
     */
    @Override
    protected void paintComponent(Graphics g) {
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
        LineMetrics lm = font.getLineMetrics(text_, frc);
        float height = lm.getAscent() + lm.getDescent();
        float x = OFFSET;
        float y = (h + height)/2 - lm.getDescent();
        g2.drawString(text_, x, y);
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
         public void mouseClicked(MouseEvent e) {
             toggleSelection();
         }

         public void mouseEntered(MouseEvent e) {
         }

         public void mouseExited(MouseEvent e) {
         }

         public void mousePressed(MouseEvent e) {
         }

         public void mouseReleased(MouseEvent e) {
         }

     }

     public CollapsablePanel(String text, JPanel panel) {
         super(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(1, 3, 0, 3);
         gbc.weightx = 1.0;
         gbc.fill = gbc.HORIZONTAL;
         gbc.gridwidth = gbc.REMAINDER;

         selected = false;
         headerPanel_ = new HeaderPanel(text);

         setBackground(new Color(200, 200, 220));
         contentPanel_ = panel;

         add(headerPanel_, gbc);
         add(contentPanel_, gbc);
         contentPanel_.setVisible(false);

         JLabel padding = new JLabel();
         gbc.weighty = 1.0;
         add(padding, gbc);

     }

     public void toggleSelection() {
         selected = !selected;

         if (contentPanel_.isShowing())
             contentPanel_.setVisible(false);
         else
             contentPanel_.setVisible(true);

         validate();

         headerPanel_.repaint();
     }
}
