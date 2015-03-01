package wwwc.nees.joint.module.kao;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
//import virtuoso.sesame2.driver.VirtuosoRepository;

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
     * @return repository a Repository object already initialized
     */
    public static synchronized Repository getRepository() {

        Configuration c = Configuration.getInstance("Repository");
        
        String classPath = c.getValue("Repository_ClassPath");
        
        RepositoryConfig config = null;
        try {
            config = (RepositoryConfig) Class.forName(classPath).newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RepositoryFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(RepositoryFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RepositoryFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // If repository is null
        if (repository == null) {

            // Creates a new Repository object
            repository = config.createNewRepository();
            try {
                repository.initialize();
                //gets the first connection that delays
                repository.getConnection().close();
            } catch (RepositoryException ex) {
                Logger.getLogger(RepositoryFactory.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
        return repository;
    }

    /**
     * Gets an instance of Repository
     *
     * @return repository a Repository object already initialized
     */
    public static synchronized Repository configureRepository(Repository repo) {

        // If repository is null
        if (repository == null) {

            // Creates a HTTPRepository object
            repository = repo;
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
