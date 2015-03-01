package br.ufal.ic.joint.interfacegrafica.control;

import br.ufal.ic.joint.RepositoryFacade;
import br.ufal.ic.joint.module.ontology.operations.OntologyCompiler;
import br.ufal.ic.joint.module.repository.operations.RepositoryConfiguration;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;

/**
 *
 * @author Williams
 */
public class Controle {

    private static RepositoryManager repository = null;
    private static String repository_URL = null;
    private static RepositoryFacade facade = null;

    public Controle() {
        facade = new RepositoryFacade();
    }

    public boolean conectarComSesame(String serverURL) {

        try {
            URL url = new URL(serverURL);
            repository = RepositoryProvider.getRepositoryManager(serverURL);
            JOptionPane.showMessageDialog(null, "Conectado ao Servidor.");
            return true;
        } catch (RepositoryConfigException ex) {
            JOptionPane.showMessageDialog(null, "Erro na Conexão.");
            return false;
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(null, "Erro na Conexão. Verifique a URL do servidor e se o Apache Tomcat foi iniciado.");
            return false;
        } catch (IllegalArgumentException i) {
            JOptionPane.showMessageDialog(null, "Erro na Conexão. Verifique a URL do servidor e se o Apache Tomcat foi iniciado.");
            return false;
        } catch (MalformedURLException m) {
            JOptionPane.showMessageDialog(null, "Erro na Conexão. Verifique a URL do servidor e se o Apache Tomcat foi iniciado.");
            return false;
        }
    }

    public boolean criaRepositorio3args(String id, String title, String serverURL) {

        if (id.equals("") || title.equals("")) {
            JOptionPane.showMessageDialog(null, "Parâmetro Nulo.");
            return true;
        }

        if (existeRepositorioID(id, serverURL)) {
            JOptionPane.showMessageDialog(null, "Repositório já existe.");
            return true;
        } else {
            //verificando se existe espaço no ID
            String id1 = verificaURI(id);
            String title1 = verificaURI(title);
            if (id1.equals(id) && title1.equals(title)) {
                facade.createRepository(id1, title1, serverURL);
                JOptionPane.showMessageDialog(null, "Repositório criado com sucesso.");
                return false;
            } else {
                JOptionPane.showMessageDialog(null, "Parâmetros com espaço.");
                return true;
            }
        }
    }

    public boolean criaRepositorio4args(String id, String title, String serverURL, RepositoryConfiguration configuration) {
        if (id.equals("") || title.equals("")) {
            JOptionPane.showMessageDialog(null, "Parâmetro Nulo.");
            return true;
        }

        if (existeRepositorioID(id, serverURL)) {
            JOptionPane.showMessageDialog(null, "Repositório já existe.");
            return true;
        } else {
            //verificando se existe espaço no ID
            String id1 = verificaURI(id);
            String title1 = verificaURI(title);
            if (id1.equals(id) && title1.equals(title)) {
                facade.createRepository(id, title, serverURL, configuration);
                JOptionPane.showMessageDialog(null, "Repositório criado com sucesso.");
                return false;
            } else {
                JOptionPane.showMessageDialog(null, "Parâmetros com espaço.");
                return true;
            }
        }
    }

