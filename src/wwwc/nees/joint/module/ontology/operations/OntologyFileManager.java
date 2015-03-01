package wwwc.nees.joint.module.ontology.operations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

/**
 *  Manager for ontology file static operations
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OntologyFileManager {

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets the URL of a file path
     *
     * @param path
     *            the ontology file path
     * @return url
     *            the URL of the path
     */
    public static URL getURLofFile(String path) {

        //Initialize the return with null
        URL url = null;

        try {
            //Creates a new file with the path
            File file = new File(path);

            //If the file is already created
            if (file.exists()) {
                //Then the file is a local one
                url = file.toURI().toURL();
            } else {
                //Otherwise it is a remote file
                url = new URL(path);
            }
        } catch (MalformedURLException e) {
            Logger.getLogger(OntologyFileManager.class.getName()).
                    log(Level.SEVERE, null, e);
        }
        return url;
    }

    /**
     * Gets the Sesame RDFHandler of a ontology file path
     *
     * @param path
     *            the ontology file path
     * @return handler
     *            the Sesame RDFHandler
     */
    public static RDFHandler getRDFHandlerForPath(String ontologyPath) {

        // Initializes the RDFHandler
        RDFHandler handler = null;
        // Creates a file with the path
        File file = new File(ontologyPath);
        try {
            // If file does not exists create one
            if (!file.exists()) {
                file.createNewFile();
            }
            // Calls the output stream of the file
            FileOutputStream stream = new FileOutputStream(file);
            // Creates the handler with RDFXML writer
            handler = new RDFXMLWriter(stream);
        } catch (IOException e) {
            Logger.getLogger(OntologyFileManager.class.getName()).
                    log(Level.SEVERE, null, e);
        }
        return handler;
    }
}
