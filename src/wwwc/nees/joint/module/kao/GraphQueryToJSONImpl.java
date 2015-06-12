package wwwc.nees.joint.module.kao;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author williams
 */
public class GraphQueryToJSONImpl implements RDFHandler {

    private JSONObject json;
    private JSONObject json_aux;
    private Map<Resource, Map<String, Map<String, Object>>> map;

    @Override
    public void startRDF() throws RDFHandlerException {
        json = new JSONObject();
        map = new HashMap<>();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
    }

    @Override
    public void handleStatement(Statement stt) throws RDFHandlerException {
//        Map<String, Map<String, Object>> pred_ob;
//        if (map.containsKey(stt.getSubject())) {
//            pred_ob = map.get(stt.getSubject());
//        } else {
//            pred_ob = new HashMap<>();
//        }
//        
//        if(stt.getObject().stringValue().startsWith("http")){}

        String subject = stt.getSubject().stringValue();
        String predicate = stt.getPredicate().getLocalName();
        String object = stt.getObject().stringValue();
        if (json.has(subject)) {
            try {
                json.getJSONObject(subject).accumulate(predicate, object);
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                json_aux = new JSONObject();
                json_aux.accumulate(predicate, object);
                json.put(subject, json_aux);
            } catch (JSONException ex) {
                Logger.getLogger(GraphQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
    }

    public String getJSONAsString() {
        return json.toString();
    }
}
