package wwwc.nees.joint.module.kao;

import info.aduna.iteration.Iterations;
import java.io.ByteArrayOutputStream;
import wwwc.nees.joint.model.OWLUris;
import wwwc.nees.joint.model.RDFUris;
import wwwc.nees.joint.model.SWRLUris;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Class which implements QueryRunner Interface for performing SPARQL queries in
 * the repository.
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class SPARQLQueryRunnerImpl implements QueryRunner {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    private Repository repository;
    private DatatypeManager datatypeManager;
    private RetrieveOperations retrieveOp;
    private static final String CALLRET = "callret-";
    // Default namespaces to use in the SPARQL queries
    public static final String DEFAULT_PREFIXES = "PREFIX rdf:<" + RDFUris.RDF
            + ">\n PREFIX owl:<" + OWLUris.OWL
            + ">\n PREFIX rdfs:<" + RDFUris.RDFS
            + ">\n PREFIX xsd:<" + RDFUris.XSD
            + ">\n PREFIX swrl:<" + SWRLUris.SWRL + ">\n";

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Simple constructor that receives an object connection of the repository.
     *
     * @param connection the <code>ObjectConnection</code> with the repository.
     *
     */
    public SPARQLQueryRunnerImpl(Repository repo) {
        this.repository = repo;
        this.datatypeManager = DatatypeManager.getInstance();
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Performs SPARQL queries in the repository, returning a single result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return object <code>Object</code> result.
     */
    @Override
    public Object executeQueryAsSingleResult(String query, URI... contexts) {
        Object returnObject = null;

        TupleQuery tupleQuery;
        try {
            RepositoryConnection conn = this.repository.getConnection();
            conn.setAutoCommit(false);

            try {

                this.retrieveOp = new RetrieveOperations(conn);

                // Creates the query based on the parameter
                tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

                // Performs the query
                TupleQueryResult result = tupleQuery.evaluate();

                if (!result.hasNext()) {
                    return null;
                }

                if (result.getBindingNames().size() > 1) {
                    List<String> names = result.getBindingNames();

                    List<String> sortedNames = new ArrayList<>();

                    Map<Integer, String> positions = new HashMap<>();
                    String[] realPositions = new String[result.getBindingNames().size()];

                    for (String string : names) {
                        Integer index = query.indexOf("?" + string);
                        if (index == -1) {
                            realPositions[Integer.getInteger(string.replace(CALLRET, ""))] = string;
                        } else {
                            positions.put(index, string);
                        }
                    }
                    List<Integer> listaKeys = new ArrayList<>(positions.keySet());
                    Collections.sort(listaKeys);

                    for (Integer integer : listaKeys) {
                        String value = positions.get(integer);
                        sortedNames.add(value);
                    }

                    for (int i = 0; i < realPositions.length; i++) {
                        if (realPositions[i] != null) {
                            sortedNames.add(i, realPositions[i]);
                        }
                    }

                    BindingSet binSet = result.next();

                    Object[] retorno = new Object[binSet.size()];
                    int counter = 0;

                    for (String bName : sortedNames) {

                        Binding binding = binSet.getBinding(bName);

                        if (binding == null) {
                            retorno[counter] = null;
                            counter++;
                            continue;
                        }

                        Value re = binSet.getBinding(bName).getValue();

                        if (re instanceof Literal) {
                            retorno[counter] = datatypeManager.convertLiteralToDataype((Literal) re);
                        } else {
                            String className = this.retrieveOp.getClassFromBase(re.stringValue(), contexts);
                            retorno[counter] = this.retrieveOp.convertOriginalForImpl(re.stringValue(), Class.forName(className), contexts);
                        }
                        counter++;
                    }
                    System.out.println(retorno);
                    return retorno;
                } else {

                    BindingSet binSet = result.next();
                    Value re = binSet.iterator().next().getValue();
                    if (re == null) {
                        return null;
                    } else if (re instanceof Literal) {
                        returnObject = datatypeManager.convertLiteralToDataype((Literal) re);
                    } else {

                        String className = this.retrieveOp.getClassFromBase(re.stringValue(), contexts);
                        if (className.equals("java.lang.Object")) {
                            returnObject = re.stringValue();
                        } else {
                            returnObject = this.retrieveOp.convertOriginalForImpl(re.stringValue(), Class.forName(className), contexts);
                        }
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
        }
        // Gets the first and only index
        return returnObject;
    }

    /**
     * Performs SPARQL queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>List<Object></code> a java.util.List with the results.
     */
    @Override
    public List<Object> executeQueryAsList(String query, URI... contexts) {

        // Creates a java.util.List
        List<Object> resultList = new ArrayList<>();

        TupleQuery tupleQuery;
        try {

            // Gets a connection from repository
            RepositoryConnection conn = this.repository.getConnection();
            // initiates the transaction
            conn.setAutoCommit(false);
            // calls the manager from retrieve operations
            this.retrieveOp = new RetrieveOperations(conn);
            try {
                // Creates the query based on the parameter
                tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

                // Performs the query
                TupleQueryResult result = tupleQuery.evaluate();

                String className = "";

                if (result.getBindingNames().size() > 1) {
                    List<String> names = result.getBindingNames();

                    List<String> sortedNames = new ArrayList<>();

                    Map<Integer, String> positions = new HashMap<>();
                    String[] realPositions = new String[result.getBindingNames().size()];

                    for (String string : names) {
                        Integer index = query.indexOf("?" + string);
                        if (index == -1) {
                            realPositions[Integer.parseInt(string.replace(CALLRET, ""))] = string;
                        } else {
                            positions.put(index, string);
                        }
                    }
                    List<Integer> listaKeys = new ArrayList<>(positions.keySet());
                    Collections.sort(listaKeys);

                    for (Integer integer : listaKeys) {
                        String value = positions.get(integer);
                        sortedNames.add(value);
                    }

                    for (int i = 0; i < realPositions.length; i++) {
                        if (realPositions[i] != null) {
                            sortedNames.add(i, realPositions[i]);
                        }
                    }

                    while (result.hasNext()) {
                        BindingSet binSet = result.next();

                        Object[] retorno = new Object[binSet.size()];
                        int counter = 0;

                        for (String bName : sortedNames) {

                            Binding binding = binSet.getBinding(bName);

                            if (binding == null) {
                                retorno[counter] = null;
                                counter++;
                                continue;
                            }

                            Value re = binSet.getBinding(bName).getValue();

                            if (re instanceof Literal) {
                                retorno[counter] = datatypeManager.convertLiteralToDataype((Literal) re);
                            } else {
                                className = this.retrieveOp.getClassFromBase(re.stringValue(), contexts);
                                retorno[counter] = this.retrieveOp.convertOriginalForImpl(re.stringValue(), Class.forName(className), contexts);
                            }
                            counter++;
                        }
                        resultList.add(retorno);
                    }

                } else {

//                    boolean identified = false;
//                    boolean literal = false;
                    //checks if there is any result
                    if (result.hasNext()) {

                        //gets the first result for type checking
                        BindingSet binSet = result.next();
                        //gets the value of the first row
                        Value re = binSet.iterator().next().getValue();

                        //if the element is a Literal, else it is an instance
                        if (re instanceof Literal) {
                            //creates the collection of literal
                            List<Literal> literals = new ArrayList<>();
                            //adds the first element
                            literals.add((Literal) re);

                            while (result.hasNext()) {
                                //gets the element in the row and casts its value to Literal
                                BindingSet binding = result.next();
                                Literal lit = (Literal) binding.iterator().next().getValue();
                                //adds in the collection
                                literals.add(lit);
                            }
                            result.close();
                            //returns the collection already parsed
                            return this.datatypeManager.convertCollectionOfLiteralToDataypes(literals);
                        } else {
                            //checks which class type the element belongs
                            className = this.retrieveOp.getClassFromBase(re.stringValue(), contexts);

                            //creates the collection of objects
                            List<String> instancesURI = new ArrayList<>();
                            //adds the first element
                            instancesURI.add(re.stringValue());

                            while (result.hasNext()) {
                                //gets the element in the row and casts its value to Literal
                                BindingSet binding = result.next();
                                String uri = binding.iterator().next().getValue().stringValue();
                                //adds in the collection
                                instancesURI.add(uri);
                            }
                            result.close();
                            return this.retrieveOp.convertCollectionOriginalForImpl(instancesURI, Class.forName(className), contexts);
                        }
                    }
                }
                result.close();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
        }
        return resultList;
    }

    @Override
    public List<Object> executeQueryAsList2(String query, URI... contexts) {

        // Creates a java.util.List
        List<Object> resultList = new ArrayList<>();
        try {

            // Gets a connection from repository
            RepositoryConnection conn = this.repository.getConnection();
            // initiates the transaction
            conn.setAutoCommit(false);
            // calls the manager from retrieve operations

            GraphQueryResult result;

            String clause_PREFIX = "";
            StringBuilder queryBuilder = new StringBuilder();
            try {
                if (query.startsWith("PREFIX")) {
                    Pattern patter_PREFIX = Pattern.compile("(([\\s]?)(PREFIX)([\\s]+)([\\w]+)(\\:)(<([a-zA-Z]{3,})://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?>))+");
                    Matcher matcher_PREFIX = patter_PREFIX.matcher(query);
                    if (matcher_PREFIX.find()) {
                        clause_PREFIX = matcher_PREFIX.group();
                        queryBuilder.append(clause_PREFIX);
                        query = query.replace(clause_PREFIX, "");
                    }
                }
                Pattern pattern_SELECT = Pattern.compile("(\\s+\\?[^\\s]+)");
                Matcher matcher_SELECT = pattern_SELECT.matcher(query);
                if (matcher_SELECT.find()) {
                    String group = matcher_SELECT.group();

                    queryBuilder.append(" CONSTRUCT {").append(group).append(" ?p ?o} WHERE{")
                            .append(group).append(" ?p ?o{")
                            .append(query).append("}}");

                }
                // Creates the query based on the parameter
                GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryBuilder.toString());

                // Performs the query
                result = graphQuery.evaluate();
                List<Statement> resultado = Iterations.asList(result);
                result.close();
                String className = "";
                this.retrieveOp = new RetrieveOperations(conn);
                for (Statement st : resultado) {
                    if (st.getPredicate().toString().equals(RDF.TYPE.toString())) {
                        className = retrieveOp.getClassFromBase1(st.getObject().stringValue());
                        result.close();
                        break;
                    }
                }
                return this.retrieveOp.convertCollectionOriginalForImpl3(resultado, Class.forName(className), contexts);
            } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                conn.close();

            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
        }
        return resultList;
    }

    @Override
    public String executeTupleQueryAsJSON(String query) {

//        ByteArrayOutputStream resultsJSON = new ByteArrayOutputStream();
//        SPARQLResultsJSONWriter jsonWriter = new SPARQLResultsJSONWriter(resultsJSON);
        TupleQueryToJSONImpl jsonWriter = new TupleQueryToJSONImpl();
        try {
            // Gets a connection from repository            
            RepositoryConnection conn = this.repository.getConnection();
            // initiates the transaction
            conn.setAutoCommit(false);
            try {
                // Creates the query based on the parameter
                TupleQuery graphQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

                // Performs the query
                graphQuery.evaluate(jsonWriter);

            } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);

            } finally {
                conn.close();
                return jsonWriter.getJSONAsString();
            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
            return jsonWriter.getJSONAsString();
        }
    }

    @Override
    public String executeGraphQueryAsJSON(String query) {

        GraphQueryToJSONImpl jsonWriter = new GraphQueryToJSONImpl();
        try {
            // Gets a connection from repository            
            RepositoryConnection conn = this.repository.getConnection();
            // initiates the transaction
            conn.setAutoCommit(false);
            try {
                // Creates the query based on the parameter
                GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
                // Performs the query

                graphQuery.evaluate(jsonWriter);
            } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);

            } finally {
                conn.close();
                return jsonWriter.getJSONAsString();
            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
            return jsonWriter.getJSONAsString();
        }
    }

    /**
     * Performs SPARQL queries in the repository, returning a java.util.Iterator
     * with the results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    @Override
    public Iterator<Object> executeQueryAsIterator(String query, URI... contexts) {
        // Creates a java.util.List
        List<Object> resultList = new ArrayList<>();

        try {
            // Changes the result to a java.util.List
            resultList = (List<Object>) this.executeQueryAsList(query, contexts);
        } catch (Exception e) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, e);
        }

        //Gets the iterator of the list
        return resultList.iterator();
    }

    /**
     * Performs SPARQL queries in the repository, returning a boolean with the
     * result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean<Object></code> true or false.
     */
    @Override
    public boolean executeBooleanQuery(String query) {

        boolean result = false;

        BooleanQuery objectQuery;
        try {
            RepositoryConnection conn = this.repository.getConnection();
            conn.setAutoCommit(false);
            try {
                // Creates the query based on the parameter
                objectQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);

                // Performs the query
                result = objectQuery.evaluate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
        }

        return result;
    }

    /**
     * Performs SPARQL update queries in the repository, returning a boolean
     * true if the query was performed with successful or false otherwise.
     *
     * @param query the <code>String</code> with the query to be performed.
     * @return <code>boolean</code> true or false.
     */
    @Override
    public boolean executeUpdateQuery(String query) {

        boolean result = false;

        try {
            RepositoryConnection conn = this.repository.getConnection();
            conn.setAutoCommit(false);
            try {
                // Creates the update query based on the parameter
                Update update = conn.prepareUpdate(QueryLanguage.SPARQL, query);

                // Performs the query
                update.execute();

                conn.commit();
                result = true;
            } catch (Exception e) {
                conn.rollback();
                Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                        log(Level.SEVERE, null, e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException eR) {
            Logger.getLogger(SPARQLQueryRunnerImpl.class.getName()).
                    log(Level.SEVERE, null, eR);
        }

        return result;
    }
}
