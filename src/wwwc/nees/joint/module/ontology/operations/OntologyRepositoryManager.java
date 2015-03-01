package wwwc.nees.joint.module.ontology.operations;

import wwwc.nees.joint.module.kao.RepositoryFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;

/**
 * Manager for create, retrieve, update and delete ontologies in the repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OntologyRepositoryManager {

    // VARIABLES ---------------------------------------------------------------
    //Variable to connect with the repository
    private Repository repository;
    //Variable that represents the Connection of the repository
    private RepositoryConnection repoConnection;
    //Variable which creates a factory for several operations
    private ValueFactory factory;

    // CONSTRUCTOR -------------------------------------------------------------
    /**
     * The constructor will start the repository and create a ValueFactory, to
     * make the operations in the persistence
     *
     */
    public OntologyRepositoryManager() {


        //Retrieves the repository in the server
        this.repository = RepositoryFactory.getRepository();

        //Create the factory
        factory = this.repository.getValueFactory();
    }

    // METHODS -----------------------------------------------------------------
    /**
     * Method that persists an ontology file, an RDF, or OWL, in the repository,
     * based on the ontology's URL.
     *
     * @param URL ontology URL
     * @param String ontology URI
     */
    private void persistURL(URL url, String ontologyURI) {

        try {
            //Get repository connection
            this.repoConnection = this.repository.getConnection();

            try {

                //Set autoCommit to false, in order to do a transaction
                this.repoConnection.setAutoCommit(false);

                //create URI from ontology's URI
                URI uri = this.factory.createURI(ontologyURI.toString());

                //Add the ontology, based on the ontology's URL,
                //in the repository specifying the base URI and the context
                this.repoConnection.add(url, ontologyURI, RDFFormat.RDFXML, uri);

                //commit the changes made in the repository
                this.repoConnection.commit();
            } catch (Exception e) {

                //If catch any exception then rollback
                repoConnection.rollback();
                Logger.getLogger(OntologyRepositoryManager.class.getName()).
                        log(Level.SEVERE, null, e);
            } finally {

                //close the repository connection
                this.repoConnection.close();
            }

        } catch (RepositoryException repoExc) {

            Logger.getLogger(OntologyRepositoryManager.class.getName()).
                    log(Level.SEVERE, null, repoExc);
        }
    }

    /**
     * Method that persists an ontology file, an RDF, or OWL, in the repository,
     * based on the ontology's path.
     *
     * @param file
     * @param ontologyURI
     */
    private void persistFile(File file, String ontologyURI) {

        try {
            //Get repository connection
            this.repoConnection = this.repository.getConnection();

            try {
                //Set autoCommit to false, in order to do a transaction
                this.repoConnection.setAutoCommit(false);

                //create URI from ontology's URI
                URI uri = this.factory.createURI(ontologyURI.toString());

                //Add the ontology, based on the ontology's file,
                //in the repository specifying the base URI and the context
                this.repoConnection.add(file, ontologyURI, RDFFormat.RDFXML, uri);

                //commit the changes made in the repository
                this.repoConnection.commit();
            } catch (Exception e) {

                //If catch any exception then rollback
                repoConnection.rollback();
                Logger.getLogger(OntologyRepositoryManager.class.getName()).
                        log(Level.SEVERE, null, e);
            } finally {

                //close the repository connection
                this.repoConnection.close();
            }
        } catch (RepositoryException repoExc) {

            Logger.getLogger(OntologyRepositoryManager.class.getName()).
                    log(Level.SEVERE, null, repoExc);
        }
    }

    /**
     * Add an ontology in the repository
     *
     * @param path the ontology path
     * @param uri the ontology uri
     */
    public void addOntology(String path, String ontologyURI) {
        File file = new File(path);
        if (file.exists()) {
            this.persistFile(file, ontologyURI);
        } else {
            try {
                URL url = new URL(path);
                this.persistURL(url, ontologyURI);
            } catch (MalformedURLException e) {
                System.err.println("File does not exists!");
                Logger.getLogger(OntologyRepositoryManager.class.getName()).
                        log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Retrieves a list of ontologies present in the repository
     *
     * @return List ontologies present in the repository
     */
    public List<String> retrieveListOfOntologies() {
        List<String> result = new ArrayList<String>();
        try {
            //Get repository connection
            this.repoConnection = this.repository.getConnection();

            try {
                //Set autoCommit to false, in order to do a transaction
                this.repoConnection.setAutoCommit(false);

                // Retrieves the statements of the rdf:type owl:ontology
                RepositoryResult<Statement> statements = this.repoConnection.getStatements((Resource) null, RDF.TYPE, OWL.ONTOLOGY, true);

                // Searchs in the result of statements their subjects with the
                //ontology URIs
                while (statements.hasNext()) {
                    Statement st = statements.next();
                    String uri = st.getSubject().stringValue();
                    result.add(uri);
                }

                //commit the changes made in the repository
                this.repoConnection.commit();
            } catch (Exception e) {

                //If catch any exception then rollback
                repoConnection.rollback();
                Logger
                        .getLogger(OntologyRepositoryManager.class
                        .getName()).
                        log(Level.SEVERE, null, e);
            } finally {

                //close the repository connection
                this.repoConnection.close();


            }
        } catch (RepositoryException repoExc) {

            Logger.getLogger(OntologyRepositoryManager.class
                    .getName()).
                    log(Level.SEVERE, null, repoExc);
        }
        return result;
    }

    /**
     * Removes an ontology of the repository
     *
     * @param uri the ontology uri
     */
    public void deleteOntology(String ontologyURI) {
        try {
            //Get repository connection
            this.repoConnection = this.repository.getConnection();

            try {
                //Set autoCommit to false, in order to do a transaction
                this.repoConnection.setAutoCommit(false);

//                create URI from ontology's URI
//                URI uri = this.factory.createURI(ontologyURI.toString());

                StringBuilder builder = new StringBuilder();
                builder.append("CONSTRUCT \n");
                builder.append("FROM <");
                builder.append(ontologyURI);
                builder.append("> \n");
                builder.append("WHERE {?x ?y ?z}");

                GraphQuery prepareGraphQuery = this.repoConnection.prepareGraphQuery(QueryLanguage.SPARQL, builder.toString());
                GraphQueryResult evaluate = prepareGraphQuery.evaluate();
                
                //create URI from ontology's URI
                URI uri = this.factory.createURI(ontologyURI.toString());

                // Removes the entire ontology of the repository
                this.repoConnection.remove(evaluate);

                //commit the changes made in the repository
                this.repoConnection.commit();
            } catch (Exception e) {

                //If catch any exception then rollback
                repoConnection.rollback();
                Logger
                        .getLogger(OntologyRepositoryManager.class
                        .getName()).
                        log(Level.SEVERE, null, e);
            } finally {

                //close the repository connection
                this.repoConnection.close();






            }
        } catch (RepositoryException repoExc) {

            Logger.getLogger(OntologyRepositoryManager.class
                    .getName()).
                    log(Level.SEVERE, null, repoExc);
        }
    }

    /**
     * Retrieves an ontology saving in the specified file path
     *
     * @param path the ontology file path
     * @param uri the ontology uri
     */
    public void retrieveOntology(String ontologyPath, String ontologyURI) {
        try {
            //Get repository connection
            this.repoConnection = this.repository.getConnection();

            try {
                //Set autoCommit to false, in order to do a transaction
                this.repoConnection.setAutoCommit(false);

                RDFHandler handler = OntologyFileManager.getRDFHandlerForPath(ontologyPath);

//                create URI from ontology's URI
//                URI uri = this.factory.createURI(ontologyURI.toString());

                StringBuilder builder = new StringBuilder();
                builder.append("CONSTRUCT \n");
                builder.append("FROM <");
                builder.append(ontologyURI);
                builder.append("> \n");
                builder.append("WHERE {?x ?y ?z}");

                GraphQuery prepareGraphQuery = this.repoConnection.prepareGraphQuery(QueryLanguage.SPARQL, builder.toString());
                prepareGraphQuery.evaluate(handler);

                // Removes the entire ontology of the repository
                // using the context of it
//                this.repoConnection.export(handler, uri);

                //commit the changes made in the repository
                this.repoConnection.commit();
            } catch (Exception e) {

                //If catch any exception then rollback
                repoConnection.rollback();
                Logger
                        .getLogger(OntologyRepositoryManager.class
                        .getName()).
                        log(Level.SEVERE, null, e);
            } finally {

                //close the repository connection
                this.repoConnection.close();
            }
        } catch (RepositoryException repoExc) {

            Logger.getLogger(OntologyRepositoryManager.class
                    .getName()).
                    log(Level.SEVERE, null, repoExc);
        }
    }

    /**
     * Updates an ontology in the repository
     *
     * @param path the ontology file path
     * @param uri the ontology uri
     */
    public void updateOntology(String path, String ontologyURI) {
        this.deleteOntology(ontologyURI);
        this.addOntology(path, ontologyURI);
    }
}
