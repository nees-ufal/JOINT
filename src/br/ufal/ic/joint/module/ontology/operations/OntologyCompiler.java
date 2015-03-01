package br.ufal.ic.joint.module.ontology.operations;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.object.compiler.OWLCompiler;
import org.openrdf.repository.object.compiler.OntologyLoader;
import org.openrdf.repository.object.managers.LiteralManager;
import org.openrdf.repository.object.managers.RoleMapper;

/**
 * Class responsible to call Alibaba owl compiler and convert ontologies into
 * Java interfaces
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OntologyCompiler {

    // VARIABLES
    // -------------------------------------------------------------------------
    //Jar file that will be compiled the ontologies
    private File jarFile;
    //List of ontologies URLs
    private List<URL> ontologies;
    //Alibaba OWLCompiler
    private OWLCompiler converter;
    //Variable to load ontologies
    private OntologyLoader loader;
    //Variable for prefixes configuration
    private PrefixConfig prefixConfig;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Class Constructor, set the path of the Jar file that will be compiled the
     * desired ontologies and the ontologies to be compiled
     *
     * @param path the path to the Jar file
     * @param urls the List of ontologies urls (either a remote or local file)
     */
    public OntologyCompiler(String path, List<String> ontologiesURLs) {
        jarFile = new File(path);
        ontologies = new ArrayList<URL>();
        this.setOntologies(ontologiesURLs);
        this.prepareCompiler();
        prefixConfig = new PrefixConfig(converter, loader);
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Add a list of ontologies that will be compiled
     *
     * @param urls a <code>List</code> with the ontologies URLs.
     */
    private void setOntologies(List<String> urls) {

        //For each URL in the list
        for (String path : urls) {

            // Gets the URL of the given path
            URL url = OntologyFileManager.getURLofFile(path);

            // Add in the list
            ontologies.add(url);
        }
    }

    /**
     * Compile the ontologies in Java code
     *
     */
    public void compile() {
        try {
            converter = prefixConfig.getConfiguredCompiler();

            //Compile the specified ontologies in the desired jar file
            converter.createJar(jarFile);
        } catch (Exception e) {
            Logger.getLogger(OntologyCompiler.class.getName()).
                    log(Level.SEVERE, null, e);
        }
    }

    /**
     * Prepare the Ontology Compiler with some default specifications
     *
     */
    private void prepareCompiler() {
        try {
            //Creates a LiteralManager
            LiteralManager literals = new LiteralManager();

            //Get current ClassLoader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            literals.setClassLoader(cl);
            RoleMapper mapper = new RoleMapper();
            //Call OWLCompiler, responsible to compile ontologies
            converter = new OWLCompiler(mapper, literals);

            //Creates a new OntologyLoader, for imports purpose
            loader = new OntologyLoader(new LinkedHashModel());
            loader.loadOntologies(ontologies);
            //If it is desired to follow ontology imports

            //Recovers the schema of the ontologies
            Model model = loader.getModel();

            //Add the imports in the list of ontologies
            ontologies.addAll(loader.getImported());

            //Set all the ontologies that will be compiled
            converter.setOntologies(ontologies);

            //Set the schema
            converter.setModel(model);

            //Set the class loader
            converter.setClassLoader(cl);

        } catch (Exception e) {
            Logger.getLogger(OntologyCompiler.class.getName()).
                    log(Level.SEVERE, null, e);
        }
    }
//    public static void main(String[] args) {
//        String path = "C:/Users/Olavo/Desktop/jarteste.jar";
//        List<String> urls = new ArrayList<String>();
//        urls.add("C:/Users/Olavo/Desktop/family.swrl.owl");
//        OntologyCompiler comp = new OntologyCompiler(path, urls);
//        comp.compile();
//    }
}
