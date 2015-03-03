package wwwc.nees.joint.module.kao;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import wwwc.nees.joint.compiler.annotations.Iri;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import wwwc.nees.joint.model.JOINTResource;

/**
 *
 * @author Olavo
 */
public class UpdateOperations {

    private final String STRING = "java.lang.String";
    private final String OBJECT = "java.lang.Object";
    private final String BOOLEAN = "java.lang.Boolean";
    private final String INTEGER = "java.lang.Integer";
    private final String FLOAT = "java.lang.Float";
    private final String DATE = "com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl";
    private RepositoryConnection connection;
    private ValueFactory f;

    public boolean isDatatype(String className) {
        if (className.equals(STRING)
                || className.equals(BOOLEAN)
                || className.equals(INTEGER)
                || className.equals(FLOAT)
                || className.equals(DATE)
                || className.equals(OBJECT)) {
            return true;
        }
        return false;
    }

    public Literal convertDatatype(String value, String className) {

        if (className.equals(BOOLEAN)) {
            return this.f.createLiteral(Boolean.parseBoolean(value));
        } else if (className.equals(INTEGER)) {
            return this.f.createLiteral(Integer.parseInt(value));
        } else if (className.equals(FLOAT)) {
            return this.f.createLiteral(Float.parseFloat(value));
        } else if (className.equals(DATE)) {
            return this.f.createLiteral(XMLGregorianCalendarImpl.parse(value));
        }
        return this.f.createLiteral(value);
    }

    public Object updateDettachedInstance(Object instance, Class classe, RepositoryConnection con, URI... contexts) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, RepositoryException, NoSuchMethodException {
        this.connection = con;
        this.f = this.connection.getValueFactory();

        URI suj = f.createURI(instance.toString());

        classe = Class.forName(classe.getName() + "Impl");

        //creates a list of statements to be inserted
        List<Statement> updSts = new ArrayList();

        //retrieves all methods which were modified
        List<String> modifiedMethods = ((JOINTResource) instance).getInnerModifiedFields();

        List<String> auxModifiedMethods = new ArrayList(modifiedMethods);

        //for over these modified methods
        for (String methodName : auxModifiedMethods) {

            // gets the method
            Method method = classe.getMethod("get" + methodName);

            method.setAccessible(true);

            //pega o predicado da propriedade
            Iri iri = method.getAnnotation(Iri.class);
            URI pred = f.createURI(iri.value());

            //removes the info from this predicate
            if (contexts == null) {
                this.connection.remove(suj, pred, null);
            } else {
                for (URI context : contexts) {
                    this.connection.remove(suj, pred, null, context);
                }
            }
            Object returnOb = method.invoke(instance);

            if (returnOb == null) { //caso de nao ter nenhuma valor a propriedade
                continue;
            }

            //pega o parametro da propriedade
            Class parameterClass = returnOb.getClass();
            String parameterClassName = parameterClass.getName();
            //contexts

            if (!parameterClassName.equals("java.util.HashSet")) {// caso de ser uma propriedade functional

                if (this.isDatatype(parameterClassName)) {
                    Literal litObj = this.convertDatatype(returnOb.toString(), parameterClassName);
                    if (contexts == null) {
                        updSts.add(f.createStatement(suj, pred, litObj));
                    } else {
                        for (URI context : contexts) {
                            updSts.add(f.createStatement(suj, pred, litObj, context));
                        }
                    }
                } else {
                    URI uriObj = f.createURI(returnOb.toString());
                    if (contexts == null) {
                        updSts.add(f.createStatement(suj, pred, uriObj));
                    } else {
                        for (URI context : contexts) {
                            updSts.add(f.createStatement(suj, pred, uriObj, context));
                        }
                    }

                }
            } else { // caso de ser multi valorado

                Set<Object> returnSet = (Set<Object>) returnOb;
                if (returnSet.isEmpty()) {
                    continue;
                } else {
                    parameterClass = returnSet.iterator().next().getClass();
                    parameterClassName = parameterClass.getName();
                }
                //Create an iterator with all contexts 
                if (this.isDatatype(parameterClassName)) {
                    //percorre a lista
                    for (Object ob : returnSet) {

                        Literal litObj = this.convertDatatype(ob.toString(), parameterClassName);
                        if (contexts == null) {
                            updSts.add(f.createStatement(suj, pred, litObj));
                        } else {
                            for (URI context : contexts) {
                                updSts.add(f.createStatement(suj, pred, litObj, context));
                            }
                        }
                    }
                } else {
                    //percorre a lista
                    for (Object ob : returnSet) {
                        URI uriObj = f.createURI(ob.toString());
                        if (contexts == null) {
                            updSts.add(f.createStatement(suj, pred, uriObj));
                        } else {
                            for (URI context : contexts) {
                                updSts.add(f.createStatement(suj, pred, uriObj, context));
                            }
                        }
                    }
                }
            }
        }

        this.connection.add((Iterable) updSts);

        //erases inner modified fields
        ((JOINTResource) instance).setInnerModifiedFields(new ArrayList<String>());

        return instance;
    }
}
