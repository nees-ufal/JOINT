package br.ufal.ic.joint.module.kao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.result.Result;

/**
 * Abstract class KAO, for operations in the persistence with SESAME and Alibaba
 * 
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public abstract class AbstractKAO {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    private Repository repository;
    // Variable to do operations in the repository
    private ObjectConnection connection;
    // Variable with the desired class to be implemented
    private Class<?> classe;
    // String with the ontology URI
    private String ontologyURI;
    // Interface to perform queries in the repository
    private QueryRunner queryRunner;
    // Controls the transactions
    private TransactionHandler transactionHandler;
    // Performs context operations
    private ContextHandler contextHandler;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Class Constructor, starts the <code>Repository</code> and creates a
     * <code>ObjectConnection</code>, to do persistence operations.
     *
     * @param classe
     *            the class to be implemented.
     * @param ontologyURI
     *            the ontology URI (not the namespace), where persistence
     *            operations will be done.
     *
     */
    public <T> AbstractKAO(Class<T> classe, String ontologyURI) {

        this.classe = classe;

        this.ontologyURI = ontologyURI;

        // Retrieves the repository in the server
        this.repository = RepositoryFactory.getRepository();

        try {

            // Creates the connection with the repository
            ObjectRepositoryFactory factory = new ObjectRepositoryFactory();
            this.connection = factory.createRepository(repository).getConnection();

            // Creates a handler for context operations
            this.contextHandler = new ContextHandler(this.connection);

            // Sets the connection context
            this.contextHandler.addContext(this.ontologyURI);

            // Creates a QueryRunner with SPARQL implementation
            this.queryRunner = new SPARQLQueryRunnerImpl(this.connection);

            // Creates a handler for transactions operations
            this.transactionHandler = new TransactionHandler(this.connection);

            // Starts the transaction
            this.transactionHandler.beginTransaction();

        } catch (RepositoryConfigException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (RepositoryException e) {
            Logger.getLogger(AbstractKAO.class.getName()).
                    log(Level.SEVERE, null, e);
        }
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance in the repository with the specified name.
     *
     * @param instanceName
     *            a <code>String</code> with the instance name.
     * @return T the new instance.
     */
    public <T> T create(String instanceName) {

        long tempo1 = System.currentTimeMillis();
        System.out.println(tempo1);
        
        // Creates an object with the URI and the .class
        Object ob = this.connection.getObjectFactory().createObject(
                this.ontologyURI + "#" + instanceName);
        long tempo2 = System.currentTimeMillis();
        System.out.println("ObjectFactory - " +  ((tempo2-tempo1)/1000));
        tempo1 = tempo2;
        
        try {

            // Saves the object in the repository
            this.connection.addObject(ob);
            tempo2 = System.currentTimeMillis();
            System.out.println("Adicionando Objeto - " + ((tempo2-tempo1)/1000));


            ob = this.connection.addDesignation(ob, classe);
            tempo1 = tempo2;
            tempo2 = System.currentTimeMillis();
            System.out.println("Designation - " + ((tempo2-tempo1)/1000));

        } catch (Exception e) {
            // If throws any exception rollback
            try {
                this.connection.rollback();
                this.connection.close();
            } catch (RepositoryException e1) {
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e1);
            }
        }
        return (T) ob;
    }

    /**
     *
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param instanceName
     *            a <code>String</code> with the instance name.
     */
    public void delete(String instanceName) {

        // Creates the instance resource
        URI instance = this.connection.getValueFactory().createURI(
                this.ontologyURI + "#" + instanceName);

        // Retrieves context
        URI context = this.contextHandler.getContext();

        // Removes the instance
        try {
            this.connection.remove(instance, null, null, context);
            this.connection.remove((Resource) null, null, instance, context);
        } catch (Exception e) {
            // If throws any exception rollback
            try {
                this.transactionHandler.rollback();
            } catch (RepositoryException e1) {
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e1);
            }
        }
    }

    /**
     * Retrieves the desired instance in the repository.
     *
     * @param instanceName
     *            a <code>String</code> with the instance name.
     * @return T the desired instance.
     */
    public <T> T retrieveInstance(String instanceName) {

        Object ob = null;

        // Creates the instance URI
        URI instance = this.connection.getValueFactory().createURI(
                this.ontologyURI + "#" + instanceName);

        // Retrieves the desired instance with the URI and the .class
        try {
            ob = this.connection.getObject(this.classe, instance);
        } catch (Exception e) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
        }

        return (T) ob;
    }

    /**
     * Retrieves all the instances of the class, passed in the constructor.
     *
     * @return <code>List<T></code> a List with the instances.
     */
    public <T> List<T> retrieveAllInstances() {

        // Creates a new java.util.List
        List<T> listInstances = new ArrayList<T>();

        try {
            // Retrieves all the instances of this class
            @SuppressWarnings("unchecked")
            Result<T> instances = (Result<T>) this.connection.getObjects(this.classe);
            // Changes the result to a java.util.List
            listInstances = instances.asList();
        } catch (Exception e) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
        }

        return listInstances;
    }

    /**
     * Saves the uncommitted changes in the repository and close the connection
     * with it.
     *
     */
    public void save() {
        try {
            // Gets the transaction and commit.
            this.transactionHandler.commitChanges();

        } catch (Exception e) {
            // If throws any exception rollback
            try {
                this.transactionHandler.rollback();

            } catch (RepositoryException e1) {
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e1);
            }
        } finally {
            try {
                this.connection.close();
            } catch (RepositoryException ex) {
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Changes the ontology URI, where the persistence operations will be done.
     *
     * @param ontologyURI
     *            the <code>String</code> with the new URI.
     */
    public void changeOntologyURI(String ontologyURI) {

        // Updates the ontology URI
        this.ontologyURI = ontologyURI;

        // Updates context
        this.contextHandler.updateContext(ontologyURI);
    }

    /**
     * Performs queries in the repository, returning a single result.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return object <code>Object</code> result.
     */
    public Object executeQueryAsSingleResult(String query) {
        return this.queryRunner.executeQueryAsSingleResult(query);
    }

    /**
     * Performs queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return <code>List<Object></code>
     *            a java.util.List with the results.
     */
    public List<Object> executeQueryAsList(String query) {
        return this.queryRunner.executeQueryAsList(query);
    }

    /**
     * Performs queries in the repository, returning a java.util.Iterator with
     * the results.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    public Iterator<Object> executeQueryAsIterator(String query) {
        return this.queryRunner.executeQueryAsIterator(query);
    }

    /**
     * Performs SPARQL queries in the repository, returning a boolean
     * with the result.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean<Object></code> true or false.
     */
    public boolean executeBooleanQuery(String query) {
        return this.queryRunner.executeBooleanQuery(query);
    }

    /**
     * Changes the class that will be used for CRUD operations.
     *
     * @param classe
     *            the class to be implemented.
     */
    protected <T> void setClass(Class<T> classe) {
        this.classe = classe;
    }
}
