package wwwc.nees.joint.module.kao.retrieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONString;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
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

/**
 * @author williams
 */
public class GraphQueryToJSONLD implements RDFHandler, JSONString {

    // VARIABLES
    // -------------------------------------------------------------------------
    //Creates a variable to store the final results as JSONLD
    private final JSONObject results;
    //Creates a variable to store the the JSON object @graph from JSONLD
    private final JSONObject results_graph;
    //
    private final JSONObject results_removed;
    private final List<String[]> triplesWithObjects;
    private final Map<String, Boolean> areArray;
    private boolean asJSONArray = true;
    private BidiMap<String, String> predicates;

    private final RepositoryConnection connection;

    public GraphQueryToJSONLD(RepositoryConnection connection) {
        predicates = new DualHashBidiMap<>();
        results = new JSONObject();
        try {
            results.put("@context", new JSONObject());
        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
        results_graph = new JSONObject();
        results_removed = new JSONObject();
        triplesWithObjects = new ArrayList<>();
        areArray = new HashMap<>();

        this.connection = connection;
    }

    public void setAsJSONArray(boolean bool) {
        this.asJSONArray = bool;
    }

    @Override
    public String toJSONString() {
        return results.toString();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            try {
                // Creates the query for retrieving the predicates and its range
                StringBuilder query = new StringBuilder();
                query.append("select ?pred ?range ")
                        .append("where{values ?pred{");
                for (String predicate : predicates.values()) {
                    query.append("<").append(predicate).append("> ");
                }
                query.append("}?pred rdfs:range ?range.}");
                TupleQuery prepareTupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
                // Performs the query
                TupleQueryResult evaluate = prepareTupleQuery.evaluate();
                while (evaluate.hasNext()) {
                    BindingSet next = evaluate.next();
                    String predicateURI = next.getBinding("pred").getValue().stringValue();
                    String predicatePrefix = predicates.removeValue(predicateURI);
                    handlePredicate(predicatePrefix, predicateURI, next.getBinding("range").getValue().stringValue());
                }
                evaluate.close();
                // Insert into the JSON the predicates that doesn't were found in the repository                
                for (Map.Entry<String, String> pred : predicates.entrySet()) {
                    handlePredicate(pred.getKey(), pred.getValue(), null);
//                    handleTypeFromSubject(pred.getKey(), pred.getValue());
                }
                predicates.clear();
            } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                throw new RDFHandlerException(ex);
            }

            // Handle all triples that can have objects instead of Literal.
            handleTriplesWithObject();

            /**
             * Now will be converted as JSON Array or maintained as JSON Object.
             * For this, each property from JSON OBJECT will be parsed as a JSON
             * OBJECT and will be added to JSON ARRAY called "results"
             */
            Iterator<String> keys_aux = results_graph.keys();
            while (keys_aux.hasNext()) {
                String subject = keys_aux.next();
                JSONObject next = results_graph.getJSONObject(subject);
                ValueFactory vf = connection.getValueFactory();
                URI subj = vf.createURI(next.getString("@id"));
                RepositoryResult<Statement> statements = connection.getStatements(subj, RDF.TYPE, null, true);
                while (statements.hasNext()) {
                    Statement statement = statements.next();
                    results.getJSONObject("@context").accumulate("@type", statement.getObject());
                }
                statements.close();
                break;
            }
            if (asJSONArray) {
                Iterator<String> keys = results_graph.keys();
                while (keys.hasNext()) {
                    String subject = keys.next();
                    JSONObject next = results_graph.getJSONObject(subject);
                    results.accumulate("@graph", next);
                }
            } else {
                results.put("@graph", results_graph);
            }
        } catch (JSONException | RepositoryException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {

    }

    /**
     * Add a JSON Object with the keywords <b>@id</b> and <b>@type</b>
     */
    private void handlePredicate(String predicatePrefix, String predicateURI, String type) throws JSONException, RepositoryException {
        if (type == null) {
            type = handleSubjectTypeFromPredicate(predicatePrefix);
        }
        JSONObject cont = new JSONObject()
                .put("@id", predicateURI)
                .put("@type", type);
        results.getJSONObject("@context").put(predicatePrefix, cont);
    }

    /**
     * Handle the prefix and the URI from predicate to avoid duplicates
     */
    private String handlePredicatePrefix(String predicatePrefix, String predicateURI) {
        if (predicates.containsKey(predicatePrefix)) {
            String get = predicates.get(predicatePrefix);
            if (!predicateURI.equals(get)) {
                predicatePrefix = predicatePrefix.concat(String.valueOf(predicates.size()));
            }
        }
        predicates.put(predicatePrefix, predicateURI);
        return predicatePrefix;
    }

    @Override
    public void handleStatement(Statement stt) throws RDFHandlerException {
        try {
            String subject = stt.getSubject().stringValue();
            URI predicate = stt.getPredicate();
            Value value = stt.getObject();
            String object = value.stringValue();
            String predicate_prefix = handlePredicatePrefix(predicate.getLocalName(), predicate.stringValue());

            //Validate the URI and it add to list of triples which possible represent an object            
            if (!(value instanceof Literal)) {
                triplesWithObjects.add(new String[]{subject, predicate_prefix, object});
            }

            JSONObject jsonObject;
            if (results_graph.has(subject)) {
                jsonObject = results_graph.getJSONObject(subject);
                if (jsonObject.has(predicate_prefix)) {
                    if (areArray.containsKey(predicate_prefix)) {
                        Boolean isArray = areArray.get(predicate_prefix);
                        if (false == isArray) {
                            converterObjectToArray(predicate_prefix);
                        }
                    } else {
                        areArray.put(predicate_prefix, false);
                    }
                    jsonObject.accumulate(predicate_prefix, object);

                } else if (areArray.containsKey(predicate_prefix)) {
                    jsonObject.append(predicate_prefix, object);
                } else {
                    jsonObject.put(predicate_prefix, object);
                }
            } else {
                jsonObject = new JSONObject();
                jsonObject.put("@id", subject);
                if (areArray.containsKey(predicate_prefix)) {
                    jsonObject.append(predicate_prefix, object);
                } else {
                    jsonObject.put(predicate_prefix, object);
                }
                results_graph.put(subject, jsonObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
    }

    private void handleTriplesWithObject() throws JSONException {
        for (String[] triple : triplesWithObjects) {
            JSONObject object;
            if (results_graph.has(triple[2])) {
                object = (JSONObject) results_graph.remove(triple[2]);
                results_removed.put(triple[2], object);
            } else if (results_removed.has(triple[2])) {
                object = results_removed.getJSONObject(triple[2]);
            } else {
                continue;
            }

            JSONObject jsonObject = results_graph.getJSONObject(triple[0]);
            if (areArray.containsKey(triple[1])) {
                JSONArray jsonArray = jsonObject.getJSONArray(triple[1]);
                jsonArray.remove(triple[2]);
                jsonArray.put(object);
            } else {
                jsonObject.put(triple[1], object);
            }
        }
    }

    private String handleSubjectTypeFromPredicate(String predicatePrefix) throws JSONException, RepositoryException {
        Iterator<String> keys = results_graph.keys();
        String id = null;
        while (keys.hasNext()) {
            if (results_graph.getJSONObject(keys.next()).has(predicatePrefix)) {
                String s = results_graph.getJSONObject(keys.next()).get(predicatePrefix).toString();
                if (s.contains("@id")) {
                    id = new JSONObject(s).getString("@id");
                } else {
                    id = s;
                }
                break;
            }
        }
        URI subj = connection.getValueFactory().createURI(id);
        RepositoryResult<Statement> statements = connection.getStatements(subj, RDF.TYPE, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            statements.close();
            return statement.getObject().stringValue();
        }
        statements.close();
        return null;
    }

    /**
     * Converts all the objects that contains the <b>predicate</b> key as an
     * json object or literal to an json array
     *
     * @param predicate
     * @throws org.codehaus.jettison.json.JSONException
     */
    private void converterObjectToArray(String predicate) throws JSONException {
        Iterator<String> keys = results_graph.keys();
        while (keys.hasNext()) {
            JSONObject jsonObject_aux = results_graph.getJSONObject(keys.next());
            if (jsonObject_aux.has(predicate)) {
                Object res = jsonObject_aux.get(predicate);
                if (!(res instanceof JSONArray)) {
                    jsonObject_aux.put(predicate, new JSONArray().put(res));
                }
            }
        }
        areArray.put(predicate, true);
    }
}
