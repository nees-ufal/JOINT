package wwwc.nees.joint.facade;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Williams
 */
public class RepositoryFacadeTest {
    
    private RepositoryFacade facade;
    private String path_backup;
    
    @Before
    public void setUp() {
        facade = new RepositoryFacade();
        path_backup = ".\\backup.jnt";
    }
    
    @After
    public void tearDown() {
        facade = null;
        path_backup = null;
    }

    /**
     * Test backup method
     */
    @Test
    public void testBackup() throws IOException {
        facade.backupRepository(path_backup);
        File file = new File(path_backup);
        assertTrue(file.exists());
        assertTrue(file.getTotalSpace() > 0);
    }
}
