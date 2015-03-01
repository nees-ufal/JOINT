package br.ufal.ic.joint.module.kao;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Factory to get an instance of the Repository specified in the configuration
 * properties
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class RepositoryFactory {

    // VARIABLES
    // -------------------------------------------------------------------------
    // The repository static variable
    private static Repository repository = null;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets an instance of Repository
     *
     * @return repository
     *              a Repository object already initialized
     */
    public static synchronized Repository getRepository() {

        // If repository is null
        if (repository == null) {

            // Reads the configuration file
//            Configuration c = Configuration.getInstance("Repository");

            // Gets the value for the repository key
            String repositoryURL = "http://localhost:9090/openrdf-sesame/repositories/bbcExemplo";//c.getValue("Repository_URL");

            // Creates a HTTPRepository object
            repository = new HTTPRepository(repositoryURL);
            try {
                repository.initialize();
            } catch (RepositoryException ex) {
                Logger.getLogger(RepositoryFactory.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
        return repository;
    }
}
