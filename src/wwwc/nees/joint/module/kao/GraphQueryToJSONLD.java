package wwwc.nees.joint.module.kao;

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
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author williams
 */
public class GraphQueryToJSONLD implements RDFHandler, JSONString {

    // VARIABLES
    // -------------------------------------------------------------------------
    //Creates a variable to store the final results as JSONLD
    private JSONObject results;
    //Creates a variable to store the the JSON object @graph from JSONLD
    private JSONObject results_graph;
    //
    private JSONObject results_removed;
    private List<String[]> triplesWithObjects;
    private Map<String, Boolean> areArray;
    private boolean asJSONArray = true;
    private BidiMap<String, String> predicates;

    private final RepositoryConnection connection;

    public GraphQueryToJSONLD(RepositoryConnection connection) {
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
        try {
            predicates = new DualHashBidiMap<>();

            results = new JSONObject();
            results.put("@context", new JSONObject());
            results_graph = new JSONObject();
            results_removed = new JSONObject();
            triplesWithObjects = new ArrayList<>();
            areArray = new HashMap<>();
        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        StringBuilder query = new StringBuilder();
        query.append("select ?pred ?range ")
                .append("where{values ?pred {");
        for (String predicate : predicates.values()) {
            query.append("<").append(predicate).append("> ");
        }
        query.append("} ?pred rdfs:range ?range.}");
        try {
            TupleQuery prepareTupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
            TupleQueryResult evaluate = prepareTupleQuery.evaluate();
            while (evaluate.hasNext()) {
                BindingSet next = evaluate.next();
                handlePredicate(next.getBinding("pred").getValue().stringValue(), next.getBinding("range").getValue().stringValue());
            }
            evaluate.close();
            if (!predicates.isEmpty()) {
                for (Map.Entry<String, String> entry : predicates.entrySet()) {
                    handlePredicate(entry.getValue(), null);
                }
            }
        } catch (JSONException | RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String[] triples : triplesWithObjects) {
            JSONObject object;
            try {
                if (results_graph.has(triples[2])) {
                    object = (JSONObject) results_graph.remove(triples[2]);
                    results_removed.put(triples[2], object);
                } else if (results_removed.has(triples[2])) {
                    object = results_removed.getJSONObject(triples[2]);
                } else {
                    continue;
                }

                JSONObject jsonObject = results_graph.getJSONObject(triples[0]);
                if (areArray.containsKey(triples[1])) {
                    JSONArray jsonArray = jsonObject.getJSONArray(triples[1]);
                    jsonArray.remove(triples[2]);
                    jsonArray.put(object);
                } else {
                    jsonObject.put(triples[1], object);
                }
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        triplesWithObjects.clear();
        /**
         * Now will be converted as JSON Array or maintained as JSON Object. For
         * this, each property from JSON OBJECT will be parsed as a JSON OBJECT
         * and will be added to JSON ARRAY called "results"
         */
        try {
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
        } catch (JSONException ex) {
            Logger.getLogger(GraphQueryToJSONLD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleNamespace(String string, String string1) throws RDFHandlerException {
    }

    private void handlePredicate(String predicateURI, String type) throws JSONException {
        if (type == null) {
            type = "";
        }
        JSONObject cont = new JSONObject()
                .put("@id", predicateURI)
                .put("@type", type);

        results.getJSONObject("@context").put(predicates.removeValue(predicateURI), cont);
    }

    private String handlePredicatePrefix(String predicatePrefix, String predicateURI) {
        if (predicates.containsValue(predicateURI)) {
            return predicatePrefix;
        } else if (predicates.containsKey(predicatePrefix)) {
            predicatePrefix = predicatePrefix.concat(String.valueOf(predicates.size()));
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
