package wwwc.nees.joint.module.kao;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import wwwc.nees.joint.facade.RepositoryFacade;
import wwwc.nees.joint.module.ontology.operations.OntologyCompiler;

/**
 * @author williams
 */
public class OntologyCompilerTest {

    private String path_target;
    private String path_source_pizza;
    private String path_source_SIOC;
    private List<String> path_sources = new ArrayList<>();
    private RepositoryFacade facade;
    private OntologyCompiler compiler;

    @Before
    public void setUp() {
        path_target = "/home/williams/codeGenerateTest.jar";
        path_source_pizza = "http://www.dcs.bbk.ac.uk/~michael/sw/slides/pizza.owl";
        path_source_SIOC = "http://rdfs.org/sioc/ns#";
        facade = new RepositoryFacade();
    }

    @After
    public void tearDown() {
        path_target = null;
        path_source_pizza = null;
        path_sources.clear();
        facade = null;
        compiler = null;
    }

    @Test
    public void codeGenerate() {
//        path_sources.add(path_source_pizza);
        path_sources.add(path_source_SIOC);
        compiler = facade.getOntologyCompiler(path_target, path_sources);
        compiler.compile();
    }
}
