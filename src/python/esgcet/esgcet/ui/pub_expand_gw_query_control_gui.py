#!/usr/bin/env python
#
# The Publisher Button Gateway Query Controls -  pub_expand_gw_query_control_gui module
#
###############################################################################
#                                                                             #
# Module:       pub_expand_gw_query_control_gui module                        #
#                                                                             #
# Copyright:    "See file Legal.htm for copyright information."               #
#                                                                             #
# Authors:      PCMDI Software Team                                           #
#               Lawrence Livermore National Laboratory:                       #
#                                                                             #
# Description:  Publisher button Gateway Query control expansion that         #
#               is found on the left side of GUI. When a button is selected,  #
#               it expand to show additional Gateway Query controls.          #
#                                                                             #
###############################################################################
#
import Tkinter, Pmw, tkFileDialog, tkFont
import os, string
import pub_editorviewer
import pub_controls
import thread
import logging
import traceback
from Tkinter import *



from pub_controls import font_weight
from esgcet.messaging import debug, info, warning, error, critical, exception

class gateway_query_widgets:
    """
    Generate the Gateway Query control widgets seen on the left when "Query Gateway" is selected.
    """
    
      
    def __init__(self, parent):
        

     
      self.parent = parent
      self.Session = self.parent.parent.Session

      global CheckVar1
      gateway_query_widgets.CheckVar1 = IntVar() # 
      global CheckVar2
      gateway_query_widgets.CheckVar2 = IntVar() # 
      global CheckVar3
      gateway_query_widgets.CheckVar3 = IntVar() # 
      global CheckVar4
      gateway_query_widgets.CheckVar4 = IntVar() # 
      
      #global CheckVar2 = IntVar()
      #global CheckVar3 = IntVar()
      
      #----------------------------------------------------------------------------------------
      # Begin the creation of the button controls
      #----------------------------------------------------------------------------------------
      glFont=tkFont.Font(self.parent.parent, family = pub_controls.button_group_font_type, size=pub_controls.button_group_font_size, weight=font_weight)

      # Create and pack the LabeledWidgets to "Select All" datasets
      bnFont=tkFont.Font(self.parent.parent, family = pub_controls.label_button_font_type,  size=pub_controls.label_button_font_size, weight=font_weight)

      # Create and pack the LabeledWidgets to "Select All" datasets
#      lw_start1 = Pmw.LabeledWidget(self.parent.control_frame5,
#                    labelpos = 'w',
#                    label_font = bnFont,
#                    label_text = 'Dataset: ')
#      lw_start1.component('hull').configure(relief='sunken', borderwidth=2)
#      lw_start1.pack(side='top', expand = 1, fill = 'both', padx=10, pady=10)
#      cw_start = Tkinter.Button(lw_start1.interior(),
#                    text='Select All',
#                    font = bnFont,
#                    background = "aliceblue",
#                    command = pub_controls.Command( self.evt_dataset_select_all ))
#      cw_start.pack(padx=10, pady=10, expand='yes', fill='both')

      # Create and pack the LabeledWidgets to "Unselect All" datasets
#      bnFont=tkFont.Font(self.parent.parent, family = pub_controls.label_button_font_type,  size=pub_controls.label_button_font_size, weight=font_weight)

#      lw_start2 = Pmw.LabeledWidget(self.parent.control_frame5,
#                    labelpos = 'w',
#                    label_font = bnFont,
#                    label_text = 'Dataset: ')
#      lw_start2.component('hull').configure(relief='sunken', borderwidth=2)
#      lw_start2.pack(side='top', expand = 1, fill = 'both', padx=10, pady=10)
#      cw_start = Tkinter.Button(lw_start2.interior(),
#                    text='Unselect All',
#                    font = bnFont,
#                    background = "aliceblue",
#                    command = pub_controls.Command( self.evt_dataset_unselect_all ))
#      cw_start.pack(padx=10, pady=10, expand='yes', fill='both')

      # Create and pack the LabeledWidgets to "Remove" selected datasets
      
      
      lw_start1 = Pmw.LabeledWidget(self.parent.control_frame6,
                    labelpos = 'w',
                    label_font = bnFont,
                    label_text = 'Select Query Action for selected Datasets: ')
      lw_start1.component('hull').configure(relief='flat', borderwidth=2)
