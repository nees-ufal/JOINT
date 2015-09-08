/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.module.kao;

import java.net.MalformedURLException;
import java.net.URL;
import org.openrdf.model.URI;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author armando
 */
public class Operation {
    
    /**
     * Identifies wheter the object is an URL, number or string
     *
     * @param object is the value of an object corresponding to the triple
     * @return a string in the form of identified type (URI, number or literal)
     */
    protected static String identifyObjectType(String object) {
        StringBuilder value = new StringBuilder();

        try {
            //is an URI
            URL isURL = new URL(object);
            value.append("<").append(object).append(">");
        } catch (MalformedURLException malformed) {
            try {
                //is a number
                Float.parseFloat(object);
                value.append(object);
            } catch (NumberFormatException n) {

                boolean parseBoolean = Boolean.parseBoolean(object);
                if (parseBoolean == true) {
                    //is a boolean
                    value.append(object);
                } else {
                    //is a string
                    value.append("\"").append(object).append("\"");
                }
            }
        }
        return value.toString();
    }
    
    public static boolean askStatements(RepositoryConnection connection, String subject, String property, String object, URI... contexts) throws RepositoryException, MalformedQueryException, UpdateExecutionException, QueryEvaluationException {
        //Query verify existence of triples
        StringBuilder query = new StringBuilder();
        //Build the query to retrieve the requested elements (?s ?p ?o) where
        // Sets the context of the instance
        
        query.append("ASK {");
        if (subject != null) {
            query.append("VALUES ?s {<").append(subject).append(">} ");
        }

        if (property != null) {
            query.append("VALUES ?p {<").append(property).append(">} ");
        }

        if (object != null) {
            query.append("VALUES ?o {");            
            String obj = identifyObjectType(object);
            
            query.append(obj).append("} ");
        }

        query.append("GRAPH ?g {?s ?p ?o.}}");       
        
        System.out.println(query.toString());
        //evaluate the graph result
//        Update prepareGraphQuery = connection.prepareUpdate(QueryLanguage.SPARQL, query.toString());
        BooleanQuery prepareGraphQuery = connection.prepareBooleanQuery(QueryLanguage.SERQL, query.toString());
        return prepareGraphQuery.evaluate();
    }

}
