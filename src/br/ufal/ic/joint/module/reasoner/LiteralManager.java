/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufal.ic.joint.module.reasoner;

import br.ufal.ic.joint.model.RDFUris;
import org.openrdf.repository.RepositoryConnection;

/**
 * Manager for manipulating xml literals
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class LiteralManager {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    RepositoryConnection connection;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Initializes the connection with the repository
     *
     * @param connection
     *            the class to be implemented.
     */
    public LiteralManager(RepositoryConnection connection) {
        this.connection = connection;
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Checks if an argument is a literal or not
     *
     * @param arg
     *            the string with an argument
     * @return boolean
     *            true if the arg is a literal, else false
     */
    public boolean checkArgumentLiteral(String arg) {

        // If the string contains a xml:string it is a literal
        if (arg.contains(RDFUris.XSD_STRING)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the literal value
     *
     * @param literal
     *            the string with the literal
     * @return value
     *            the content inside the literal
     */
    public String getLiteralValue(String literal) {

        // Gets the substring of the literal between "
        String value = literal.substring(1, literal.lastIndexOf("\""));

        return value;
    }
}
