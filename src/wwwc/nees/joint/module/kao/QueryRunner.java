package wwwc.nees.joint.module.kao;

import java.util.Iterator;
import java.util.List;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Interface for query operations in the repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public interface QueryRunner {

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Performs queries in the repository, returning a single result.
     *
     * @param connection receives an object of connection with the repository
     * @param query the <code>String</code> with the query to be performed.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return object <code>Object</code> result.
     */
    public Object executeQueryAsSingleResult(RepositoryConnection connection, String query, URI... contexts) throws Exception;

    /**
     * Performs queries in the repository, returning a java.util.List of
     * results.
     *
     * @param connection receives an object of connection with the repository
     * @param query the <code>String</code> with the query to be performed.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return <code>List</code> a java.util.List with the results.
     */
    public List<Object> executeQueryAsList(RepositoryConnection connection, String query, URI... contexts) throws Exception;

    public List<Object> executeQueryAsList2(RepositoryConnection connection, String query, URI... contexts) throws Exception;

    public String executeTupleQueryAsJSON(RepositoryConnection connection, String query)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException,
            TupleQueryResultHandlerException;

    public String executeGraphQueryAsJSON(RepositoryConnection connection, String query) throws Exception;

    /**
     * Performs queries in the repository, returning a java.util.Iterator with
     * the results.
     *
     * @param connection receives an object of connection with the repository
     * @param query the <code>String</code> with the query to be performed.
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    public Iterator<Object> executeQueryAsIterator(RepositoryConnection connection, String query, URI... contexts)
            throws Exception;

    /**
     * Performs SPARQL queries in the repository, returning a boolean with the
     * result.
     *
     * @param connection receives an object of connection with the repository
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean</code> true or false.
     */
    boolean executeBooleanQuery(RepositoryConnection connection, String query) throws Exception;

    /**
     * Performs SPARQL update queries in the repository, returning a boolean
     * true if the query was performed with successful or false otherwise.
     *
     * @param connection receives an object of connection with the repository
     * @param query the <code>String</code> with the query to be performed.
     */
    public void executeUpdateQuery(RepositoryConnection connection, String query) throws Exception;

}
