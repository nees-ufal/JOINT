package br.ufal.ic.joint.module.kao;

import org.openrdf.model.URI;
import org.openrdf.repository.object.ObjectConnection;

/**
 * Class that handles the context in the repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class ContextHandler {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    private ObjectConnection connection;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Initializes the connection with the repository
     *
     * @param connection
     *            the class to be implemented.
     */
    public ContextHandler(ObjectConnection connection) {
        this.connection = connection;
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Add an URI as context to the connection
     *
     * @param uri
     *          the string with the uri context
     */
    public void addContext(String ontologyURI) {
        // Sets the connection context
        URI contextURI = this.connection.getValueFactory().createURI(
                ontologyURI);
        this.connection.setAddContexts(contextURI);

    }

    /**
     * Gets the URI context associated in the connection
     *
     * @return uri
     *            the associated context
     */
    public URI getContext() {
        URI[] contexts = this.connection.getAddContexts();
        return contexts[0];
    }

    /**
     * Updates the URI context associated in the connection
     *
     * @param uri
     *          the string with the uri context
     */
    public void updateContext(String ontologyURI) {
        URI[] contexts = this.connection.getAddContexts();

        this.connection.setRemoveContexts(contexts);

        this.addContext(ontologyURI);
    }

    /**
     * Removes all the contexts associated with this connection
     *
     */
    public void eraseContexts() {
        URI[] contexts = this.connection.getAddContexts();

        this.connection.setRemoveContexts(contexts);
    }
}
