package wwwc.nees.joint.facade;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Williams
 */
public class RepositoryFacadeTest {

    private RepositoryFacade facade;
    private String backupPath;
    private String ontologyPath;
    private String ontologyURI;

    @Before
    public void setUp() {
        facade = new RepositoryFacade();
        backupPath = ".\\backup.jnt";
        ontologyPath = "/home/williams/Projetos/JOINT/test/lib/index.rdf";
        ontologyURI = "http://xmlns.com/foaf/0.1/";
    }

    @After
    public void tearDown() {
        facade = null;
        backupPath = "";
        ontologyPath = "";
        ontologyURI = "";
    }

    @Test
    public void addOntology() {
        List<String> listOfOntologiesInitial = facade.retrieveListOfOntologies();
        //checks whether the ontology does not exist in the triple store
        if (listOfOntologiesInitial.contains(ontologyURI)) {
            System.out.println("The ontology already exists in the triple store. So it is not possible to test this method.");
            assertTrue(!listOfOntologiesInitial.contains(ontologyURI));
        }
        //adds the ontology
        facade.addOntology(ontologyPath, ontologyURI);
        List<String> listOfOntologiesFinal = facade.retrieveListOfOntologies();
        //checks whether the ontology exists in the triple store
        assertTrue(listOfOntologiesFinal.contains(ontologyURI));
    }

    @Test
    public void deleteOntology() {
        List<String> listOfOntologiesInitial = facade.retrieveListOfOntologies();

        //checks whether the ontology exists in the triple store
        if (!listOfOntologiesInitial.contains(ontologyURI)) {
            System.out.println("The ontology does not exist in the triple store. So it is not possible to test this method.");
            assertTrue(listOfOntologiesInitial.contains(ontologyURI));
        }
        //removes the ontology
        facade.deleteOntology(ontologyURI);
        List<String> listOfOntologiesFinal = facade.retrieveListOfOntologies();
        //checks whether the ontology does not exist in the triple store
        assertTrue(!listOfOntologiesFinal.contains(ontologyURI));
    }

    /**
     * Test backup method
     */
    @Test
    public void testBackup() throws IOException {
        facade.backupRepository(backupPath);
        File file = new File(backupPath);
        assertTrue(file.exists());
        assertTrue(file.getTotalSpace() > 0);
    }
}
