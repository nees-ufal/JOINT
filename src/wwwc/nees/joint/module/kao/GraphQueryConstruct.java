package wwwc.nees.joint.module.kao;

import info.aduna.iteration.Iterations;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 * @author Thyago
 */
public class GraphQueryConstruct {

    private final RepositoryConnection connection;

    public GraphQueryConstruct(RepositoryConnection connection) {
        this.connection = connection;
    }

    public List<Statement> getStatementsAsList(String subj, String property, String obj, URI... contexts)
            throws Exception, NullPointerException {

        StringBuilder query = new StringBuilder();

        //Build the query to retrieve the requested elements (?s ?p ?o) where
        // Sets the context of the instance
        query.append("SELECT ?s ?p ?o ?g WHERE { ");

        if (contexts != null && contexts.length != 0) {
            query.append("VALUES ?g {");
            for (URI context : contexts) {
                query.append(" <").append(context.toString()).append("> ");
            }
            query.append("} ");
        }

        query.append("GRAPH ?g {");

        if (subj != null) {//If subj isn't equals null, add values tag to ?s with subj in the query
            query.append("VALUES ?s {<").append(subj).append(">} ");
        }

        if (property != null) {
            query.append("VALUES ?p {<").append(property).append(">} ");
        }

        if (obj != null) {//If obj isn't equals null, add values tag to ?o with obj in the query
            query.append("VALUES ?o {<").append(obj).append(">}");
        }

        //Query to retrieve the informations (?s ?p ?o)
        //Ends tag of the context
        query.append(" ?s ?p ?o.}}");
        //evaluate the graph result
        GraphQuery prepareGraphQuery = this.connection.prepareGraphQuery(QueryLanguage.SPARQL, query.toString());
        GraphQueryResult graphResult = prepareGraphQuery.evaluate();

        //Creating a list of statetments with graph query result results
        List<Statement> statements = Iterations.asList(graphResult);
        graphResult.close();

        return statements;
    }

    public GraphQueryResult getStatementsAsGraphQuery(String subj, String property, String obj, URI... contexts)
            throws Exception, NullPointerException {

        StringBuilder query = new StringBuilder();

        //Build the query to retrieve the requested elements (?s ?p ?o) where
        // Sets the context of the instance
        query.append("SELECT ?s ?p ?o ?g WHERE { ");

        if (contexts != null && contexts.length != 0) {
            query.append("VALUES ?g {");
            for (URI context : contexts) {
                query.append(" <").append(context.toString()).append("> ");
            }
            query.append("} ");
        }

        query.append("GRAPH ?g {");

        if (subj != null) {//If subj isn't equals null, add values tag to ?s with subj in the query
            query.append("VALUES ?s {<").append(subj).append(">} ");
        }

        if (property != null) {
            query.append("VALUES ?p {<").append(property).append(">} ");

        }

        if (obj != null) {//If obj isn't equals null, add values tag to ?o with obj in the query
            query.append("VALUES ?o {<").append(obj).append(">} ");
        }

        //Query to retrieve the informations (?s ?p ?o)
        //Ends tag of the context
        query.append("?s ?p ?o.} }");
        //evaluate the graph result
        GraphQuery prepareGraphQuery = this.connection.prepareGraphQuery(QueryLanguage.SPARQL, query.toString());

        return prepareGraphQuery.evaluate();
    }

    public GraphQueryResult getStatementsAsGraphQuery(List<String> subj, List<String> property, List<String> obj, URI... contexts)
            throws Exception, NullPointerException {

        StringBuilder query = new StringBuilder();

        //Build the query to retrieve the requested elements (?s ?p ?o) where
        // Sets the context of the instance
        query.append("SELECT ?s ?p ?o ?g WHERE { ");

        if (contexts != null && contexts.length != 0) {
            query.append("VALUES ?g {");
            for (URI context : contexts) {
                query.append(" <").append(context.toString()).append("> ");
            }
            query.append("} ");
        }

        query.append("GRAPH ?g {");

        if (subj != null && !subj.isEmpty()) {//If subj isn't equals null, add values tag to ?s with subj in the query
            query.append("VALUES ?s { ");
            for (String s : subj) {
                query.append("<").append(s).append("> ");
            }
            query.append("} ");
        }

        if (property != null && !property.isEmpty()) {
            query.append("VALUES ?p { ");
            for (String p : property) {
                query.append(" <").append(p).append("> ");
            }
            query.append("} ");
        }

        if (obj != null && !obj.isEmpty()) {//If obj isn't equals null, add values tag to ?o with obj in the query
            query.append("VALUES ?o { ");
            for (String o : obj) {
                query.append("<").append(o).append("> ");
            }
            query.append("} ");
        }

        //Query to retrieve the informations (?s ?p ?o)
        //Ends tag of the context
        query.append(" ?s ?p ?o.} }");
        //evaluate the graph result
        GraphQuery prepareGraphQuery = this.connection.prepareGraphQuery(QueryLanguage.SPARQL, query.toString());
        GraphQueryResult graphResult = prepareGraphQuery.evaluate();

        return graphResult;
    }
}
