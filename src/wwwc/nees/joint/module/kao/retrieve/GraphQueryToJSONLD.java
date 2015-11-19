package wwwc.nees.joint.module.kao.retrieve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import wwwc.nees.joint.module.kao.DatatypeManager;

/**
 * @author williams
 */
public class GraphQueryToJSONLD implements RDFHandler {

    // CONSTANTS    
    private final String CONTEXT = "@context";
    private final String OBJECT = "@object";
    private final String GRAPH = "@graph";
    private final String ID = "@id";
    private final String TYPE = "@type";

    // VARIABLES
    // -------------------------------------------------------------------------
    //Creates a variable to store the final results as JSONLD
    private final JSONObject results;
    //Creates a variable to store the the JSON object @graph from JSONLD
    private JSONObject results_context;
    private JSONObject results_graph;
    private JSONObject results_object;
    //
    private final List<String[]> triplesWithObjects;
    private final List<String> areArray;
    private final List<Feature> features;
    private final RepositoryConnection connection;
    private final ValueFactory vf;

    public GraphQueryToJSONLD(RepositoryConnection connection, Feature... features) throws JSONException {
        results = new JSONObject()
                .put(CONTEXT, new JSONObject())
                .put(GRAPH, new JSONObject())
                .put(OBJECT, new JSONObject());

        this.features = Arrays.asList(features);
        triplesWithObjects = new ArrayList<>();
        areArray = new ArrayList<>();

        this.connection = connection;
        this.vf = connection.getValueFactory();
    }

