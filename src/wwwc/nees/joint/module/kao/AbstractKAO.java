package wwwc.nees.joint.module.kao;

import wwwc.nees.joint.module.kao.retrieve.QueryRunner;
import wwwc.nees.joint.module.kao.retrieve.RetrieveOperations;
import wwwc.nees.joint.module.kao.retrieve.SPARQLQueryRunnerImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UpdateExecutionException;
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
    private final Repository repository;
    // Variable to do operations in the repository
    private RepositoryConnection connection;
    // Variable with the desired class to be implemented
    private Class<?> classe;
    // Interface to perform queries in the repository
    private final QueryRunner queryRunner;
    // URI[] of graph to save triples
    private URI[] contexts;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Class Constructor, starts the <code>Repository</code> and creates a
     * <code>ObjectConnection</code>, to do persistence operations.
     *
     * @param classe the class to be implemented.
     *
     */
    public <T> AbstractKAO(Class<T> classe) {

        this.classe = classe;

        // Retrieves the repository in the server
        this.repository = RepositoryFactory.getRepository();
        // Creates a QueryRunner with SPARQL implementation
        this.queryRunner = new SPARQLQueryRunnerImpl();
        this.contexts = new URI[]{};
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance in the repository with the specified name.
     *
     * @param instanceName a <code>String</code> with the instance name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return T the new instance.
     */
    public <T> T create(String ontologyURI, String instanceName, java.net.URI... contexts) {
        return create(ontologyURI + instanceName, contexts);
    }

    /**
     * Creates a new instance in the repository with the specified uri.
     *
     * @param instanceURI a <code>String</code> with the instance uri.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return T the new instance.
     */
    public <T> T create(String instanceURI, java.net.URI... contexts) {
        setContexts(contexts);

        Object ob = null;
        try {
            connection = this.repository.getConnection();
            try {
                connection.begin();

                ob = new CreateOperations().create(connection, instanceURI, this.classe, this.getContexts());

                connection.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Creates a new instance with a unique ID in the repository with the
     * specified prefix.
     *
     * @param instancePrefix a <code>String</code> with the prefix name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return T the new instance.
     */
    public <T> T createWithUniqueID(String ontologyURI, String instancePrefix, java.net.URI... contexts) {

        setContexts(contexts);

        Object ob = null;
        try {
            connection = this.repository.getConnection();
            try {
                connection.begin();
                ob = new CreateOperations().createWithUniqueID(connection, ontologyURI, instancePrefix, this.classe, this.getContexts());
                connection.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param ontologyURI is the URI from the ontology which represents the
     * instance.
     * @param instanceName a <code>String</code> with the instance name.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     */
    public void delete(String ontologyURI, String instanceName, java.net.URI... contexts) {
        delete(ontologyURI + instanceName, contexts);
    }

    public void delete(String instanceURI, java.net.URI... contexts) {
        setContexts(contexts);

        try {
            connection = this.repository.getConnection();

            try {
                //gets connection
                connection.begin();
                //removes the quads that have the corresponding subject 
//                removeOpe.remove(ontologyURI, instanceName, con, this.getContexts());
                new RemoveOperations().remove_SPARQLUpdate(connection, instanceURI, this.getContexts());

                // Saves the object in the repository
                connection.commit();
            } catch (RepositoryException | MalformedQueryException | UpdateExecutionException e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
        }
    }

    /**
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param instance an object T with the instance
     * @param contexts the graphs in which the instance is removed.
     */
    public <T> void delete(T instance, java.net.URI... contexts) {
        try {
            setContexts(contexts);

            connection = this.repository.getConnection();

            try {
                //gets connection
                connection.begin();

                new RemoveOperations().remove_SPARQLUpdate(connection, instance.toString(), this.getContexts());
//                removeOpe.remove(instance, con, this.getContexts());

                // Saves the object in the repository
                connection.commit();

            } catch (RepositoryException | MalformedQueryException | UpdateExecutionException e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();

            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
        }
    }

    /**
     * Retrieves the desired instance in the repository.
     *
     * @param ontologyURI : a <code>String</code> with the ontology base URI
     * @param instanceName a <code>String</code> with the instance name.
     * @param contexts the graphs in which the instance is removed.
     * @return T the desired instance.
     */
    public <T> T retrieveInstance(String ontologyURI, String instanceName, java.net.URI... contexts) {
        return retrieveInstance(ontologyURI + instanceName, contexts);
    }

    /**
     * Retrieves the desired instance in the repository.
     *
     * @param instanceURI a <code>String</code> with the instance name.
     * @param contexts the graphs in which the instance is removed.
     * @return T the desired instance.
     */
    public <T> T retrieveInstance(String instanceURI, java.net.URI... contexts) {
        Object ob = null;
        try {
            setContexts(contexts);
            connection = this.repository.getConnection();
            try {
                //gets connection
                connection.begin();

                ob = new RetrieveOperations().retrieveInstance(connection, instanceURI, classe, this.getContexts());

                // Saves the object in the repository
                connection.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();

            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Retrieves all the instances of the class, passed in the constructor.
     *
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return <code>List</code> a List with the instances.
     */
    public <T> List<T> retrieveAllInstances(java.net.URI... contexts) {
        setContexts(contexts);
        // Creates a new java.util.List
        List<T> listInstances = new ArrayList<>();

        try {
            connection = this.repository.getConnection();
            try {
                //gets connection
                connection.begin();

                listInstances = (List<T>) new RetrieveOperations().retrieveAllInstances(connection, classe, this.getContexts());

                // Saves the object in the repository
                connection.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();

            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
        }

        return listInstances;
    }

    /**
     * Saves the uncommitted changes in the repository and close the connection
     * with it.
     *
     * @param instance is the object which will be updated
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be updated.
     */
    public <T> T update(T instance, java.net.URI... contexts) {
        setContexts(contexts);
        Object ob = null;

        try {
            //gets connection
            connection = this.repository.getConnection();
            try {
                connection.begin();

                ob = new UpdateOperations().updateDettachedInstance(connection, instance, classe, this.getContexts());
                
                // Saves the object in the repository
                connection.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                connection.rollback();
                Logger
                        .getLogger(AbstractKAO.class
                                .getName()).log(Level.SEVERE, null, e);
            } finally {
                connection.close();

            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class
                    .getName()).log(Level.SEVERE, null, eR);
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
        Object object = null;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                object = this.queryRunner.executeQueryAsSingleResult(connection, query);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    /**
     * Performs queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query the <code>String</code> with the query to be performed.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     *
     * @return <code>List</code> a java.util.List with the results.
     */
    public List executeSPARQLqueryResultList(String query, java.net.URI... contexts) {
        setContexts(contexts);
        List<Object> objects = null;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                objects = this.queryRunner.executeQueryAsList(connection, query, this.getContexts());
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return objects;
    }

    public List executeSPARQLqueryResultList2(String query, java.net.URI... contexts) {
        setContexts(contexts);
        List<Object> objects = null;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                objects = this.queryRunner.executeQueryAsList2(connection, query, this.getContexts());
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objects;
    }

    public String executeSPARQLtupleQueryAsJSONString(String query) {
        String results = null;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                results = this.queryRunner.executeTupleQueryAsJSON(connection, query).replaceAll("=", ":");
                connection.commit();
            } catch (RepositoryException | MalformedQueryException | QueryEvaluationException | TupleQueryResultHandlerException ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Performs query in the repository, returning the results in an adapted
     * format from JSON-LD specification
     *
     * @param query the String with the query to be performed.
     * @return a JSON as String
     */
    public JSONObject executeSPARQLgraphQueryAsJSONLDString(String query) {
        return executeSPARQLgraphQueryAsJSONLDString(query, true);
    }

    /**
     * Performs query in the repository, returning the results in an adapted
     * format from JSON-LD specification
     *
     * @param query the String with the query to be performed.
     * @param graphAsJSONArray defines if the <b><code>@graph</code> key</b> is
     * a JSON Array. If value is true, then is an array, else, is a JSON Object
     * where the <b><code>@id</code> key</b> are the keys of the objects. <b>By
     * default it's <code>true</code></b>.
     * @return a JSON as String
     */
    public JSONObject executeSPARQLgraphQueryAsJSONLDString(String query, boolean graphAsJSONArray) {
        JSONObject results = null;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                results = this.queryRunner.executeGraphQueryAsJSONLD(connection, query, graphAsJSONArray);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Performs queries in the repository, returning a java.util.Iterator with
     * the results.
     *
     * @param query the <code>String</code> with the query to be performed.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     *
     * @return <code>Iterator</code> a java.util.List with the results.
     */
    public Iterator executeQueryAsIterator(String query, java.net.URI... contexts) {
        //Converter to Iterator and return
        return executeSPARQLqueryResultList(query, contexts).iterator();
    }

    /**
     * Performs SPARQL queries in the repository, returning a boolean with the
     * result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean</code> true or false.
     */
    public boolean executeBooleanQuery(String query) {
        boolean result = false;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                result = this.queryRunner.executeBooleanQuery(connection, query);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Performs SPARQL update queries in the repository, returning a boolean
     * true if the query was performed with successful or false otherwise.
     *
     * @param query the <code>String</code> with the query to be performed.
     */
    public void executeSPARQLUpdateQuery(String query) {
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                this.queryRunner.executeUpdateQuery(connection, query);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets contexts from quadstore.
     *
     * @param containsTerm is a regular expression that must be matched with the
     * contexts
     * @return an URI list
     * @throws NullPointerException occurs when the containsTerm is null.
     */
    public List<Object> getDatasets(String containsTerm) {
        List<Object> datasets = null;
        try {
            //retrieves a connection with the repository
            connection = this.repository.getConnection();
            try {
                //starts a transaction
                connection.begin();
                //performs the query
                datasets = new RetrieveOperations().getDatasets(connection, containsTerm);
                connection.commit();
            } catch (RepositoryException | NullPointerException ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                connection.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connection.close();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return datasets;
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

    private URI[] getContexts() {
        return contexts;
    }

    public void setContexts(java.net.URI[] contexts) {
        this.contexts = new URI[]{};
        if (contexts != null) {
            List<URI> uris = new ArrayList<>();
            for (java.net.URI uri : contexts) {
                uris.add(new URIImpl(uri.toString()));
            }
            this.contexts = uris.toArray(this.contexts);
        }
    }
}
