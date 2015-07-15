package wwwc.nees.joint.module.kao;

import info.aduna.iteration.Iterations;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import wwwc.nees.joint.compiler.annotations.Iri;
import wwwc.nees.joint.model.JOINTResource;

/**
 *
 * @author Olavo
 */
public class LazyLoader {

    private static final String OBJECT_CLASS = "java.lang.Object";
    private static final String SET_CLASS = "java.util.Set";
    private static final String METHOD_SET_INNERFIELDS = "setInnerModifiedFields";
    private static final String METHOD_SET_LAZYLOADED = "setLazyLoaded";
    private static final String METHOD_SET_URI = "setURI";
    private static final String PREF_SETTER = "set";
    private static final String SUF_IMPL_CLASS = "Impl";

    private final DatatypeManager datatypeManager;
    private RepositoryConnection connection;
    private ValueFactory f;
    private final Map<String, String> packages;
    private final GraphQueryConstruct graphQueryConstruct;

    public LazyLoader(RepositoryConnection con) {
        this.connection = con;
        this.f = this.connection.getValueFactory();
        this.packages = ConceptsPackageInfo.getPackagesInfo(this.getClass());
        this.datatypeManager = DatatypeManager.getInstance();
        this.graphQueryConstruct = new GraphQueryConstruct(con);
    }

    public String getClassFromBase(String subj, URI... contexts) throws Exception {
        ValueFactory f = connection.getValueFactory();
        URI sub = f.createURI(subj);

        RepositoryResult stts = this.connection.getStatements(sub, RDF.TYPE, null, true, contexts);
        List<Statement> statements = Iterations.asList(stts);
        stts.close();
        //List<Statement> statements = graphQueryConstruct.getStatementsByGraphQuery(subj, RDF.TYPE.toString(), null);

        //Preparar a query para recuperar o tipo de instancia
        if (statements.size() > 0) {
            Statement st = statements.get(0);

            statements = null;

            String uriObj = st.getObject().stringValue();
            String nameClasse = this.packages.get(uriObj);
            if (nameClasse == null) {
                return OBJECT_CLASS;
            }
            return nameClasse;
        }
        return OBJECT_CLASS;
    }

    //OLD METHOD (CHANGED BY ONE WHERE IT DOESN'T USES GET STATMENTS)
//    public String getClassFromBase(String subj) throws RepositoryException {
//        RepositoryResult<Statement> sts = this.connection.getStatements((Resource) this.f.createURI(subj), org.openrdf.model.vocabulary.RDF.TYPE, null, true);
//        if (sts.hasNext()) {
//            Statement st = sts.next();
//            String uriObj = st.getObject().stringValue();
//            String nameClasse = this.packages.get(uriObj);
//            if (nameClasse == null) {
//                return OBJECT_CLASS;
//            }
//            return nameClasse;
//        }
//        return OBJECT_CLASS;
//    }
    private Object createJOINTResourceObject(String instanceName) {

        //creates a new instance of JOINTResource
        JOINTResource obj = new JOINTResource();

        //calls the method setURI
        obj.setURI(instanceName);

        //return the object
        return obj;
    }

