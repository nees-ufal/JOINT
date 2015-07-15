package wwwc.nees.joint.module.kao;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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
    private final RepositoryConnection connection;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Initializes the connection with the repository
     *
     * @param connection the class to be implemented.
     */
    public TransactionHandler(RepositoryConnection connection) {
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
        this.connection.begin();
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
