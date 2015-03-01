/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.module.reasoner;

import wwwc.nees.joint.model.RDFUris;
import wwwc.nees.joint.module.kao.SPARQLQueryRunnerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Parser for transforming rdf list in the graph to java.util.List
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class RDFListParser {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    private RepositoryConnection connection;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Initializes the connection with the repository
     *
     * @param connection
     *            the class to be implemented.
     */
    public RDFListParser(RepositoryConnection connection) {
        this.connection = connection;
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets the java.util.List with the elements presented in the
     *  passed rdf:list node
     *
     * @param node
     *            the string with rdf:list node
     * @return list
     *            java.util.List with the elements in the rdf:list
     */
    public List<String> retrieveList(String node) {

        // Creates the return list
        List<String> list = new ArrayList<String>();

        try {
            // while the list is not over
            while (!node.equals(RDFUris.RDF_NIL)) {

                // Creates the query to search the elements of the list
                // the first represents an element and the rest is a
                // blank node
                StringBuilder listQuery = new StringBuilder();

                listQuery.append("select first, rest from {");
                listQuery.append(node);
                listQuery.append("} rdf:first {first}; rdf:rest {rest}");
                String query = listQuery.toString();

                // Performs the serql query
                TupleQuery tupleQuery = this.connection.
                        prepareTupleQuery(QueryLanguage.SERQL, query);

                // Evaluates the query
                TupleQueryResult result = tupleQuery.evaluate();

                // Gets the first line result
                BindingSet set = result.next();

                // Gets the value of the first variable and add in the list
                String first = set.getValue("first").toString();
                list.add(first);

                // Gets the value of the rest that is a blank node
                node = set.getValue("rest").toString();

            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
    }
}
