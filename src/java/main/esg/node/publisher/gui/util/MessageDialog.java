/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package esg.node.publisher.gui.util;

/**
 *
 * @Created By: ganzberger1
 * @Date: Oct 15, 2010
 * COPYRIGHT NOTICE
 * Copyright (C) 2010 Lawrence Livermore National Laboratory.
 */

import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * COPYRIGHT NOTICE
 * GnemUtils Version 1.0
 * Copyright (C) 2005 Lawrence Livermore National Laboratory.
 * User: dodge1
 * Date: Mar 17, 2006
 */
public class MessageDialog extends JFrame {
    protected String title = "WARNING MESSAGE";
    protected int Type = JOptionPane.WARNING_MESSAGE; // 2
    /**
     * Creates a new instance of ExceptionDialog
     */
    protected boolean OverrideTitle = true;

    public void SetTitle( String t )
    {
        this.title = t;      // must adjust the frame size if this gets too long..
        OverrideTitle = false;
    }

    public void SetMessageType( int msgType )
    {
        this.Type = msgType;
        if( OverrideTitle ){
            switch (Type) {
                case JOptionPane.ERROR_MESSAGE:
                    title = "ERROR MESSAGE";
                    break;
                case JOptionPane.PLAIN_MESSAGE:
                    title = "MESSAGE";
                    break;
                case JOptionPane.INFORMATION_MESSAGE:
                    title = "INFORMATIONAL MESSAGE";
                    break;
                case JOptionPane.WARNING_MESSAGE:
                    title = "WARNING MESSAGE";
                    break;
                default:
                    title = "WARNING MESSAGE";
            }
        }
    }

    public void DisplayMessage( String s )
    {
        String[] msgs = new String[1];
        msgs[0] = s;
        String[] msg = parse( msgs );
        JOptionPane.setRootFrame( this );
        JOptionPane.getRootFrame().toFront();

        JOptionPane.showMessageDialog( this, msg, this.title, this.Type );

    }

    public void DisplayMessage( String[] msgs )
    {
        String[] msg = parse( msgs );
        this.toFront();
        JOptionPane.getRootFrame().toFront();
        JOptionPane.showMessageDialog( this, msg, this.title, this.Type );

    }


    public void displayException( Exception e )
    {
        this.toFront();
        if( !OverrideTitle ) SetTitle( "Exception" ); // set this unless user explicitly set it earlier.
        String[] msgs = new String[1];
        msgs[0] = e.toString();
        if( msgs != null ){
            String msg[] = parse( msgs );
            JOptionPane.showMessageDialog( this, msg, this.title, this.Type );
        }
        e.printStackTrace();
    }


    public static String[] parse( String[] input )
    {
        int size = input.length;
        String temp;
        String token;
        Vector<String> result = new Vector<String>();
        StringTokenizer st;
        for ( int i = 0; i < size; i++ ){
            st = new StringTokenizer( input[i] );
            temp = "";
            while( st.hasMoreTokens() ){
                token = st.nextToken();
                if( ( temp.length() ) + ( token.length() ) > 80 ){
                    result.addElement( temp );
                    temp = token + " "; // keep processing this string...
                }
                else{
                    temp = temp.concat( token );
                    temp = temp.concat( " " );
                }
            }
            if( temp.length() > 0 ){
                result.addElement( temp );
            }
        }
        return result.toArray( new String[0] );
    }

}
