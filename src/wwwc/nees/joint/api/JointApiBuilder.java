/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wwwc.nees.joint.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import wwwc.nees.joint.compiler.source.JavaClassBuilder;

/**
 *
 * @author armando
 */
public class JointApiBuilder {

    private String basepath;
    private final String pkg = "wwwc.nees.joint.api";
    private final String apiName = "JointApi";
    private final String author = "/**\n"
            + " *\n"
            + " * @author armando\n"
            + " */";

    public JointApiBuilder() {
    }

    public JointApiBuilder setDir(File dir) {
        this.basepath = dir.getAbsolutePath();
        return this;
    }

    public void build() {
        StringBuilder pathBuilder = new StringBuilder(basepath);
        pathBuilder
                .append("/")
                .append(pkg.replace(".","/"))
                .append("/");
        try {
            new File(pathBuilder.toString()).mkdirs();
            pathBuilder
                .append(apiName)
                .append(".java");
            JavaClassBuilder classBuilder = new JavaClassBuilder(new File(pathBuilder.toString()));
            classBuilder
                    .abstractName(apiName)
                    .close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JointApiBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
