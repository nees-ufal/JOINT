package wwwc.nees.joint.facade;

import wwwc.nees.joint.module.ontology.operations.OntologyCompiler;
import wwwc.nees.joint.module.ontology.operations.OntologyOperations;
import wwwc.nees.joint.module.ontology.operations.OntologyOperationsImpl;
import wwwc.nees.joint.module.reasoner.RuleEngine;
import wwwc.nees.joint.module.reasoner.SWRLEngine;
import wwwc.nees.joint.module.repository.operations.RepositoryConfiguration;
import wwwc.nees.joint.module.repository.operations.RepositoryOperations;
import wwwc.nees.joint.module.repository.operations.RepositoryOperationsImpl;
import java.util.List;

/**
 *  Main class for performing several operations with ontologies and
 * repositories
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class RepositoryFacade{

    // VARIABLES
    // -------------------------------------------------------------------------
    // Run rules in the repository
    private RuleEngine ruleEngine;
    // Handle repository operations
    private RepositoryOperations repoOper;
    // Handle ontologies operations
    private OntologyOperations ontOper;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Intatiates the required variables
     *
     */
    public RepositoryFacade() {
        this.ontOper = new OntologyOperationsImpl();
        this.repoOper = new RepositoryOperationsImpl();
        this.ruleEngine = new SWRLEngine();
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Performs rules in the specified repository
     *
     * @param repositoryURL
     *            the Repository URL
     * @return statements
     *            the Number of inferred Statements
     */
    public int performRulesInRepository(String repositoryURL) {
        return this.ruleEngine.performRulesInRepository(repositoryURL);
    }

    /**
     * Performs rules in the repository specified by configuration.properties
     *
     * @return statements
     *            the Number of inferred Statements
     */
    public int performRulesInRepository() {
        return this.ruleEngine.performRulesInRepository();
    }

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
        this.repoOper.createRepository(id, title, serverURL);
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
            RepositoryConfiguration config) {
        this.repoOper.createRepository(id, title, serverURL, config);
    }

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
    public void removeRepository(String id, String serverURL) {
        this.repoOper.removeRepository(id, serverURL);
    }
    
    /**
     * Retrieves a list of repositories in the sesame server
     *
     * @param url
     *            the sesame server url
     * @return List
     *            list of repositories
     */
    public List<String> getListOfRepositories(String serverURL){
        return this.repoOper.getListOfRepositories(serverURL);
    }

    /**
     * Erases all data in one repository
     *
     * @param url
     *            the repository URL
     */
    public void clearRepository(String repositoryURL) {
        this.repoOper.clearRepository(repositoryURL);
    }

    /**
     * Copies all data from one repository to others.
     * Be carefull, this will erase all data of the others.
     *
     * @param mainURL
     *            the main repository URL with the data to be copied
     * @param repositories
     *            the repositories urls where the data will be copied to
     */
    public void copyRepository(String mainRepository, String... urls) {
        this.repoOper.copyRepository(mainRepository, urls);
    }

    /**
     * Creates a backup file of the repository with the path specified
     *
     * @param url
     *            the main repository URL
     * @param path
     *            the backup file path
     */
    public void backupRepository(String repositoryURL, String filePath) {
        this.repoOper.backupRepository(repositoryURL, filePath);
    }

    /**
     * Restores the backup file to the repository. This will erase any
     * previous data in the repository
     *
     * @param url
     *            the main repository URL
     * @param path
     *            the backup file path
     */
    public void restoreBackup(String repositoryURL, String filePath) {
        this.repoOper.restoreBackup(repositoryURL, filePath);
    }

    /**
     * Exports each ontology in the repository to a folder
     *
     * @param url
     *            the repository URL
     * @param path
     *            the folder path where the ontologies will be saved
     */
    public void exportRepositoryOntologies(String repositoryURL, String folderPath) {
        this.repoOper.exportRepositoryOntologies(repositoryURL, folderPath);
    }

    /**
     * Add an ontology in the repository
     *
     * @param path
     *            the ontology path
     * @param uri
     *            the ontology uri
     */
    public void addOntology(String path, String ontologyURI) {
        this.ontOper.addOntology(path, ontologyURI);
    }

    /**
     * Removes an ontology of the repository
     *
     * @param uri
     *            the ontology uri
     */
    public void deleteOntology(String ontologyURI) {
        this.ontOper.deleteOntology(ontologyURI);
    }

    /**
     * Retrieves an ontology saving in the specified file path
     *
     * @param path
     *            the ontology file path
     * @param uri
     *            the ontology uri
     */
    public void retrieveOntology(String path, String ontologyURI) {
        this.ontOper.retrieveOntology(path, ontologyURI);
    }
    
    /**
     * Retrieves a list of ontologies present in the repository
     *
     * @return List ontologies present in the repository
     */
    public List<String> retrieveListOfOntologies(){
        return this.ontOper.retrieveListOfOntologies();
    }

    /**
     * Updates an ontology in the repository
     *
     * @param path
     *            the ontology file path
     * @param uri
     *            the ontology uri
     */
    public void updateOntology(String path, String ontologyURI) {
        this.ontOper.updateOntology(path, ontologyURI);
    }

    /**
     * Checks the ontology consistency
     *
     * @param path
     *            the ontology file path
     * @return boolean
     *            true if the consistency is ok, else false
     */
    public boolean checkOntologyConsistency(String path) {
        return this.ontOper.checkOntologyConsistency(path);
    }

    /**
     * Retrieves the OntologyCompiler to generate java code from ontologies
     *
     * @param path
     *            the jar file path wich the java code will be saved
     * @param urls
     *            a list with the ontologies URLs
     * @return compiler
     *            the compiler of ontologies
     */
    public OntologyCompiler getOntologyCompiler(String path, List<String> ontologiesURLs) {
        return this.ontOper.getOntologyCompiler(path, ontologiesURLs);
    }
}