    public DefaultListModel listaRepositorio(String serverURL) {

        DefaultListModel listaRepo = new DefaultListModel();

        RepositoryProvider rep = new RepositoryProvider();
        RepositoryManager repository = null;
        try {
            repository = rep.getRepositoryManager(serverURL);

            for (RepositoryInfo repo : repository.getAllRepositoryInfos()) {

                String i = repo.getId();
                if (!i.equals("SYSTEM")) {
                    listaRepo.addElement(i);
                }
            }
            return listaRepo;

        } catch (RepositoryException ex) {
            Logger.getLogger(Controle.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(Controle.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }

    }

    public boolean gerarCodigoJava(String path, String ontologyPath) {
        if (new File(ontologyPath).exists()) {
            if (existFile(path)) {
                List<String> listaGerar = new ArrayList<String>();
                listaGerar.add(ontologyPath);
                OntologyCompiler compiler = facade.getOntologyCompiler(path, listaGerar);
                compiler.compile();
                JOptionPane.showMessageDialog(null, "Código gerado com sucesso.");
                return true;
            }
            return false;

        } else {
            JOptionPane.showMessageDialog(null, "Ontologia não existe neste diretório.");
            return false;
        }
    }

    public boolean clearRepository(String repositoryURL, String serverURL) {
        try {
            String repoURL = existeRepositorioURL(repositoryURL, serverURL);
            facade.clearRepository(repoURL);
            JOptionPane.showMessageDialog(null, "Repositório Limpo Com Sucesso!");
            return true;
        } catch (IllegalArgumentException m) {
            JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
            return false;
        }
    }

    public boolean copyRepository(String mainRepositoryURL, String seconURL, String serverURL) {
        if (mainRepositoryURL.equals(seconURL)) {
            JOptionPane.showMessageDialog(null, "Impossivel copiar para o mesmo repositório!");
            return false;
        }
        try {
            String repoOrigem = existeRepositorioURL(mainRepositoryURL, serverURL);
            String repoDestino = existeRepositorioURL(seconURL, serverURL);

            facade.copyRepository(repoOrigem, repoDestino);
            JOptionPane.showMessageDialog(null, "Repositório copiado com sucesso.");
            return true;
        } catch (IllegalArgumentException i) {
            JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
            return false;
        }
    }

    public boolean retrieveOntology(String path, String ontologyURI, String repositoryURL, String serverURL) {

        if (existFile(path)) {
            try {
                String uri = verificaURI(ontologyURI);
                URL url = new URL(uri);

                // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
                String repoURL = existeRepositorioURL(repositoryURL, serverURL);

//                facade.retrieveOntology(path, uri, repoURL);
                facade.retrieveOntology(path, uri);
                JOptionPane.showMessageDialog(null, "Ontologia recuperada com sucesso.");
                return true;
            } catch (IllegalArgumentException i) {
                JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
                return false;
            } catch (MalformedURLException m) {
                JOptionPane.showMessageDialog(null, "URI inválida.");
                return false;
            }
        }
        return false;
    }

    public boolean removeRepository(String ID, String serverURL) {
        String title = ID;
        if (ID.equals("")) {
            JOptionPane.showMessageDialog(null, "Campo Null!");
            return false;
        } else if (!existeRepositorioID(ID, serverURL)) {
            JOptionPane.showMessageDialog(null, "Repositório não existe.");
            return false;
        }

        facade.removeRepository(ID, title, serverURL);
        if (!existeRepositorioID(ID, serverURL)) {
            JOptionPane.showMessageDialog(null, "Repositório Removido com sucesso!");
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Error!");
            return false;
        }
    }

    public boolean backupRepository(String repositoryURL, String filePath, String serverURL) {

        if (existFile(filePath)) {
            try {
                // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
                String repoURL = existeRepositorioURL(repositoryURL, serverURL);

                facade.backupRepository(repoURL, filePath);
                JOptionPane.showMessageDialog(null, "Backup criado com sucesso.");
                return true;
            } catch (IllegalArgumentException i) {
                JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
                return false;
            }
        }
        return false;

    }

    public boolean restoreBackup(String repositoryURL, String filePath, String serverURL) {

        if (new File(filePath).exists()) {
            try {
                String repoURL = existeRepositorioURL(repositoryURL, serverURL);
                facade.restoreBackup(repoURL, filePath);
                JOptionPane.showMessageDialog(null, "Repositório Restaurado Com Sucesso!");
                return true;
            } catch (IllegalArgumentException i) {
                JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
                return false;
            }

        } else {
            JOptionPane.showMessageDialog(null, "Path Inexistente!");
            return false;
        }
    }

    public String verificaURI(String valor) {
        for (int i = 0; i < valor.length(); i++) {
            if (valor.charAt(i) == ' ') {
                return " ";
            }
        }
        return valor;
    }

    public boolean adicionaOntologia(String pathOnto, String uriOnto, String repositoryURL, String serverURL) {

        if (new File(pathOnto).exists()) {
            try {
                String uri = verificaURI(uriOnto);
                URL url = new URL(uri);
                // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
                String repoURL = existeRepositorioURL(repositoryURL, serverURL);

//                facade.addOntology(pathOnto, uri, repoURL);
                facade.addOntology(pathOnto, uri);
                JOptionPane.showMessageDialog(null, "Ontologia adicionada com sucesso.");
                return true;
            } catch (IllegalArgumentException i) {
                JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
                return false;
            } catch (MalformedURLException m) {
                JOptionPane.showMessageDialog(null, "URI inválida.");
                return false;
            }

        } else {
            JOptionPane.showMessageDialog(null, "Ontologia não existe nesse diretório.");
            return false;
        }
    }

    public boolean deleteOntology(String ontoURI, String repositoryURL, String serverURL) {
        String uri = null;
        String repoURL = null;
        try {
            uri = verificaURI(ontoURI);
            URL url = new URL(uri);
            // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
            repoURL = existeRepositorioURL(repositoryURL, serverURL);

//            facade.deleteOntology(uri, repoURL);
            facade.deleteOntology(uri);
            JOptionPane.showMessageDialog(null, "Ontologia removida com sucesso.");
            return true;
        } catch (MalformedURLException m) {
            JOptionPane.showMessageDialog(null, "URI inválida.");
            return false;
        } catch (IllegalArgumentException i) {
            JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
            return false;
        }
    }

    public boolean exportaOntologia(String repositoryURL, String dir, String serverURL) {
        if (existFile(dir)) {
            try {
                // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
                String repoURL = existeRepositorioURL(repositoryURL, serverURL);

                facade.exportRepositoryOntologies(repoURL, dir);
                JOptionPane.showMessageDialog(null, "Ontologia foi exportada.");
                return true;
            } catch (IllegalArgumentException i) {
                JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
                return false;
            } catch (NullPointerException n) {
                JOptionPane.showMessageDialog(null, "Parâmetros inválidos.");
                return false;
            }
        }
        return false;
    }

    public boolean checaConsistencia(String ontologyURL) {
        try {
            if (facade.checkOntologyConsistency(ontologyURL)) {
                JOptionPane.showMessageDialog(null, "Ontologia em estado correto.");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Ontologia em estado de erro.");
                return true;
            }
        } catch (NullPointerException nulo) {
            JOptionPane.showMessageDialog(null, "Ontologia não existe no diretório especificado.");
            return false;
        }
    }

    public boolean atualizaOntologia(String path, String ontologyURI, String repositoryURL, String serverURL) {
        if (new File(path).exists()) {
            try {
                String uri = verificaURI(ontologyURI);
                URL url = new URL(uri);
                // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
                String repoURL = existeRepositorioURL(repositoryURL, serverURL);
//                facade.updateOntology(path, uri, repoURL);
                facade.updateOntology(path, uri);
                JOptionPane.showMessageDialog(null, "Ontologia atualizada com sucesso.");
                return true;
            } catch (MalformedURLException m) {
                JOptionPane.showMessageDialog(null, "URI mal formada.");
                return false;
            } catch (IllegalArgumentException i) {
                JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Ontologia não existe neste diretório.");
            return false;
        }

    }

    public boolean executarRegras(String repositoryURL, String serverURL) {

        try {
            // Se o repositorio existir vai retornar a URL, senão, retornará nulo.
            String repoURL = existeRepositorioURL(repositoryURL, serverURL);
            JOptionPane.showMessageDialog(null, repoURL);
            facade.performRulesInRepository(repoURL);
            JOptionPane.showMessageDialog(null, "Regras executadas com sucesso.");
            return true;
        } catch (IllegalArgumentException i) {
            JOptionPane.showMessageDialog(null, "Erro. Verifique a URL do Repositório.");
            return false;
        }

    }

    public boolean existeRepositorioID(String id, String serverURL) {
        try {
            RepositoryManager rm = RepositoryProvider.getRepositoryManager(serverURL);
            for (RepositoryInfo r : rm.getAllUserRepositoryInfos()) {
                if (id.equals(r.getId())) {
                    return true;
                }
            }
            return false;
        } catch (RepositoryConfigException ex) {
            return false;
        } catch (RepositoryException ex) {
            return false;
        }
    }

    public String existeRepositorioURL(String repoURL, String serverURL) {
        try {
            RepositoryManager rm = RepositoryProvider.getRepositoryManager(serverURL);
            for (RepositoryInfo r : rm.getAllUserRepositoryInfos()) {
                if (repoURL.equals(r.getLocation().toString())) {
                    return repoURL;
                }
            }
            return null;
        } catch (RepositoryConfigException ex) {
            return null;
        } catch (RepositoryException ex) {
            return null;
        }
    }

    public boolean existFile(String filePath) {
        if (new File(filePath).exists()) {
            int option = JOptionPane.showConfirmDialog(null, "Deseja substituir?", "Arquivo já existe neste diretório", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                return true;
            } else {
                return false;
            }
        } else {
            if (new File(filePath).getParentFile().exists()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Diretório não existe.");
                return false;
            }
        }
    }

    public void setRepositoryURL(String id, String serverURL) {
        RepositoryProvider rep = new RepositoryProvider();
        RepositoryManager repository = null;
        try {
            repository = rep.getRepositoryManager(serverURL);

            for (RepositoryInfo repo : repository.getAllRepositoryInfos()) {

                String i = repo.getId();

                if (i.equals(id)) {
                    this.repository_URL = repo.getLocation().toString();

                }
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(Controle.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(Controle.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getRepositoryURL() {
        return this.repository_URL;
    }
}
