/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pcmdi;

/**
 *
 * @author ganzberger1
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.GetParams;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSCredential;

public class MyProxyLogon {

    private static int lifetime = 2 * 3600; // seconds
    private static String hostname = "pcmdi11.llnl.gov";
    private static int port = 7512;

    public MyProxyLogon(String hostname, int port, int lifetime) {
        this.hostname = hostname;
        this.port = port;
        this.lifetime = lifetime;
    }

    public boolean getCredentials(String username, String password) {

        MyProxy myProxy = new MyProxy(hostname, port);
        GetParams getParams = new GetParams();
        getParams.setUserName(username);
        getParams.setPassphrase(password);
        getParams.setLifetime(lifetime);

        GSSCredential proxy = null;
        boolean result = true;
        try {
            proxy = myProxy.get(null, getParams);
            String userhome = System.getProperty("user.home");
            result = export(userhome + "/.globus/certificate-file", proxy);

            // System.out.println(proxy.getName().toString());
            // System.out.println(proxy.getRemainingLifetime());
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * Save proxy into a proxy file
     *
     * @param proxy_location the proxy file to be written
     * @return success or failure
     */
    synchronized public static boolean export(String proxy_location, GSSCredential proxy) {
        File f = null;
        FileOutputStream out = null;
        try {
            if (proxy_location != null) {
                System.out.println("Storing new credential in " + proxy_location);

                // create a file
                f = new File(proxy_location);
                f.delete();
                if (!f.exists() || f.canWrite()) {

                    try {

                        // set read only permissions
                        //Util.setOwnerAccessOnly(proxy_location);

                        out = new FileOutputStream(f);
                        // write the contents
                        byte[] data = ((ExtendedGSSCredential) proxy).export(
                                ExtendedGSSCredential.IMPEXP_OPAQUE);
                        out.write(data);
                    } finally {
                        if (out != null) {
                            out.close();
                        }

                    }
                    System.out.println("Stored credential OK.");
                }
            }
            return true;
        } catch (Exception e) {
            //	System.out.println("", e.toString());
            return false;
        }

    }
}
