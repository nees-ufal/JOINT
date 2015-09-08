package wwwc.nees.joint.module.kao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * @author Olavo
 */
public class RemoveOperations extends Operation {

    /**
     * Removes the desired instance in the repository, passing the instance name
     *
     * @param ontologyURI
     * @param instanceName a <code>String</code> with the instance name.
     * @param connection an object representing the connection with the database
     * @param contexts an array of URIs that represent the contexts where
     * statements will be removed
     * @throws org.openrdf.repository.RepositoryException
     */
    public void remove(String ontologyURI, String instanceName, RepositoryConnection connection, URI... contexts)
            throws RepositoryException, Exception {

        ValueFactory f = connection.getValueFactory();
        // Creates the instance resource
        URI instance = f.createURI(ontologyURI + instanceName);
        connection.remove(instance, null, null, contexts);
        connection.remove((Resource) null, null, instance, contexts);
    }

    /**
     * Removes the desired instance in the repository.
     *
     * @param instance an <code>Object</code> representing the instance.
     * @param connection an object representing the connection with the database
     * @param contexts an array of URIs that represent the contexts where
     * statements will be removed
     * @throws org.openrdf.repository.RepositoryException
     */
    public <T> void remove(T instance, RepositoryConnection connection, URI... contexts)
            throws RepositoryException, Exception {

        ValueFactory f = connection.getValueFactory();

        // Creates the instance resource
        URI instanceURI = f.createURI(instance.toString());

        connection.remove(instanceURI, null, null, contexts);
        connection.remove((Resource) null, null, instanceURI, contexts);
    }

    /**
     * Removes an desired quad in the repository using SPARQL Update
     *
     * @param connection an object representing the connection with the database
     * @param subject representing the subject corresponding in a quad.
     * @param property representing the predicate corresponding in a quad.
     * @param object representing the object corresponding in a quad.
     * @param contexts representing the contexts where statements will be
     * removed
     */
    public static <T> void removeStatements(RepositoryConnection connection, String subject, String property, String object, URI... contexts) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        //Query to retrieve the informations (?s ?p ?o)
        StringBuilder query = new StringBuilder();
        //Build the query to retrieve the requested elements (?s ?p ?o) where
        // Sets the context of the instance

        //bool to validate existence of statements
        boolean existsStatements = true;

        try {
            existsStatements = askStatements(connection, subject, property, object, contexts);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(RemoveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (existsStatements) {
            query.append("DELETE {");
            if (contexts.length == 0) {
                query.append("GRAPH ?g {?s ?p ?o.}");
            } else {
                for (URI context : contexts) {
                    query.append("GRAPH <").append(context.stringValue()).append("> {?s ?p ?o.}");
                }
            }
            query.append("} WHERE {");
            if (subject != null) {
                query.append("VALUES ?s {<").append(subject).append(">} ");
            }

            if (property != null) {
                query.append("VALUES ?p {<").append(property).append(">} ");
            }

            if (object != null) {
                query.append("VALUES ?o {");
                String obj = identifyObjectType(object);
                query.append(obj).append("} ");
            }

            query.append("GRAPH ?g {?s ?p ?o.}}");

            System.out.println(query.toString());
            //evaluate the graph result
            Update prepareGraphQuery = connection.prepareUpdate(QueryLanguage.SPARQL, query.toString());

            prepareGraphQuery.execute();
        }
    }

    /**
     * Removes the desired instance of the repository using SPARQL Update
     *
     * @param instanceURI an <code>Object</code> representing the instance.
     * @param connection an object representing the connection with the database
     * @param contexts an array of URIs that represent the contexts where
     */
    public void remove_SPARQLUpdate(RepositoryConnection connection, String instanceURI, URI... contexts) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        removeStatements(connection, instanceURI, null, null, contexts);
        removeStatements(connection, null, null, instanceURI, contexts);
    }
}
