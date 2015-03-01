package wwwc.nees.joint.module.ontology.operations;

import java.util.List;

/**
 *  Class which implements the OntologyOperations interface
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OntologyOperationsImpl implements OntologyOperations {

    // VARIABLES ---------------------------------------------------------------
    //Variable for performing ontologies operations in the repository
    private OntologyRepositoryManager ontologyMgr;
    //Variable for validating ontologies
    private OWLValidation validation;

    // CONSTRUCTOR -------------------------------------------------------------
    public OntologyOperationsImpl() {
        this.ontologyMgr = new OntologyRepositoryManager();
        this.validation = new OWLValidation();
    }

    // METHODS -----------------------------------------------------------------
    /**
     * Add an ontology in the repository
     *
     * @param path
     *            the ontology path
     * @param uri
     *            the ontology uri
     */
    public void addOntology(String path, String ontologyURI) {
        this.ontologyMgr.addOntology(path, ontologyURI);
    }

    /**
     * Removes an ontology of the repository
     *
     * @param uri
     *            the ontology uri
     */
    public void deleteOntology(String ontologyURI) {
        this.ontologyMgr.deleteOntology(ontologyURI);
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
        this.ontologyMgr.retrieveOntology(path, ontologyURI);
    }
    
    /**
     * Retrieves a list of ontologies present in the repository
     *
     * @return List ontologies present in the repository
     */
    public List<String> retrieveListOfOntologies() {
        return this.ontologyMgr.retrieveListOfOntologies();
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
        this.ontologyMgr.updateOntology(path, ontologyURI);
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
        return this.validation.checkOntologyConsistency(path);
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
        return new OntologyCompiler(path, ontologiesURLs);
    }
}
