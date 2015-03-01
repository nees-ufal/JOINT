package wwwc.nees.joint.module.kao.test;

import java.util.HashSet;
import java.util.Set;
import joint.codegen.foaf.Person;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import wwwc.nees.joint.module.kao.AbstractKAO;

/**
 * @author williams
 */
public class AbstractKAOTest {

    class KAO extends AbstractKAO {

        public <T extends Object> KAO(Class<T> classe) {
            super(classe);
        }

    }

    private KAO kao;
    private String ontologyURI;
    private URI contextA;
    private URI contextB;

    private Person person;
    private Set<String> firstName;
    private String gender;
    private int age;
    private String birthday;

    @Before
    public void setUp() {
        ontologyURI = "http://xmlns.com/foaf/0.1/";
        contextA = new URIImpl(ontologyURI + "a/");
        contextB = new URIImpl(ontologyURI + "b/");
        kao = new KAO(Person.class);
        kao.addContext(contextA);
        kao.addContext(contextB);
        firstName = new HashSet<>();
        firstName.add("Williams");
        gender = "Masculino";
        age = 22;
        birthday = "07-23";
    }

    @After
    public void tearDown() {
        kao.delete(ontologyURI, (String) firstName.toArray()[0]);
        ontologyURI = null;
        kao = null;
        contextA = null;
        contextB = null;
        firstName = null;
        gender = null;
        age = 0;
        birthday = null;
    }

    @Test
    public void create() {
        person = kao.create(ontologyURI, (String) firstName.toArray()[0]);
        person.setFoafFirstName(firstName);
        person.setFoafAge(age);
        person.setFoafGender(gender);
        person.setFoafBirthday(birthday);
        kao.update(person);
        assertNotNull("A instância não deve ser nula.", kao.retrieveInstance(ontologyURI, (String) firstName.toArray()[0]));
    }

}
