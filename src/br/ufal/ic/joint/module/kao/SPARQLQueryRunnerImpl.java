package br.ufal.ic.joint.module.kao;

import br.ufal.ic.joint.model.OWLUris;
import br.ufal.ic.joint.model.RDFUris;
import br.ufal.ic.joint.model.SWRLUris;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.result.Result;

/**
 * Class which implements QueryRunner Interface for performing SPARQL queries in
 * the repository.
 * 
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class SPARQLQueryRunnerImpl implements QueryRunner {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    private ObjectConnection connection;

    // Default namespaces to use in the SPARQL queries
    public static final String DEFAULT_PREFIXES = "PREFIX rdf:<" + RDFUris.RDF
            + ">\n PREFIX owl:<" + OWLUris.OWL
            + ">\n PREFIX rdfs:<" + RDFUris.RDFS
            + ">\n PREFIX xsd:<" + RDFUris.XSD
            + ">\n PREFIX swrl:<" + SWRLUris.SWRL + ">\n";

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Simple constructor that receives an object connection of the repository.
     *
     * @param connection
     *            the <code>ObjectConnection</code> with the repository.
     *
     */
    public SPARQLQueryRunnerImpl(ObjectConnection connection) {
        this.connection = connection;
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Performs SPARQL queries in the repository, returning a single result.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return object <code>Object</code> result.
     */
    @Override
    public Object executeQueryAsSingleResult(String query) {
        // Creates a java.util.List
        List<Object> resultList = new ArrayList<Object>();

        ObjectQuery objectQuery;
        try {
            // Creates the query based on the parameter
            objectQuery = this.connection.prepareObjectQuery(query);

            // Performs the query
            Result<?> result = objectQuery.evaluate();

            // Changes the result to a java.util.List
            resultList = (List<Object>) result.asList();
        } catch (Exception e) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, e);
        }
        // Gets the first and only index
        return resultList.get(0);
    }

    /**
     * Performs SPARQL queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return <code>List<Object></code> a java.util.List with the results.
     */
    @Override
    public List<Object> executeQueryAsList(String query) {
        // Creates a java.util.List
        List<Object> resultList = new ArrayList<Object>();

        ObjectQuery objectQuery;
        try {
            // Creates the query based on the parameter
            objectQuery = this.connection.prepareObjectQuery(query);

            // Performs the query
            Result<?> result = objectQuery.evaluate();

            // Changes the result to a java.util.List
            resultList = (List<Object>) result.asList();
        } catch (Exception e) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, e);
        }

        return resultList;
    }

    /**
     * Performs SPARQL queries in the repository, returning a java.util.Iterator
     * with the results.
     *
     * @param query
     *            the <code>String</code> with the query to be performed.
     *
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    @Override
    public Iterator<Object> executeQueryAsIterator(String query) {
        // Creates a java.util.List
        List<Object> resultList = new ArrayList<Object>();

        ObjectQuery objectQuery;
        try {
            // Creates the query based on the parameter
            objectQuery = this.connection.prepareObjectQuery(query);

            // Performs the query
            Result<?> result = objectQuery.evaluate();

            // Changes the result to a java.util.List
            resultList = (List<Object>) result.asList();
        } catch (Exception e) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, e);
        }

        //Gets the iterator of the list
        return resultList.iterator();
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
    @Override
    public boolean executeBooleanQuery(String query) {

        boolean result = false;

        BooleanQuery objectQuery;
        try {
            // Creates the query based on the parameter
            objectQuery = this.connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);

            // Performs the query
            result = objectQuery.evaluate();

        } catch (Exception e) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, e);
        }

        //Gets the iterator of the list
        return result;
    }
}
