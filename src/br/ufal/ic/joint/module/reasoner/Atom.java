package br.ufal.ic.joint.module.reasoner;

/**
 * An Atom Plain Old Java Object (POJO)
 * 
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public class Atom {

    // ATTRIBUTES
    // -------------------------------------------------------------------------
    // Predicate
    private String property;
    // Subject
    private String argument1;
    // Object
    private String argument2;

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Gets the argument 1
     *
     * @return argument1
     *            
     */
    public String getArgument1() {
        return argument1;
    }

    /**
     * Sets the argument 1
     *
     * @param argument1
     *
     */
    public void setArgument1(String argument1) {
        this.argument1 = argument1;
    }

    /**
     * Gets the argument 2
     *
     * @return argument2
     *
     */
    public String getArgument2() {
        return argument2;
    }

    /**
     * Sets the argument 2
     *
     * @param argument2
     *
     */
    public void setArgument2(String argument2) {
        this.argument2 = argument2;
    }

    /**
     * Gets the property
     *
     * @return property
     *
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets the property
     *
     * @param property
     *
     */
    public void setProperty(String property) {
        this.property = property;
    }
}
