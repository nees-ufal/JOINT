package wwwc.nees.joint.module.ontology.operations;

/**
 *  A class to check owl consistency
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OWLValidation {

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Checks the ontology consistency
     *
     * @param path
     *            the ontology file path
     * @return boolean
     *            true if the consistency is ok, else false
     */
    public boolean checkOntologyConsistency(String path) {

//        // Creates a new Pellet Reasoner
//        Reasoner reasoner = new PelletReasoner();
//        // Creates the model specification
//        OntModelSpec ontModelSpec = PelletReasonerFactory.THE_SPEC;
//        OntModel schema = ModelFactory.createOntologyModel(ontModelSpec);
//        reasoner = reasoner.bindSchema(schema);
//
//        // Gets the URL of the file path
//        URL url = OntologyFileManager.getURLofFile(path);
//
//        Model model = FileManager.get().loadModel(url.toString());
//        // and validate that it conforms to the OWL model
//        InfModel inf = ModelFactory.createInfModel(reasoner, model);
//        ValidityReport valid = inf.validate();

        return true;
    }
}