#      lw_start1.pack(side='top', expand = 1, fill = 'both', padx=10, pady=10)      
      lw_start1.grid(row=0, sticky=N)
      
      
      QueryFiles = Checkbutton(self.parent.control_frame6, text = "Files", variable = gateway_query_widgets.CheckVar1, \
                 onvalue = 1, offvalue = 0, height=2, width = 5) 
      QueryList = Checkbutton(self.parent.control_frame6, text  = "List", variable = gateway_query_widgets.CheckVar2, \
                 onvalue = 1, offvalue = 0, height=2, width = 4)
      QueryURLs = Checkbutton(self.parent.control_frame6, text  = "Urls", variable = gateway_query_widgets.CheckVar4, \
                 onvalue = 1, offvalue = 0, height=2, width = 4)
      QueryMetadata = Checkbutton(self.parent.control_frame6, text = "Metadata", variable = gateway_query_widgets.CheckVar3, \
                 onvalue = 1, offvalue = 0, height=2, width = 8) 
      
      QueryFiles.grid(row=1, column=0, sticky=W)
      QueryList.grid(row=2, column=0, sticky=W)
      
      QueryURLs.grid(row=3, column=0, sticky=W)
      QueryMetadata.grid(row=4, column=0, sticky=W)
      
      bnFont=tkFont.Font(self.parent.parent, family = pub_controls.label_button_font_type,  size=pub_controls.label_button_font_size, weight=font_weight)

      lw_start3 = Pmw.LabeledWidget(self.parent.control_frame6,
                    labelpos = 'w',
                    label_font = bnFont,
                    label_text = 'Query Gateway: ')
      lw_start3.component('hull').configure(relief='sunken', borderwidth=2)
      lw_start3.pack(side='bottom', expand = 1, fill = 'both', padx=10, pady=10)
      cw_start = Tkinter.Button(lw_start3.interior(),
                    text='Query',
                    font = bnFont,
                    background = "lightblue",
                    command = pub_controls.Command( self.evt_query_selected_dataset ))
      cw_start.pack(padx=10, pady=10, expand='yes', fill='both')
      lw_start3.grid(row=5, sticky=W)
      
      QueryFiles.select()
      QueryList.select()
      QueryURLs.select()
      QueryMetadata.select()
#      Pmw.alignlabels( (lw_start1, C1,C2,C3, lw_start3) )
#      Pmw.alignlabels( (lw_start1, lw_start2, lw_start3) )
#      Pmw.alignlabels( (lw_start3) )

      #----------------------------------------------------------------------------------------
      # End the creation of the button controls
      #----------------------------------------------------------------------------------------

    #----------------------------------------------------------------------------------------
    # Button events to Select, Unselect, and Remove datasets
    #----------------------------------------------------------------------------------------
    def evt_dataset_select_all( self ):
       self.parent.parent.menu.Dataset.evt_select_all_dataset( self.parent.parent )

    def evt_dataset_unselect_all( self ):
       self.parent.parent.menu.Dataset.evt_unselect_all_dataset( self.parent.parent )

    def evt_query_selected_dataset( self ):
       self.parent.parent.menu.Dataset.evt_query_gw_for_dataset( self.parent.parent )

    @staticmethod
    def get_CheckBox1():
        return gateway_query_widgets.CheckVar1.get() 
    
    @staticmethod
    def get_CheckBox2():
        return gateway_query_widgets.CheckVar2.get() 
    
    @staticmethod
    def get_CheckBox3():
        return gateway_query_widgets.CheckVar3.get() 
    
    @staticmethod
    def get_CheckBox4():
        return gateway_query_widgets.CheckVar4.get() 
    
#---------------------------------------------------------------------
# End of File
#---------------------------------------------------------------------
