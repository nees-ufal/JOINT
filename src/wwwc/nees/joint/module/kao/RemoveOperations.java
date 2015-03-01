package wwwc.nees.joint.module.kao;

import java.util.Iterator;
import java.util.List;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * @author Olavo
 */
public class RemoveOperations {

    /**
     * Removes the desired instance in the repository, passing the instance name
     *
     * @param instanceName a <code>String</code> with the instance name.
     */
//    public void remove(String ontologyURI, String instanceName, RepositoryConnection connection)
//            throws RepositoryException, Exception {
//
//        ValueFactory f = connection.getValueFactory();
//
//        // Creates the instance resource
//        URI instance = f.createURI(ontologyURI + instanceName);
//
//        connection.remove(instance, null, null);
//        connection.remove((Resource) null, null, instance);
//
//    }
    public void remove(String ontologyURI, String instanceName, List<URI> contexts, RepositoryConnection connection)
            throws RepositoryException, Exception {

        ValueFactory f = connection.getValueFactory();
        // Creates the instance resource
        URI instance = f.createURI(ontologyURI + instanceName);
        if (contexts.isEmpty()) {
            connection.remove(instance, null, null);
            connection.remove((Resource) null, null, instance);
            return;
        }
        Iterator<URI> context = contexts.iterator();
        while (context.hasNext()) {
            URI c = context.next();
            connection.remove(instance, null, null, c);
            connection.remove((Resource) null, null, instance, c);
        }
    }

    /**
     *
     * Removes the desired instance in the repository.
     *
     * @param instance an <code>Object</code> representing the instance.
     */
//    public <T> void remove(T instance, RepositoryConnection connection)
//            throws RepositoryException, Exception {
//
//        ValueFactory f = connection.getValueFactory();
//
//        // Creates the instance resource
//        URI instanceURI = f.createURI(instance.toString());
//
//        connection.remove(instanceURI.toString(), null, null);
//        connection.remove((Resource) null, null, instanceURI.toString());
//    }
    public <T> void remove(T instance, List<URI> contexts, RepositoryConnection connection)
            throws RepositoryException, Exception {

        ValueFactory f = connection.getValueFactory();

        // Creates the instance resource
        URI instanceURI = f.createURI(instance.toString());

        if (contexts.isEmpty()) {
            connection.remove(instanceURI, null, null);
            connection.remove((Resource) null, null, instanceURI);
            return;
        }
        Iterator<URI> context = contexts.iterator();
        while (context.hasNext()) {
            URI c = context.next();
            connection.remove(instanceURI, null, null, c);
            connection.remove((Resource) null, null, instanceURI, c);
        }
    }
}