    public JSONObject getResults() {
        return results;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        try {
            results_context = results.getJSONObject(CONTEXT);
            results_graph = results.getJSONObject(GRAPH);
            results_object = results.getJSONObject(OBJECT);
        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        DatatypeManager datatypeMng = DatatypeManager.getInstance();
        try {
            Iterator<String> predicates_names = results_context.keys();
            if (predicates_names.hasNext()) {
                // Creates the query for retrieving the predicates and its range
                StringBuilder query = new StringBuilder();
                query.append("select ?pred ?range ")
                        .append("where{values ?pred{");
                while (predicates_names.hasNext()) {
                    String predicate_prefix = predicates_names.next();
                    String predicate_uri = results_context.getJSONObject(predicate_prefix).getString(ID);
                    query.append("<").append(predicate_uri).append("> ");
                }
                query.append("}?pred rdfs:range ?range.}");
                try {
                    TupleQuery prepareTupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
                    // Performs the query
                    TupleQueryResult evaluate = prepareTupleQuery.evaluate();
                    while (evaluate.hasNext()) {
                        BindingSet next = evaluate.next();
                        String predicateURI = next.getBinding("pred").getValue().stringValue();
                        URI predicate = new URIImpl(predicateURI);
                        String range = next.getBinding("range").getValue().stringValue();
                        addTypeOfPredicate(predicate.getLocalName(), range);
                        if (datatypeMng.namespacesClass.containsKey(range)) {
                            triplesWithObjects.removeIf((value) -> (value[1].equals(predicate.getLocalName())));
                        }
                    }
                    evaluate.close();
                } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                    throw new RDFHandlerException(ex);
                }
            }

            if (!features.contains(Feature.NOT_HANDLE_TRIPLES_WITH_OBJECTS)) {
                // Handles all triples that can have objects instead of Literal.
                handleTriplesWithObject();
            }
            /**
             * Now will be converted as JSON Array or maintained as JSON Object.
             * For this, each property from JSON OBJECT will be parsed as a JSON
             * OBJECT and will be added to JSON ARRAY called "results"
             */
            if (features.contains(Feature.PRINT_GRAPH_AS_JSONARRAY)) {
                JSONArray array = new JSONArray();
                Iterator<String> keys = results_graph.keys();
                while (keys.hasNext()) {
                    String subject = keys.next();
                    JSONObject jsonObject = results_graph.getJSONObject(subject);
                    //verify if some keys from JSONObject are array
                    convertObjectToArray_Object(jsonObject);

                    array.put(jsonObject.put(ID, subject));

                }
                convertObjectToArray_Objects(results_object);
                results.put(GRAPH, array);
            } else {
                convertObjectToArray_Objects(results_object);
                convertObjectToArray_Objects(results_graph);
            }

        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        if (!results_context.has(prefix)) {
            try {
                results_context.put(prefix, new JSONObject().put(ID, uri));
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addTypeOfPredicate(String predicatePrefix, String type) throws JSONException, RepositoryException {
        results_context.getJSONObject(predicatePrefix).put(TYPE, type);
    }

    @Override
    public void handleStatement(Statement stt) throws RDFHandlerException {
        try {
            String subject = stt.getSubject().stringValue();
            String predicate_prefix = stt.getPredicate().getLocalName();
            String predicate_uri = stt.getPredicate().stringValue();
            Value value = stt.getObject();
            String object = value.stringValue();
            handleNamespace(predicate_prefix, predicate_uri);

            //Validate the URI and it add to list of triples which possible represent an object            
            if (!(value instanceof Literal)) {
                triplesWithObjects.add(new String[]{subject, predicate_prefix, object});
            }

            JSONObject jsonObject;

            if (results_graph.has(subject)) {
                jsonObject = results_graph.getJSONObject(subject);
                if (jsonObject.has(predicate_prefix) && !areArray.contains(predicate_prefix)) {
                    areArray.add(predicate_prefix);
                }
                jsonObject.accumulate(predicate_prefix, object);
            } else {
                jsonObject = new JSONObject().put(predicate_prefix, object);
                results_graph.put(subject, jsonObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
    }

    /**
     * Handles all triples that can have objects instead of Literal.
     */
    private void handleTriplesWithObject() throws JSONException {
        for (int i = 0; i < triplesWithObjects.size(); i++) {
            //gets the triple that can have an object
            String[] triple = triplesWithObjects.get(i);
            //gets the object resource from triple
            String triple_object = triple[2];
            if (results_graph.has(triple_object)) {
                //Gets and removes the instance of the graph results
                JSONObject obj = (JSONObject) results_graph.remove(triple_object);
                if (!results_object.has(triple_object)) {

                    results_object.put(triple_object, obj);

                    //removes all the triples that contains the same uri as object
                    triplesWithObjects.removeIf((t) -> (t[2].equals(triple_object)));
                    //decrease 1 in the counter to avoid error in the index from the list 
                    i--;
                }
            }
        }
    }

    public String getTypeOfSubject(String subject) throws RepositoryException {
        RepositoryResult<Statement> statements = connection.getStatements(vf.createURI(subject), RDF.TYPE, null, true);
        String type = null;
        while (statements.hasNext()) {
            Statement statement = statements.next();
            type = statement.getObject().stringValue();
            break;
        }
        statements.close();
        return type;
    }

    /**
     * Converts all the objects that contains the <b>predicate</b> key as an
     * json object or literal to an json array
     *
     * @param predicate
     * @throws org.codehaus.jettison.json.JSONException
     */
    private void convertObjectToArray_Object(JSONObject object) throws JSONException {
        for (String predicate : areArray) {
            if (object.has(predicate)) {
                Object ob = object.get(predicate);
                if (!(ob instanceof JSONArray)) {
                    object.put(predicate, new JSONArray().put(ob));
                }
            }
        }
    }

    private void convertObjectToArray_Objects(JSONObject object) throws JSONException {
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            JSONObject jsonObject = object.getJSONObject(keys.next());
            for (String predicate : areArray) {
                if (jsonObject.has(predicate)) {
                    Object ob = jsonObject.get(predicate);
                    if (!(ob instanceof JSONArray)) {
                        jsonObject.put(predicate, new JSONArray().put(ob));
                    }
                }
            }
        }
    }
}
