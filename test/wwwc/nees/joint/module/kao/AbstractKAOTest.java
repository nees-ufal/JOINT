package wwwc.nees.joint.module.kao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import joint.codegen.foaf.Agent;
import joint.codegen.foaf.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
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
        foafGraph_A = new URIImpl(ontologyURI + "A/");
        foafGraph_B = new URIImpl(ontologyURI + "B/");
        graphs = new URI[]{new URIImpl(ontologyURI + "A/"), new URIImpl(ontologyURI + "B/")};
//        graphs = new URI[]{};
        kao = new AbstractKAOImpl(Person.class);

        instanceName = "Tereza";
    }

    @After
    public void tearDown() {
        kao.executeBooleanQuery("clear graph <" + foafGraph_A.toString() + ">");
        kao.executeBooleanQuery("clear graph <" + foafGraph_B.toString() + ">");
        kao.executeBooleanQuery("clear graph <sesame:nil>");
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
        System.out.println("create");

        Person expected = kao.create(ontologyURI, instanceName, graphs);
        Person result = kao.retrieveInstance(ontologyURI, instanceName, graphs);

        assertEquals(expected, result);
    }

    /**
     * Test of createWithUniqueID method, of class AbstractKAO.
     */
    @Test
    public void testCreateWithUniqueID() {
        System.out.println("createWithUniqueID");
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
        System.out.println("delete");
        Person p = kao.create(ontologyURI, instanceName, graphs);
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
        System.out.println("delete");
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        kao.delete(expected, graphs);
        Person result = kao.retrieveInstance(ontologyURI, instanceName, graphs);
        assertNull(result);
    }

    /**
     * Test of retrieveInstance method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveInstance() {
        System.out.println("retrieveInstance");
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
        System.out.println("retrieveAllInstances");
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
        System.out.println("update");
        Person expected = kao.create(ontologyURI, instanceName);

        expected.setFoafAge(22);
        Person result = kao.update(expected);
        assertEquals(expected, result);
    }

    /**
     * Test of executeSPARQLquerySingleResult method, of class AbstractKAO.
     */
    @Test
    public void testExecuteSPARQLquerySingleResult() {
        System.out.println("executeSPARQLquerySingleResult");
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
        System.out.println("executeSPARQLqueryResultList");
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
        List<Person> result = kao.executeSPARQLqueryResultList(query, graphs);
        assertEquals(expected.size(), result.size());
    }

    /**
     * Test of executeQueryAsIterator method, of class AbstractKAO.
     */
    @Test
    public void testExecuteQueryAsIterator() {
        System.out.println("executeQueryAsIterator");
        String query = "select ?s where {?s a foaf:Person.}";
        List<Person> list_expected = new ArrayList<>(kao.executeSPARQLqueryResultList(query, graphs));

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
        System.out.println("executeBooleanQuery");
        Person expected = kao.create(ontologyURI, instanceName, graphs);
        String query = "ASK where {values ?s {<" + expected.toString() + ">} ?s a foaf:Person.}";
        boolean result = kao.executeBooleanQuery(query);
        assertTrue(result);
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
