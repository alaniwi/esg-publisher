/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pcmdi;
/**
 *
 * @author ganzberger1
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 *
 * @Created By: ganzberger1
 * @Date: Jan 5, 2012 COPYRIGHT NOTICE Copyright (C) 2010 Lawrence Livermore
 * National Laboratory.
 * cmip5.output2.INM.inmcm4.sstClim4xCO2.mon.landIce.LImon.r1i1p1 |
 * /cmip5/data/cmip5/output2/INM/inmcm4/sstClim4xCO2/mon/landIce/LImon/r1i1p1/mrfso/1/
 * mrfso_LImon_inmcm4_sstClim4xCO2_r1i1p1_185001-187912.nc | 31127932 |
 * mod_time=1297687379.000000 | checksum=c1d103f8234e8df955ed899116a1a386 |
 * checksum_type=MD5
 *
 */
public class ComputeMd5 {

    BufferedReader in;
    PrintWriter out;

    private String getMd5(String file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
//        File f = new File("C:\\STAGE\\From_Pcmdi3\\mrfso_LImon_inmcm4_sstClim4xCO2_r1i1p1_185001-187912.nc");
        InputStream is = null;
	String output = null;
        try {
            File f = new File(file);

            is = new FileInputStream(f);
            byte[] buffer = new byte[8192];
            int read = 0;

            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            output = bigInt.toString(16);
//		System.out.println("MD5: " + output);
            return output;
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Skipping this file...");
            //throw new RuntimeException("Unable to process file for MD5", e);
        } finally {

            is.close();

        }
        return output;
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }

    public void createNewMapFile(String inMap, String outMap) throws IOException {


        File om = new File(outMap);
        if (om.exists()) {
            om.delete();
        }

        out = new PrintWriter(new BufferedWriter(new FileWriter(outMap)));

        String line;
        try {
            in = new BufferedReader(new FileReader(inMap));
            while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                process(line);
            }
        } catch (Exception e) {
            System.out.println("From createNewMapFile: " + e.toString());
        } finally {
            close();
            out.flush();
            out.close();
        }

    }

    public void process(String line) throws NoSuchAlgorithmException, IOException {


        String one = " | checksum=";
        String two = " | checksum_type=MD5";
        String baseLine;
        if (line.indexOf("checksum=") > 0) {
            baseLine = line.substring(0, line.indexOf("checksum=") - 3);
        } else {
            baseLine = line;
        }
        String csum = null;
        String filename = getFileName(baseLine);
        try{
            csum = getMd5(filename);
        }catch (Exception e){
            return;
        }
        if (csum==null) 
		return;
	//System.out.println("csum is "+csum);
        String newLine = baseLine + one + csum + two;
        write(newLine);
    }

    protected String getFileName(String aLine) {
        //use a second Scanner to parse the content of each line
        Scanner scanner = new Scanner(aLine);
        String filename = null;
        scanner.useDelimiter(" \\| ");
        if (scanner.hasNext()) {
            String item1 = scanner.next();
            filename = scanner.next();
            System.out.println("filename is : " + filename.trim());
        } else {
            System.out.println("Empty or invalid line. Unable to process.");
        }
        return filename;
        //no need to call scanner.close(), since the source is a String
    }

    public void write(String line) {
        out.println(line);
        out.flush();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        ComputeMd5 c5 = new ComputeMd5();
        String infile = args[0];
        String outfile = infile + "_new.txt";
        c5.createNewMapFile(infile, outfile);
//        c5.createNewMapFile( "C:\\STAGE\\From_Pcmdi3\\cmip5_inm_v2.txt" ,  "C:\\STAGE\\From_Pcmdi3\\new_map.txt");
//        String result = c5.getMd5("C:\\STAGE\\From_Pcmdi3\\mrfso_LImon_inmcm4_sstClim4xCO2_r1i1p1_185001-187912.nc");
//        System.out.println("MD5: " + result);
    }
}


