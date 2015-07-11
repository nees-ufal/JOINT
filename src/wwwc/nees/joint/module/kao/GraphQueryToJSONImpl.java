package wwwc.nees.joint.module.kao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public void startRDF() throws RDFHandlerException {
        results = new JSONObject();
        results_removed = new JSONObject();
        triplesWithObjects = new ArrayList<>();
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
            triplesWithObjects.add(new String[]{subject, predicate, object});
        } catch (MalformedURLException ex) {
        }

        if (results.has(subject)) {
            try {
                results.getJSONObject(subject).accumulate(predicate, object);
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                json_aux = new JSONObject();
                json_aux.accumulate(predicate, object);
                results.put(subject, json_aux);
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
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
