package wwwc.nees.joint.module.kao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for reading the configuration.properties in the project
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public final class Configuration {

    // VARIABLES AND CONSTANTS
    // -------------------------------------------------------------------------
    private static final Logger logger;

    static {
        logger = Logger.getLogger(Configuration.class.getName());
    }
    //Singleton configuration instance
    private static Configuration instance;
    // The file name
    private static String file_name;
    // The source path of the file
    private static String source;
    // The properties file
    private Properties prop;
    // Variable representing the string file path
    private String path;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Creates a new this.persistenceOWLLog.getConfiguration() instance of
     * Configuration
     *
     * @param name
     *          the string with the file name
     */
    private Configuration(final String file_name) {
        Configuration.file_name = file_name;

        if (Configuration.source != null) {

            try {
                StringBuilder builderPath = new StringBuilder();
                builderPath.append(new File("").getAbsolutePath());
                builderPath.append(System.getProperty("file.separator"));
                builderPath.append(Configuration.source);
                builderPath.append(System.getProperty("file.separator"));
                builderPath.append(Configuration.file_name);
                builderPath.append(".properties");

                this.path = builderPath.toString();

                if (new File(path).exists()) {
                    this.prop = new Properties();
                    this.prop.load(new FileInputStream(path));
                    logger.log(Level.INFO, "Loading configurations of {0}", path);
                    return;
                }

            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Classpath not found");

            } catch (IOException e) {
                throw new IllegalArgumentException("Troubles trying to open the file.");
            }
        }

        try {
            StringBuilder builderPath = new StringBuilder();
            builderPath.append(file_name);
            builderPath.append(".properties");

            this.path = builderPath.toString();
            System.out.println(this.path);

            this.prop = new Properties();
            this.prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(this.path));

            logger.log(Level.INFO, "Loading configurations of {0}", this.path);

        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("Configuration file not found." + this.path);

        } catch (IOException ex) {
            throw new IllegalArgumentException("Troubles trying to open the file.");
        }
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets the instance of the class
     *
     * @param name
     *            the string with the file name
     * @return configuration
     *            the new instance.
     */
    public synchronized static Configuration getInstance(final String file_name) {
        if (Configuration.file_name == null || !Configuration.file_name.equals(file_name)) {
            Configuration.instance = new Configuration(file_name);
        }

        return Configuration.instance;
    }

    /**
     * Gets the value of a particular key
     *
     * @param key
     *            the associated key
     * @return value
     *            the string representing the value of the key
     */
    public String getValue(final String key) {
        if (!this.prop.containsKey(key)) {
            throw new IllegalArgumentException("Key" + key + " not found");
        }

        return this.prop.getProperty(key);
    }

    /**
     * Gets all the keys presented in the configuration file
     *
     * @return keys
     *            a collection with all the keys
     */
    public Collection<Object> getKeys() {
        return this.prop.keySet();
    }

    /**
     * Puts a line in the configuration file with Key - Value
     *
     * @param key
     *            the string with the key
     * @param value
     *            the string with the value
     */
    public void put(String key, String value) {
        this.prop.put(key, value);

        try {
            this.prop.store(new FileOutputStream(this.path), "");
        } catch (IOException e) {
            Logger.getLogger(Configuration.class.getName()).
                    log(Level.SEVERE, null, e);
        }
    }

    /**
     * Static method to set the source
     *
     * @param source
     *            the string with the source
     */
    public static void setSource(final String source) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(".classpath"));

            while (scanner.hasNext()) {

                String src = "";
                if ((src = scanner.nextLine()).contains("kind=\"src\"")) {

                    if (source.equals(src.split("path=\"")[1].split("\"")[0])) {
                        Configuration.source = source;
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger(Configuration.class.getName()).
                    log(Level.SEVERE, null, e);
        }

        throw new IllegalStateException("Source not found!!!");
    }
}
