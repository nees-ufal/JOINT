/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.module.reasoner;

/**
 * Interface for perfoming rules in the repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public interface RuleEngine {

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Performs rules in the specified repository
     *
     * @param url
     *            the Repository URL
     * @return statements
     *            the Number of inferred Statements
     */
    public int performRulesInRepository(String repositoryURL);

    /**
     * Performs rules in the repository specified by configuration.properties
     *
     * @return statements
     *            the Number of inferred Statements
     */
    public int performRulesInRepository();
}
