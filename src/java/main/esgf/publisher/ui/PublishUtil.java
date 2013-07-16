/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esgf.publisher.ui;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author ganzberger1
 */
public class PublishUtil {

    PublicationParametersGUI gui = null;

    public PublishUtil(PublicationParametersGUI g) {
        this.gui = g;
    }

    public boolean MyProxyLoginX(String user, String port, String server) {

        //String[] commands = {"/usr/local/globus/bin/myproxy-logon -s " + server + " -l " + user + " -p " + port + " -o /export/ganzberger1/.globus/certificate-file"};
        String[] commands = {"/usr/local/globus/bin/myproxy-logon", " -s ", server, " -l ", user, " -p ", port, " -o ", "/export/" + user + "/.globus/certificate-file"};
        try {
            setup();
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(commands);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line = null;

            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            gui.updateOutputTextArea("MyProxyLogin Exited with error code " + exitVal);

        } catch (Exception e) {
            System.out.println("Exception results: " + e.toString());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static List<String> getMultiLineInfoFromINI(String iniFile, String project, String element) throws IOException {

        List<String> result = new ArrayList<String>();
        DataInputStream in = null;
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(iniFile);
            // Get the object of DataInputStream
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            boolean processingProject = false;
            boolean processingElement = false;
            while ((strLine = br.readLine()) != null) {
                // look for the project stanza and process till done
                if (processingElement) {
                    if (strLine.equals("")) {
                        break;
                    }
                    if (strLine.substring(0, 1).trim().equals("")) {
                        String second = getSecondElement(strLine);
                        if (second != null) {
                            result.add(second.trim());
                            //System.out.println(second);
                        }
                    } else {
                        break;
                    }
                }
                if (strLine.startsWith("[project:" + project + "]")) {
                    processingProject = true;
                    //System.out.println("Found " + project);
                } else if (processingProject && strLine.startsWith("[")) {
                    break;
                }
                if (processingProject && !processingElement) {
                    String result1 = findElement(strLine, element);
                    if (result1 != null) {
                        processingElement = true;;
                    }
                }
            }
            //Close the input stream
            return result;
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    private static String getSecondElement(String strLine) {

        StringTokenizer st2 = new StringTokenizer(strLine.trim(), "|");
        int cnt = 0;
        while (st2.hasMoreElements()) {
            String item = (String) st2.nextElement();
            if (cnt == 1) {
                return item;
            } else {
                cnt++;
            }
        }
        return null;
    }

    /**
     * category_defaults = institute | LLNL ..
     *
     * @param iniFile
     * @param project
     * @param element
     * @param ikey
     * @param ivalue
     * @return
     * @throws IOException
     */
    @SuppressWarnings("empty-statement")
    public static HashMap<String, String> getDualMultiLineInfoFromINI(String iniFile, String project, String element, int ikey, int ivalue) throws IOException {

        HashMap<String, String> result = new HashMap<String, String>();
        DataInputStream in = null;
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(iniFile);
            // Get the object of DataInputStream
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine = null;
            //Read File Line By Line
            boolean processingProject = false;
            boolean processingElement = false;
            String sub = null;
            while ((strLine = br.readLine()) != null) {
                // look for the project stanza and process till done
                if (processingElement) {
                    if (strLine.equals("")) {
                        break;
                    }
                    if (strLine.substring(0, 1).trim().equals("")) {
                        boolean good = addKeyPair(result, strLine.trim(), ikey, ivalue);
                        //String second = getSecondElement(strLine);
                        // if (!good) {
                        //gui.updateOutputTextArea("Warning...error occured while parsing " + iniFile + " " + project + " " + element);
                        //     System.out.println("Warning...error occured while parsing " + iniFile + " " + project + " " + element);
                        //  }
                    } else {
                        break;
                    }
                }
                if (strLine.startsWith("[project:" + project + "]")) {
                    processingProject = true;
                    //System.out.println("Found " + project);
                } else if (processingProject && strLine.startsWith("[")) {
                    break;
                }
                if (processingProject && !processingElement) {
                    String result1 = findElement(strLine, element);
                    if (result1 != null) {
                        processingElement = true;;
                    }
                }
            }
            //Close the input stream
            return result;
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    /**
     * Input: submodel | enum | true | true |4 or experiment | land-uq
     *
     * parse the input line and if possible add a new key pair to the map the
     * key is element number ikey in the parsed string and ivalue the value.
     *
     * @param map
     * @param strLine
     * @param ikey
     * @param ivalue
     * @return
     */
    public static boolean addKeyPair(HashMap<String, String> map, String strLine, int ikey, int ivalue) {

        String sKey = "";
        String sValue = "";
        String delims = "| ";
        StringTokenizer st = new StringTokenizer(strLine, delims);
        int elementCnt = 0;
        while (st.hasMoreElements()) {
            if (ikey == elementCnt) {
                sKey = (String) st.nextElement();
            }
            if (ivalue == elementCnt) {
                sValue = (String) st.nextElement();
            }
            elementCnt++;
        }


        if (sKey == "" || sValue == "") {
            return false;
        }
        map.put(sKey, sValue);

        return true;
    }

    public static boolean addKeyPairX(HashMap<String, String> map, String strLine, int ikey, int ivalue) {

        String sKey = "";
        String sValue = "";
        String delims = "|";
        String[] tokens = strLine.split(delims);
        int len = tokens.length;
        if (len > ikey && len > ivalue) {
            sKey = tokens[ikey];
            sValue = tokens[ivalue];
            if (sKey == "" || sValue == "") {
                return false;
            }
            map.put(sKey, sValue);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Parse the ini file, return the line for the element within the given
     * project. e.g. for project: cssef element: directory_format returns:
     * /esg/data/%(project)s/%(model)s/%(submodel)s/%(experiment)s/%(subexperiment)s/%(ensemble)s
     *
     * @param iniFile (full path)
     * @param project
     * @param element
     * @return
     */
    public static String getInfoFromINI(String iniFile, String project, String element) throws IOException {

        DataInputStream in = null;
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(iniFile);
            // Get the object of DataInputStream
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            boolean processingProject = false;
            while ((strLine = br.readLine()) != null) {
                // look for the project stanza and process till done
                if (strLine.startsWith("[project:" + project + "]")) {
                    processingProject = true;
                    //System.out.println("Found " + project);
                } else if (processingProject && strLine.startsWith("[")) {
                    break;
                }
                if (processingProject) {
                    String result = findElement(strLine, element);
                    if (result != null) {
                        return result;
                    }
                }
            }
            //Close the input stream
            return null;
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    /**
     * Parse the strings, ignore the element name and the first "="
     *
     * @param strLine
     * @param element
     * @return
     */
    private static String findElement(String strLine, String element) {


        String delims = " =";
        String[] tokens = strLine.split(delims);
        int len = tokens.length;
        if (len > 0) {

            if (tokens[0].equals(element)) {

                return strLine;
            }
        }
        return null;
    }

    /**
     * This version accepts = embedded in the element description...
     *
     * @param strLine
     * @param element
     * @return
     */
    private static String findElement2(String strLine, String element) {


        String delims = " ";
        String[] tokens = strLine.split(delims);
        int len = tokens.length;
        if (len >= 2) {

            if (tokens[0].equals(element)) {
                // assume tokens[1] == "=" so skip that
                StringBuffer result = new StringBuffer(tokens[2]);
                for (int i = 3; i < tokens.length; i++) {
                    result.append(tokens[i]);
                }
                return result.toString();
            }
        }
        return null;
    }

    public void setup2() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("/etc/csg.sh");
//        Map<String, String> env = pb.environment();
// env.put("VAR1", "myValue");
// env.remove("OTHERVAR");
// env.put("VAR2", env.get("VAR1") + "suffix");
// pb.directory(new File("myDir"));
        Process p = pb.start();
    }

    public void setup() {
        String[] commands1 = {"/etc/esg.env"};
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(commands1);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line = null;

            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            //System.out.println("Setup Exited with error code " + exitVal);

        } catch (Exception e) {
            System.out.println("Error in PublishUtil.setup " + e.toString());
            e.printStackTrace();
        }


    }

    class InputStreamHandler
            extends Thread {

        /**
         * Stream being read
         */
        private InputStream m_stream;
        /**
         * The StringBuffer holding the captured output
         */
        private StringBuffer m_captureBuffer;

        /**
         * Constructor.
         *
         * @param
         */
        InputStreamHandler(StringBuffer captureBuffer, InputStream stream) {
            m_stream = stream;
            m_captureBuffer = captureBuffer;
            start();
        }

        /**
         * Stream the data.
         */
        public void run() {
            try {
                int nextChar;
                while ((nextChar = m_stream.read()) != -1) {
                    m_captureBuffer.append((char) nextChar);
                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Execute the command in the shell
     *
     * @param commands
     */
    public void CommandExecution(String[] commands) {

        StringBuffer sb = new StringBuffer("");
        Process pr = null;
        BufferedReader input = null;
        StringBuffer errBuffer=null;
        StringBuffer inBuffer=null;;
        
        try {
            for (String cmd : commands) {

                sb.append(cmd).append(" ");
            }
            gui.updateOutputTextArea("Executing command: " + sb.toString());
            pr = Runtime.getRuntime().exec(commands);

            inBuffer = new StringBuffer();
            InputStream inStream = pr.getInputStream();
            new InputStreamHandler(inBuffer, inStream);

            errBuffer = new StringBuffer();
            InputStream errStream = pr.getErrorStream();
            new InputStreamHandler(errBuffer, errStream);

            pr.waitFor();
        } catch (Exception e) {
            gui.updateOutputTextArea("Error in PublishUtil.CommandExecution " + e.toString());
        } finally {
            if (pr != null) {
                pr.destroy();
            }
            if (inBuffer!=null) 
                gui.updateOutputTextArea(inBuffer.toString());
            if (errBuffer!=null && this.gui.pref.getOutputVerbose()) 
                gui.updateOutputTextArea(errBuffer.toString());
        }
    }
    
        /**
         * Execute the command in the shell
         *
         * @param commands
         */
    

    public void CommandExecution0(String[] commands) {

        StringBuffer sb = new StringBuffer("");
        Process pr = null;
        BufferedReader input = null;
        try {
            for (String cmd : commands) {

                sb.append(cmd).append(" ");
            }
            gui.updateOutputTextArea("Executing command: " + sb.toString());

            // String[] test = new String[]{sb.toString()};

            Runtime rt = Runtime.getRuntime();
            pr = rt.exec(commands);

            input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line;

            while ((line = input.readLine()) != null) {
                gui.updateOutputTextArea(line);
            }

            int exitVal = pr.waitFor();
            gui.updateOutputTextArea("Command Completed result code " + exitVal);
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
            gui.updateOutputTextArea("Error in PublishUtil.CommandExecution " + e.toString());
        } finally {
            if (pr != null) {
                pr.destroy();
            }

        }

    }

    public void CommandExecution2(String[] commands) {

        StringBuffer sb = new StringBuffer("");
        Process pr = null;
        try {
            for (String cmd : commands) {

                sb.append(cmd).append(" ");
            }
            gui.updateOutputTextArea("Executing command: " + sb.toString());

            // String[] test = new String[]{sb.toString()};

            Runtime rt = Runtime.getRuntime();
            pr = rt.exec(commands);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String line;

//            char[] characters = new char[4096];
//	    while (input.read(characters) != -1) {}
//	    while (err.read(characters) != -1) {}            
//            while ((line = input.readLine()) != null) {
//                gui.updateOutputTextArea(line);
//            }
//            while ((line = err.readLine()) != null) {
//                gui.updateOutputTextArea(line);
//            }

            int exitVal = pr.waitFor();
            gui.updateOutputTextArea("Command Completed result code " + exitVal);

        } catch (Exception e) {
            gui.updateOutputTextArea("Error in PublishUtil.CommandExecution " + e.toString());
        } finally {
            if (pr != null) {
                pr.destroy();
            }
        }

    }

    /**
     *
     * @param aFile
     * @param aContents
     * @throws FileNotFoundException
     * @throws IOException
     */
    static public void setContents(File aFile, String aContents)
            throws FileNotFoundException, IOException {
        if (aFile == null) {
            throw new IllegalArgumentException("File should not be null.");
        }
        if (!aFile.exists()) {
            throw new FileNotFoundException("File does not exist: " + aFile);
        }
        if (!aFile.isFile()) {
            throw new IllegalArgumentException("Should not be a directory: " + aFile);
        }
        if (!aFile.canWrite()) {
            throw new IllegalArgumentException("File cannot be written: " + aFile);
        }


        Writer output = new BufferedWriter(new FileWriter(aFile));
        try {
            //FileWriter always assumes default encoding is OK!
            output.write(aContents);
        } finally {
            output.close();
        }
    }

    /**
     *
     * @param aFile
     * @param files_to_publish
     * @return
     */
    static public String getContents(File aFile, List<File> files_to_publish) {
        //...checks on aFile are elided
        StringBuilder contents = new StringBuilder();

        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null; //not declared within while loop
        /*
                 * readLine is a bit quirky :
                 * it returns the content of a line MINUS the newline.
                 * it returns null only for the END of the stream.
                 * it returns an empty String if two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    for (File nextfile : files_to_publish) {
                        if (line.lastIndexOf(nextfile.getName()) >= 0) {
                            contents.append(line);
                            contents.append(System.getProperty("line.separator"));
                        }
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }

    /**
     * Write out a new file
     *
     * @param output
     * @param subset
     */
    public void writing(String output, String subset) {
        try {
            File statText = new File(output);

            if (statText.exists()) {
                statText.delete();
                statText = new File(output);
            }
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(subset);
            w.close();
        } catch (IOException e) {
            System.err.println("problem writing to new output file");
        }
    }

    /**
     * Write out a new map file consisting only of those files selected by the
     * user in the gui.
     *
     * @param MapOut
     * @param files_to_publish
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    File mapFileAdjustment(String MapOut, List<File> files_to_publish) throws FileNotFoundException, IOException {
        File aFile = new File(MapOut);
        String subset = getContents(aFile, files_to_publish);

        String newMapOut = MapOut + "_publish";
        File freturn = new File(newMapOut);
        if (freturn.exists()) {
            freturn.delete();
        }
        freturn = new File(newMapOut);
        //setContents(freturn, subset);
        writing(newMapOut, subset);
        return freturn;
    }

    /**
     * The move will actually be a file copy and replace any existing files by
     * that name.
     *
     * @param sourceFile
     * @param destDir
     * @return
     * @throws IOException
     */
    public boolean move_a_file(File sourceFile, String destDir) throws IOException {
        boolean result = true;
        try {
            File destFile = new File(destDir + sourceFile.getName());
//            if (destFile.exists()) {
//                if (sourceFile.getAbsoluteFile().equals(destFile.getAbsoluteFile())) {
//                    gui.updateOutputTextArea("INFO: source==dest....no explicit move necessary");
//                    return true; // if the source==dest skip...
//                }
//                destFile.delete();
//            }
            // Default is to copy/replace unless the source/dest are the same
            Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(destFile.getAbsolutePath()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // This would move the file unless it already exists
//            if (sourceFile.renameTo(destFile)) {
//                gui.updateOutputTextArea("File is moved successful!");
//                result = true;
//            } else {
//                gui.updateOutputTextArea("File is failed to move!");
//            }
        } catch (Exception e) {
            System.out.println("Warning from move_a_file ");
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     *
     * @param targetDir
     * @param files_to_publish
     * @return
     * @throws IOException
     */
    boolean moveFiles(String targetDir, List<File> files_to_publish) throws IOException {
        for (File file : files_to_publish) {
            if (!move_a_file(file, targetDir)) {
                return false;
            }
        }
        return true;
    }
}
