package wwwc.nees.joint.module.kao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import wwwc.nees.joint.compiler.annotations.Iri;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import wwwc.nees.joint.model.JOINTResource;

/**
 * @author Olavo
 */
public class RetrieveOperations {

    private final DatatypeManager datatypeManager;
    private final Map<String, String> packages;
    private static final String OBJECT_CLASS = "java.lang.Object";
    private static final String SET_CLASS = "java.util.Set";
    private static final String METHOD_SET_INNERFIELDS = "setInnerModifiedFields";
    private static final String METHOD_SET_LAZYLOADED = "setLazyLoaded";
    private static final String METHOD_SET_URI = "setURI";
    private static final String PREF_SETTER = "set";
    private static final String SUF_IMPL_CLASS = "Impl";

    public RetrieveOperations() {
        this.packages = ConceptsPackageInfo.getPackagesInfo(this.getClass());
        this.datatypeManager = DatatypeManager.getInstance();
    }

    /**
     * Retrieves the desired instance in the repository.
     *
     * @param <T>
     * @param instanceURI a <code>String</code> with the instance URI
     * @param clazz a <code>Class</code> with the instance type
     * @param connection receives an object of connection with the repository
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return T the desired instance.
     * @throws java.lang.Exception any exception
     */
    public <T> T retrieveInstance(String instanceURI, Class<T> clazz,
            RepositoryConnection connection, URI... contexts) throws Exception {
        return (T) this.convertOriginalForImpl(connection, instanceURI, clazz, contexts);
    }

    /**
     * Retrieves all the instances of the class, passed in the constructor.
     *
     * @param clazz a <code>Class</code> with the instance type
     * @param connection receives an object of connection with the repository
     * @param contexts <code>URI</code> represent the graphs in which the query
     * will be performed.
     * @return a <code>List</code> with the instances.
     * @throws java.lang.Exception any exception
     */
    public <T> List<T> retrieveAllInstances(Class<T> clazz,
            RepositoryConnection connection, URI... contexts) throws Exception {

        ValueFactory f = connection.getValueFactory();

        // Creates a new java.util.List
        List<T> listInstances = new ArrayList<>();
        RepositoryResult<Statement> stts = connection.getStatements(null, RDF.TYPE, f.createURI(((Iri) clazz.getAnnotation(Iri.class)).value()), true, contexts);
        while (stts.hasNext()) {
            List<String> instancesName = new ArrayList<>();

            Statement statement = stts.next();

            instancesName.add(statement.getSubject().stringValue());

            listInstances.addAll((Collection<? extends T>) (T) this.convertCollectionOriginalForImpl(connection, instancesName, clazz, contexts));
        }
        stts.close();
        return listInstances;
    }

    public String getClassFromBase(RepositoryConnection connection, String subj, URI... contexts) throws RepositoryException {
        URI sub = connection.getValueFactory().createURI(subj);

        RepositoryResult<Statement> statements = connection.getStatements(sub, RDF.TYPE, null, true, contexts);
        //Preparar a query para recuperar o tipo de instancia
        while (statements.hasNext()) {
            String uriObj = statements.next().getObject().stringValue();
            statements.close();
            String nameClasse = this.packages.get(uriObj);
            if (nameClasse == null) {
                return OBJECT_CLASS;
            }
            return nameClasse;
        }
        statements.close();
        return OBJECT_CLASS;
    }

    public String getClassFromBase1(String obj) throws Exception {
        String nameClasse = this.packages.get(obj);
        if (nameClasse == null) {
            return OBJECT_CLASS;
        }
        return nameClasse;
    }

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

