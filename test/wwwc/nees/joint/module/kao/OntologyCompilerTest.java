package wwwc.nees.joint.module.kao;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import wwwc.nees.joint.module.ontology.operations.OntologyCompiler;

/**
 * @author williams
 */
public class OntologyCompilerTest {

    private String path_target;
    private List<String> path_sources = new ArrayList<>();
    private OntologyCompiler compiler;
    private String base_path = System.getProperty("user.dir");

    @Before
    public void setUp() {
        path_target = base_path+"/dist/codeGenerateTest.jar";
        
        //Adding ontologies
        path_sources.add("http://www.dcs.bbk.ac.uk/~michael/sw/slides/pizza.owl"); // Pizza Ontology
        path_sources.add("http://rdfs.org/sioc/ns#"); // SIOC Ontology
        path_sources.add("http://nees.com.br/linkn/onto/osm/"); // OSM Ontology
        path_sources.add("http://www.w3.org/2004/02/skos/core#"); // SKOS Ontology
        path_sources.add("http://swat.cse.lehigh.edu/onto/univ-bench.owl"); // Univ-Bench Ontology
        path_sources.add("http://schema.rdfs.org/all.rdf"); // Schema.org Ontology
    }

    @After
    public void tearDown() {
        path_target = null;
        path_sources.clear();
        compiler = null;
    }

    @Test
    public void codeGenerate() {
        compiler = new OntologyCompiler(path_target, path_sources);
        compiler.compile();
    }
}
