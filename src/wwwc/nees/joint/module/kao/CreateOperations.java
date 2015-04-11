package wwwc.nees.joint.module.kao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import wwwc.nees.joint.compiler.annotations.Iri;

/**
 * @author Olavo
 */
public class CreateOperations {

    private Method lazyLoaded;
    private Method setURI;
    private String classIri;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance in the repository with the specified name.
     *
     * @param instanceName a <code>String</code> with the instance name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @return T the new instance.
     */
    public <T> T create(String ontologyURI, String instanceName, Class<T> clazz, RepositoryConnection connection, URI... contexts)
            throws ClassNotFoundException, RepositoryException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NullPointerException {
        Object ob = null;
        Class classImpl;
        try {
            classImpl = Class.forName(clazz.getName() + "Impl");
            setURI = classImpl.getMethod("setURI", String.class);
            lazyLoaded = classImpl.getMethod("setLazyLoaded", boolean.class);
            classIri = ((Iri) clazz.getAnnotation(Iri.class)).value();

            //Criar o objeto da class impl
            ob = classImpl.newInstance();

            //gets connection
            ValueFactory f = connection.getValueFactory();

            //creates the subject and the object
            URI subj = f.createURI(ontologyURI + instanceName);
            URI obj = f.createURI(classIri);

            //adds the designation
            connection.add(subj, RDF.TYPE, obj, contexts);

            setURI.invoke(ob, ontologyURI + instanceName);

            lazyLoaded.invoke(ob, true);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            Logger.getLogger(CreateOperations.class.getName()).log(Level.SEVERE, null, ex);
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
    public <T> T createWithUniqueID(String ontologyURI, String instancePrefix, Class<T> clazz, RepositoryConnection connection, URI... contexts)
            throws ClassNotFoundException, RepositoryException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException,NullPointerException {
        // Creates an object with the URI and the .class
        Object ob = null;
        Class classImpl;
        try {
            classImpl = Class.forName(clazz.getName() + "Impl");
            setURI = classImpl.getMethod("setURI", String.class);
            lazyLoaded = classImpl.getMethod("setLazyLoaded", boolean.class);
            classIri = ((Iri) clazz.getAnnotation(Iri.class)).value();

            //Criar o objeto da class impl
            ob = classImpl.newInstance();

            //gets connection
            ValueFactory f = connection.getValueFactory();

            //Creates an unique ID
            UUID uuid = UUID.randomUUID();
            String id = uuid.toString();

            //creates the subject and the object
            URI subj = f.createURI(ontologyURI + instancePrefix + id);
            //URI obj = f.createURI(((Iri) classImpl.getAnnotation(Iri.class)).value());
            URI obj = f.createURI(classIri);

            //adds the designation
            connection.add(subj, RDF.TYPE, obj, contexts);

            Method met = classImpl.getMethod("setURI", String.class);
            met.invoke(ob, ontologyURI + instancePrefix + id);

            Method met2 = classImpl.getMethod("setLazyLoaded", boolean.class);
            met2.invoke(ob, false);

        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            Logger.getLogger(CreateOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (T) ob;
    }
}
