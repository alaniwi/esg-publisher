/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esgf.publisher.ui;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.nio.*; //nio.file.StandardCopyOption.*;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ganzberger1
 */
public class SimplePublisher {

    static String Port = "7512";
    static int port1 = 7512;
    static String Server = "llnl.pcmdi11.gov";
    static String EsgINI = "/esg/config/esgcet/esg.ini";
    static String MapOut =  "simple_publisher_map.txt";
    static String MapOut1 = "simple_publisher_map.txt";
    static String targetBaseDir = "/esg/data/";
    static List<String> cmd3 = new ArrayList<String>();
    String targetDir = "";
    String Experiment = "";
    String SubExperiment = "";
    String Institute = "";
    String Model = "";
    String SubModel = "";
    String Ensemble = "";
    String Time_Frequency = "";
    String Realm = "";
    String OpenID = "";
    char[] OpenIDPassword;
    boolean gui_done = false;
    static List<File> files_to_publish = null;
    public String Project = "cssef"; // default
    List<String> project_options = new ArrayList<String>();
    PublicationParametersGUI gui = null;
    PublishUtil publishUtil = null;
    String[] Inputargs;
    private boolean loggedIn=false;

    /**
     * @param args the command line arguments
     */
    public SimplePublisher() {
    }

    public static void main(String[] args) {

        SimplePublisher p = new SimplePublisher();
        p.Inputargs = args;
        p.init();
        
        p.initialize();
        Logger l0 = Logger.getLogger("");
        l0.removeHandler(l0.getHandlers()[0]);
        try {
            p.rungui();
        } catch (IOException ex) {
            System.out.println("Rungui error " + ex.toString());

        }

    }

    public void init() {
        
            gui = new PublicationParametersGUI(this);
            gui.makeVisible(true);
            publishUtil = new PublishUtil(gui);
            gui.setLogin(false);
                 
    }
    
    
    public void initialize(){
        try{
            MapOut = getHomeDir() + File.separator + MapOut1;
            gui.updateOutputTextArea("");

            for (int i = 0; i < Inputargs.length; i++) { //String arg: args){
                if (Inputargs[i].equals("-h")) {
                    gui.updateOutputTextArea(" -s xxx : server to use, def: " + Server);
                    gui.updateOutputTextArea(" -o xxx : output map file to use, def: " + MapOut);
                    gui.updateOutputTextArea(" -i xxx : the esg.ini file to use, def: " + EsgINI);
                    gui.updateOutputTextArea(" -p xxx : the port to use, def: " + Port);
                    gui.updateOutputTextArea(" -h : program options ");
                    return;
                }
                if (Inputargs[i].equals("-s")) {
                    Server = Inputargs[i + 1];
                    continue;
                }
                if (Inputargs[i].equals("-o")) {
                    MapOut = Inputargs[i + 1];
                    continue;
                }
                if (Inputargs[i].equals("-i")) {
                    EsgINI = Inputargs[i + 1];
                    continue;
                }
                if (Inputargs[i].equals("-p")) {
                    Port = Inputargs[i + 1];
                    continue;
                }
            }

            gui.updateOutputTextArea("Parameters :");
            gui.updateOutputTextArea(" SERVER to use: " + Server);
            gui.updateOutputTextArea(" PORT to use: " + Port);

            gui.updateOutputTextArea(" ESG.INI file to use: " + EsgINI);
            //gui.updateOutputTextArea(" Output map file to use: " + MapOut);

            this.project_options.add(this.Project); // just add cssef for now...
            initializeGui();


        } catch (Exception e) {
            System.out.println("Error in Initialize " + e.toString());

        }
    }

    public void initializeGui() throws IOException {
        List<String> institutes = getFieldsFromINI("institute_options");
        if (!institutes.isEmpty()) {
            gui.updateInstitute(institutes);
        }

        List<String> ensembles = getFieldsFromINI("ensemble_options");
        if (!ensembles.isEmpty()) {
            gui.updateEnsembles(ensembles);
        }

        List<String> models = getFieldsFromINI("model_options");
        if (!models.isEmpty()) {
            gui.updateModels(models);
        }

        List<String> submodels = getFieldsFromINI("submodel_options");
        if (!submodels.isEmpty()) {
            gui.updateSubmodels(submodels);
        }

        List<String> experiments = getExperimentFieldsFromINI();
        if (experiments != null) {
            if (!experiments.isEmpty()) {
                gui.updateExperiments(experiments);
            }
        }

        //List<String> projects = getFieldsFromINI("project_options");
        if (!project_options.isEmpty()) {
            gui.updateProjects(project_options);
        }

        /**
         * Now set any defaults
         */
        setDefaultsInGui();

    }

