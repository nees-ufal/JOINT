package br.ufal.ic.joint.module.kao;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

/**
 * Handles the transactions in the repository connection
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class TransactionHandler {

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
    public TransactionHandler(ObjectConnection connection) {
        this.connection = connection;
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Starts the transaction in the connection
     *
     * @throws RepositoryException
     */
    public void beginTransaction() throws RepositoryException {
        this.connection.setAutoCommit(false);
    }

    /**
     * Commits changes made in the connection
     *
     * @throws RepositoryException
     */
    public void commitChanges() throws RepositoryException {
        this.connection.commit();
    }

    /**
     * Rollback uncommited changes in the connection
     *
     * @throws RepositoryException
     */
    public void rollback() throws RepositoryException {
        this.connection.rollback();
    }
}