    public Object getNotLoadedObject(String instanceName, String clazzName) {

        //creates a null object
        Object obj = null;
        try {
            // checks if it is a java object
            if (clazzName.equals(OBJECT_CLASS)) {
                //if yes, than create a JOINT resource instance
                obj = this.createJOINTResourceObject(instanceName);
            } else {

                //creates an instance with the concrete class
                Class clazz = Class.forName(clazzName + SUF_IMPL_CLASS);
                obj = clazz.newInstance();
                //casts the object to the upper class JOINTResource and
                //calls the methods setURI and setLazyLoaded
                ((JOINTResource) obj).setURI(instanceName);
                ((JOINTResource) obj).setLazyLoaded(false);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(LazyLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return the object
        return obj;
    }

    private Map<String, List<Value>> sortPropertiesAndValues(List<Statement> statements) {
        //creates a map to hold all values of the properties
        Map<String, List<Value>> mapProperties = new HashMap<>();

        //puts the property values in the map
        for (Statement s : statements) {
            String prop = s.getPredicate().stringValue();
            if (mapProperties.containsKey(prop)) {
                mapProperties.get(prop).add(s.getObject());
            } else {
                List<Value> values = new ArrayList<>();
                values.add(s.getObject());
                mapProperties.put(prop, values);
            }
        }
        return mapProperties;
    }

    public void lazyLoadObject(Object objectClassImpl, String className, URI... contexts) {

        try {

            this.f = this.connection.getValueFactory();

            String instanceName = objectClassImpl.toString();
            URI suj = f.createURI(instanceName);
            Class classImpl = Class.forName(className);

            //casts the object to the upper class JOINTResource and
            //calls the methods setURI and setLazyLoaded
            ((JOINTResource) objectClassImpl).setURI(instanceName);
            ((JOINTResource) objectClassImpl).setLazyLoaded(true);

            //gets all methods of the desired class
            Method[] allMethodsClassImpl = classImpl.getMethods();

            //retrieves all values of the properties of the instance
            //List<Statement> statements = graphQueryConstruct.getStatementsByGraphQuery(suj.toString(), null, null);
            RepositoryResult<Statement> stts = this.connection.getStatements(suj, null, null, true, contexts);
            List<Statement> statements = Iterations.asList(stts);
            stts.close();

            //creates a map to hold all values of the properties
            Map<String, List<Value>> mapProperties = this.sortPropertiesAndValues(statements);

            //for each method, searches for the setter ones
            for (Method method : allMethodsClassImpl) {

                //retrieves the method name
                String nomeMetodoImpl = method.getName();
                //checks if it is a setter one
                if (nomeMetodoImpl.startsWith(PREF_SETTER)) {

                    //if it is the methods setURI or setLazyLoaded or 
                    // set InnerFields, do nothing
                    if (nomeMetodoImpl.startsWith(METHOD_SET_URI)
                            || nomeMetodoImpl.startsWith(METHOD_SET_LAZYLOADED)
                            || nomeMetodoImpl.startsWith(METHOD_SET_INNERFIELDS)) {
                        continue;
                    }

                    //ignores Java supress warning check
                    method.setAccessible(true);

                    //ignores Java supress warning check
                    method.setAccessible(true);
                    //gets the name of the method parameter class
                    String parameterClassName = method.getParameterTypes()[0].getName();

                    //retrieves the associated annotation that contais the predicate
                    Iri iri = method.getAnnotation(Iri.class);

                    //if the property has no value, it enters in the first if
                    //if the property is functional, it enters in the second if
                    // else it has multi values
                    if (!mapProperties.containsKey(iri.value())) {
                        //if the parameter is a setter one, invoke the method
                        //with an empty new HashSet
                        if (parameterClassName.equals(SET_CLASS)) {
                            method.invoke(objectClassImpl, new HashSet());
                        }
                    } else if (!parameterClassName.equals(SET_CLASS)) {
                        //gets the Value from the object of this property
                        Value objValue = mapProperties.get(iri.value()).get(0);
                        //gets the assiciated URI
                        String valueURI = objValue.stringValue();
                        //it the valueURI is not empty
                        if (!valueURI.isEmpty()) {
                            //if it is a datatype
                            if (this.datatypeManager.isDatatype(objValue)) {
                                //mapps to the an specific java native Class
                                method.invoke(objectClassImpl, this.datatypeManager.
                                        convertLiteralToDataype((Literal) objValue));
                                //else it is an istance
                            } else {
                                //gets the class name from the triple store
                                parameterClassName = this.getClassFromBase(valueURI);
                                //gets a new instance with its properties not loaded
                                method.invoke(objectClassImpl, this.
                                        getNotLoadedObject(valueURI, parameterClassName));
                            }
                        }
                    } else {
                        //gets all values of the property and put in the list
                        List<Value> listValues = mapProperties.get(iri.value());
                        //creates the set to be used in the method
                        Set<Object> returnSet = new HashSet<>();
                        //gets the first value for type searching
                        Value v = listValues.get(0);
                        //if it is a datatype
                        if (this.datatypeManager.isDatatype(v)) {
                            //crawls the list of values converting to a
                            //specific java native Class
                            for (Value objValue : listValues) {
                                returnSet.add(this.datatypeManager.convertLiteralToDataype((Literal) objValue));
                            }
                            //else it is an istance
                        } else {
                            //gets the class name from the triple store
                            parameterClassName = this.getClassFromBase(v.stringValue());
                            //crawls the list of values converting to the
                            //specific Class
                            for (Value objValue : listValues) {
                                String objIt = objValue.stringValue();
                                //gets a new instance with its properties not loaded
                                returnSet.add(this.getNotLoadedObject(objIt, parameterClassName));
                            }
                        }
                        //invokes the method with the converted parameter
                        method.invoke(objectClassImpl, returnSet);
                    }
                }
            }

            //calls the setInnerModifiedFields to erase the modified fields
            //of the instance (update mechanics)
            ((JOINTResource) objectClassImpl).setInnerModifiedFields(new ArrayList<String>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadObject(Object ob, String className) {
        try {
            RepositoryConnection con = RepositoryFactory.getRepository().getConnection();

            //gets connection
            con.begin();

            LazyLoader lazyLoader = new LazyLoader(con);
            try {

                lazyLoader.lazyLoadObject(ob, className);

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
    }
}