    public Object convertOriginalForImpl(RepositoryConnection connection, String instanceName, Class clazz, URI... contexts) throws Exception {

        URI suj = connection.getValueFactory().createURI(instanceName);
        //checks if this instance is in the triple store
        boolean objectNull = connection.hasStatement(suj, RDF.TYPE, null, true, contexts);

        if (!objectNull) {
            return null;
        }

        // checks if it is a java object
        if (clazz.getName().equals(OBJECT_CLASS)) {
            //if yes, than create a JOINT resource instance
            return this.createJOINTResourceObject(instanceName);
        }

        //creates an instance with the concrete class
        Class classImpl = Class.forName(clazz.getName() + SUF_IMPL_CLASS);
        Object obj = classImpl.newInstance();

        //casts the object to the upper class JOINTResource and
        //calls the methods setURI and setLazyLoaded
        ((JOINTResource) obj).setURI(instanceName);
        ((JOINTResource) obj).setLazyLoaded(true);

        //gets all methods of the desired class
        Method[] allMethodsClassImpl = classImpl.getMethods();

        //retrieves all values of the properties of the instance
        GraphQueryConstruct graphQueryConstruct = new GraphQueryConstruct(connection);
        List<Statement> statements = graphQueryConstruct.getStatementsAsList(suj.toString(), null, null, contexts);
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
                        method.invoke(obj, new HashSet());
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
                            method.invoke(obj, this.datatypeManager.
                                    convertLiteralToDataype((Literal) objValue, parameterClassName));
                            //else it is an istance
                        } else {
                            //gets the class name from the triple store
                            parameterClassName = this.getClassFromBase(connection, valueURI, contexts);
                            //gets a new instance with its properties not loaded
                            method.invoke(obj, this.
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
                        parameterClassName = this.getClassFromBase(connection, v.stringValue(), contexts);
                        //crawls the list of values converting to the
                        //specific Class
                        for (Value objValue : listValues) {
                            String objIt = objValue.stringValue();
                            //gets a new instance with its properties not loaded
                            returnSet.add(this.getNotLoadedObject(objIt, parameterClassName));
                        }
                    }
                    //invokes the method with the converted parameter
                    method.invoke(obj, returnSet);
                }
            }
        }
        //calls the setInnerModifiedFields to erase the modified fields
        //of the instance (update mechanics)
        ((JOINTResource) obj).setInnerModifiedFields(new ArrayList<String>());
        return obj;
    }

    public List<Object> convertCollectionOriginalForImpl(RepositoryConnection connection, List<String> instancesName, Class clazz, URI... contexts) throws Exception {

        List<Object> returnList = new ArrayList<>();

        // checks if it is a java object
        if (clazz.getName().equals(OBJECT_CLASS)) {

            //crawls the list of instances name
            for (String name : instancesName) {
                //if yes, than create a JOINT resource instance
                returnList.add(this.createJOINTResourceObject(name));
            }
            return returnList;
        }

        //gets the concrete desired class
        Class classImpl = Class.forName(clazz.getName() + SUF_IMPL_CLASS);

        //gets all methods of the concrete class
        Method[] allMethodsClassImpl = classImpl.getMethods();

        //constructs a query to get all information about the objects that will
        //be parsed
        GraphQueryConstruct graphQueryConstruct = new GraphQueryConstruct(connection);
        GraphQueryResult stts = graphQueryConstruct.getStatementsAsGraphQuery(instancesName, null, null, contexts);
        //creates a map with key - uri/object - list of statements
        Map<String, List<Statement>> cInformation = new HashMap<>();
        //iterates the previous graph result
        while (stts.hasNext()) {
            //gets the statement
            Statement st = stts.next();
            //gets the uri key
            String uri = st.getSubject().stringValue();

            //checks if the instanceURI is already in the map, if not
            if (!cInformation.containsKey(uri)) {
                //creates a new list of statements
                List<Statement> objects = new ArrayList<>();
                //adds this one
                objects.add(st);
                // puts in the map with the associated uri as a key
                cInformation.put(uri, objects);
            } else {
                //else, gets the list and adds this statement
                cInformation.get(uri).add(st);
            }
        }
        stts.close();
        for (String instanceURI : instancesName) {

            //creates an instance with the concrete class
            Object obj = classImpl.newInstance();

            //casts the object to the upper class JOINTResource and
            //calls the methods setURI and setLazyLoaded
            ((JOINTResource) obj).setURI(instanceURI);
            ((JOINTResource) obj).setLazyLoaded(true);

            //recupera os objects de todas as propriedades
            List<Statement> statements = cInformation.get(instanceURI);
            //creates a map to hold all values of the properties
            Map<String, List<Value>> mapProperties = this.sortPropertiesAndValues(statements);

            //for each method, searches for the setter ones
            for (Method method : allMethodsClassImpl) {

                //ignores Java supress warning check
                method.setAccessible(true);
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
                            method.invoke(obj, new HashSet());
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
                                method.invoke(obj, this.datatypeManager.
                                        convertLiteralToDataype((Literal) objValue, parameterClassName));
//                                method.invoke(obj, this.datatypeManager.
//                                        convertLiteralToDataype((Literal) objValue));
                                //else it is an istance
                            } else {
                                //gets the class name from the triple store
                                parameterClassName = this.getClassFromBase(connection, valueURI, contexts);
                                //gets a new instance with its properties not loaded                                
                                method.invoke(obj, this.
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
                            parameterClassName = this.getClassFromBase(connection, v.stringValue(), contexts);
                            //crawls the list of values converting to the
                            //specific Class
                            for (Value objValue : listValues) {
                                String objIt = objValue.stringValue();
                                //gets a new instance with its properties not loaded
                                returnSet.add(this.getNotLoadedObject(objIt, parameterClassName));
                            }
                        }
                        //invokes the method with the converted parameter
                        method.invoke(obj, returnSet);
                    }
                }
            }
            //calls the setInnerModifiedFields to erase the modified fields
            //of the instance (update mechanics)
            ((JOINTResource) obj).setInnerModifiedFields(new ArrayList<String>());
            //adds the object in the returnList
            returnList.add(obj);
        }
        return returnList;
    }

    public List<Object> convertCollectionOriginalForImpl2(RepositoryConnection connection, List<String> instancesName, Class clazz, URI... contexts) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        HashMap<String, List<Method>> methodMap = new HashMap<>();

        // checks if it is a java object
        if (clazz.getName().equals(OBJECT_CLASS)) {
            List<Object> returnList = new ArrayList<>();
            //crawls the list of instances name
            for (String name : instancesName) {
                //if yes, than create a JOINT resource instance
                returnList.add(this.createJOINTResourceObject(name));
            }
            return returnList;
        }

        //gets the concrete desired class
        Class classImpl = Class.forName(clazz.getName() + SUF_IMPL_CLASS);

        //gets all methods of the concrete class
        Method[] allMethodsClassImpl = classImpl.getMethods();

        //populating map between IRI and methods of concrete class
        for (Method m : allMethodsClassImpl) {
            try {
                String propertyURI = m.getAnnotation(Iri.class).value();
                List<Method> mList = (methodMap.containsKey(propertyURI)) ? methodMap.get(propertyURI) : new ArrayList<Method>();
                mList.add(m);
                methodMap.put(propertyURI, mList);
            } catch (NullPointerException e) {
            }
        }

        //constructs a query to get all information about the objects that will
        //be parsed
        GraphQueryConstruct graphQueryConstruct = new GraphQueryConstruct(connection);
        GraphQueryResult stts = graphQueryConstruct.getStatementsAsGraphQuery(instancesName, null, null, contexts);

        //iterates the previous graph result
        while (stts.hasNext()) {
            Statement statement = stts.next();
            if (!methodMap.containsKey(statement.getPredicate().toString())) {
                continue;
            }
            String subjectURI = statement.getSubject().stringValue();

            Object o;
            if (!result.containsKey(subjectURI)) {
                o = classImpl.newInstance();
                //casts the object to the upper class JOINTResource and
                //calls the methods setURI and setLazyLoaded
                ((JOINTResource) o).setURI(subjectURI);
                ((JOINTResource) o).setLazyLoaded(true);
            } else {
                o = result.get(subjectURI);
            }

            List<Method> methodList = methodMap.get(statement.getPredicate().toString());

            Method setterMethod = null;
            Method getterMethod = null;

            // defining getter and setter methods
            for (Method m : methodList) {
                //ignores Java supress warning check
                m.setAccessible(true);

                if (m.getName().startsWith(PREF_SETTER)) {
                    setterMethod = m;
                } else {
                    getterMethod = m;
                }
            }

            //gets the name of the method parameter class
            String parameterClassName = setterMethod.getParameterTypes()[0].getName();

            //gets the Value from the object of this property
            Value objValue = statement.getObject();

            //gets the assiciated URI
            String valueURI = objValue.stringValue();

            //if the property is functional, it enters in the first if
            // else it has multi values
            if (!parameterClassName.equals(SET_CLASS)) {

                //it the valueURI is not empty
                if (!valueURI.isEmpty()) {

                    //if it is a datatype
                    if (this.datatypeManager.isDatatype(objValue)) {
                        //mapps to the an specific java native Class                                 
                        setterMethod.invoke(o, this.datatypeManager.
                                convertLiteralToDataype((Literal) objValue, parameterClassName));
                        //else it is an istance
                    } else {
                        //gets the class name from the triple store
                        parameterClassName = this.getClassFromBase(connection, valueURI, contexts);

                        //gets a new instance with its properties not loaded                                
                        setterMethod.invoke(o, this.
                                getNotLoadedObject(valueURI, parameterClassName));
                    }
                }
            } else {
                Set<Object> set = (Set<Object>) getterMethod.invoke(o);
                if (set == null) {
                    set = new HashSet<>();
                }
                //gets the first value for type searching
                Value v = statement.getObject();
                //if it is a datatype
                if (this.datatypeManager.isDatatype(v)) {

                    //specific java native Class
                    set.add(this.datatypeManager.convertLiteralToDataype((Literal) v));

                    //else it is an istance
                } else {
                    //gets the class name from the triple store
                    parameterClassName = this.getClassFromBase(connection, v.stringValue(), contexts);

                    //gets a new instance with its properties not loaded
                    set.add(this.getNotLoadedObject(v.stringValue(), parameterClassName));
                }

                //invokes the method with the converted parameter
                setterMethod.invoke(o, set);

                //calls the setInnerModifiedFields to erase the modified fields
                //of the instance (update mechanics)
                ((JOINTResource) o).setInnerModifiedFields(new ArrayList<String>());

            }
            result.put(subjectURI, o);
        }
        stts.close();
        return new ArrayList<>(result.values());
    }

    public List<Object> convertCollectionOriginalForImpl3(RepositoryConnection connection, List<Statement> graphQuery, Class clazz, URI... contexts) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        HashMap<String, List<Method>> methodMap = new HashMap<>();

        //constructs a query to get all information about the objects that will
        //be parsed
        //gets the concrete desired class
        Class classImpl = Class.forName(clazz.getName() + SUF_IMPL_CLASS);

        //gets all methods of the concrete class
        Method[] allMethodsClassImpl = classImpl.getMethods();

        //populating map between IRI and methods of concrete class
        for (Method m : allMethodsClassImpl) {
            try {
                String propertyURI = m.getAnnotation(Iri.class).value();
                List<Method> mList = (methodMap.containsKey(propertyURI)) ? methodMap.get(propertyURI) : new ArrayList<Method>();
                mList.add(m);
                methodMap.put(propertyURI, mList);
            } catch (NullPointerException e) {
            }
        }

        //iterates the previous graph result
        for (Statement statement : graphQuery) {
            if (!methodMap.containsKey(statement.getPredicate().toString())) {
                continue;
            }
            String subjectURI = statement.getSubject().stringValue();

            Object o = null;
            if (!result.containsKey(subjectURI)) {
                o = classImpl.newInstance();

                //casts the object to the upper class JOINTResource and
                //calls the methods setURI and setLazyLoaded
                ((JOINTResource) o).setURI(subjectURI);
                ((JOINTResource) o).setLazyLoaded(true);
            } else {

                o = result.get(subjectURI);

            }

            List<Method> methodList = methodMap.get(statement.getPredicate().toString());

            Method setterMethod = null;
            Method getterMethod = null;

            // defining getter and setter methods
            for (Method m : methodList) {
                //ignores Java supress warning check
                m.setAccessible(true);

                if (m.getName().startsWith(PREF_SETTER)) {
                    setterMethod = m;
                } else {
                    getterMethod = m;
                }
            }

            //gets the name of the method parameter class
            String parameterClassName = setterMethod.getParameterTypes()[0].getName();

            //gets the Value from the object of this property
            Value objValue = statement.getObject();

            //gets the assiciated URI
            String valueURI = objValue.stringValue();

            //if the property is functional, it enters in the first if
            // else it has multi values
            if (!parameterClassName.equals(SET_CLASS)) {

                //it the valueURI is not empty
                if (!valueURI.isEmpty()) {

                    //if it is a datatype
                    if (this.datatypeManager.isDatatype(objValue)) {
                        //mapps to the an specific java native Class  
                        setterMethod.invoke(o, this.datatypeManager.
                                convertLiteralToDataype((Literal) objValue, parameterClassName));
                        //else it is an istance
                    } else {
                        //gets the class name from the triple store
                        parameterClassName = this.getClassFromBase(connection, valueURI, contexts);

                        //gets a new instance with its properties not loaded                                
                        setterMethod.invoke(o, this.
                                getNotLoadedObject(valueURI, parameterClassName));
                    }
                }
            } else {
                Set<Object> set = (Set<Object>) getterMethod.invoke(o);
                if (set == null) {
                    set = new HashSet<>();
                }
                //gets the first value for type searching
                Value v = statement.getObject();
                //if it is a datatype
                if (this.datatypeManager.isDatatype(v)) {

                    //specific java native Class
                    set.add(this.datatypeManager.convertLiteralToDataype((Literal) v));

                    //else it is an istance
                } else {
                    //gets the class name from the triple store
                    parameterClassName = this.getClassFromBase(connection, v.stringValue(), contexts);

                    //gets a new instance with its properties not loaded
                    set.add(this.getNotLoadedObject(v.stringValue(), parameterClassName));
                }

                //invokes the method with the converted parameter
                setterMethod.invoke(o, set);

                //calls the setInnerModifiedFields to erase the modified fields
                //of the instance (update mechanics)
                ((JOINTResource) o).setInnerModifiedFields(new ArrayList<String>());

            }
            result.put(subjectURI, o);
        }
        return new ArrayList<>(result.values());
    }

    /**
     * Gets all the contexts from quadstore.
     *
     * @param connection receives an object of connection with the repository
     * @return an URI list
     * @throws org.openrdf.repository.RepositoryException occurs error in the
     * connection with the database.
     */
    public List<java.net.URI> getContexts(RepositoryConnection connection) throws RepositoryException {
        //creates a variable to store temporarialy the results
        List<java.net.URI> results = new ArrayList<>();
        RepositoryResult<Resource> contexts = connection.getContextIDs();
        while (contexts.hasNext()) {
            results.add(java.net.URI.create(contexts.next().stringValue()));
        }
        contexts.close();
        return results;
    }
}
