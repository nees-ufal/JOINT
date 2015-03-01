/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.module.repository.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

/**
 *  Manager for performing repository operations, like create and
 * remove repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class RepositoryManager {

    // METHODS -----------------------------------------------------------------
    /**
     * Creates a OWLIM lite repository in the sesame server
     *
     * @param id
     *            the repository id
     * @param title
     *            the repository title
     * @param url
     *            the sesame server url
     */
    public void createRepository(String id, String title, String serverURL) {

        // Retrieves the OWLIM lite configuration
        RepositoryConfiguration owlimConfig = null;
        SailRepositoryConfig config = new SailRepositoryConfig(owlimConfig.
                getSailConfiguration());

        // Creates a new sesame repository configuration
        RepositoryConfig repo = new RepositoryConfig(id, title, config);
        try {
            repo.validate();

            // Creates a new RemoteRepositoryManager aiming to connect with the server url
            RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
            manager.initialize();

            // Add the configuration to the server
            manager.addRepositoryConfig(repo);
            manager.shutDown();
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a repository with the specified configuration
     * in the sesame server
     *
     * @param id
     *            the repository id
     * @param title
     *            the repository title
     * @param url
     *            the sesame server url
     * @param configuration
     *            a RepositoryConfiguration implementation
     */
    public void createRepository(String id, String title, String serverURL,
            RepositoryConfiguration configuration) {

        // Creates a new SailConfiguration with the given RepositoryConfiguration
        SailRepositoryConfig config = new SailRepositoryConfig(configuration.
                getSailConfiguration());

        // Creates a new sesame repository configuration
        RepositoryConfig repo = new RepositoryConfig(id, title, config);
        try {
            repo.validate();


            // Creates a new RemoteRepositoryManager aiming to connect with the server url
            RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
            manager.initialize();

            // Add the configuration to the server
            manager.addRepositoryConfig(repo);
            manager.shutDown();
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes a repository in the sesame server
     *
     * @param id
     *            the repository id
     * @param url
     *            the sesame server url
     */
    public void removeRepository(String id, String serverURL) {

        try {
            // Creates a new RemoteRepositoryManager to connect with the server url
            RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
            manager.initialize();

            // Removes the repository with its id
            manager.removeRepository(id);
            manager.shutDown();
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Retrieves a list of repositories in the sesame server
     *
     * @param url
     *            the sesame server url
     * @return List
     *            list of repositories
     */
    public List<String> getListOfRepositories(String serverURL) {

        List<String> listResult = new ArrayList<String>();
        try {
            // Creates a new RemoteRepositoryManager to connect with the server url
            RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
            manager.initialize();

            // Removes the repository with its id
            listResult.addAll(manager.getRepositoryIDs());
            manager.shutDown();
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        return listResult;
    }

    /**
     * Erases all data in one repository
     *
     * @param url
     *            the repository URL
     */
    public void clearRepository(String repositoryURL) {

        // Creates a new HTTPRepository instance with the given url
        Repository repo = new HTTPRepository(repositoryURL);

        try {
            // Initializes repository
            repo.initialize();

            // Gets the repository connection
            RepositoryConnection con = repo.getConnection();

            // Clears the repository with any context
            con.clear((Resource) null);

            // Closes any resources
            con.close();
            repo.shutDown();
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
