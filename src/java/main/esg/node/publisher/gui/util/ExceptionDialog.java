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
/**
 * COPYRIGHT NOTICE
 * GnemUtils Version 1.0
 * Copyright (C) 2005 Lawrence Livermore National Laboratory.
 * User: ganzberger1
 * Date: Mar 17, 2006
 */

import javax.swing.*;

/**
 * @author ganz
 *         This class has several methods to display error/informational messages to a dialog box.
 *         You can send a database exception object, a string or a string array
 *         You may also optionally set the title.
 */
public class ExceptionDialog extends MessageDialog {
    // ERROR_MESSAGE = 0
    // PLAIN_MESSAGE = -1
    // INFORMATIONAL_MESSAGE = 1

    public ExceptionDialog()
    {
        //  this.setSize(600,200);
    }

    public static void displayError( Exception e )
    {
        ExceptionDialog smd = new ExceptionDialog();
        smd.SetMessageType( JOptionPane.WARNING_MESSAGE );
        smd.displayException( e );
    }
}

