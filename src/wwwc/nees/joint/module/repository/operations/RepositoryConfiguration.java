/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.module.repository.operations;

import org.openrdf.sail.config.SailImplConfig;

/**
 *  Interface representing a Repository Configuration which
 * contains a SailImplConfig (sesame repository configuration)
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public interface RepositoryConfiguration {

    // METHODS -----------------------------------------------------------------
    /**
     * Gets a Sesame Sail Repository configuration
     *
     * @return config
     *            the SailImplConfig object for sesame repository configuration
     */
    public SailImplConfig getSailConfiguration();
}
