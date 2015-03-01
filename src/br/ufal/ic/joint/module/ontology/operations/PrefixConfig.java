package br.ufal.ic.joint.module.ontology.operations;

import org.openrdf.repository.object.compiler.OWLCompiler;
import org.openrdf.repository.object.compiler.OntologyLoader;

/**
 *  Handles the configuration of the prefixes in the code generator
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class PrefixConfig {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Static final representing the prefix for the ontology compiler
    public static final String JOINT_PREFIX = "joint.codegen.";
    // Alibaba OWL compiler
    private OWLCompiler converter;
    // Variable to load the ontologies
    private OntologyLoader loader;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Sets the default prefixes for the code generator
     *
     * @param compiler
     *              an OWLCompiler object
     * @param loader
     *              an OntologyLoader object
     */
    public PrefixConfig(OWLCompiler converter, OntologyLoader loader) {
        this.converter = converter;
        this.loader = loader;
        this.converter.setPackagePrefix(JOINT_PREFIX);
        this.setDefaultPackagesPrefix();
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Sets the default prefixes for the code generator
     *
     */
    private void setDefaultPackagesPrefix() {
        converter.setPrefixNamespaces(loader.getNamespaces());
    }

    /**
     * Gets the already configured compiler
     *
     */
    public OWLCompiler getConfiguredCompiler() {
        return converter;
    }
}
