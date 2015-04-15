package wwwc.nees.joint.module.kao;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import joint.codegen.foaf.Agent;
import joint.codegen.foaf.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.util.iterators.Iterators;

/**
 * @author armando
 */
public class AbstractKAOTest {

    private AbstractKAOImpl kao;
    private String ontologyURI;
    private URI foafGraph_A;
    private URI foafGraph_B;
    private String instanceName;
    private URI[] graphs;

    @Before
    public void setUp() {
        ontologyURI = "http://xmlns.com/foaf/0.1/";
        foafGraph_A = URI.create(ontologyURI + "A/");
        foafGraph_B = URI.create(ontologyURI + "B/");
        graphs = new URI[]{foafGraph_A, foafGraph_B};
        kao = new AbstractKAOImpl(Person.class);

        instanceName = "Tereza";
    }

    @After
    public void tearDown() {
        kao.executeBooleanQuery("clear graph <" + foafGraph_A.toString() + ">");
        kao.executeBooleanQuery("clear graph <" + foafGraph_B.toString() + ">");
//        kao.executeBooleanQuery("clear graph <sesame:nil>");
        kao = null;
        ontologyURI = "";
        foafGraph_A = null;
        foafGraph_B = null;
        instanceName = "";
    }

    /**
     * Test of create method, of class AbstractKAO.
     */
    @Test
    public void testCreate() {
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        Person result = kao.retrieveInstance(ontologyURI, instanceName, graphs);

        assertEquals(expected, result);
    }

    /**
     * Test of createWithUniqueID method, of class AbstractKAO.
     */
    @Test
    public void testCreateWithUniqueID() {
        String instancePrefix = "Person_";
        List<Person> expected = kao.retrieveAllInstances(graphs);
        Person p1 = kao.createWithUniqueID(ontologyURI, instancePrefix, graphs);
        Person p2 = kao.createWithUniqueID(ontologyURI, instancePrefix, graphs);
        if (graphs.length == 0) {
            expected.add(p1);
            expected.add(p2);
        } else {
            for (URI context : graphs) {
                expected.add(p1);
                expected.add(p2);
            }
        }
        List<Person> result = kao.retrieveAllInstances(graphs);
        assertEquals(expected.size(), result.size());
        assertTrue(p1.toString() != p2.toString());
    }

    /**
     * Test of delete method, of class AbstractKAO.
     */
    @Test
    public void testDelete_String_String() {
        Person p = kao.create(ontologyURI, instanceName, graphs);
        p.setFoafGender("Feminino");
        p.setFoafAge(22);
        kao.update(p, graphs);
        kao.delete(ontologyURI, instanceName, graphs);
        Person p_exp = kao.retrieveInstance(ontologyURI, instanceName, graphs);
        assertNotNull(p);
        assertNull(p_exp);
    }

    /**
     * Test of delete method, of class AbstractKAO.
     */
    @Test
    public void testDelete_GenericType() {
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        expected.setFoafGender("Feminino");
        expected.setFoafAge(22);
        kao.update(expected, graphs);
        kao.delete(expected, graphs);
        Person result = kao.retrieveInstance(ontologyURI, instanceName, graphs);
        assertNull(result);
    }

    /**
     * Test of retrieveInstance method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveInstance() {
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        kao.update(expected, graphs);
        Person result = kao.retrieveInstance(ontologyURI, instanceName, graphs);
        assertEquals(expected, result);
    }

    /**
     * Test of retrieveAllInstances method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveAllInstances() {
        List<Person> initial = kao.retrieveAllInstances(graphs);
        Person p = kao.create(ontologyURI, instanceName, graphs);
        if (graphs.length == 0) {
            initial.add(p);
        } else {
            for (URI context : graphs) {
                initial.add(p);
            }
        }
        List<Person> result = kao.retrieveAllInstances(graphs);

        assertEquals(initial.size(), result.size());
    }

    /**
     * Test of update method, of class AbstractKAO.
     */
    @Test
    public void testUpdate() {
        Person person = kao.create(ontologyURI, instanceName, graphs);
        person.setFoafAge(22);
        person.setFoafGender("Feminino");
        kao.update(person, graphs);
        //
        Person person_Aux = kao.retrieveInstance(ontologyURI, instanceName, graphs);
        person_Aux.setFoafAge(new Integer(500));
        person_Aux.setFoafGender("Masculino");
        Person result = kao.update(person_Aux, graphs);
        assertNotSame(person, result);
        assertEquals(500, result.getFoafAge());
    }

