package wwwc.nees.joint.module.kao.retrieve;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONString;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * @author williams
 */
public class TupleQueryToJSONImpl implements TupleQueryResultHandler, JSONString {

    private final JSONArray json = new JSONArray();
    private JSONObject json_aux;
    private List<String> list;

    @Override
    public void handleBoolean(boolean bln) throws QueryResultHandlerException {
    }

    @Override
    public void handleLinks(List<String> list) throws QueryResultHandlerException {
    }

    @Override
    public void startQueryResult(List<String> list) throws TupleQueryResultHandlerException {
        this.list = list;
    }

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
    }

    @Override
    public void handleSolution(BindingSet bs) throws TupleQueryResultHandlerException {
        json_aux = new JSONObject();
        for (String s : list) {
            try {
                json_aux.accumulate(s, bs.getValue(s).stringValue());
            } catch (JSONException ex) {
                Logger.getLogger(TupleQueryToJSONImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        json.put(json_aux);
    }

    @Override
    public String toJSONString() {
        return json.toString();
    }
}
