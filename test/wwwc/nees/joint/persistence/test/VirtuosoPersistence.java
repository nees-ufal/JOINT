package wwwc.nees.joint.persistence.test;

import org.openrdf.repository.*;
import virtuoso.sesame2.driver.VirtuosoRepository;
import wwwc.nees.joint.module.kao.RepositoryConfig;

/**
 * @author armando
 */
public class VirtuosoPersistence implements RepositoryConfig {

    @Override
    public Repository createNewRepository() {
        return new VirtuosoRepository("jdbc:virtuoso://localhost:1111", "dba", "dba");
    }

}