    public void setDefaultsInGui() throws IOException {

        HashMap<String, String> map = PublishUtil.getDualMultiLineInfoFromINI(EsgINI, this.Project, "category_defaults", 0, 1);

        gui.setDefProject(this.Project);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();


            if (key.equals("model")) {
                gui.setDefModel(value);
            }

            if (key.equals("submodel")) {
                gui.setDefSubmodel(value);
            }

            if (key.equals("institute")) {
                gui.setDefInstitute(value);
            }

            if (key.equals("experiment")) {
                gui.setDefExperiment(value);
            }
        }
    }

    public List<String> getFieldsFromINI(String field) throws IOException {

        List<String> result = new ArrayList<String>();
        // read this from the project stanza...

        String expr = PublishUtil.getInfoFromINI(EsgINI, this.Project, field);
        String delims = "[,= ]+"; // so the delimiters are:  = space
        String[] tokens = expr.split(delims);

        for (String one : tokens) {
            if (one.equals(field)) {
                continue;
            }
            //gui.updateOutputTextArea(one);
            result.add(one);
        }


        return (result);

    }

    public String getHomeDir() {
        String userHome = System.getProperty("user.home");
        return userHome;
    }

    private String getUserHome(String userName) throws IOException {
        return new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo ~" + userName}).getInputStream())).readLine();
    }

    public List<String> getExperimentFieldsFromINI() throws IOException {

        // read this from the project stanza...

        return PublishUtil.getMultiLineInfoFromINI(EsgINI, this.Project, "experiment_options");


    }

    public void setGUIDone() {
        this.gui_done = true;
    }
    //pcmdi.setParameters(institute, experiment, subexperiment, model,submodel, ensemble,  getOpenID(), getOpenIDPassword());

    public void setParameters(String project, String institute, String Experiment, String subexperiment, String Model,
            String submodel, String ensemble, String realm, String tf, String OpenID, char[] OpenIDPassword, List<File> files) {
        this.Experiment = Experiment;
        this.SubExperiment = subexperiment;
        this.Model = Model;
        this.SubModel = submodel;
        this.Institute = institute;
        this.Ensemble = ensemble;
        this.Realm = realm;
        this.Time_Frequency = tf;
        this.files_to_publish = files;
        this.OpenID = OpenID;
        this.OpenIDPassword = OpenIDPassword;
        this.Project = project;
    }

    public void setOpenid(String OpenID, char[] OpenIDPassword ){
        this.OpenID = OpenID;
        this.OpenIDPassword = OpenIDPassword;
        
    }
    public void rungui() throws IOException {

        if (!gui.isVisible()) {
            gui.makeVisible(true);
        }


        while (!gui_done) {
            try {
                Thread.currentThread().sleep(4000);
            } catch (InterruptedException e) {
            }
        }
        System.exit(0);


    }

    public void login(){
       
        
            String pw = "";
            for (int i = 0; i < OpenIDPassword.length; i++) {
                pw = pw + OpenIDPassword[i];
            }
            gui.updateOutputTextArea("Logging in OpenID " + OpenID + " ");
            //gui.updateOutputTextArea("Experiment " + Experiment + " Model " + Model + " OpenID " + OpenID + " ");
            //gui.updateOutputTextArea("SubExperiment " + SubExperiment + "  SubModel " + SubModel + " Ensemble " + Ensemble);


            //boolean result = publishUtil.MyProxyLogin(this.OpenID, Port, Server);
            if (!loggedIn) {
                MyProxyLogon mpl = new MyProxyLogon(Server, port1, 7200);
                boolean result = mpl.getCredentials(gui.getOpenID(), pw);
                if (result) {
                    gui.updateOutputTextArea("login succeeded");
                    this.loggedIn = true;
                    gui.setLogin(true);
                } else {
                    gui.updateOutputTextArea("login failed");
                    this.loggedIn = false;
                    gui.setLogin(false);
                    return;
                }
            }
            
    
    }
    
    public void publish() {

        try {
            
           //if (!loggedIn)
           //     login();


            List<String> subDirs = new ArrayList<String>();
            // create an array with each subdirectory for the publication


            subDirs.add("esg");
            subDirs.add("data");

            targetDir = createDirectory(subDirs);

            if (targetDir == null) {
                System.out.print("Warning: directory creation failed..exiting");
                return;
            }

            boolean movedOk = publishUtil.moveFiles(targetDir, files_to_publish);
            cmd3.clear();
            cmd3.add("/usr/local/uvcdat/bin/esgscan_directory");
            cmd3.add("--read-directories");
            cmd3.add("-i");
            cmd3.add(EsgINI);
            cmd3.add("-o");
            cmd3.add(MapOut);


            if (this.Project.equals("")) {
                gui.updateOutputTextArea("Warning project value is null...Please chaeck and retry...exiting");
                return;
            }
            cmd3.add("--project");
            cmd3.add(this.Project);


            if (!this.Model.equals("")) {
                cmd3.add("-p");
                cmd3.add("model=" + this.Model);

            }

            if (!this.SubModel.equals("")) {
                cmd3.add("-p");
                cmd3.add("submodel=" + this.SubModel);

            }

            if (!this.Institute.equals("")) {
                cmd3.add("-p");
                cmd3.add("institute=" + this.Institute);

            }

            if (!this.Experiment.equals("")) {
                cmd3.add("-p");
                cmd3.add("experiment=" + this.Experiment);

            }
            if (!this.SubExperiment.equals("")) {
                cmd3.add("-p");
                cmd3.add("sub_experiment=" + this.SubExperiment);

            }

            if (!this.Ensemble.equals("")) {
                cmd3.add("-p");
                cmd3.add("ensemble=" + this.Ensemble);
            }
            if (!this.Realm.equals("")) {
                cmd3.add("-p");
                cmd3.add("realm=" + this.Realm);
            }
            if (!this.Time_Frequency.equals("")) {
                cmd3.add("-p");
                cmd3.add("time_frequency=" + this.Time_Frequency);
            }

            cmd3.add(targetDir);



            gui.updateOutputTextArea("about to run setup...");
            publishUtil.setup();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        gui.updateProgressBar(0);
                        //publishUtil.testPub((String[]) tc.toArray(new String[0]));

                        publishUtil.CommandExecution((String[]) cmd3.toArray(new String[0]));
                        File newMapOut = publishUtil.mapFileAdjustment(SimplePublisher.MapOut, SimplePublisher.files_to_publish);

                        // test
                        List<String> tc1 = new ArrayList<String>();
                        tc1.add("/usr/local/uvcdat/bin/esgunpublish");
                        tc1.add("--database-delete");
                        tc1.add("-i");
                        tc1.add(EsgINI);
                        tc1.add("--map");
                        tc1.add(newMapOut.getAbsolutePath());

                        if (gui.pref.getUnpublishExisting()){
                            gui.updateOutputTextArea("About to first run the esgunpublish...");
                            publishUtil.CommandExecution((String[]) tc1.toArray(new String[0]));
                        }
                        List<String> tc = new ArrayList<String>();
                        tc.add("/usr/local/uvcdat/bin/esgpublish");
                        tc.add("-i");
                        tc.add(EsgINI);
                        tc.add("--map");
                        tc.add(newMapOut.getAbsolutePath());
                        tc.add("--project");                        
                        tc.add(Project);
                        tc.add("-u");
                        tc.add("--service");
                        tc.add("fileservice");
                        
                        if (!Model.equals("")) {
                            tc.add("-p");                     
                            tc.add("model=" + Model);
                        }
                        if (!SubModel.equals("")) {
                            tc.add("-p");
                            tc.add("submodel=" + SubModel);
                        }
                        if (!Institute.equals("")) {
                            tc.add("-p");
                            tc.add("institute=" + Institute);
                        }
                        if (!Experiment.equals("")) {
                            tc.add("-p");
                            tc.add("experiment=" + Experiment);
                        }
                        if (!Ensemble.equals("")) {
                            tc.add("-p");
                            tc.add("ensemble=" + Ensemble);
                        }
                        tc.add("--thredds");
                        tc.add("--publish");
                        gui.updateOutputTextArea("About to run the esgpublish...");
                        publishUtil.CommandExecution((String[]) tc.toArray(new String[0]));

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SimplePublisher.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(SimplePublisher.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        gui.updateProgressBar(1);
                        initialize();
                    }
                }
            });

            thread.start();
        } catch (Exception e) {
            System.out.println("Error in SimplePublisher.publish " + e.toString());
        }
    }

    /**
     * The items defined within the dataset_id must be defined
     *
     * @return
     * @throws IOException
     */
    public boolean verifyRequired() throws IOException {

        List<String> required = new ArrayList<String>();
        // read this from the project stanza...

        String expr = PublishUtil.getInfoFromINI(EsgINI, this.Project, "dataset_id");
        String delims = "[%\\().= ]+"; // so the delimiters are:  + - * / ^ space
        String[] tokens = expr.split(delims);

        for (String one : tokens) {
            if (one.equals("s") || (one.equals("dataset_id"))) {
                continue;
            }
            //gui.updateOutputTextArea(one);
            required.add(one);
        }

        if (!haveRequired(required)) {
            return false;
        }
        return true;
    }

    /**
     * For cssef/clm data on pcmdi11 the directory format is known so I am using
     * that structure here. TODO: To make this more versatile, read the
     * directory_format and dynamically build the path from that...
     *
     *
     * directory_format =
     * /esg/data/%(project)s/%(model)s/%(submodel)s/%(experiment)s/%(subexperiment)s/%(ensemble)s
     * dataset_id =
     * %(project)s.%(model)s.%(submodel)s.%(experiment)s.%(subexperiment)s.%(ensemble)s
     *     
* Build a list in order so we can create our directory structure to store
     * the files. If any of the required fields are not defined then return an
     * error with a popup warning and exit.
     *
     * @param subdirs
     * @return
     */
    public String createDirectory(List<String> subdirs) throws IOException {

        List<String> required = new ArrayList<String>();
        // read this from the project stanza...

        String expr = PublishUtil.getInfoFromINI(EsgINI, this.Project, "dataset_id");
        // String expr = "dataset_id =       %(project)s.%(model)s.%(submodel)s.%(experiment)s.%(subexperiment)s.%(ensemble)s";
        String delims = "[%\\().= ]+"; // so the delimiters are:  + - * / ^ space
        String[] tokens = expr.split(delims);

        for (String one : tokens) {
            if (one.equals("s") || (one.equals("dataset_id"))) {
                continue;
            }
            //gui.updateOutputTextArea(one);
            required.add(one);
        }

        if (!haveRequired(required)) {
            return null;
        }

        // order is important!
        // If the directory structure support other parameters, add them here:

        subdirs.add(this.Project);
        addDirs(subdirs, required, "model", this.Model);
        addDirs(subdirs, required, "submodel", this.SubModel);
        addDirs(subdirs, required, "experiment", this.Experiment);
        addDirs(subdirs, required, "subexperiment", this.SubExperiment);
        addDirs(subdirs, required, "ensemble", this.Ensemble);


        return (CreateSubDirs(subdirs));

    }

    public void addDirs(List<String> subdirs, List<String> required, String name, String field) {
        for (String x : required) {
            if (x.equals(name)) {
                subdirs.add(field);
                break;
            }
        }

    }

    /**
     * make sure we have all the required fields (read from esg.ini file?)
     *
     * @param required
     * @return
     */
    public boolean haveRequired(List<String> required) {

        for (String one : required) {
            if (one.equals("model")) {
                if (this.Model.equals("")) {
                    return false;
                }
            }
            if (one.equals("submodel")) {
                if (this.SubModel.equals("")) {
                    return false;
                }
            }
            if (one.equals("experiment")) {
                if (this.Experiment.equals("")) {
                    return false;
                }
            }
            if (one.equals("subexperiment")) {
                if (this.SubExperiment.equals("")) {
                    return false;
                }
            }
            if (one.equals("ensemble")) {
                if (this.Ensemble.equals("")) {
                    return false;
                }
            }


        }
        return true;
    }

    /**
     * Returns true if sucessfully able to create the sub-directories returns
     * false if they already existand there are files in that directory, (a
     * previous publication that should be unpublished?) Or the directories
     * could not be created (permission issues?).
     *
     * @param subdirs
     * @return
     */
    private String CreateSubDirs(List<String> subdirs) {



        StringBuffer path = new StringBuffer("/");

        for (String subdir : subdirs) {
            path.append(subdir);
            path.append("/");

        }
        File dir = new File(path.toString());
        if (dir.exists()) {
            return path.toString();
        }
        dir.mkdirs();
        if (dir.exists()) {
            return path.toString();
        }
        return null;

    }

    public String getExperiment() {
        return Experiment;
    }

    public String getSubExperiment() {
        return SubExperiment;
    }

    public String getInstitute() {
        return Institute;
    }

    public String getModel() {
        return Model;
    }

    public String getSubModel() {
        return SubModel;
    }

    public String getEnsemble() {
        return Ensemble;
    }

    public String getTime_Frequency() {
        return Time_Frequency;
    }

    public String getRealm() {
        return Realm;
    }

    public String getOpenID() {
        return OpenID;
    }

    public String getProject() {
        return Project;
    }
}
