package wwwc.nees.joint.module.reasoner;

import wwwc.nees.joint.model.OWLUris;
import wwwc.nees.joint.module.kao.RepositoryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;

import org.openrdf.model.Value;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * SWRL engine for rdf graphs, perform swrl rules in the repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class SWRLEngine implements RuleEngine {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Static variable to control new data inferred
    private static boolean newData = true;
    // Number of inferred statements
    private int infStatements;
    // Parser rdf list to a java.util.List
    private RDFListParser rdfList;
    // Parser a swrl atom to joint Atom
    private SWRLAtomParser atomParser;
    // Manages operations with literals
    private LiteralManager literalMgr;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Performs rules in the specified repository
     *
     * @param url
     *            the Repository URL
     * @return statements
     *            the Number of inferred Statements
     */
    public int performRulesInRepository(String repositoryURL) {

        // Inferred Statements
        int inf = 0;

        // Creates a new HTTPRepository with the specified URL
        Repository repo = new HTTPRepository(repositoryURL);
        try {
            repo.initialize();

            // Perform the Rules in the repository passing its connection
            inf = this.runSWRLRules(repo.getConnection());
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inf;
    }

    /**
     * Performs rules in the repository specified by configuration.properties
     *
     * @return statements
     *            the Number of inferred Statements
     */
    public int performRulesInRepository() {

        // Inferred Statements
        int inf = 0;

        // Gets the repository in the configuration file
        Repository repo = RepositoryFactory.getRepository();
        try {

            // Perform the Rules in the repository passing its connection
            inf = this.runSWRLRules(repo.getConnection());
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SWRLEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inf;
    }

    /**
     * Runs SWRL rules in one repository passing its connection as parameter
     *
     * @param connection
     *            the Repository connection
     * @return statements
     *            the Number of inferred Statements
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     */
    private int runSWRLRules(RepositoryConnection con)
            throws RepositoryException, QueryEvaluationException,
            MalformedQueryException {


        // Creates the Parsers and Managers required by the engine
        rdfList = new RDFListParser(con);
        atomParser = new SWRLAtomParser(con);
        literalMgr = new LiteralManager(con);

        // Number of inferred statements
        infStatements = 0;

        // While there are new Data to verify
        while (newData) {
            newData = false;

            // Creates the query to search for SWRL rules in the repository
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT rule, head, body FROM ");
            queryBuilder.append("{rule} rdf:type {swrl:Imp}, ");
            queryBuilder.append("{rule} swrl:head {head}, ");
            queryBuilder.append("{rule} swrl:body {body} ");
            queryBuilder.append("USING NAMESPACE swrl = ");
            queryBuilder.append("<http://www.w3.org/2003/11/swrl#>");
            String queryImp = queryBuilder.toString();

            // Evaluates the query
            TupleQuery tImp = con.prepareTupleQuery(QueryLanguage.SERQL, queryImp);
            TupleQueryResult result = tImp.evaluate();

            // While there are results
            while (result.hasNext()) {

                // Gets the value for each variable
                BindingSet set = result.next();
                Value rule = set.getValue("rule");
                Value head = set.getValue("head");
                Value body = set.getValue("body");

                // Parses the node to search for its RDF list
                List<String> headList = rdfList.retrieveList(head.toString());
                List<String> bodyList = rdfList.retrieveList(body.toString());

                // Infers the rule
                this.inferRule(rule.toString(), headList, bodyList, con);
            }
        }

        return infStatements;
    }

    /**
     * Retrieves the list of SWRL atoms passing a list of nodes
     *
     * @param nodes
     *            the List of nodes representing atoms in the graph
     * @return atoms
     *            the List of atoms
     */
    private List<Atom> retrieveSWRLAtoms(List<String> list) {

        // Creates a list of Atoms
        List<Atom> atoms = new ArrayList<Atom>();

        // For each string in the list of nodes
        for (String s : list) {

            // Retrieves the atom with its node
            Atom atom = atomParser.getTripleofAtom(s);
            atoms.add(atom);
        }
        return atoms;
    }

    /**
     * Infers the rule with its body and head in the repository
     *
     * @param rule
     *            the String with the rule URI
     * @param headList
     *            List with the head atoms nodes
     * @param bodyList
     *            List with the body atoms nodes
     * @param connection
     *            the Repository Connection
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     */
    private void inferRule(String rule, List<String> h, List<String> b, RepositoryConnection con)
            throws RepositoryException, QueryEvaluationException,
            MalformedQueryException {

        // Creates the list of variables in the rule
        List<String> varList = new ArrayList<String>();

        // Creates the list of variables representing literals in the rule
        Map<String, String> literalList = new HashMap<String, String>();

        List<Atom> headAtoms = this.retrieveSWRLAtoms(h);
        List<Atom> bodyAtoms = this.retrieveSWRLAtoms(b);

        for (Atom atom : headAtoms) {

            // Checks if the argument 2 is a literal one
            String arg2 = atom.getArgument2();
            boolean isLiteral = literalMgr.checkArgumentLiteral(arg2);

            // Gets argument1
            String arg1 = atom.getArgument1();

            // If argument2 is not null
            if (!arg2.equals("null")) {

                // Checks if the list of query variables contains
                // the argument1
                if (!varList.contains(arg1)) {
                    varList.add(arg1);
                }
                // If argument 2 is not literal
                if (!isLiteral) {
                    // Checks if the list of query variables contains
                    // the argument2
                    if (!varList.contains(arg2)) {
                        varList.add(arg2);
                    }
                } else {
                    // Checks if the list of literals contains the
                    // argument2
                    if (!literalList.containsKey(arg2)) {
                        int index = literalList.size() + 1;
                        literalList.put(arg2, "var" + index);
                    }
                }
                // Checks if the list of query variables contains
                // the argument1
            } else if (!varList.contains(arg1)) {
                varList.add(arg1);
            }
        }
        for (Atom atom : bodyAtoms) {


            // Checks if the argument 2 is a literal one
            String arg2 = atom.getArgument2();
            boolean isLiteral = literalMgr.checkArgumentLiteral(arg2);

            // Gets argument1
            String arg1 = atom.getArgument1();

            // If argument2 is not null
            if (!arg2.equals("null")) {

                // Checks if the list of query variables contains
                // the argument1
                if (!varList.contains(arg1)) {
                    varList.add(arg1);
                }
                // If argument 2 is not literal
                if (!isLiteral) {
                    // Checks if the list of query variables contains
                    // the argument2
                    if (!varList.contains(arg2)) {
                        varList.add(arg2);
                    }
                } else {
                    // Checks if the list of literals contains the
                    // argument2
                    if (!literalList.containsKey(arg2)) {
                        int index = literalList.size() + 1;
                        literalList.put(arg2, "var" + index);
                    }
                }
                // Checks if the list of query variables contains
                // the argument1
            } else if (!varList.contains(arg1)) {
                varList.add(arg1);
            }
        }

        // Starts to build the query representing the body of the rule

        StringBuilder queBuilder = new StringBuilder();
        queBuilder.append("SELECT ");

        // Builds the variables
        for (String v : varList) {
            String local = this.getLocalNamespace(v);
            queBuilder.append(local);
            queBuilder.append(", ");
        }
        String subString = queBuilder.substring(0, queBuilder.length() - 2);
        queBuilder = new StringBuilder(subString);
        queBuilder.append(" FROM ");

        for (Atom atom : bodyAtoms) {

            // Checks if the argument 2 is a literal one
            String arg2 = atom.getArgument2();
            boolean isLiteral = literalMgr.checkArgumentLiteral(arg2);

            // Gets argument1
            String arg1 = atom.getArgument1();

            // Gets property
            String prop = atom.getProperty();

            // If argument2 is not null then triple has a propertyPredicate
            if (!arg2.equals("null")) {

                // If the property is not DifferentFrom
                // neither SameAs
                if ((!prop.equals(OWLUris.OWL_DIFF_FROM))
                        && (!prop.equals(OWLUris.OWL_SAME_AS))) {

                    // If the argument2 is a literal
                    // build arg1 prop literal
                    if (isLiteral) {
                        queBuilder.append("{");
                        queBuilder.append(this.getLocalNamespace(arg1));
                        queBuilder.append("} <");
                        queBuilder.append(prop);
                        queBuilder.append("> {");
                        queBuilder.append(literalList.get(arg2));
                        queBuilder.append("}, ");
                        // Else build arg1 property arg2
                    } else {
                        queBuilder.append("{");
                        queBuilder.append(this.getLocalNamespace(arg1));
                        queBuilder.append("} <");
                        queBuilder.append(prop);
                        queBuilder.append("> {");
                        queBuilder.append(this.getLocalNamespace(arg2));
                        queBuilder.append("}, ");
                    }
                }

                // Else the triple has a classPredicate
            } else {
                queBuilder.append("{");
                queBuilder.append(this.getLocalNamespace(arg1));
                queBuilder.append("} rdf:type {<");
                queBuilder.append(prop);
                queBuilder.append(">}, ");
            }

        }
        subString = queBuilder.substring(0, queBuilder.length() - 2);
        queBuilder = new StringBuilder(subString);
        queBuilder.append(" WHERE ");

        // Boolean to control the correct presence of the WHERE clause
        boolean whereClause = false;
        for (Atom atom : bodyAtoms) {

            // Checks if the argument 2 is a literal one
            String arg2 = atom.getArgument2();
            boolean isLiteral = literalMgr.checkArgumentLiteral(arg2);

            // Gets argument1
            String arg1 = atom.getArgument1();

            // Gets property
            String prop = atom.getProperty();

            // If the property is DifferentFrom
            if (prop.equals(OWLUris.OWL_DIFF_FROM)) {
                queBuilder.append(this.getLocalNamespace(arg1));
                queBuilder.append(" != ");
                queBuilder.append(this.getLocalNamespace(arg2));
                queBuilder.append(" AND ");
                whereClause = true;
                // Else if the property is SameAs
            } else if (prop.equals(OWLUris.OWL_SAME_AS)) {
                queBuilder.append(this.getLocalNamespace(arg1));
                queBuilder.append(" = ");
                queBuilder.append(this.getLocalNamespace(arg2));
                queBuilder.append(" AND ");
                whereClause = true;
                // Else if argument2 is a literal one
            } else if (isLiteral) {
                queBuilder.append(literalList.get(arg2));
                queBuilder.append(" like \"");
                queBuilder.append(literalMgr.getLiteralValue(arg2));
                queBuilder.append("\" AND ");
                whereClause = true;
            }
        }

        if (whereClause) {
            // If there is the WHERE clause then and 'AND' is overlapping the
            // string
            subString = queBuilder.substring(0, queBuilder.length() - 4);
            queBuilder = new StringBuilder(subString);

        } else {
            // Else the WHERE clause is overlapping the string
            subString = queBuilder.substring(0, queBuilder.length() - 6);
            queBuilder = new StringBuilder(subString);
        }

        String query = queBuilder.toString();

        TupleQuery tBinding = con.prepareTupleQuery(QueryLanguage.SERQL, query);
        TupleQueryResult tRes = tBinding.evaluate();

        // Creates the URIs that representing the possible new data inferred
        URIImpl sub;
        URIImpl pre;
        URIImpl obj;
        URIImpl cont;

        String ontologyURI = rule.substring(0, rule.lastIndexOf("#"));

        StringBuilder headBuilder = new StringBuilder();

        TupleQuery tHead;
        for (Atom atom : headAtoms) {

            // Gets argument2
            String arg2 = atom.getArgument2();

            // Gets argument1
            String arg1 = atom.getArgument1();

            // Gets property
            String prop = atom.getProperty();

            tBinding = con.prepareTupleQuery(QueryLanguage.SERQL, query);
            tRes = tBinding.evaluate();

            // If argument2 is not null then triple has a propertyPredicate
            // Else the triple has a classPredicate
            if (!arg2.equals("null")) {

                while (tRes.hasNext()) {
                    BindingSet set = tRes.next();

                    // The triple will be composed by
                    // argument1 URI, property URI and argument2 URI
                    sub = new URIImpl(set.getValue(this.getLocalNamespace(arg1)).toString());
                    pre = new URIImpl(prop);
                    obj = new URIImpl(set.getValue(this.getLocalNamespace(arg2)).toString());
                    cont = new URIImpl(ontologyURI);

                    // Builds the head query to check if already exists the
                    // triple in the repository
                    headBuilder = new StringBuilder();
                    headBuilder.append("SELECT * FROM {s} p {o} ");
                    headBuilder.append("WHERE s=<");
                    headBuilder.append(sub);
                    headBuilder.append("> AND p=<");
                    headBuilder.append(pre);
                    headBuilder.append("> AND o=<");
                    headBuilder.append(obj);
                    headBuilder.append(">");
                    String qHead = headBuilder.toString();

                    // Evaluates the query
                    tHead = con.prepareTupleQuery(QueryLanguage.SERQL, qHead);
                    TupleQueryResult tResHead = tHead.evaluate();

                    // If there is no result then new data can be inferred
                    if (!tResHead.hasNext()) {

                        // Changes the boolean cause new data was inferred
                        newData = true;

                        // Add the inferred triple and count the statement
                        con.add(sub, pre, obj, (Resource) cont);
                        infStatements++;
                    }
                }
            } else {

                while (tRes.hasNext()) {
                    BindingSet set = tRes.next();

                    // The triple will be composed by
                    // argument1 URI, rdf:type and property URI (an owl:Class)
                    sub = new URIImpl(set.getValue(arg1).toString());
                    pre = new URIImpl(RDF.TYPE.toString());
                    obj = new URIImpl(prop);
                    cont = new URIImpl(ontologyURI);

                    // Builds the head query to check if already exists the
                    // triple in the repository
                    headBuilder = new StringBuilder();
                    headBuilder.append("SELECT * FROM {s} p {o} ");
                    headBuilder.append("WHERE s=<");
                    headBuilder.append(sub);
                    headBuilder.append("> AND p=<");
                    headBuilder.append(pre);
                    headBuilder.append("> AND o=<");
                    headBuilder.append(obj);
                    headBuilder.append(">");
                    String qHead = headBuilder.toString();

                    // Evaluates the query
                    tHead = con.prepareTupleQuery(QueryLanguage.SERQL, qHead);
                    TupleQueryResult tResHead = tHead.evaluate();

                    // If there is no result then new data can be inferred
                    if (!tResHead.hasNext()) {

                        // Changes the boolean cause new data was inferred
                        newData = true;

                        // Add the inferred triple and count the statement
                        con.add(sub, pre, obj, (Resource) cont);
                        infStatements++;
                    }
                }
            }
        }

    }

    /**
     * Gets the local namespace of an URI, the part after "#"
     *
     * @param uri
     *            the String with the URI
     * @return local
     *            the local namespace
     */
    public String getLocalNamespace(Object str) {
        // Gets the string of the object
        String temp = str.toString();

        // Gets the local part of the URI
        temp = temp.substring(temp.lastIndexOf("#") + 1);
        return temp;
    }
}
