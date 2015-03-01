/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufal.ic.joint.module.repository.operations;

import com.ontotext.trree.owlim_ext.config.OWLIMSailConfig;
import org.openrdf.sail.config.SailImplConfig;

/**
 *  Interface representing a Repository Configuration which
 * contains a SailImplConfig (sesame repository configuration)
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class OWLIMLiteRepositoryConfigImpl implements RepositoryConfiguration {

    // METHODS -----------------------------------------------------------------
    /**
     * Gets a OWLIM lite implementation of the
     * Sesame Sail Repository configuration
     *
     * @return config
     *            the SailImplConfig object for sesame repository configuration
     */
    public SailImplConfig getSailConfiguration() {
        // Creates a new OWLIM lite configuration
        OWLIMSailConfig config = new OWLIMSailConfig();
        return config;
    }
}