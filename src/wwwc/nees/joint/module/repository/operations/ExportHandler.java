/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wwwc.nees.joint.module.repository.operations;

import wwwc.nees.joint.module.ontology.operations.OntologyFileManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *  Handler for export ontologies from a repository to a folder
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class ExportHandler {

    // METHODS -----------------------------------------------------------------
    /**
     * Exports each ontology in the repository to a folder
     *
     * @param url
     *            the repository URL
     * @param path
     *            the folder path where the ontologies will be saved
     */
    public void exportRepositoryOntologies(String repositoryURL, String folderPath){

        // Gets the repository instance with the given url
        Repository repo = new HTTPRepository(repositoryURL);
        try {
            // Gets the repository connection
            repo.initialize();
            RepositoryConnection con = repo.getConnection();

            // Retrieves the contexts presents in the repository
            RepositoryResult<Resource> contexts = con.getContextIDs();

            while(contexts.hasNext()){

                // for each context export the context to a new local file
                Resource resource = contexts.next();
                String localName = resource.toString();
                String filePath = folderPath + localName;
                RDFHandler handler = OntologyFileManager.getRDFHandlerForPath(filePath);
                try {
                    // Exports the ontology
                    con.export(handler, resource);
                } catch (RDFHandlerException ex) {
                    Logger.getLogger(ExportHandler.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }

        } catch (RepositoryException ex) {
            Logger.getLogger(ExportHandler.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

    }

}
