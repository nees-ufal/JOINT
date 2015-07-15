package wwwc.nees.joint.module.repository.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import wwwc.nees.joint.module.kao.GraphQueryConstruct;
import wwwc.nees.joint.module.kao.RepositoryFactory;

/**
 * Manager for backuping data from the repository
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class BackupManager {

    // VARIABLES ---------------------------------------------------------------
    // Final constants representing the backup tokens
    private static final String ORI_STA_TOKEN = "<statement>";
    private static final String FIN_STA_TOKEN = "</statement>";
    private static final String ORI_SUB_TOKEN = "<subject>";
    private static final String FIN_SUB_TOKEN = "</subject>";
    private static final String ORI_PRE_TOKEN = "<predicate>";
    private static final String FIN_PRE_TOKEN = "</predicate>";
    private static final String ORI_OBJ_TOKEN = "<object>";
    private static final String FIN_OBJ_TOKEN = "</object>";
    private static final String ORI_CON_TOKEN = "<context>";
    private static final String FIN_CON_TOKEN = "</context>";

    private final Repository repository;

    public BackupManager() {
        repository = RepositoryFactory.getRepository();
    }

    // METHODS -----------------------------------------------------------------
    /**
     * Copies all data from one repository to others. Be carefull, this will
     * erase all data of the others.
     *
     * @param mainURL the main repository URL with the data to be copied
     * @param repositories the repositories urls where the data will be copied
     * to
     */
    public void copyRepository(String mainRepository, String... urls) {

        // Gets the main repository
        Repository mainRepo = new HTTPRepository(mainRepository);
        try {
            // Retrieves the connection of the main one
            RepositoryConnection mainCon = mainRepo.getConnection();

            List<RepositoryConnection> otherCon = new ArrayList<RepositoryConnection>();

            // Gets a list of the others repositories connections
            for (String url : urls) {
                Repository repo = new HTTPRepository(url);
                repo.initialize();
                RepositoryConnection con = repo.getConnection();
                con.clear((Resource) null);
                otherCon.add(con);
            }
            RepositoryResult<Statement> result = mainCon.getStatements((Resource) null, null, null, true);
            while (result.hasNext()) {
                Statement t = result.next();

                for (RepositoryConnection c : otherCon) {
                    c.add(t, t.getContext());
                }
            }

            for (RepositoryConnection con : otherCon) {
                con.close();
            }
            mainCon.close();
            mainRepo.shutDown();

        } catch (RepositoryException ex) {
            Logger.getLogger(BackupManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Creates a backup file of the repository with the path specified
     *
     * @param filePath the backup file path
     */
    public void backupRepository(String filePath) {
        // Gets the main repository
        try {
            // Retrieves the connection of the main one
            RepositoryConnection connection = repository.getConnection();
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
            GregorianCalendar calendar = new GregorianCalendar();
            Logger.getLogger("Joint Backup Manager - Date and Time: " + calendar.getTime().toString());

            FileWriter writer = new FileWriter(file);
            PrintWriter print = new PrintWriter(writer, true);

            GraphQueryConstruct queryContruct = new GraphQueryConstruct(connection);
            GraphQueryResult result = queryContruct.getStatementsAsGraphQuery((String) null, null, null);
            while (result.hasNext()) {
                Statement t = result.next();
                String line = this.generateStatementLine(t);
                print.println(line);
            }
            result.close();
            print.close();
            writer.close();
            Logger.getLogger("File Joint Backup was stored in " + file.getCanonicalPath());

            connection.close();

        } catch (QueryEvaluationException | RepositoryException | IOException ex) {
            Logger.getLogger(BackupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(BackupManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Restores the backup file to the repository. This will erase any previous
     * data in the repository
     *
     * @param repositoryURL the main repository URL
     * @param filePath the backup file path
     */
    public void restoreBackup(String repositoryURL, String filePath) {
        // Gets the main repository
        Repository mainRepo = new HTTPRepository(repositoryURL);
        try {

            mainRepo.initialize();
            // Retrieves the connection of the main one
            RepositoryConnection mainCon = mainRepo.getConnection();

            File file = new File(filePath);

            if (!file.exists()) {
                System.err.println("File not Found " + filePath);
                return;
            }
            FileReader reader = new FileReader(file);
            BufferedReader buffer = new BufferedReader(reader);

            buffer.readLine();

            mainCon.clear((Resource) null);
            mainCon.begin();
            ValueFactory f = mainCon.getValueFactory();

            try {
                while (buffer.ready()) {
                    String line = buffer.readLine();

                    String statement = this.retrievesLineStatement(line);
                    String subject = this.retrievesLineSubject(statement);
                    String predicate = this.retrievesLinePredicate(statement);
                    String object = this.retrievesLineObject(statement);
                    String context = this.retrievesLineContext(statement);

                    Resource subResource = this.getSubjectResource(subject, f);
                    URI predURI = this.getPredicateURI(predicate, f);
                    Value objValue = this.getObjectValue(object, f);
                    Resource contResource = this.getContextResource(context, f);

                    mainCon.add(subResource, predURI, objValue, contResource);
                }
                mainCon.commit();
            } catch (Exception e) {
                mainCon.rollback();
            } finally {

                buffer.close();
                reader.close();

                mainCon.close();
                mainRepo.shutDown();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(BackupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BackupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a String representing one statement
     *
     * @param statement a Statement
     * @return line the string representing a statement line
     */
    private String generateStatementLine(Statement sta) {
        StringBuilder builder = new StringBuilder();
        builder.append(ORI_STA_TOKEN + ORI_SUB_TOKEN);
        builder.append(sta.getSubject().stringValue());
        builder.append(FIN_SUB_TOKEN + ORI_PRE_TOKEN);
        builder.append(sta.getPredicate().stringValue());
        builder.append(FIN_PRE_TOKEN + ORI_OBJ_TOKEN);
        builder.append(sta.getObject().stringValue());
        builder.append(FIN_OBJ_TOKEN + ORI_CON_TOKEN);
        if (sta.getContext() == null) {
            builder.append("null");
        } else {
            builder.append(sta.getContext().stringValue());
        }
        builder.append(FIN_CON_TOKEN + FIN_STA_TOKEN);

        return builder.toString();
    }

    /**
     * Retrieves the content inserted between the statement tokens
     *
     * @param line a Statement line
     * @return content the string between statement tokens
     */
    private String retrievesLineStatement(String line) {

        // Creates the return string
        String pureStatement = "";

        // Checks if the passed line contains the statement initial token
        // and the final one, if not throws exception
        if ((line.contains(ORI_STA_TOKEN)) && (line.contains(FIN_STA_TOKEN))) {

            // Gets the size of the first token
            int oriTokenLenght = ORI_STA_TOKEN.length();

            // Retrieves the index of the first token in the line plus his size
            int initialSubstring = line.indexOf(ORI_STA_TOKEN) + oriTokenLenght;

            // Retrieves the index of the final token
            int finalSubstring = line.indexOf(FIN_STA_TOKEN);

            // Creates a new substring with the calculated indexes
            pureStatement = line.substring(initialSubstring, finalSubstring);

        } else {
            System.err.println("Malformed Backup Line: " + line);
        }
        return pureStatement;
    }

    /**
     * Retrieves the content inserted between the subject tokens
     *
     * @param line a Statement line
     * @return content the string between subject tokens
     */
    private String retrievesLineSubject(String line) {

        // Creates the return string
        String pureSubject = "";

        // Checks if the passed line contains the subject initial token
        // and the final one, if not throws exception
        if ((line.contains(ORI_SUB_TOKEN)) && (line.contains(FIN_SUB_TOKEN))) {

            // Gets the size of the first token
            int oriTokenLenght = ORI_SUB_TOKEN.length();

            // Retrieves the index of the first token in the line plus his size
            int initialSubstring = line.indexOf(ORI_SUB_TOKEN) + oriTokenLenght;

            // Retrieves the index of the final token
            int finalSubstring = line.indexOf(FIN_SUB_TOKEN);

            // Creates a new substring with the calculated indexes
            pureSubject = line.substring(initialSubstring, finalSubstring);

        } else {
            System.err.println("Malformed Backup Line: " + line);
        }
        return pureSubject;
    }

    /**
     * Retrieves the content inserted between the predicate tokens
     *
     * @param line a Statement line
     * @return content the string between predicate tokens
     */
    private String retrievesLinePredicate(String line) {

        // Creates the return string
        String purePredicate = "";

        // Checks if the passed line contains the predicate initial token
        // and the final one, if not throws exception
        if ((line.contains(ORI_PRE_TOKEN)) && (line.contains(FIN_PRE_TOKEN))) {

            // Gets the size of the first token
            int oriTokenLenght = ORI_PRE_TOKEN.length();

            // Retrieves the index of the first token in the line plus his size
            int initialSubstring = line.indexOf(ORI_PRE_TOKEN) + oriTokenLenght;

            // Retrieves the index of the final token
            int finalSubstring = line.indexOf(FIN_PRE_TOKEN);

            // Creates a new substring with the calculated indexes
            purePredicate = line.substring(initialSubstring, finalSubstring);

        } else {
            System.err.println("Malformed Backup Line: " + line);
        }
        return purePredicate;
    }

    /**
     * Retrieves the content inserted between the object tokens
     *
     * @param line a Statement line
     * @return content the string between object tokens
     */
    private String retrievesLineObject(String line) {

        // Creates the return string
        String pureObject = "";

        // Checks if the passed line contains the object initial token
        // and the final one, if not throws exception
        if ((line.contains(ORI_OBJ_TOKEN)) && (line.contains(FIN_OBJ_TOKEN))) {

            // Gets the size of the first token
            int oriTokenLenght = ORI_OBJ_TOKEN.length();

            // Retrieves the index of the first token in the line plus his size
            int initialSubstring = line.indexOf(ORI_OBJ_TOKEN) + oriTokenLenght;

            // Retrieves the index of the final token
            int finalSubstring = line.indexOf(FIN_OBJ_TOKEN);

            // Creates a new substring with the calculated indexes
            pureObject = line.substring(initialSubstring, finalSubstring);

        } else {
            System.err.println("Malformed Backup Line: " + line);
        }
        return pureObject;
    }

    /**
     * Retrieves the content inserted between the context tokens
     *
     * @param line a Statement line
     * @return content the string between context tokens
     */
    private String retrievesLineContext(String line) {

        // Creates the return string
        String pureContext = "";

        // Checks if the passed line contains the context initial token
        // and the final one, if not throws exception
        if ((line.contains(ORI_CON_TOKEN)) && (line.contains(FIN_CON_TOKEN))) {

            // Gets the size of the first token
            int oriTokenLenght = ORI_CON_TOKEN.length();

            // Retrieves the index of the first token in the line plus his size
            int initialSubstring = line.indexOf(ORI_CON_TOKEN) + oriTokenLenght;

            // Retrieves the index of the final token
            int finalSubstring = line.indexOf(FIN_CON_TOKEN);

            // Creates a new substring with the calculated indexes
            pureContext = line.substring(initialSubstring, finalSubstring);

        } else {
            System.err.println("Malformed Backup Line: " + line);
        }
        return pureContext;
    }

    /**
     * Creates the Resource which represents a subject
     *
     * @param subject the string with the subject (an URI)
     * @param factory the ValueFactory of the repository connection
     * @return resource the Resource which represents a subject
     */
    private Resource getSubjectResource(String subject, ValueFactory f) {

        // Checks if it is a blank node
        if (subject.contains("node")) {

            // Creates a new blank node with the defined id
            return f.createBNode(subject);

            // Checks if it is an URI
        } else if (subject.contains("http")) {

            // Then creates a new URI with the specified String
            return f.createURI(subject);

        } else {
            // If none above, throws an unexpected exception
            System.err.println("Unexpected Backup Token, was expecting"
                    + " an URI or BlankNode but was " + subject);
            return null;
        }
    }

    /**
     * Creates the URI which represents a predicate
     *
     * @param predicate the string representing an URI with the predicate
     * @param factory the ValueFactory of the repository connection
     * @return uri the URI which represents a predicate
     */
    private URI getPredicateURI(String predicate, ValueFactory f) {

        // Checks if it is an URI
        if (predicate.contains("http")) {

            // Then creates a new URI with the specified String
            return f.createURI(predicate);

        } else {
            // If none above, throws an unexpected exception
            System.err.println("Unexpected Backup Token, was expecting"
                    + " an URI but was " + predicate);
            return null;
        }
    }

    /**
     * Creates the Value which represents an object
     *
     * @param object the string with the object (an URI)
     * @param factory the ValueFactory of the repository connection
     * @return value the Value which represents an object
     */
    private Value getObjectValue(String object, ValueFactory f) {
        // Checks if it is a blank node
        if (object.contains("node")) {

            // Creates a new blank node with the defined id
            return f.createBNode(object);

            // Checks if it is an URI
        } else if (object.contains("http")) {

            // Then creates a new URI with the specified String
            return f.createURI(object);

        } else {
            // If none above it is a literal
            return f.createLiteral(object);
        }
    }

    /**
     * Creates the Resource which represents a context
     *
     * @param context the string with the context (an URI)
     * @param factory the ValueFactory of the repository connection
     * @return resource the Resource which represents a context
     */
    private Resource getContextResource(String context, ValueFactory f) {
        // Checks if it is an URI
        if (context.contains("http")) {

            // Then creates a new URI with the specified String
            return f.createURI(context);

            // Checks if there is no associated context
        } else if (context.contains("null")) {
            return null;
        } else {
            // If none above, throws an unexpected exception
            System.err.println("Unexpected Backup Token, was expecting"
                    + " an URI but was " + context);
            return null;
        }
    }
}
