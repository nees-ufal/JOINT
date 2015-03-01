/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufal.ic.joint.interfacegrafica;

import br.ufal.ic.joint.interfacegrafica.control.Controle;
import br.ufal.ic.joint.module.repository.operations.OWLIMLiteRepositoryConfigImpl;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.FlowView;
import javax.swing.tree.*;
import org.openrdf.query.algebra.Exists;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.http.config.HTTPRepositoryFactory;

/**
 *
 * @author Williams
 */
public class FirstWindow extends JFrame {

    private Controle controle = null;
    private static JPanel painelFundo = null;
    private static JPanel painelTopo = null;
    private static JPanel painelEsq = null;
    private static JPanel painelCentro = null;
    private static Dimension tamanhoTela = null;
    private static Dimension tPainelEsq = null;
    private static Dimension tPainelTopo = null;
    private static JTree arvore = null;
    private static JMenuBar menuBar = null;
    private static JMenu menuArquivo = null;
    private static JMenu menuAjuda = null;
    private static JMenuItem menuItemTutorial = null;
    private static JMenuItem menuItemSobre = null;
    private static JMenuItem menuItemSair = null;
    private DefaultMutableTreeNode noRaiz = new DefaultMutableTreeNode("Operações");
    private JList lista = null;
    private JTextField serverURL = null;
    private JTextField id = null;
    private JTextField title = null;
    private JRadioButton configuration = null;
    private JTextField repoURL = null;
    private JTextField mainRepo = null;
    private JTextField seconRepo = null;
    private JRadioButton URLconf = null;
    private JTextField repoID = null;
    private JTextField ontoURL = null;
    private JTextField ontoURI = null;

    public FirstWindow() {
        super();

        initializeMenu();
        initializePanel();

        //Evento para fechar clicando no botão 'X' e pelo menu 'Sair'
        arquivoSair();
        fecharX();

        //eventos
        eventos();

    }

    public void adicionaNoArvore(String noP, String[] noF) {
        DefaultMutableTreeNode noPai = new DefaultMutableTreeNode(noP);
        DefaultMutableTreeNode noFilho = null;

        if (noF.length > 0) {
            for (String i : noF) {
                noFilho = new DefaultMutableTreeNode(i);
                noPai.add(noFilho);
            }
        }
        noRaiz.add(noPai);
    }

    public static void expandirJTree(JTree tree) {
        for (int row = 0; row <= tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
    }

    public void initializeMenuArvore() {
        String[] nosFilhosConectar = {"Conectar"};
        adicionaNoArvore("Servidor", nosFilhosConectar);
        String[] nosFilhosRepositorio = {"Criar", "Limpar", "Copiar", "Backup", "Restaurar", "Remover"};
        adicionaNoArvore("Repositório", nosFilhosRepositorio);
        String[] nosFilhosOntologia = {"Adicionar", "Deletar", "Recuperar", "Atualizar", "Exportar", "Checa Consistência", "Executar Regras", "Gerar Código Java"};
        adicionaNoArvore("Ontologia", nosFilhosOntologia);

        arvore = new JTree(noRaiz);
        expandirJTree(arvore);
        arvore.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpaque(true);
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);

        Dimension dimensaoTree = new Dimension(tPainelEsq.width - 50, 25);
        renderer.setPreferredSize(dimensaoTree);

        arvore.setCellRenderer(renderer);
        arvore.setBackground(SystemColor.LIGHT_GRAY);
        painelEsq.add(arvore, BorderLayout.LINE_START);
    }

