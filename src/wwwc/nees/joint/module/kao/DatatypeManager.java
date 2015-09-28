package wwwc.nees.joint.module.kao;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Olavo
 */
public class DatatypeManager {
    
    private final String STRING_CLASS = "java.lang.String";
    private final String STRING_NS = "http://www.w3.org/2001/XMLSchema#string";
    private final String BOOLEAN_CLASS = "java.lang.Boolean";
    private final String BOOLEAN_NS = "http://www.w3.org/2001/XMLSchema#boolean";
    private final String INTEGER_CLASS = "java.lang.Integer";
    private final String INTEGER_NS = "http://www.w3.org/2001/XMLSchema#int";
    private final String FLOAT_CLASS = "java.lang.Float";
    private final String FLOAT_NS = "http://www.w3.org/2001/XMLSchema#float";
    private final String DECIMAL_CLASS = "java.math.BigDecimal";
    private final String DECIMAL_NS = "http://www.w3.org/2001/XMLSchema#decimal";
    private final String DOUBLE_CLASS = "java.lang.Double";
    private final String DOUBLE_NS = "http://www.w3.org/2001/XMLSchema#double";
    private final String OBJECT_CLASS = "java.lang.Object";
    private final String OBJECT_NS = "java:Object";
    private final String DATETIME_CLASS = "javax.xml.datatype.XMLGregorianCalendar";
    private final String DATETIME_NS = "http://www.w3.org/2001/XMLSchema#dateTime";
    private final String URI_CLASS = "java.net.URI";
    private final String URI_NS = "http://www.w3.org/2001/XMLSchema#anyURI";
    public BidiMap<String, Class> namespacesClass;

    // VARIABLES
    // -------------------------------------------------------------------------
    // The repository static variable
    private static DatatypeManager manager = null;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets an instance of DatatypeManager
     *
     * @return repository a Repository object already initialized
     */
    public static DatatypeManager getInstance() {

        // If manager is null
        if (manager == null) {
            manager = new DatatypeManager();
        }
        return manager;
    }

