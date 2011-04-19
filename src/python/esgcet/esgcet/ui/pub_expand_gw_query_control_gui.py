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
from esgcet.query.gateway import getRemoteMetadataService, getGatewayDatasetMetadata, getGatewayDatasetChildren, getGatewayExperiments, getGatewayDatasetFields, getGatewayDatasetFiles, getGatewayDatasetAccessPoints


from pub_controls import font_weight
from esgcet.messaging import debug, info, warning, error, critical, exception

class gateway_query_widgets:
    """
    Generate the Gateway Query control widgets seen on the left when "Query Gateway" is selected.
    """
    
      
    def __init__(self, parent):
        

     
      self.parent = parent
      self.Session = self.parent.parent.Session

      global CheckVar_Files
      gateway_query_widgets.CheckVar_Files = IntVar() # 
      global CheckVar_List
      gateway_query_widgets.CheckVar_List = IntVar() # 
      global CheckVar_MetaData
      gateway_query_widgets.CheckVar_MetaData = IntVar() # 
      global CheckVar_URLs
      gateway_query_widgets.CheckVar_URLs = IntVar() # 
      
      #global Check_Verbose
      gateway_query_widgets.Check_Verbose = StringVar() # 
  
      #----------------------------------------------------------------------------------------
      # Begin the creation of the button controls
      #----------------------------------------------------------------------------------------
      glFont=tkFont.Font(self.parent.parent, family = pub_controls.button_group_font_type, size=pub_controls.button_group_font_size, weight=font_weight)

      # Create and pack the LabeledWidgets to "Select All" datasets
      bnFont=tkFont.Font(self.parent.parent, family = pub_controls.label_button_font_type,  size=pub_controls.label_button_font_size, weight=font_weight)

      
      self.group_list_generation = Pmw.Group(self.parent.control_frame6a,
                        tag_text = 'Query Choices using Selected Datasets:',
                        tag_font = glFont,
                        tagindent = 25)
      
  
      self.QueryFiles1 = Checkbutton(self.group_list_generation.interior(), text = "Files", variable = gateway_query_widgets.CheckVar_Files, \
                 onvalue = 1, offvalue = 0, height=2, width = 5) 
      self.QueryList1 = Checkbutton(self.group_list_generation.interior(), text  = "List", variable = gateway_query_widgets.CheckVar_List, \
                 onvalue = 1, offvalue = 0, height=2, width = 4)
      self.QueryURLs1 = Checkbutton(self.group_list_generation.interior(), text  = "Urls", variable = gateway_query_widgets.CheckVar_URLs, \
                 onvalue = 1, offvalue = 0, height=2, width = 4)
      self.QueryMetadata1 = Checkbutton(self.group_list_generation.interior(), text = "Metadata", variable = gateway_query_widgets.CheckVar_MetaData, \
                 onvalue = 1, offvalue = 0, height=2, width = 8) 
           
      # padding, if necessary
      #self.label = Tkinter.Label(self.group_list_generation.interior(),text=' ')
      #self.label.grid(row=0, column=0, sticky='W')
      
      self.QueryFiles1.grid(row=1, column=0, sticky='W', ipadx=0, ipady=0, padx=0, pady=0)
      self.QueryList1.grid(row=2, column=0, sticky='W', ipadx=0, ipady=0, padx=0, pady=0)      
      self.QueryURLs1.grid(row=3, column=0, sticky='W', ipadx=0, ipady=0, padx=0, pady=0)
      self.QueryMetadata1.grid(row=4, column=0, sticky='W', ipadx=0, ipady=0, padx=0, pady=0)
       
      self.parent.parent.balloon.bind(self.QueryFiles1, "List the data files in the selected parent datasets")
      self.parent.parent.balloon.bind(self.QueryList1, "List the children in the selected parent datasets (verbose lists metadata for each child dataset)")
      self.parent.parent.balloon.bind(self.QueryURLs1, "List the file access point URLs of the selected parent datasets")
      self.parent.parent.balloon.bind(self.QueryMetadata1, "List the metadata associated with the selected datasets")
      
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
      lw_start3.grid(row=6, sticky=W)
      
      self.parent.parent.balloon.bind(cw_start, "Issue a Gateway Query from the selected choices above and using the selected datasets.")
      
      self.group_list_generation.pack(side='top', fill='x', pady=3)
 
