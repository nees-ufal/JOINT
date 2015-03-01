package wwwc.nees.joint.module.repository.operations;

import java.util.List;

/**
 *  Interface for providing general repository operations
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public interface RepositoryOperations {

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
    public void createRepository(String id, String title, String serverURL);

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
    public void createRepository(String id, String title, String serverURL, RepositoryConfiguration config);

    /**
     * Removes a repository in the sesame server
     *
     * @param id
     *            the repository id
     * @param title
     *            the repository title
     * @param url
     *            the sesame server url
     */
    public void removeRepository(String id, String serverURL);

    /**
     * Retrieves a list of repositories in the sesame server
     *
     * @param url
     *            the sesame server url
     * @return List
     *            list of repositories
     */
    public List<String> getListOfRepositories(String serverURL);
    
    /**
     * Erases all data in one repository
     *
     * @param url
     *            the repository URL
     */
    public void clearRepository(String repositoryURL);

    /**
     * Copies all data from one repository to others.
     * Be carefull, this will erase all data of the others.
     *
     * @param mainURL
     *            the main repository URL with the data to be copied
     * @param repositories
     *            the repositories urls where the data will be copied to
     */
    public void copyRepository(String mainRepository, String... urls);

    /**
     * Creates a backup file of the repository with the path specified
     *
     * @param url
     *            the main repository URL
     * @param path
     *            the backup file path
     */
    public void backupRepository(String repositoryURL, String filePath);

    /**
     * Restores the backup file to the repository. This will erase any
     * previous data in the repository
     *
     * @param url
     *            the main repository URL
     * @param path
     *            the backup file path
     */
    public void restoreBackup(String repositoryURL, String filePath);

    /**
     * Exports each ontology in the repository to a folder
     *
     * @param url
     *            the repository URL
     * @param path
     *            the folder path where the ontologies will be saved
     */
    public void exportRepositoryOntologies(String repositoryURL, String folderPath);
}
