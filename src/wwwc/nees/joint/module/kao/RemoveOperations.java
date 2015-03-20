package wwwc.nees.joint.module.kao;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * @author Olavo
 */
public class RemoveOperations {

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
     *
     * Removes the desired instance in the repository.
     *
     * @param <T>
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

    public <T> void removeStatements(RepositoryConnection connection, String subj, String property, String object, String context)
            throws Exception, NullPointerException {

        StringBuilder query = new StringBuilder();

        //Build the query to retrieve the requested elements (?s ?p ?o) where
        // Sets the context of the instance
        query.append("DELETE FROM ");
        if (context != null) {
            query.append("<").append(context).append("> ");
        } else {
            query.append("<sesame:nil> ");
        }
        query.append("{?s ?p ?o.} WHERE {");
        if (subj != null) {
            query.append(" VALUES ?s {<").append(subj).append(">} ");
        }

        if (property != null) {
            query.append(" VALUES ?p {<").append(property).append(">} ");
        }

        if (object != null) {
            query.append(" VALUES ?o {");
            /**
             * Identifies wheter the object is a number, if it is, then don't
             * need to add quotes (\"), only the object value
             */
            try {
                Float.parseFloat(object);
                query.append(object);
            } catch (NumberFormatException e) {
                query.append("\"").append(object).append("\"");
            } finally {
                query.append("}");
            }
        }
        query.append("?s ?p ?o.}");
        //Query to retrieve the informations (?s ?p ?o)
        System.out.println(query.toString());
        //evaluate the graph result
        Update prepareGraphQuery = connection.prepareUpdate(QueryLanguage.SPARQL, query.toString());
        prepareGraphQuery.execute();
    }
}
