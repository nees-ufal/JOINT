package wwwc.nees.joint.module.ontology.operations;

import java.util.List;

/**
 * Class which implements the OntologyOperations interface
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OntologyOperationsImpl implements OntologyOperations {

    // VARIABLES ---------------------------------------------------------------
    //Variable for performing ontologies operations in the repository
    private final OntologyRepositoryManager ontologyMgr;

    // CONSTRUCTOR -------------------------------------------------------------
    public OntologyOperationsImpl() {
        this.ontologyMgr = new OntologyRepositoryManager();
    }

    // METHODS -----------------------------------------------------------------
    /**
     * Add an ontology in the repository
     *
     * @param path the ontology path
     * @param ontologyURI the ontology uri
     */
    @Override
    public void addOntology(String path, String ontologyURI) {
        this.ontologyMgr.addOntology(path, ontologyURI);
    }

    /**
     * Removes an ontology of the repository
     *
     * @param ontologyURI the ontology uri
     */
    @Override
    public void deleteOntology(String ontologyURI) {
        this.ontologyMgr.deleteOntology(ontologyURI);
    }

    /**
     * Retrieves an ontology saving in the specified file path
     *
     * @param path the ontology file path
     * @param ontologyURI the ontology uri
     */
    @Override
    public void retrieveOntology(String path, String ontologyURI) {
        this.ontologyMgr.retrieveOntology(path, ontologyURI);
    }

    /**
     * Retrieves a list of ontologies present in the repository
     *
     * @return List ontologies present in the repository
     */
    @Override
    public List<String> retrieveListOfOntologies() {
        return this.ontologyMgr.retrieveListOfOntologies();
    }

    /**
     * Updates an ontology in the repository
     *
     * @param path the ontology file path
     * @param ontologyURI the ontology uri
     */
    @Override
    public void updateOntology(String path, String ontologyURI) {
        this.ontologyMgr.updateOntology(path, ontologyURI);
    }

    /**
     * Retrieves the OntologyCompiler to generate java code from ontologies
     *
     * @param path the jar file path wich the java code will be saved
     * @param ontologiesURLs a list with the ontologies URLs
     * @return compiler the compiler of ontologies
     */
    @Override
    public OntologyCompiler getOntologyCompiler(String path, List<String> ontologiesURLs) {
        return new OntologyCompiler(path, ontologiesURLs);
    }
}