    private DatatypeManager() {
        try {
            namespacesClass = new DualHashBidiMap<>();
            namespacesClass.put(BOOLEAN_NS, Class.forName(BOOLEAN_CLASS));
            namespacesClass.put(INTEGER_NS, Class.forName(INTEGER_CLASS));
            namespacesClass.put(FLOAT_NS, Class.forName(FLOAT_CLASS));
            namespacesClass.put(DATETIME_NS, Class.forName(DATETIME_CLASS));
            namespacesClass.put(STRING_NS, Class.forName(STRING_CLASS));
            namespacesClass.put(DECIMAL_NS, Class.forName(DECIMAL_CLASS));
            namespacesClass.put(DOUBLE_NS, Class.forName(DOUBLE_CLASS));
            namespacesClass.put(URI_NS, Class.forName(URI_CLASS));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatatypeManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean isDatatype(String className) {
        if (className.equals(STRING_CLASS)
                || className.equals(BOOLEAN_CLASS)
                || className.equals(INTEGER_CLASS)
                || className.equals(FLOAT_CLASS)
                || className.equals(DATETIME_CLASS)
                || className.equals(OBJECT_CLASS)
                || className.equals(DECIMAL_CLASS)
                || className.equals(DOUBLE_CLASS)
                || className.equals("?")) {
            return true;
        }
        return false;
    }

    public boolean isDatatype(Value v) {
        if (v instanceof Literal) {
            return true;
        }
        return false;
    }

    public Object convertDatatype(String value, String className) {
        if (className.equals(BOOLEAN_CLASS)) {
            return Boolean.getBoolean(value);
        } else if (className.equals(INTEGER_CLASS)) {
            return Integer.parseInt(value);
        } else if (className.equals(FLOAT_CLASS)) {
            return Float.parseFloat(value);
        } else if (className.equals(DECIMAL_CLASS)) {
            return Float.parseFloat(value);
        } else if (className.equals(DOUBLE_CLASS)) {
            return Double.parseDouble(value);
        } else if (className.equals(DATETIME_CLASS)) {
            return XMLGregorianCalendarImpl.parse(value);
        }
        return value;
    }

    public List<Object> convertCollectionDatatype(List<Literal> values, String className) {
        List<Object> datatypes = new ArrayList<>();
        if (className.equals(BOOLEAN_CLASS)) {
            for (Literal lit : values) {
                datatypes.add(Boolean.getBoolean(lit.stringValue()));
            }
        } else if (className.equals(INTEGER_CLASS)) {
            for (Literal lit : values) {
                datatypes.add(Integer.parseInt(lit.stringValue()));
            }
        } else if (className.equals(FLOAT_CLASS)) {
            for (Literal lit : values) {
                datatypes.add(Float.parseFloat(lit.stringValue()));
            }
        } else if (className.equals(DECIMAL_CLASS)) {
            for (Literal lit : values) {
                datatypes.add(Float.parseFloat(lit.stringValue()));
            }
        } else if (className.equals(DOUBLE_CLASS)) {
            for (Literal lit : values) {
                datatypes.add(Double.parseDouble(lit.stringValue()));
            }
        } else if (className.equals(DATETIME_CLASS)) {
            for (Literal lit : values) {
                datatypes.add(XMLGregorianCalendarImpl.parse(lit.stringValue()));
            }
        } else {
            for (Literal lit : values) {
                datatypes.add(lit.stringValue());
            }
        }
        return datatypes;
    }

    public Literal convertDatatypeToLiteral(ValueFactory f, String value, String className) {

        if (className.equals(BOOLEAN_CLASS)) {
            return f.createLiteral(Boolean.getBoolean(value));
        } else if (className.equals(INTEGER_CLASS)) {
            return f.createLiteral(Integer.parseInt(value));
        } else if (className.equals(FLOAT_CLASS)) {
            return f.createLiteral(Float.parseFloat(value));
        } else if (className.equals(DECIMAL_CLASS)) {
            return f.createLiteral(Float.parseFloat(value));
        } else if (className.equals(DOUBLE_CLASS)) {
            return f.createLiteral(Double.parseDouble(value));
        } else if (className.equals(DATETIME_CLASS)) {
            return f.createLiteral(XMLGregorianCalendarImpl.parse(value));
        }
        return f.createLiteral(value);
    }

    public Object convertLiteralToDataype(Literal lit) throws Exception {
        URI dType = lit.getDatatype();
        String datatype;
        if (dType == null) {
            datatype = OBJECT_NS;
        } else {
            datatype = dType.stringValue();
        }
        Class<?> type;
        if (namespacesClass.containsKey(datatype)) {
            type = namespacesClass.get(datatype);
        } else if (datatype.equals(OBJECT_NS)) {
            try {
                type = Class.forName(OBJECT_CLASS);
            } catch (ClassNotFoundException e) {
                throw new Exception(e);
            }
        } else {
            throw new Exception("Unknown datatype: " + datatype);
        }
        return convertDatatype(lit.stringValue(), type.getName());
    }

    public Object convertLiteralToDataype(Literal lit, String parameterClassName) throws Exception {
        URI dType = lit.getDatatype();
        String datatype;
        if (dType == null) {
            String className = namespacesClass.getKey(Class.forName(parameterClassName));
            if (className != null) {
                datatype = className;
            } else {
                datatype = OBJECT_NS;
            }
        } else {
            datatype = dType.stringValue();
        }

        Class<?> type;
        if (namespacesClass.containsKey(datatype)) {
            type = namespacesClass.get(datatype);
        } else if (datatype.equals(OBJECT_NS)) {
            try {
                type = Class.forName(OBJECT_CLASS);
            } catch (ClassNotFoundException e) {
                throw new Exception(e);
            }
        } else {
            throw new Exception("Unknown datatype: " + datatype);
        }
        return convertDatatype(lit.stringValue(), type.getName());
    }

    public List<Object> convertCollectionOfLiteralToDataypes(List<Literal> literals) throws Exception {

        //checks if it is not null
        if (literals == null || literals.isEmpty()) {
            return new ArrayList<>();
        }

        //gets the first element for checking type
        URI dType = literals.get(0).getDatatype();
        String datatype;

        //if the type is null, than it is a Java Object
        if (dType == null) {
            datatype = OBJECT_NS;
        } else {
            //else, gets the value of the its datatype
            datatype = dType.stringValue();
        }

        Class<?> type;
        //checks if the datatype is previous knowm
        if (namespacesClass.containsKey(datatype)) {
            //gets the class that matchs the datatype
            type = namespacesClass.get(datatype);
        } else if (datatype.equals(OBJECT_NS)) {
            //if its a generic object
            try {
                //than mapp to java.lang.Object
                type = Class.forName(OBJECT_CLASS);
            } catch (ClassNotFoundException e) {
                throw new Exception(e);
            }
        } else {
            //if it cannot discovery which datatype is, throws this exception
            throw new Exception("Unknown datatype: " + datatype);
        }
        //parses the entire collection to a list of datatypes
        return convertCollectionDatatype(literals, type.getName());
    }

}
