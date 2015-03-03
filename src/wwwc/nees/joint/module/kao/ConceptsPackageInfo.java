package wwwc.nees.joint.module.kao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory to get an instance of the Repository specified in the configuration
 * properties
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class ConceptsPackageInfo {

    // VARIABLES
    // -------------------------------------------------------------------------
    // The repository static variable
    private static Map<String, String> packages = null;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets an instance of Repository
     *
     * @return repository a Repository object already initialized
     */
    public static synchronized Map<String, String> getPackagesInfo(Class classe) {

        // If repository is null
        if (packages == null) {

            packages = new HashMap<String, String>();
            InputStream st = classe.getResourceAsStream("/META-INF/org.openrdf.conceptsMapping");
            InputStreamReader reader = new InputStreamReader(st);

            BufferedReader red = new BufferedReader(reader);

            try {
                while (red.ready()) {
                    String pkgLine = red.readLine();
                    String key = pkgLine.substring(0, pkgLine.indexOf(" = "));
                    String value = pkgLine.substring(pkgLine.indexOf(" = ") + 3);
                    packages.put(key, value);
                }

            } catch (IOException ex) {
                Logger.getLogger(ConceptsPackageInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return packages;
    }

    /**
     * Reads the lib, putting all packages and classes in the map.
     *
     * @return repository a Repository object already initialized
     */
    public static void readPackagesInfo() {

        packages = new HashMap<String, String>();
        InputStream st = ConceptsPackageInfo.class.getResourceAsStream("/META-INF/org.openrdf.concepts");
        InputStreamReader reader = new InputStreamReader(st);

        BufferedReader red = new BufferedReader(reader);

        try {
            while (red.ready()) {

                String pkgLine = red.readLine();
                String key = pkgLine.substring(0, pkgLine.indexOf(" = "));
                String value = pkgLine.substring(pkgLine.indexOf(" = ") + 3);
                packages.put(key, value);
            }

        } catch (IOException ex) {
            Logger.getLogger(ConceptsPackageInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
