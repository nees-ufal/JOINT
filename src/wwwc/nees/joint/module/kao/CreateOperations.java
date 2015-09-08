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
public class CreateOperations extends Operation{

    private Method lazyLoaded;
    private Method setURI;
    private String classIri;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance in the repository with the specified uri.
     *
     * @param instanceURI a <code>URI</code> with the instance uri.
     * @param connection receives an object of connection with the repository
     * @param contexts <code>URI</code> represent the graphs in which the
     * instance will be inserted.
     * @return T the new instance.
     */
    public <T> T create(RepositoryConnection connection, String instanceURI, Class<T> clazz, URI... contexts)
            throws ClassNotFoundException, RepositoryException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NullPointerException {
        Object ob = null;
        Class classImpl;
        try {
            classImpl = Class.forName(clazz.getName() + "Impl");
            setURI = classImpl.getMethod("setURI", String.class);
            lazyLoaded = classImpl.getMethod("setLazyLoaded", boolean.class);
            classIri = ((Iri) clazz.getAnnotation(Iri.class)).value();

            //Creates the object to Impl class
            ob = classImpl.newInstance();

            ValueFactory vf = connection.getValueFactory();

            //creates the object
            URI subj = vf.createURI(instanceURI);
            URI obj = vf.createURI(classIri);

            //adds the designation
            connection.add(subj, RDF.TYPE, obj, contexts);

            setURI.invoke(ob, instanceURI);

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
     * @param connection receives an object of connection with the repository
     * @param instancePrefix a <code>String</code> with the prefix name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @param contexts <code>URI</code> represent the graphs in which the
     * instance will be inserted.
     * @return T the new instance.
     */
    public <T> T createWithUniqueID(RepositoryConnection connection, String ontologyURI, String instancePrefix, Class<T> clazz, URI... contexts)
            throws ClassNotFoundException, RepositoryException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NullPointerException {
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
