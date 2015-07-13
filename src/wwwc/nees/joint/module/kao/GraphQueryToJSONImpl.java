package wwwc.nees.joint.module.kao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author williams
 */
public class GraphQueryToJSONImpl implements RDFHandler {

    private JSONObject results;
    private JSONObject json_aux;
    private JSONObject results_removed;
    private List<String[]> triplesWithObjects;

    private Set<String> areArray;

    @Override
    public void startRDF() throws RDFHandlerException {
        results = new JSONObject();
        results_removed = new JSONObject();
        triplesWithObjects = new ArrayList<>();
        areArray = new HashSet<>();
    }

    @Override
    public void endRDF() throws RDFHandlerException {

        for (String[] triples : triplesWithObjects) {
            JSONObject object;
            try {
                if (results.has(triples[2])) {
                    object = (JSONObject) results.remove(triples[2]);
                    results_removed.put(triples[2], object);
                } else if (results_removed.has(triples[2])) {
                    object = (JSONObject) results_removed.getJSONObject(triples[2]);
                } else {
                    continue;
                }
                results.getJSONObject(triples[0]).put(triples[1], object);
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
    }

    @Override
    public void handleStatement(Statement stt) throws RDFHandlerException {
        String subject = stt.getSubject().stringValue();
        String predicate = stt.getPredicate().getLocalName();
        String object = stt.getObject().stringValue();
        //Validate the URI and it add to list of triples which possible represent an object
        try {
            new URL(object);
            if (!subject.equals(object)) {
                triplesWithObjects.add(new String[]{subject, predicate, object});
            }
        } catch (MalformedURLException ex) {
        }

        JSONObject jsonObject;
        if (results.has(subject)) {
            try {
                jsonObject = results.getJSONObject(subject);
                if (jsonObject.has(predicate)) {
                    //
                    converterObjectToArray(predicate);
                    //
                    areArray.add(predicate);
                    jsonObject.accumulate(predicate, object);

                } else if (areArray.contains((String) predicate)) {
                    jsonObject.append(predicate, object);
                } else {
                    jsonObject.put(predicate, object);
                }
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                jsonObject = new JSONObject();
                if (areArray.contains((String) predicate)) {
                    jsonObject.append(predicate, object);
                } else {
                    jsonObject.put(predicate, object);
                }
                results.put(subject, jsonObject);
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void converterObjectToArray(String predicate) throws JSONException {
        //
        Iterator<String> keys = results.keys();
        while (keys.hasNext()) {
            JSONObject jsonObject_aux = results.getJSONObject(keys.next());
            if (jsonObject_aux.has(predicate)) {
                try {
                    jsonObject_aux.getJSONArray(predicate);
                } catch (Exception e) {
                    Object value_temp = jsonObject_aux.remove(predicate);
                    jsonObject_aux.append(predicate, value_temp);
                }
            }
        }
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
    }

    public String getJSONAsString() {
        return results.toString();
    }
}
