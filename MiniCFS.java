import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/*
 * MiniCFS.java - interface grafica do MiniCFS (Pessoa B).
 *
 * Para rodar, execute o main desta classe. Pela linha de comando:
 *     javac *.java
 *     java MiniCFS
 */
public class MiniCFS extends JPanel {

    private static final long serialVersionUID = 1L;   // evita aviso do compilador

    private static final Color COR_ERRO = new Color(183, 28, 28);
    private static final Color COR_NORMAL = new Color(33, 33, 33);

    private final ArvoreRubroNegra arvore = new ArvoreRubroNegra();
    private int proximoPid = 1;

    private final JTextField campoNome = new JTextField(12);
    private final JTextField campoPid = new JTextField(5);
    private final JLabel status = new JLabel("Digite um nome e clique em Criar processo.");
    private final PainelArvore painelArvore = new PainelArvore(arvore);
    private final DefaultTableModel tabela =
            new DefaultTableModel(new Object[] { "PID", "Nome", "vruntime" }, 0);
    private final JScrollPane rolagemTabela;

    public MiniCFS() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Comandos (parte de cima)
        add(criarComandos(), BorderLayout.NORTH);

        // Arvore desenhada (esquerda) e fila em ordem (direita)
        JScrollPane rolagemArvore = new JScrollPane(painelArvore);
        rolagemArvore.setBorder(BorderFactory.createTitledBorder(
                "\u00c1rvore vermelha e preta  (dentro do n\u00f3: vruntime, embaixo: pid)"));

        JTable tabelaVisual = new JTable(tabela);
        tabelaVisual.setDefaultEditor(Object.class, null);   // tabela so para leitura
        rolagemTabela = new JScrollPane(tabelaVisual);
        rolagemTabela.setPreferredSize(new Dimension(280, 0));

        JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rolagemArvore, rolagemTabela);
        divisao.setResizeWeight(1.0);   // o espaco extra vai para a arvore
        add(divisao, BorderLayout.CENTER);

        // Mensagens (parte de baixo)
        status.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));
        add(status, BorderLayout.SOUTH);

        atualizarTela();
    }

    private JPanel criarComandos() {
        JButton botaoCriar = new JButton("Criar processo");
        JButton botaoRodada = new JButton("Executar 1 rodada");
        JButton botaoMatar = new JButton("Matar processo");

        // Apertar Enter num campo faz o mesmo que o botao ao lado dele.
        botaoCriar.addActionListener(e -> rodarComando(this::criarProcesso));
        campoNome.addActionListener(e -> rodarComando(this::criarProcesso));
        botaoRodada.addActionListener(e -> rodarComando(this::executarRodada));
        botaoMatar.addActionListener(e -> rodarComando(this::matarProcesso));
        campoPid.addActionListener(e -> rodarComando(this::matarProcesso));

        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(botaoCriar);
        painel.add(Box.createHorizontalStrut(20));
        painel.add(botaoRodada);
        painel.add(Box.createHorizontalStrut(20));
        painel.add(new JLabel("PID:"));
        painel.add(campoPid);
        painel.add(botaoMatar);
        return painel;
    }


    // ---------- Comandos ----------

    /* Roda um comando e atualiza a tela. */
    private void rodarComando(Runnable comando) {
        try {
            comando.run();
        } catch (ArithmeticException e) {
            mostrar("Limite de vruntime atingido. A rodada nao foi executada.", true);
        }
        atualizarTela();
    }

    private void criarProcesso() {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            mostrar("Digite um nome para o processo.", true);
            return;
        }

        // Como no CFS do Linux: o processo novo entra com o menor vruntime
        // atual. Se entrasse com 0, rodaria varias vezes seguidas ate
        // alcancar os outros.
        int vruntime = 0;
        if (!arvore.estaVazia())
            vruntime = arvore.minimo(arvore.getRaiz()).processo.getVruntime();

        Processo p = new Processo(proximoPid, nome, vruntime);
        arvore.inserir(p);
        proximoPid++;

        campoNome.setText("");
        mostrar("Processo \"" + nome + "\" criado com pid " + p.getPid()
                + " e vruntime " + vruntime + ".", false);
    }

    private void executarRodada() {
        Processo p = Escalonador.executarRodada(arvore);
        if (p == null) {
            mostrar("Fila vazia: nenhum processo para executar.", true);
            return;
        }
        mostrar("Rodou \"" + p.getNome() + "\" (pid " + p.getPid()
                + "). Novo vruntime: " + p.getVruntime() + ".", false);
    }

    private void matarProcesso() {
        int pid;
        try {
            pid = Integer.parseInt(campoPid.getText().trim());
        } catch (NumberFormatException e) {
            mostrar("O PID precisa ser um inteiro.", true);
            return;
        }

        No alvo = arvore.buscarPorPid(pid);
        if (alvo == null) {
            mostrar("Nenhum processo com pid " + pid + ".", true);
            return;
        }

        Processo p = alvo.processo;
        arvore.remover(alvo);
        campoPid.setText("");
        mostrar("Processo \"" + p.getNome() + "\" (pid " + pid + ") finalizado.", false);
    }


    // ---------- Tela ----------

    /* Atualiza a tabela (percurso em ordem) e o desenho da arvore. */
    private void atualizarTela() {
        List<Processo> fila = arvore.percursoEmOrdem();

        tabela.setRowCount(0);
        for (Processo p : fila)
            tabela.addRow(new Object[] { p.getPid(), p.getNome(), p.getVruntime() });

        String quantos = fila.size() == 1 ? "1 processo" : fila.size() + " processos";
        rolagemTabela.setBorder(BorderFactory.createTitledBorder("Percurso em ordem (" + quantos + ")"));

        painelArvore.atualizar();
    }

    private void mostrar(String mensagem, boolean erro) {
        status.setText(mensagem);
        status.setForeground(erro ? COR_ERRO : COR_NORMAL);
    }


    // ---------- Programa principal ----------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // se nao der, fica o visual padrao do Java
            }
            JFrame janela = new JFrame("MiniCFS - escalonador com \u00e1rvore vermelha e preta");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setContentPane(new MiniCFS());
            janela.setSize(1000, 600);
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
