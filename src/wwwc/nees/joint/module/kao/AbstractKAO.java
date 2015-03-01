package wwwc.nees.joint.module.kao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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
    private RepositoryConnection con;
    // Variable with the desired class to be implemented
    private Class<?> classe;
    // Interface to perform queries in the repository
    private QueryRunner queryRunner;
    // URI of graph to sabe tiples
    private List<URI> contexts;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Class Constructor, starts the <code>Repository</code> and creates a
     * <code>ObjectConnection</code>, to do persistence operations.
     *
     * @param classe the class to be implemented.
     * @param ontologyURI the ontology URI (not the namespace), where
     * persistence operations will be done.
     *
     */
    public <T> AbstractKAO(Class<T> classe) {

        this.classe = classe;

        // Retrieves the repository in the server
        this.repository = RepositoryFactory.getRepository();
//        try {
//            this.con = this.repository.getConnection();
//        } catch (RepositoryException ex) {
//            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
//        }
        // Creates a QueryRunner with SPARQL implementation
        this.queryRunner = new SPARQLQueryRunnerImpl(this.repository);
        this.contexts = new ArrayList<>();
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance in the repository with the specified name.
     *
     * @param instanceName a <code>String</code> with the instance name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @return T the new instance.
     */
    public <T> T create(String ontologyURI, String instanceName) {

        CreateOperations createOpe = new CreateOperations();

        Object ob = null;
        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();
            try {
                con.setAutoCommit(false);
                ob = createOpe.create(ontologyURI, instanceName, this.classe, this.contexts, con);
                con.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Creates a new instance with a unique ID in the repository with the
     * specified prefix.
     *
     * @param instancePrefix a <code>String</code> with the prefix name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @return T the new instance.
     */
    public <T> T createWithUniqueID(String ontologyURI, String instancePrefix) {

        CreateOperations createOpe = new CreateOperations();

        Object ob = null;
        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();
            try {
                con.setAutoCommit(false);
                ob = createOpe.createWithUniqueID(ontologyURI, instancePrefix, this.classe, this.contexts, con);
                con.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param instanceName a <code>String</code> with the instance name.
     */
    public void delete(String ontologyURI, String instanceName) {

        RemoveOperations removeOpe = new RemoveOperations();
        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();

            try {
                //gets connection
                con.setAutoCommit(false);

//                removeOpe.remove(ontologyURI, instanceName, context);
                removeOpe.remove(ontologyURI, instanceName, contexts, con);
                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
    }

    /**
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param instanceName a <code>String</code> with the instance name.
     */
    public <T> void delete(T instance) {
        RemoveOperations removeOpe = new RemoveOperations();
        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();

            try {
                //gets connection
                con.setAutoCommit(false);
//                removeOpe.remove(instance, con);
                removeOpe.remove(instance, contexts, con);
                // Saves the object in the repository
                con.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
    }

    /**
     * Retrieves the desired instance in the repository.
     *
     * @param ontologyURI : a <code>String</code> with the ontology base URI
     * @param instanceName a <code>String</code> with the instance name.
     * @return T the desired instance.
     */
    public <T> T retrieveInstance(String ontologyURI, String instanceName) {

        Object ob = null;

        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();
            RetrieveOperations retrieveOpe = new RetrieveOperations(con);
            try {
                //gets connection
                con.setAutoCommit(false);

                ob = retrieveOpe.retrieveInstance(ontologyURI, instanceName, classe, contexts, con);

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
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
        List<T> listInstances = new ArrayList<>();

        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();
            RetrieveOperations retrieveOpe = new RetrieveOperations(con);
            try {
                //gets connection
                con.setAutoCommit(false);

                listInstances = (List<T>) retrieveOpe.retrieveAllInstances(classe, contexts, con);

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }

        return listInstances;
    }

    /**
     * Saves the uncommitted changes in the repository and close the connection
     * with it.
     *
     */
    public <T> T update(T instance) {
        Object ob = null;
        UpdateOperations updateOpe = new UpdateOperations();

        try {
//            RepositoryConnection conn = this.repository.getConnection();
            con = this.repository.getConnection();
            try {
                //gets connection
                con.setAutoCommit(false);

                ob = updateOpe.updateDettachedInstance(instance, classe, contexts, con);

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Performs queries in the repository, returning a single result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return object <code>Object</code> result.
     */
    public Object executeSPARQLquerySingleResult(String query) {
        return this.queryRunner.executeQueryAsSingleResult(query, contexts);
    }

    /**
     * Performs queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>List<Object></code> a java.util.List with the results.
     */
    public List executeSPARQLqueryResultList(String query) {
        return this.queryRunner.executeQueryAsList(query, contexts);
    }

    /**
     * Performs queries in the repository, returning a java.util.Iterator with
     * the results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    public Iterator<Object> executeQueryAsIterator(String query) {
        return this.queryRunner.executeQueryAsIterator(query, contexts);
    }

    /**
     * Performs SPARQL queries in the repository, returning a boolean with the
     * result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean<Object></code> true or false.
     */
    public boolean executeBooleanQuery(String query) {
        return this.queryRunner.executeBooleanQuery(query);
    }

    /**
     * Changes the class that will be used for CRUD operations.
     *
     * @param classe the class to be implemented.
     */
    public <T> void setClasse(Class<T> classe) {
        this.classe = classe;
    }

    /**
     * Retrieves the current class that will be used for CRUD operations.
     *
     * @return classe the class to be implemented.
     */
    public Class<?> retrieveClass() {
        return this.classe;
    }

    public List<URI> getContexts() {
        return contexts;
    }

    public void addContext(URI context) {
        contexts.add(context);
    }

    public void setContexts(List<URI> contexts) throws IllegalArgumentException {
        this.contexts = contexts;
    }

}