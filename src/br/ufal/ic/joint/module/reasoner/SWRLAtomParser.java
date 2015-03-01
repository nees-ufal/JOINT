/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufal.ic.joint.module.reasoner;

import br.ufal.ic.joint.model.OWLUris;
import br.ufal.ic.joint.model.SWRLUris;
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
 * Parser for transforming atom nodes in the graph to joint Atoms objects
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class SWRLAtomParser {

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
    public SWRLAtomParser(RepositoryConnection connection) {
        this.connection = connection;
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets the Atom object representing the passed atom node in the graph
     *
     * @param node
     *            the String with the Atom node
     * @return atom
     *            the joint Atom object
     */
    public Atom getTripleofAtom(String atomNode) {

        // Creates a new Atom
        Atom atom = new Atom();
        try {

            // Builds the query for search each piece of the swrl atom
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select arg1, arg2, property, class from {");
            queryBuilder.append(atomNode);
            queryBuilder.append("} swrl:argument1 {arg1};");
            queryBuilder.append(" [swrl:argument2 {arg2}];");
            queryBuilder.append(" [swrl:propertyPredicate {property}];");
            queryBuilder.append(" [swrl:classPredicate {class}]");
            queryBuilder.append(" using namespace");
            queryBuilder.append(" swrl = <http://www.w3.org/2003/11/swrl#>");
            String query = queryBuilder.toString();

            // Performs the SERQL query in the repository and evaluates
            TupleQuery tuple = this.connection.prepareTupleQuery(QueryLanguage.SERQL, query);
            TupleQueryResult result = tuple.evaluate();

            // Gets the result set
            BindingSet resultSet = result.next();

            // Creates the triple variables
            String property = "null";
            String arg1 = "null";
            String arg2 = "null";

            // If the atom has a property predicate
            if (resultSet.getValue("property") != null) {

                // Gets each value of the atom
                property = resultSet.getValue("property").toString();
                arg1 = resultSet.getValue("arg1").toString();
                arg2 = resultSet.getValue("arg2").toString();

                // Else if the atom has a class predicate
            } else if (resultSet.getValue("class") != null) {

                // Gets each value of the atom
                property = resultSet.getValue("class").toString();
                arg1 = resultSet.getValue("arg1").toString();
                arg2 = "null";

            } else {

                // Gets the arguments of the atom
                arg1 = resultSet.getValue("arg1").toString();
                arg2 = resultSet.getValue("arg2").toString();

                // Creates a query to search for its rdf:type
                queryBuilder = new StringBuilder();
                queryBuilder.append("select type from {");
                queryBuilder.append(atomNode);
                queryBuilder.append("} rdf:type {type}");
                query = queryBuilder.toString();

                // Performs the SERQL query in the repository and evaluates
                tuple = this.connection.prepareTupleQuery(QueryLanguage.SERQL, query);
                result = tuple.evaluate();

                // While still have results
                while (result.hasNext()) {

                    // Gets the result set
                    BindingSet qSet = result.next();
                    String type = qSet.getValue("type").toString();

                    // If the type of the atom is a SWRL DifferentFrom
                    if (type.equals(SWRLUris.SWRL_DIFF_ATOM)) {
                        property = OWLUris.OWL_DIFF_FROM;

                        // Else if the type of the atom is a SWRL SamesAs
                    } else if (type.equals(SWRLUris.SWRL_SAME_ATOM)) {
                        property = OWLUris.OWL_SAME_AS;
                    }
                }
            }

            // Add the values to the return object
            atom.setProperty(property);
            atom.setArgument1(arg1);
            atom.setArgument2(arg2);
        } catch (RepositoryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return atom;
    }
}