    public void initializePanel() {

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //Titulo da janela
        this.setTitle("Java Ontology Integrated Toolkit - JOINT");

        //Alterando Icone da barra de titulos
        ImageIcon icone = new ImageIcon("src\\icon.png");
        this.setIconImage(icone.getImage());

        //obtendo tamanho da tela
        tamanhoTela = this.getToolkit().getScreenSize();
        this.setSize(tamanhoTela);

        //Painel Topo
        painelTopo = new JPanel(new BorderLayout());
//        painelTopo.setLayout(null);
        painelTopo.setBackground(SystemColor.LIGHT_GRAY);
        painelTopo.setPreferredSize(new Dimension(tamanhoTela.width / 7, tamanhoTela.height / 9));
        painelTopo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        painelTopo.setOpaque(true);

        ImageIcon logoJOINTEsq = new ImageIcon("src\\logoTop.png");
        JLabel tituloTopoEsq = new JLabel("  JOINT");
        tituloTopoEsq.setIcon(logoJOINTEsq);

        Font fonte = new Font("Baskerville Old Face", NORMAL, 55);
        tituloTopoEsq.setFont(fonte);
        painelTopo.add(tituloTopoEsq, BorderLayout.WEST);

        ImageIcon logoNEES = new ImageIcon("src\\nees3.png");
        JLabel tituloNEESDir = new JLabel();
        tituloNEESDir.setIcon(logoNEES);
        painelTopo.add(tituloNEESDir, BorderLayout.EAST);

        //Painel Esquerdo
        painelEsq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEsq.setBackground(SystemColor.lightGray);
        painelEsq.setOpaque(true);
        tPainelEsq = new Dimension(tamanhoTela.width / 8, tamanhoTela.height / 2);
        painelEsq.setPreferredSize(tPainelEsq);
        painelEsq.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        //Painel Centro
        painelCentro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelCentro.setOpaque(false);
//        painelEsq.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        painelCentro.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        painelFundo = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image imagem = new ImageIcon("src\\logoJOINT_g.gif").getImage();
                g.drawImage(imagem, 0, 0, this);
            }
        };
        painelFundo.setBorder(new EmptyBorder(5, 5, 5, 5));
        painelFundo.setLayout(new BorderLayout(0, 0));

        initializeMenuArvore();

        painelFundo.add(painelTopo, BorderLayout.NORTH);
        painelFundo.add(painelEsq, BorderLayout.LINE_START);
        painelFundo.add(painelCentro, BorderLayout.CENTER);

        this.setContentPane(painelFundo);
    }

    public void reconstruirPanel() {
        painelCentro.removeAll();
        painelCentro.repaint();
        painelCentro.revalidate();
        arvore.clearSelection();
    }

    public void eventos() {
        arvore.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                //Returns the last path element of the selection.
                //This method is useful only when the selection model allows a single selection.
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) arvore.getLastSelectedPathComponent();

                if (node == null) //Nothing is selected.     
                {
                    return;
                }

                Object nodeInfo = node.getUserObject();

                if ((node.toString()).equals("Conectar")) {
                    conectaSesame();
                }
                if ((node.toString()).equals("Criar")) {
                    criaRepositorio();

                }
                if ((node.toString()).equals("Limpar")) {
                    limparRepositorio();
                }
                if ((node.toString()).equals("Copiar")) {
                    copiarRepositorio();
                }
                if ((node.toString()).equals("Remover")) {
                    removerRepositorio();
                }
                if ((node.toString()).equals("Backup")) {
                    backupRepositorio();
                }
                if ((node.toString()).equals("Restaurar")) {
                    restaurarRepositorio();
                }
                if ((node.toString()).equals("Repositório")) {
                    painelCentro.setLayout(null);
                    reconstruirPanel();
                    listarRepositorio();
                }

                if ((node.toString()).equals("Adicionar")) {
                    adicionaOntologia();
                }
                if ((node.toString()).equals("Deletar")) {
                    deletarOntologia();
                }
                if ((node.toString()).equals("Exportar")) {
                    exportarOntologia();
                }
                if ((node.toString()).equals("Atualizar")) {
                    atualizarOntologia();
                }
                if ((node.toString()).equals("Gerar Código Java")) {
                    gerarCodigoJava();
                }
                if ((node.toString()).equals("Checa Consistência")) {
                    checaConsistencia();
                }
                if ((node.toString()).equals("Executar Regras")) {
                    executarRegras();
                }
                if ((node.toString()).equals("Recuperar")) {
                    recuperarOntologia();
                }


            }
        });
    }

    public void conectaSesame() {

        setTituloLabel("Conectando ao Servidor do Sesame");

        setLabel("URL do Servidor", 1, 90);

        serverURL = new JTextField();
        serverURL.setBounds(135, 90, 200, 30);
        painelCentro.add(serverURL);

        JButton botao = new JButton("Conectar ao Servidor");
        botao.setBounds(40, 150, 180, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controle = new Controle();
                if (controle.conectarComSesame(serverURL.getText())) {
                    reconstruirPanel();
                }

            }
        });
    }

    public void criaRepositorio() {

        setTituloLabel("Criando Repositório");
        setLabel("ID", 1, 90);

        id = new JTextField();
        id.setBounds(135, 90, 200, 30);
        painelCentro.add(id);

        setLabel("Title", 1, 140);

        title = new JTextField();
        title.setBounds(135, 140, 200, 30);
        painelCentro.add(title);

        setLabel("Configuração", 1, 190);

        configuration = new JRadioButton("Usar configurações");
        configuration.setBounds(135, 195, 20, 20);
        painelCentro.add(configuration);

        JButton botao = new JButton("Criar Repositório");
        botao.setBounds(40,
                240, 130, 30);
        painelCentro.add(botao);

        //reconstroi Panel reconstruirPanel();

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (configuration.isSelected()) {
                    OWLIMLiteRepositoryConfigImpl config = new OWLIMLiteRepositoryConfigImpl();
                    try {
                        if (!controle.criaRepositorio4args(id.getText(), title.getText(),
                                serverURL.getText(), config)) {
                            reconstruirPanel();
                        }
                    } catch (NullPointerException n) {
                        JOptionPane.showMessageDialog(null, "Primeiro conecte-se ao servidor.");
                    }
                } else {
                    try {
                        if (!controle.criaRepositorio3args(id.getText(), title.getText(), serverURL.getText())) {
                            reconstruirPanel();
                        }
                    } catch (NullPointerException n) {
                        JOptionPane.showMessageDialog(null, "Primeiro conecte-se ao servidor.");
                    }
                }

            }
        });
    }

    public void limparRepositorio() {

        setTituloLabel("Limpar Repositório");

        setLabel("URL do Repositório", 1, 90);

        final JTextField repoURL = new JTextField();
        repoURL.setBounds(125, 90, 200, 30);
        painelCentro.add(repoURL);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 90, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });
            }
        });

        JButton botao = new JButton("Limpar Repositório");
        botao.setBounds(40, 150, 150, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.clearRepository(repoURL.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte - se ao servidor.");
                }
            }
        });
    }

    public void restaurarRepositorio() {

        setTituloLabel("Restaurar Repositório");

        setLabel("URL do Repositório", 1, 90);

        final JTextField URL = new JTextField();
        URL.setBounds(135, 90, 200, 30);
        painelCentro.add(URL);

        setLabel("Backup de Origem", 1, 140);

        final JTextField filePath = new JTextField();
        filePath.setBounds(135, 140, 200, 30);
        painelCentro.add(filePath);



        JButton botao2 = new JButton("...");
        botao2.setBounds(325, 140, 20, 30);
        painelCentro.add(botao2);

        JButton botao = new JButton("Restaurar Backup");
        botao.setBounds(
                20, 190, 170, 30);
        painelCentro.add(botao);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(325, 90, 20, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();
                painelCentro.revalidate();
                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        URL.setText(controle.getRepositoryURL());
                        URL.repaint();
                    }
                });
            }
        });
        botao2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String path = procurarAbrir();
                filePath.setText(path);
                filePath.repaint();
            }
        });

        botao.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (controle.restoreBackup(URL.getText(), filePath.getText(), serverURL.getText())) {
                                reconstruirPanel();
                            }

                        } catch (NullPointerException n) {
                            JOptionPane.showMessageDialog(null, "Primeiro conecte - se ao servidor.");
                        }

                    }
                });
    }

    public void removerRepositorio() {
        setTituloLabel("Remover Repositório");
        setLabel("ID do Repositório", 1, 90);

        final JTextField ID = new JTextField();
        ID.setBounds(135, 90, 200, 30);
        painelCentro.add(ID);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 90, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        ID.setText(lista.getSelectedValue().toString());
                        ID.repaint();
                    }
                });
            }
        });

        JButton botao = new JButton("Remover Repositório");
        botao.setBounds(40, 150, 180, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controle = new Controle();
                try {
                    if (controle.removeRepository(ID.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte - se ao servidor.");
                }
            }
        });
    }

    public void listarRepositorio() {
        final JFrame frame2 = new JFrame();
        try {
            DefaultListModel modelo = controle.listaRepositorio(serverURL.getText());

            lista = new JList(modelo);
            lista.setOpaque(false);
            lista.setBorder(BorderFactory.createEtchedBorder());

            lista.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {

                    frame2.dispose();
                }
            });

            frame2.setSize(300, 300);
            frame2.setLocationRelativeTo(null);
            frame2.setTitle("Lista de Repositórios");
            frame2.setResizable(false);

            JScrollPane barraRolagem = new JScrollPane(lista);
            barraRolagem.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            barraRolagem.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

            frame2.getContentPane().add(barraRolagem, BorderLayout.CENTER);

            frame2.setVisible(true);

        } catch (NullPointerException n) {
            JOptionPane.showMessageDialog(null, "Primeiro conecte-se ao servidor.");
        }
    }

    public void copiarRepositorio() {

        setTituloLabel("Copiar Repositório");

        setLabel("Origem", 5, 90);

        mainRepo = new JTextField();
        mainRepo.setBounds(135, 90, 200, 30);
        painelCentro.add(mainRepo);

        setLabel("Destino", 5, 140);

        seconRepo = new JTextField();
        seconRepo.setBounds(135, 140, 200, 30);
        painelCentro.add(seconRepo);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 90, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        mainRepo.setText(controle.getRepositoryURL());
                        mainRepo.repaint();
                    }
                });
            }
        });

        JButton escolhaRepo2 = new JButton("...");
        escolhaRepo2.setBounds(340, 140, 30, 30);
        escolhaRepo2.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo2);

        escolhaRepo2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        seconRepo.setText(controle.getRepositoryURL());
                        seconRepo.repaint();
                    }
                });
            }
        });

        JButton botao = new JButton("Copiar Repositório");
        botao.setBounds(40, 190, 170, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.copyRepository(mainRepo.getText(), seconRepo.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se ao servidor.");
                }
            }
        });

    }

    public void backupRepositorio() {

        setTituloLabel("Backup Repositório");

        setLabel("URL do Repositório", 1, 90);

        final JTextField url = new JTextField();
        url.setBounds(135, 90, 200, 30);
        painelCentro.add(url);

        setLabel("Salvar em", 5, 140);

        final JTextField filePath = new JTextField();
        filePath.setBounds(135, 140, 200, 30);
        painelCentro.add(filePath);

        JButton botao2 = new JButton("...");
        botao2.setBounds(340, 140, 30, 30);
        painelCentro.add(botao2);

        JButton botao = new JButton("Fazer Backup");
        botao.setBounds(
                20, 190, 170, 30);
        painelCentro.add(botao);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 90, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();
                painelCentro.revalidate();
                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        url.setText(controle.getRepositoryURL());
                        url.repaint();
                    }
                });
            }
        });

        botao2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarSalvar();
                filePath.setText(path);
                filePath.repaint();
                filePath.revalidate();
            }
        });

        botao.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {

                        try {
                            if (controle.backupRepository(url.getText(), filePath.getText(), serverURL.getText())) {
                                reconstruirPanel();
                            }

                        } catch (NullPointerException n) {
                            JOptionPane.showMessageDialog(null, "Primeiro conecte - se ao servidor.");
                        }
                    }
                });
    }

    public void deletarOntologia() {

        setTituloLabel("Deletar Ontologia");

        setLabel("URI da Ontologia", 1, 90);

        final JTextField URI = new JTextField();
        URI.setBounds(135, 90, 200, 30);
        painelCentro.add(URI);

        setLabel("URL do Repositório", 1, 140);

        repoURL = new JTextField();
        repoURL.setBounds(135, 140, 200, 30);
        painelCentro.add(repoURL);

        JButton listaRepo = new JButton("...");
        listaRepo.setBounds(340, 140, 30, 30);
        listaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(listaRepo);

        listaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });
            }
        });


        JButton botao = new JButton("Deletar");
        botao.setBounds(40, 190, 100, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.deleteOntology(URI.getText(), repoURL.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se no servidor.");
                }
            }
        });

    }

    public void recuperarOntologia() {

        setTituloLabel("Recuperar Ontologia");

        setLabel("Destino", 5, 90);

        final JTextField filePath = new JTextField();
        filePath.setBounds(135, 90, 200, 30);
        painelCentro.add(filePath);

        setLabel("URI da Ontologia", 5, 140);

        final JTextField URI = new JTextField();
        URI.setBounds(135, 140, 200, 30);
        painelCentro.add(URI);

        setLabel("URL do Repositório", 5, 190);

        final JTextField repoURL = new JTextField();
        repoURL.setBounds(135, 190, 200, 30);
        painelCentro.add(repoURL);

        JButton listaRepo = new JButton("...");
        listaRepo.setBounds(340, 190, 30, 30);
        painelCentro.add(listaRepo);

        listaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });
            }
        });

        JButton botao = new JButton("Recuperar");
        botao.setBounds(40, 250, 170, 30);
        painelCentro.add(botao);

        JButton botao2 = new JButton("...");
        botao2.setBounds(340, 90, 30, 30);
        painelCentro.add(botao2);

        botao2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String path = procurarSalvar();
                filePath.setText(path);
                filePath.repaint();
            }
        });

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.retrieveOntology(filePath.getText(), URI.getText(), repoURL.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte - se ao servidor.");
                }
            }
        });

    }

    public String procurarAbrir() {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new FileNameExtensionFilter(".owl - *Ontology", "owl"));
        chooser.setFileFilter(new FileNameExtensionFilter(".jnt - *Joint", "jnt"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        painelCentro.add(chooser);

        int i = chooser.showOpenDialog(painelCentro);
        if (chooser.APPROVE_OPTION == i) {
            return chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    public String procurarSalvar() {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new FileNameExtensionFilter(".owl", "owl - *Ontology"));
        chooser.setFileFilter(new FileNameExtensionFilter(".jar", "jar - *Java Code"));
        chooser.setFileFilter(new FileNameExtensionFilter(".jnt", "jnt - *Joint"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        painelCentro.add(chooser);
        int i = chooser.showSaveDialog(painelCentro);
        if (chooser.APPROVE_OPTION == i) {
            return chooser.getSelectedFile().getAbsolutePath() + chooser.getFileFilter().getDescription();
        } else {
            return null;
        }
    }

    public void adicionaOntologia() {

        setTituloLabel("Adicionando Ontologia");

        setLabel("URL da Ontologia", 1, 90);

        ontoURL = new JTextField();
        ontoURL.setBounds(135, 90, 200, 30);
        painelCentro.add(ontoURL);

        JButton procurarOnto = new JButton("...");
        procurarOnto.setBounds(340, 90, 30, 30);
        procurarOnto.setToolTipText("Procurar");
        painelCentro.add(procurarOnto);

        procurarOnto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarAbrir();
                ontoURL.setText(path);
                ontoURL.repaint();
            }
        });

        setLabel("URI da Ontologia", 1, 140);

        ontoURI = new JTextField();
        ontoURI.setBounds(135, 140, 200, 30);
        painelCentro.add(ontoURI);

        setLabel("URL do Repositório", 5, 190);

        repoURL = new JTextField();
        repoURL.setBounds(135, 190, 200, 30);
        painelCentro.add(repoURL);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 190, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });
            }
        });

        JButton botao = new JButton("Adicionar Ontologia");
        botao.setBounds(40, 250, 160, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.adicionaOntologia(ontoURL.getText(), ontoURI.getText(), repoURL.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se no servidor.");
                }
            }
        });
    }

    public void setTituloLabel(String title) {
        painelCentro.setLayout(null);
        //limpa e reconstroi panel
        reconstruirPanel();

        JLabel titulo = new JLabel(title, JLabel.CENTER);
        titulo.setBorder(BorderFactory.createRaisedBevelBorder());
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setBounds(50, 10, 350, 30);
        painelCentro.add(titulo);
    }

    public void setLabel(String label, int x, int y) {
        JLabel jLabelURL = new JLabel(label, JLabel.RIGHT);
        jLabelURL.setFont(new Font("Arial", Font.HANGING_BASELINE, 14));
        jLabelURL.setBounds(x, y, 125, 30);
        painelCentro.add(jLabelURL);
    }

    public void exportarOntologia() {

        setTituloLabel("Exportar Ontologia");

        setLabel("URL do Repositório", 1, 90);

        final JTextField repoURL = new JTextField();
        repoURL.setBounds(135, 90, 200, 30);
        painelCentro.add(repoURL);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 90, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });

            }
        });

        setLabel("Salvar em", 1, 140);

        final JTextField dir = new JTextField();
        dir.setBounds(135, 140, 200, 30);
        painelCentro.add(dir);

        JButton salvar = new JButton("...");
        salvar.setBounds(340, 140, 30, 30);
        salvar.setToolTipText("Procurar");
        painelCentro.add(salvar);

        salvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarSalvar();
                dir.setText(path);
                dir.repaint();
                dir.revalidate();
            }
        });


        JButton botao = new JButton("Exportar Ontologia");
        botao.setBounds(20, 200, 150, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.exportaOntologia(repoURL.getText(), dir.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se ao servidor.");
                }
            }
        });

    }

    public void atualizarOntologia() {
        setTituloLabel("Atualizar Ontologia");

        setLabel("URL da Ontologia", 1, 90);

        ontoURL = new JTextField();
        ontoURL.setBounds(135, 90, 200, 30);
        painelCentro.add(ontoURL);

        JButton procurarOnto = new JButton("...");
        procurarOnto.setBounds(340, 90, 30, 30);
        procurarOnto.setToolTipText("Procurar");
        painelCentro.add(procurarOnto);

        procurarOnto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarAbrir();
                ontoURL.setText(path);
                ontoURL.repaint();
                ontoURL.revalidate();
            }
        });

        setLabel("URI da Ontologia", 1, 140);

        ontoURI = new JTextField();
        ontoURI.setBounds(135, 140, 200, 30);
        painelCentro.add(ontoURI);

        setLabel("URL do Repositório", 1, 190);

        repoURL = new JTextField();
        repoURL.setBounds(135, 190, 200, 30);
        painelCentro.add(repoURL);

        JButton listaRepo1 = new JButton("...");
        listaRepo1.setBounds(340, 190, 30, 30);
        listaRepo1.setToolTipText("Listar Repositórios");
        painelCentro.add(listaRepo1);

        listaRepo1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });
            }
        });

        JButton botao = new JButton("Atualizar Ontologia");
        botao.setBounds(40, 250, 160, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.atualizaOntologia(ontoURL.getText(), ontoURI.getText(), repoURL.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se no servidor.");
                }
            }
        });
    }

    public void checaConsistencia() {

        setTituloLabel("Checar Consistência da Ontologia");

        setLabel("URL da Ontologia", 1, 90);

        final JTextField dir = new JTextField();
        dir.setBounds(135, 90, 200, 30);
        painelCentro.add(dir);

        JButton escolhaDir = new JButton("...");
        escolhaDir.setBounds(340, 90, 20, 30);
        escolhaDir.setToolTipText("Procurar Ontologia");
        painelCentro.add(escolhaDir);

        escolhaDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarAbrir();
                dir.setText(path);
                dir.repaint();
                dir.revalidate();
            }
        });

        JButton checa = new JButton("Checar");
        checa.setBounds(40, 150, 150, 30);
        painelCentro.add(checa);

        checa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.checaConsistencia(dir.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se no servidor.");
                }
            }
        });

    }

    public void gerarCodigoJava() {

        setTituloLabel("Gerando Código Java");

        setLabel("Salvar em", 1, 90);

        final JTextField salvar = new JTextField();
        salvar.setBounds(135, 90, 200, 30);
        painelCentro.add(salvar);

        JButton botaoSalvar = new JButton("...");
        botaoSalvar.setText("Procurar");
        botaoSalvar.setBounds(340, 90, 30, 30);
        painelCentro.add(botaoSalvar);

        botaoSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarSalvar();
                salvar.setText(path);
                salvar.repaint();
            }
        });

        setLabel("URL da Ontologia", 1, 140);

        final JTextField caminhoOnto = new JTextField();
        caminhoOnto.setBounds(135, 140, 200, 30);
        painelCentro.add(caminhoOnto);

        JButton botaoAbrir = new JButton("...");
        botaoAbrir.setText("Procurar");
        botaoAbrir.setBounds(340, 140, 30, 30);
        painelCentro.add(botaoAbrir);

        botaoAbrir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = procurarAbrir();
                caminhoOnto.setText(path);
                caminhoOnto.repaint();
            }
        });

        JButton botao = new JButton("Gerar");
        botao.setBounds(40, 200, 70, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.gerarCodigoJava(salvar.getText(), caminhoOnto.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se no servidor.");
                }

            }
        });
    }

    public void executarRegras() {
        setTituloLabel("Executar Regras SRWL");

        setLabel("URL do Repositório", 1, 90);

        final JTextField repoURL = new JTextField();
        repoURL.setBounds(135, 90, 200, 30);
        painelCentro.add(repoURL);

        JButton escolhaRepo = new JButton("...");
        escolhaRepo.setBounds(340, 90, 30, 30);
        escolhaRepo.setToolTipText("Listar Repositórios");
        painelCentro.add(escolhaRepo);

        escolhaRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarRepositorio();
                painelCentro.repaint();

                lista.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        controle.setRepositoryURL(lista.getSelectedValue().toString(), serverURL.getText());
                        repoURL.setText(controle.getRepositoryURL());
                        repoURL.repaint();
                    }
                });

            }
        });

        JButton botao = new JButton("Executar Regras");
        botao.setBounds(40, 200, 150, 30);
        painelCentro.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controle.executarRegras(repoURL.getText(), serverURL.getText())) {
                        reconstruirPanel();
                    }
                } catch (NullPointerException n) {
                    JOptionPane.showMessageDialog(null, "Primeiro conecte-se no servidor.");
                }
            }
        });

    }

    public void tutorial() throws URISyntaxException {
        menuItemTutorial.addActionListener(new ActionListener() {
            URI uri = URI.create("http://jointnees.sourceforge.net/tutorial.html");

            @Override
            public void actionPerformed(ActionEvent e) {

                open(uri);
            }
        });
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
            }
        } else {
        }
    }

    public void arquivoSair() {
        menuItemSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fechaJanela();
            }
        });
    }

    public void fecharX() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    fechaJanela();
                }
            }
        });
    }

    public void fechaJanela() {

        int selectedOption = JOptionPane.showConfirmDialog(null, "Deseja Sair Realmente?", "JOINT", JOptionPane.YES_NO_OPTION);
        if (selectedOption == JOptionPane.YES_OPTION) {
            System.exit(0);
        }

    }

    public void initializeMenu() {

        //Criando Barra do Menu
        menuBar = new JMenuBar();
        // Criando Menu Arquivo
        menuArquivo = new JMenu("Arquivo");
        menuItemSair = new JMenuItem("Sair");
        menuArquivo.add(menuItemSair);
        menuBar.add(menuArquivo);

        // Criando Menu Ajuda
        menuAjuda = new JMenu("Ajuda");
        menuItemSobre = new JMenuItem("Sobre");
        menuAjuda.add(menuItemSobre);
        menuItemTutorial = new JMenuItem("Tutorial");
        menuAjuda.add(menuItemTutorial);
        menuBar.add(menuAjuda);

        setJMenuBar(menuBar);

    }

    public static void main(String[] args) {
        FirstWindow first = new FirstWindow();
        first.setVisible(true);
    }
}
