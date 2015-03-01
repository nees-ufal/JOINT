/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.module.kao;

import java.util.Iterator;
import java.util.List;
import joint.codegen.foaf.Person;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author armando
 */
public class AbstractKAOTest {

    public AbstractKAOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        AbstractKAOImpl kao = new AbstractKAOImpl(null);
        kao.executeBooleanQuery("clear graph <sesame:nil>");
        kao.executeBooleanQuery("clear graph <http://xmlns.com/foaf/0.1/>");
        
    }

    /**
     * Test of create method, of class AbstractKAO.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        String ontologyURI = "http://xmlns.com/foaf/0.1/";
        URI foafGraph = new URIImpl("http://xmlns.com/foaf/0.1/");
        URI sesameGraph = new URIImpl("sesame:nil");
        String instanceName = "Williams";
        AbstractKAO kao = new AbstractKAOImpl(Person.class);
        
        //kao.addContext(foafGraph);
        //kao.addContext(sesameGraph);

        Object expResult = kao.create(ontologyURI, instanceName);
        Object result = kao.retrieveInstance(ontologyURI, instanceName);

        assertEquals(expResult, result);
    }

    /**
     * Test of createWithUniqueID method, of class AbstractKAO.
     */
    @Test
    public void testCreateWithUniqueID() {
        System.out.println("createWithUniqueID");
        String ontologyURI = "http://xmlns.com/foaf/0.1/";
        String instancePrefix = "Person_";
        AbstractKAO kao = new AbstractKAOImpl(Person.class);
        Person p1 = kao.createWithUniqueID(ontologyURI, instancePrefix);
        Person p2 = kao.createWithUniqueID(ontologyURI, instancePrefix);
        
        List<Person> list = kao.retrieveAllInstances();
        assertTrue((list.size() > 0) && (p1 != p2));
    }

    /**
     * Test of delete method, of class AbstractKAO.
     */
    @Test
    public void testDelete_String_String() {
        System.out.println("delete");
        String ontologyURI = "";
        String instanceName = "";
        AbstractKAO instance = null;
        instance.delete(ontologyURI, instanceName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class AbstractKAO.
     */
    @Test
    public void testDelete_GenericType() {
        System.out.println("delete");
        Object instance_2 = null;
        AbstractKAO instance = null;
        instance.delete(instance_2);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveInstance method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveInstance() {
        System.out.println("retrieveInstance");
        String ontologyURI = "";
        String instanceName = "";
        AbstractKAO instance = null;
        Object expResult = null;
        Object result = instance.retrieveInstance(ontologyURI, instanceName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveAllInstances method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveAllInstances() {
        System.out.println("retrieveAllInstances");
        AbstractKAO instance = null;
        List expResult = null;
        List result = instance.retrieveAllInstances();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class AbstractKAO.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        Object instance_2 = null;
        AbstractKAO instance = null;
        Object expResult = null;
        Object result = instance.update(instance_2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeSPARQLquerySingleResult method, of class AbstractKAO.
     */
    @Test
    public void testExecuteSPARQLquerySingleResult() {
        System.out.println("executeSPARQLquerySingleResult");
        String query = "";
        AbstractKAO instance = null;
        Object expResult = null;
        Object result = instance.executeSPARQLquerySingleResult(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeSPARQLqueryResultList method, of class AbstractKAO.
     */
    @Test
    public void testExecuteSPARQLqueryResultList() {
        System.out.println("executeSPARQLqueryResultList");
        String query = "";
        AbstractKAO instance = null;
        List expResult = null;
        List result = instance.executeSPARQLqueryResultList(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeQueryAsIterator method, of class AbstractKAO.
     */
    @Test
    public void testExecuteQueryAsIterator() {
        System.out.println("executeQueryAsIterator");
        String query = "";
        AbstractKAO instance = null;
        Iterator<Object> expResult = null;
        Iterator<Object> result = instance.executeQueryAsIterator(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeBooleanQuery method, of class AbstractKAO.
     */
    @Test
    public void testExecuteBooleanQuery() {
        System.out.println("executeBooleanQuery");
        String query = "";
        AbstractKAO instance = null;
        boolean expResult = false;
        boolean result = instance.executeBooleanQuery(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setClasse method, of class AbstractKAO.
     */
    @Test
    public void testSetClasse() {
        System.out.println("setClasse");
        AbstractKAO instance = null;
        instance.setClasse(null);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveClass method, of class AbstractKAO.
     */
    @Test
    public void testRetrieveClass() {
        System.out.println("retrieveClass");
        AbstractKAO instance = null;
        Class expResult = null;
        Class result = instance.retrieveClass();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getContexts method, of class AbstractKAO.
     */
    @Test
    public void testGetContexts() {
        System.out.println("getContexts");
        AbstractKAO instance = null;
        List<URI> expResult = null;
        List<URI> result = instance.getContexts();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addContext method, of class AbstractKAO.
     */
    @Test
    public void testAddContext() {
        System.out.println("addContext");
        URI context = null;
        AbstractKAO instance = null;
        instance.addContext(context);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContexts method, of class AbstractKAO.
     */
    @Test
    public void testSetContexts() {
        System.out.println("setContexts");
        List<URI> contexts = null;
        AbstractKAO instance = null;
        instance.setContexts(contexts);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class AbstractKAOImpl extends AbstractKAO {

        public <T> AbstractKAOImpl(Class<T> classe) {
            super(classe);
        }

    }

}
