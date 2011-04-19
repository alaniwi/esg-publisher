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
      
  
      #----------------------------------------------------------------------------------------
      # Begin the creation of the button controls
      #----------------------------------------------------------------------------------------
      glFont=tkFont.Font(self.parent.parent, family = pub_controls.button_group_font_type, size=pub_controls.button_group_font_size, weight=font_weight)

      # Create and pack the LabeledWidgets to "Select All" datasets
      bnFont=tkFont.Font(self.parent.parent, family = pub_controls.label_button_font_type,  size=pub_controls.label_button_font_size, weight=font_weight)

      
      self.group_list_generation = Pmw.Group(self.parent.control_frame6a,
                        tag_text = 'Selection Choices:',
                        tag_font = glFont,
                        tagindent = 25)
      
  
      QueryFiles1 = Checkbutton(self.group_list_generation.interior(), text = "Files", variable = gateway_query_widgets.CheckVar1, \
                 onvalue = 1, offvalue = 0, height=2, width = 5) 
      QueryList1 = Checkbutton(self.group_list_generation.interior(), text  = "List", variable = gateway_query_widgets.CheckVar2, \
                 onvalue = 1, offvalue = 0, height=2, width = 4)
      QueryURLs1 = Checkbutton(self.group_list_generation.interior(), text  = "Urls", variable = gateway_query_widgets.CheckVar4, \
                 onvalue = 1, offvalue = 0, height=2, width = 4)
      QueryMetadata1 = Checkbutton(self.group_list_generation.interior(), text = "Metadata", variable = gateway_query_widgets.CheckVar3, \
                 onvalue = 1, offvalue = 0, height=2, width = 8) 
           
      QueryFiles1.grid(row=0, column=0, sticky=W)
      QueryList1.grid(row=1, column=0, sticky=W)
      
      QueryURLs1.grid(row=2, column=0, sticky=W)
      QueryMetadata1.grid(row=3, column=0, sticky=W)
            
      #self.parent.control_frame6a.pack(side="top", fill = 'x', pady=5)
      
      bnFont=tkFont.Font(self.parent.parent, family = pub_controls.label_button_font_type,  size=pub_controls.label_button_font_size, weight=font_weight)

      lw_start3 = Pmw.LabeledWidget(self.group_list_generation.interior(),
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
      
      
      self.group_list_generation.pack(side='top', fill='x', pady=3)
      
#########################################################################################
 
      # Line separator
      w = Canvas(self.parent.control_frame6a, width=600, height=2)
      w.pack()

      w.create_line(0, 1, 600, 1, fill="black")

      self.group_output_generation = Pmw.Group(self.parent.control_frame6a,
                        tag_text = 'Output Format Options:',
                        tag_font = glFont,
                        tagindent = 25)
      
      self.on_off = Pmw.RadioSelect(self.group_output_generation.interior(),
        buttontype = 'radiobutton',
        orient = 'horizontal',
        labelpos = 'w',
        command = pub_controls.Command( self.evt_verbose_on_or_off, ),
        label_text = 'Verbose: ',
                label_font = bnFont,
        hull_borderwidth = 2,
        hull_relief = 'ridge',
      )
      self.on_off.pack(side = 'top', expand = 1, padx = 10, pady = 10)

      # Add some buttons to the radiobutton RadioSelect.
      for text in ('On', 'Off'):
          self.on_off.add(text, font = bnFont)
      self.on_off.setvalue('On')   
      self.group_output_generation.pack(side='top', fill='x', pady=3)
      
      QueryFiles1.select()
      QueryList1.select()
      QueryURLs1.select()
      QueryMetadata1.select()
      


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
    
       #-----------------------------------------------------------------
    # event functions to toggle working from online or offline mode
    #-----------------------------------------------------------------
    def evt_verbose_on_or_off( self, tag ):
        # Reset the button colors
        #self.cw_dir.configure( background=self.save_dir_btn_color, foreground='black' )
        #self.cw_file.configure( background=self.save_file_btn_color, foreground='black' )
        #self.cw_reg.configure( background=self.save_reg_btn_color, foreground='black' )

        if tag == "Off": 
           self.parent.parent.verbose_off = True

        if tag == "On":
           self.parent.parent.verbose_off = False

         #  self.lw_dir.pack(side='top', before=self.lw_file, expand = 1, fill = 'both', padx=10, pady=10)


       
    
#---------------------------------------------------------------------
# End of File
#---------------------------------------------------------------------
