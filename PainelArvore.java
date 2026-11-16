import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/*
 * PainelArvore.java - desenha a arvore vermelha e preta (Pessoa B).
 *
 * Posicao de cada no:
 *   - na horizontal, a ordem do no no percurso em ordem (1o, 2o, 3o...).
 *     Assim os nos nunca se sobrepoem e a ordem da arvore aparece da
 *     esquerda para a direita;
 *   - na vertical, a profundidade do no.
 */
public class PainelArvore extends JPanel {

    private static final long serialVersionUID = 1L;   // evita aviso do compilador

    private static final int DIAMETRO = 38;
    private static final int ESPACO_X = 50;     // distancia entre vizinhos no percurso
    private static final int ESPACO_Y = 64;     // distancia entre niveis
    private static final int MARGEM = 24;

    private static final Color COR_VERMELHO = new Color(198, 40, 40);
    private static final Color COR_PRETO = new Color(33, 33, 33);
    private static final Color COR_LINHA = new Color(160, 160, 160);
    private static final Font FONTE_VRUNTIME = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font FONTE_PID = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private final ArvoreRubroNegra arvore;
    private final Map<No, Point> posicoes = new HashMap<No, Point>();
    private int quantidade;             // nos ja posicionados no percurso
    private int profundidadeMaxima;

    public PainelArvore(ArvoreRubroNegra arvore) {
        this.arvore = arvore;
        setBackground(Color.WHITE);
        atualizar();
    }

    /* Recalcula as posicoes. Chame depois de cada mudanca na arvore. */
    public void atualizar() {
        posicoes.clear();
        quantidade = 0;
        profundidadeMaxima = 0;
        posicionar(arvore.getRaiz(), 0);

        int largura = 2 * MARGEM + Math.max(quantidade - 1, 0) * ESPACO_X + DIAMETRO;
        int altura = 2 * MARGEM + profundidadeMaxima * ESPACO_Y + DIAMETRO + 14;  // 14: texto do pid
        setPreferredSize(new Dimension(largura, altura));
        revalidate();
        repaint();
    }

    /* Percurso em ordem: esquerda, no, direita. */
    private void posicionar(No x, int profundidade) {
        if (x == arvore.getNil())
            return;

        posicionar(x.esq, profundidade + 1);

        int cx = MARGEM + DIAMETRO / 2 + quantidade * ESPACO_X;
        int cy = MARGEM + DIAMETRO / 2 + profundidade * ESPACO_Y;
        posicoes.put(x, new Point(cx, cy));
        quantidade++;
        profundidadeMaxima = Math.max(profundidadeMaxima, profundidade);

        posicionar(x.dir, profundidade + 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (arvore.estaVazia()) {
            g2.setColor(Color.GRAY);
            g2.setFont(FONTE_VRUNTIME);
            centralizar(g2, "Nenhum processo na fila", getWidth() / 2, getHeight() / 2);
        } else {
            // centraliza a arvore na horizontal quando sobra espaco
            int sobra = getWidth() - getPreferredSize().width;
            g2.translate(Math.max(0, sobra / 2), 0);

            // primeiro as linhas, para os circulos ficarem por cima delas
            g2.setColor(COR_LINHA);
            g2.setStroke(new BasicStroke(2f));
            desenharLinhas(g2, arvore.getRaiz());
            desenharNos(g2, arvore.getRaiz());
        }
        g2.dispose();
    }

    private void desenharLinhas(Graphics2D g2, No x) {
        No nil = arvore.getNil();
        if (x == nil)
            return;

        Point p = posicoes.get(x);
        if (x.esq != nil) {
            Point filho = posicoes.get(x.esq);
            g2.drawLine(p.x, p.y, filho.x, filho.y);
        }
        if (x.dir != nil) {
            Point filho = posicoes.get(x.dir);
            g2.drawLine(p.x, p.y, filho.x, filho.y);
        }
        desenharLinhas(g2, x.esq);
        desenharLinhas(g2, x.dir);
    }

    private void desenharNos(Graphics2D g2, No x) {
        if (x == arvore.getNil())
            return;

        Point p = posicoes.get(x);
        int raio = DIAMETRO / 2;

        // circulo com a cor do no
        g2.setColor(x.cor == Cor.VERMELHO ? COR_VERMELHO : COR_PRETO);
        g2.fillOval(p.x - raio, p.y - raio, DIAMETRO, DIAMETRO);

        // vruntime (a chave) dentro do circulo
        g2.setColor(Color.WHITE);
        g2.setFont(FONTE_VRUNTIME);
        centralizar(g2, String.valueOf(x.processo.getVruntime()), p.x, p.y);

        // pid embaixo do circulo
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(FONTE_PID);
        centralizar(g2, "pid " + x.processo.getPid(), p.x, p.y + raio + 9);

        desenharNos(g2, x.esq);
        desenharNos(g2, x.dir);
    }

    /* Escreve o texto centralizado no ponto (cx, cy). */
    private static void centralizar(Graphics2D g2, String texto, int cx, int cy) {
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(texto) / 2;
        int y = cy + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(texto, x, y);
    }
}
