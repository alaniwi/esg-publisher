/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esgf.publisher.ui;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *These notes describe how to build a dataset_id | version file for a set of files.


First run esgscan_directory and create your map file for the subset of datasets you want.

Next, create a file 
esgquery_gateway --list pcmdi.BCC --service  http://pcmdi3.llnl.gov/esgcet/remote/hessian/guest/remoteMetadataService --verbose > BCC_INFO.out


This will produce output for ALL BCC datasets along with their version.

I need to write a code that reads in the MAP file, and for one dataset at a time read the BCC_INFO.out to locate the dataset entry and parse the 
line to get the version.

Finally, the dataset_id along with the version should be written to a file (see esgcopy_files which uses this).

"dataset_id | version"
* 
* * Tasks: Read in the map file (in_map) to get one datasetid to process at a time,
 * Here is a sample line:
 * cmip5.output1.BCC.bcc-csm1-1.decadal1980.fx.ocean.fx.r0i0p0 | 
 * /cmip5/data/cmip5/output1/BCC/bcc-csm1-1/decadal1980/fx/ocean/fx/r0i0p0/volcello/1/volcello_fx_bcc-csm1-1_decadal1980_r0i0p0.nc | 
 * 14054836 | mod_time=1335902750.000000 | checksum=68821a70245fb61e7233aae6e266cf1f | checksum_type=MD5
 *
 * Just parse the first string delineated by |.
 * 
 * With the datasetid, open the datset file (in_dataset), look through and look for
 * the string of datasetid. If not found, print an error and continue.  If found,
 * parse that line to get the version;
 * 
 * | cmip5.output1.BCC.bcc-csm1-1.1pctCO2.day.atmos.day.r1i1p1            | published | 
 *   project=CMIP5, model=BCC-CSM1.1, Beijing Climate Center, China Meteorological Administration, 
 *   experiment=1 percent per year CO2, time_frequency=day, modeling realm=atmos, ensemble=r1i1p1, version=1  
 * | http://bcccsm.cma.gov.cn/thredds/esgcet/1/cmip5.output1.BCC.bcc-csm1-1.1pctCO2.day.atmos.day.r1i1p1.v1.xml 
 * 
 * Parse and look for "version="
 * 
 * Write out the datase_id "|" version
 * 
 * to the output file
 * 
 * Continue this until there are no more lines to process in the map file.

 * @author ganzberger1
 */
public class BuildDatasetVersions {
    
    
    BufferedReader in_dataset;
    BufferedReader in_map; 
    PrintWriter out;

    public void createNewMapFile(String inDataset, String inMap, String outMap) throws IOException {

        Map<String,String> results = new HashMap<String,String>();
        File om = new File(outMap);
        if (om.exists())
            om.delete();

        out= new PrintWriter(new BufferedWriter(new FileWriter(outMap)));

        String line;
        try {
            in_map = new BufferedReader(new FileReader(inMap));
            while ((line = in_map.readLine()) != null && !line.trim().isEmpty()) {
                process(line, inDataset, results);
            }
            // now read in the datasets
           in_dataset = new BufferedReader(new FileReader(inDataset));
            while ((line = in_dataset.readLine()) != null && !line.trim().isEmpty()) {
                getVersion(line, results);
            }           
            
            
            writeResults(results);
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            if (in_map != null) {
                in_map.close();
            }
        }

    }

    private void writeResults(Map<String,String> results){
    
        for (String datasetid : results.keySet() ) {
            // Get the String value that goes with the datasetid (key)       
            String version = results.get( datasetid );
            write(datasetid + " | " + version);
            //System.out.println( datasetid + " | " + version);
        } 
    
    }
    public void process(String line, String inDataset, Map<String,String> results) throws NoSuchAlgorithmException, FileNotFoundException{     
       
        String dataset_id = getDatasetID(line,1);
        if (dataset_id==null){
            System.out.println( "ERROR can't parse this line: " + line);
            return;
        }
        if ( !results.containsKey( dataset_id ) ) {
            results.put( dataset_id, new String( "No Version" ) );
        } 
       
    }