#########################################################################################
      self.group_experiments_generation = Pmw.Group(self.parent.control_frame6a,
                        tag_text = 'Other Options:',
                        tag_font = glFont,
                        tagindent = 25)


      # Line separator
      """
      w = Canvas(self.parent.control_frame6a, width=600, height=2)
      w.pack()

      w.create_line(0, 1, 600, 1, fill="black")
      """
      
      lw_start4 = Pmw.LabeledWidget(self.group_experiments_generation.interior(),
                    labelpos = 'w',
                    label_font = bnFont,
                    label_text = 'Experiments List: ')
      lw_start4.component('hull').configure(relief='sunken', borderwidth=2)
      lw_start4.pack(side='bottom', expand = 1, fill = 'both', padx=10, pady=10)
      cw_start4 = Tkinter.Button(lw_start4.interior(),
                    text='Generate',
                    font = bnFont,
                    background = "lightblue",
                    command = pub_controls.Command( self.evt_list_experiments ))
      cw_start4.pack(padx=10, pady=10, expand='yes', fill='both')
      lw_start4.grid(row=1, sticky=W)
      self.parent.parent.balloon.bind(cw_start4, "Build a List.")
      
      self.group_experiments_generation.pack(side='top', fill='x', pady=3)
      
      
     
#########################################################################################
 
      # Line separator
      """
      w = Canvas(self.parent.control_frame6a, width=600, height=2)
      w.pack()

      w.create_line(0, 1, 600, 1, fill="black")
      """
      
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
      gateway_query_widgets.Check_Verbose = True 
      self.group_output_generation.pack(side='left', fill='x', pady=3)
      
      self.QueryFiles1.select()
      self.QueryList1.select()
      self.QueryURLs1.select()
      self.QueryMetadata1.select()
      


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
       datasetName = None
       filesParent = None
       init_file = None
       listDatasetChildren = False
       listExperiments = False
       listFiles = False
       listMetadata = False
       listUrls = False
       parentDataset = None
#       serviceUrl = None
       urlParent = None
       verbose = gateway_query_widgets.get_Verbose()
       
       
       if (gateway_query_widgets.get_CheckVar_Files()==True or gateway_query_widgets.get_CheckVar_List()==True or gateway_query_widgets.get_CheckVar_MetaData()==True or gateway_query_widgets.get_CheckVar_URLs()==True ):
            datasetNames = self.parent.parent.menu.Dataset.evt_query_gw_for_dataset( self.parent.parent )
            for name,version in datasetNames:
                print name
       if True: return;
       """       
       for flag, arg in args:
           if flag=='--files':
               listFiles = True
               filesParent = arg             
#           elif flag=='--list':
#               listDatasetChildren = True
#               parentDataset = arg
#           elif flag in ['--experiments', '--list-experiments']:
#               listExperiments = True
           elif flag=='--metadata':
               listMetadata = True
               datasetName = arg
 #          elif flag=='--service-url':
 #              serviceUrl = arg
           elif flag=='--urls':
               listUrls = True
               urlParent = arg
        """  

    # Load the configuration and set up a database connection
    #config = loadConfig(init_file)
    #initLogging('DEFAULT')

       
       if get_CheckVar_List()==True:
           for parentDataset,version in datasetNames:                
               result = getGatewayDatasetChildren(parentDataset, serviceUrl=serviceUrl)
               if not verbose:
                   for childid in result:
                       print childid
               else:
                   fullresult = [getGatewayDatasetMetadata(item, serviceUrl=serviceUrl) for item in result]
                   printResult(getGatewayDatasetFields(), fullresult)

       if get_CheckVar_MetaData()==True:
           for datasetName,version in datasetNames: 
                header = getGatewayDatasetFields()
                result = getGatewayDatasetMetadata(datasetName, serviceUrl=serviceUrl)
                printResult(header, [result])

       if get_CheckVar_Files()==True:
           for filesParent,version in datasetNames: 
                header, result = getGatewayDatasetFiles(filesParent, serviceUrl=serviceUrl)
                printResult(header, result)
        
       if get_CheckVar_URLs()==True:
           for urlParent,version in datasetNames: 
                header, result = getGatewayDatasetAccessPoints(urlParent, serviceUrl=serviceUrl)
                printResult(header, result)
        
        
    def evt_list_experiments( self):    
        header, result = getGatewayExperiments(serviceUrl=serviceUrl)
        printResult(header, result)


    @staticmethod
    def get_CheckVar_Files():
        return gateway_query_widgets.CheckVar_Files.get() 
    
    @staticmethod
    def get_CheckVar_List():
        return gateway_query_widgets.CheckVar_List.get() 
    
    @staticmethod
    def get_CheckVar_MetaData():
        return gateway_query_widgets.CheckVar_MetaData.get() 
    
    @staticmethod
    def get_CheckVar_URLs():
        return gateway_query_widgets.CheckVar_URLs.get() 
    
    @staticmethod
    def get_Verbose():       
        return (gateway_query_widgets.Check_Verbose)                  
       #-----------------------------------------------------------------
    # event functions to toggle working from online or offline mode
    #-----------------------------------------------------------------
    def evt_verbose_on_or_off( self, tag ):
 

        if tag == "Off": 
           self.parent.parent.verbose_off = True

        if tag == "On":
           self.parent.parent.verbose_off = False

        gateway_query_widgets.Check_Verbose = not self.parent.parent.verbose_off

       
    
#---------------------------------------------------------------------
# End of File
#---------------------------------------------------------------------
