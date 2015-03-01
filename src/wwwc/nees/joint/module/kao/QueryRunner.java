package wwwc.nees.joint.module.kao;

import java.util.Iterator;
import java.util.List;
import org.openrdf.model.URI;

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
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return object <code>Object</code> result.
     */
    public Object executeQueryAsSingleResult(String query, List<URI> contexts);

    /**
     * Performs queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>List<Object></code> a java.util.List with the results.
     */
    public List<Object> executeQueryAsList(String query, List<URI> contexts);

    /**
     * Performs queries in the repository, returning a java.util.Iterator with
     * the results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    public Iterator<Object> executeQueryAsIterator(String query, List<URI> contexts);

    /**
     * Performs SPARQL queries in the repository, returning a boolean with the
     * result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean<Object></code> true or false.
     */
    boolean executeBooleanQuery(String query);
}
