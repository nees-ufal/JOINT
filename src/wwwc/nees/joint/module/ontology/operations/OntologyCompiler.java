package wwwc.nees.joint.module.ontology.operations;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import wwwc.nees.joint.compiler.model.rdf.Model;
import wwwc.nees.joint.compiler.model.rdf.impl.LinkedHashModel;
import wwwc.nees.joint.compiler.OWLCompiler;
import wwwc.nees.joint.compiler.OntologyLoader;
import wwwc.nees.joint.compiler.managers.LiteralManager;
import wwwc.nees.joint.compiler.managers.RoleMapper;

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

    public static void main(String[] args) {
        System.out.println("getOntologyCompiler");
        String path = "C:\\repositorio-meututor\\trunks\\MeuTutorAdhoc\\lib\\MeuTutorModel.jar";
        String prefix = "file:///C:\\repositorio-meututor\\Modelagem\\Ontologies\\MeuTutor\\";
        String foaf = prefix + "foaf.owl";
        String sioc = prefix + "sioc.owl";
        String cp = prefix + "MeuTutor.Domain.CP.owl";
        String resource = prefix + "MeuTutor.Domain.Resource.owl";
        String curriculum = prefix + "MeuTutor.Domain.Curriculum.owl";
        String domain = prefix + "MeuTutor.Domain.owl";
        String pedagogical = prefix + "MeuTutor.Pedagogical.owl";
        String gamification = prefix + "MeuTutor.Gamification.owl";
        String administrator = prefix + "MeuTutor.Administrator.owl";
        String learner = prefix + "MeuTutor.Learner.owl";
        String collaborative = prefix + "MeuTutor.Collaborativelearning.owl";
        String peer = prefix + "MeuTutor.PeerAssessment.owl";
        String adm = prefix + "MeuTutor.Administrator.owl";
        String organization = prefix + "MeuTutor.Organization.owl";
        String school = prefix + "MeuTutor.School.owl";
        String teacher = prefix + "MeuTutor.Teacher.owl";
        String coordinator = prefix + "MeuTutor.Coordinator.owl";
        String principal = prefix + "MeuTutor.Principal.owl";
        List<String> ontologiesURLs = new ArrayList<String>();
        ontologiesURLs.add(foaf);
        ontologiesURLs.add(sioc);
        ontologiesURLs.add(cp);
        ontologiesURLs.add(resource);
        ontologiesURLs.add(curriculum);
        ontologiesURLs.add(domain);
        ontologiesURLs.add(pedagogical);
        ontologiesURLs.add(gamification);
        ontologiesURLs.add(administrator);
        ontologiesURLs.add(learner);
        ontologiesURLs.add(collaborative);
        ontologiesURLs.add(peer);
        ontologiesURLs.add(adm);
        ontologiesURLs.add(organization);
        ontologiesURLs.add(school);
        ontologiesURLs.add(teacher);
        ontologiesURLs.add(coordinator);
        ontologiesURLs.add(principal);

        OntologyCompiler result = new OntologyCompiler(path, ontologiesURLs);
        result.compile();
        System.out.println("fim");
    }
}