    /**
     * Test of executeSPARQLquerySingleResult method, of class AbstractKAO.
     */
    @Test
    public void testExecuteSPARQLquerySingleResult() {
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        String query = "select ?s where {values ?s {<" + expected + ">} ?s a foaf:Person.}";
        Person result = (Person) kao.executeSPARQLquerySingleResult(query);
        assertEquals(expected, result);
    }

    /**
     * Test of executeSPARQLqueryResultList method, of class AbstractKAO.
     */
    @Test
    public void testExecuteSPARQLqueryResultList() {
        String query = "select ?s where {?s a foaf:Person.}";
        List<Person> expected = kao.executeSPARQLqueryResultList(query);
        Person p1 = kao.create(ontologyURI, instanceName + "1", graphs);
        Person p2 = kao.create(ontologyURI, instanceName + "2", graphs);

        //An instance can be added in more than one context, therefore, it must be recorded for each context
        if (graphs.length == 0) {
            expected.add(p1);
            expected.add(p2);
        } else {
            for (URI context : graphs) {
                expected.add(p1);
                expected.add(p2);
            }
        }
        List<Person> result = kao.executeSPARQLqueryResultList(query);
        assertEquals(expected.size(), result.size());
    }

    /**
     * Test of executeQueryAsIterator method, of class AbstractKAO.
     */
    @Test
    public void testExecuteQueryAsIterator() {
        String query = "select ?s where {?s a foaf:Person.}";
        List<Person> list_expected = new ArrayList<>(kao.executeSPARQLqueryResultList(query));

        Person expected1 = kao.create(ontologyURI, instanceName + "1", graphs);
        Person expected2 = kao.create(ontologyURI, instanceName + "2", graphs);
        if (graphs.length == 0) {
            list_expected.add(expected1);
            list_expected.add(expected2);
        }
        for (URI context : graphs) {
            list_expected.add(expected1);
            list_expected.add(expected2);
        }

        Iterator<Person> result_aux = kao.executeQueryAsIterator(query, graphs);
        List<Person> result = Iterators.asList(result_aux);
        assertEquals(list_expected.size(), result.size());
        //assertArrayEquals(list_expected.toArray(), result.toArray());

    }

    /**
     * Test of executeBooleanQuery method, of class AbstractKAO.
     */
    @Test
    public void testExecuteBooleanQuery() {
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        String query = "ASK where {values ?s {<" + expected.toString() + ">} ?s a foaf:Person.}";
        boolean result = kao.executeBooleanQuery(query);
        assertTrue(result);
    }

    /**
     * Test of executeSPARQLUpdateQuery method, of class AbstractKAO.
     */
    @Test
    public void testExecuteSPARQLUpdateQuery() {
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        expected.setFoafAge(30);
        kao.update(expected, graphs);

        String exp_gender = (String) expected.getFoafGender();
        String query = "INSERT { GRAPH ?g1 {?s foaf:gender \"Masculino\"}} \n"
                + "WHERE { GRAPH ?g {values ?s {<" + expected.toString() + ">} ?s a foaf:Person; foaf:age ?age.}\n"
                + "BIND(?g as ?g1)}";
        boolean result = kao.executeSPARQLUpdateQuery(query);
        Person res_person = kao.retrieveInstance(ontologyURI, instanceName, graphs);
        String res_gender = (String) res_person.getFoafGender();
        assertTrue(result);
        assertNotSame(exp_gender, res_gender);
    }

    /**
     * Test of setClasse method, of class AbstractKAO.
     */
    @Test
    public void testSetClasse() {
        System.out.println("setClasse");
        Class expected = Agent.class;
        kao.setClasse(expected);
        Class result = kao.retrieveClass();
        assertEquals(expected, result);
    }

    /**
     * Test of retrieveClass method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveClass() {
        System.out.println("retrieveClass");
        Class expResult = Agent.class;
        kao.setClasse(expResult);
        Class result = kao.retrieveClass();
        assertEquals(expResult, result);
    }

    public class AbstractKAOImpl extends AbstractKAO {

        public <T> AbstractKAOImpl(Class<T> classe) {
            super(classe);
        }

    }

}