    protected String getDatasetID(String aLine,int field){
    //use a second Scanner to parse the content of each line
    Scanner scanner = new Scanner(aLine);
    String dataset_id = null;
    scanner.useDelimiter(" \\| ");
    if (field==2)
        scanner.useDelimiter(" ");
   // scanner.useDelimiter(" \\| ");
    if ( scanner.hasNext() ){
      dataset_id = scanner.next().trim(); 
      if (scanner.hasNext() && field==2){
          dataset_id = scanner.next().trim(); 
      }
      //System.out.println("datasetid is : " + dataset_id);
      return dataset_id;
    }
    else {
      System.out.println("Empty or invalid line. Unable to process.");
    }
    return dataset_id;
    //no need to call scanner.close(), since the source is a String
  }
    
  protected void getVersion(String aLine,Map<String,String> results){
/*
 * | cmip5.output1.BCC.bcc-csm1-1.1pctCO2.day.atmos.day.r1i1p1            | published | 
 *   project=CMIP5, model=BCC-CSM1.1, Beijing Climate Center, China Meteorological Administration, 
 *   experiment=1 percent per year CO2, time_frequency=day, modeling realm=atmos, ensemble=r1i1p1, version=1  
 * | http://bcccsm.cma.gov.cn/thredds/esgcet/1/cmip5.output1.BCC.bcc-csm1-1.1pctCO2.day.atmos.day.r1i1p1.v1.xml 
 * 
 * Parse and look for "version="
 */
    //use a second Scanner to parse the content of each line
    Scanner sc1 = new Scanner(aLine);
    Scanner sc2 = null;
    Scanner sc3 = null;
    String line = null;
    String line2 = null;
    String version = null;
    
    
    String dataset_id = getDatasetID( aLine, 2);
    
    if (( dataset_id==null) || (!results.containsKey( dataset_id )) ) 
    {
        return;  // not one we need to process
    } 
    sc1.useDelimiter(" \\| ");
   
    while ( sc1.hasNext() )
    {
        line = sc1.next().trim();  
        
            sc2 = new Scanner(line);         
            sc2.useDelimiter(",");
            while ((sc2.hasNext())) 
            {

  
                line2= sc2.next();
                sc3 = new Scanner(line2);
               
                sc3.useDelimiter("=");
                while ((sc3.hasNext())) 
                {
                    String next = sc3.next();
                    if (next.equals(" version"))
                    {
                        if (sc3.hasNext()) version = sc3.next();
                        break;
                    }
                }
                if (version !=null)
                {
                    results.put(dataset_id, version);
                    break;
                }      
                    
            }
                if (version !=null) break;
            
        if (version !=null) break;
              
    }    
            
    if (sc1 != null)
        sc1.close();
    if (sc2 != null)
        sc2.close();
    if (sc3 != null)
        sc3.close();
   // System.out.println("Warning: No version found for dataset "+ dataset_id);
  
  }
    
    public void write(String line){
        out.println(line);
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
       
	  
          String inMap = args[0]; 
          String inDatasets = args[1];
          String path=args[2];
          String outfile = inMap+"_versions.txt";
	  
//        c5.createNewMapFile( "C:\\STAGE\\From_Pcmdi3\\cmip5_inm_v2.txt" ,  "C:\\STAGE\\From_Pcmdi3\\new_map.txt");
//        String result = c5.getMd5("C:\\STAGE\\From_Pcmdi3\\mrfso_LImon_inmcm4_sstClim4xCO2_r1i1p1_185001-187912.nc");
//        System.out.println("MD5: " + result);
    
            
            if (args.length != 3){
                System.out.println("Please provide proper input parameters in the following order:  'BuildDatasetVersions f1, f2, f3'  where ");
                System.out.println("f1: map file ");
                System.out.println("f2: gateway info file ");
                System.out.println("f3: path to the two files above ");
                return;
            }
        BuildDatasetVersions c5 = new BuildDatasetVersions();
	 c5.createNewMapFile(path+"/"+ inDatasets, path+"/"+ inMap, path+"/"+ outfile);
//        String outfile = infile+"_new.txt";
//	  c5.createNewMapFile(infile, outfile);
//        c5.createNewMapFile( "C:\\STAGE\\From_Pcmdi3\\cmip5_inm_v2.txt" ,  "C:\\STAGE\\From_Pcmdi3\\new_map.txt");
//        String result = c5.getMd5("C:\\STAGE\\From_Pcmdi3\\mrfso_LImon_inmcm4_sstClim4xCO2_r1i1p1_185001-187912.nc");
//        System.out.println("MD5: " + result);
    }
    
}


