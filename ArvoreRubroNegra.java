import java.util.ArrayList;
import java.util.List;

/*
 * ArvoreRubroNegra.java - arvore vermelha e preta do MiniCFS.
 *
 * Divisao do trabalho neste arquivo:
 *   - Pessoa B (pronto): construtor, remocao (transplante, remover e
 *     correcao apos remover), percurso em ordem e busca por pid.
 *   - Pessoa A (a fazer): rotacoes, inserir, buscar e minimo. Por enquanto
 *     estao so com a assinatura, para o resto do codigo compilar.
 *
 * Usa um no sentinela "nil" (sempre PRETO) no lugar de null, como no
 * Cormen, "Algoritmos", capitulo 13.
 */
public class ArvoreRubroNegra {

    private final No nil;
    private No raiz;

    public ArvoreRubroNegra() {
        nil = new No(null, Cor.PRETO);
        nil.esq = nil;
        nil.dir = nil;
        nil.pai = nil;
        raiz = nil;                 // arvore vazia
    }

    public No getRaiz() {
        return raiz;
    }

    public No getNil() {
        return nil;
    }

    public boolean estaVazia() {
        return raiz == nil;
    }


    // =====================================================================
    // PESSOA A - a implementar (por enquanto so as assinaturas)
    // =====================================================================

    /* Rotacoes. Atencao: ao mover o filho do meio, so atualize o pai dele
       se ele NAO for o nil (como no Cormen). A correcao da remocao usa
       nil.pai e quebra se a rotacao mexer nele. */
    private void rotacaoEsquerda(No x) {
        throw new UnsupportedOperationException("Pessoa A: rotacaoEsquerda");
    }

    private void rotacaoDireita(No x) {
        throw new UnsupportedOperationException("Pessoa A: rotacaoDireita");
    }

    /* Cria um no VERMELHO com o processo p e insere, com a correcao de cores. */
    public void inserir(Processo p) {
        throw new UnsupportedOperationException("Pessoa A: inserir");
    }

    /* Busca pela chave (vruntime, pid). Devolve null se nao achar. */
    public No buscar(int vruntime, int pid) {
        throw new UnsupportedOperationException("Pessoa A: buscar");
    }

    /* Menor no da subarvore com raiz x (x nao pode ser o nil). */
    public No minimo(No x) {
        throw new UnsupportedOperationException("Pessoa A: minimo");
    }


    // =====================================================================
    // PESSOA B
    // =====================================================================

    // ---------- Remocao ----------

    /* Coloca a subarvore com raiz v no lugar da subarvore com raiz u. */
    private void transplante(No u, No v) {
        if (u.pai == nil)
            raiz = v;
        else if (u == u.pai.esq)
            u.pai.esq = v;
        else
            u.pai.dir = v;

        // Feito mesmo quando v e o nil: a correcao precisa saber quem e o
        // pai de x, ainda que x seja o sentinela.
        v.pai = u.pai;
    }

    /* Remove o no z da arvore, com a correcao de cores. */
    public void remover(No z) {
        if (z == null || z == nil)
            return;

        No y = z;                   // no que sai da sua posicao original
        Cor corOriginalY = y.cor;
        No x;                       // no que ocupa o antigo lugar de y

        if (z.esq == nil) {
            // z nao tem filho esquerdo: o filho direito sobe
            x = z.dir;
            transplante(z, z.dir);
        } else if (z.dir == nil) {
            // z nao tem filho direito: o filho esquerdo sobe
            x = z.esq;
            transplante(z, z.esq);
        } else {
            // z tem dois filhos: o sucessor y (menor no da subarvore
            // direita) toma o lugar de z e fica com a cor de z
            y = minimo(z.dir);
            corOriginalY = y.cor;
            x = y.dir;

            if (y.pai == z) {
                x.pai = y;          // necessario quando x e o nil
            } else {
                transplante(y, y.dir);
                y.dir = z.dir;
                y.dir.pai = y;
            }

            transplante(z, y);
            y.esq = z.esq;
            y.esq.pai = y;
            y.cor = z.cor;
        }

        // Se saiu um no preto, algum caminho perdeu um preto: corrigir.
        if (corOriginalY == Cor.PRETO)
            corrigirRemocao(x);
    }

    /*
     * Correcao apos remover.
     *
     * Quando o no que saiu era PRETO, os caminhos que passam por x ficaram
     * com um preto a menos. A ideia e pensar em x como "duplamente preto":
     * o laco empurra esse preto extra para cima ou resolve com rotacoes.
     * Se x for vermelho (ou a raiz), basta pinta-lo de preto no final.
     *
     * w e o irmao de x. Casos quando x e filho esquerdo
     * (quando x e filho direito e o espelho: troca esq <-> dir):
     *
     *   Caso 1: w vermelho.
     *           Recolore w e o pai e rotaciona o pai. O novo irmao e preto,
     *           entao cai num dos casos 2, 3 ou 4.
     *   Caso 2: w preto e os dois filhos de w pretos.
     *           Pinta w de vermelho e o problema sobe para o pai.
     *   Caso 3: w preto, filho esquerdo de w vermelho e direito preto.
     *           Recolore e rotaciona w, o que transforma no caso 4.
     *   Caso 4: w preto e filho direito de w vermelho.
     *           Recolore, rotaciona o pai e a arvore fica correta. Fim.
     */
    private void corrigirRemocao(No x) {
        No w;   // irmao de x

        while (x != raiz && x.cor == Cor.PRETO) {
            if (x == x.pai.esq) {
                w = x.pai.dir;

                if (w.cor == Cor.VERMELHO) {                             // caso 1
                    w.cor = Cor.PRETO;
                    x.pai.cor = Cor.VERMELHO;
                    rotacaoEsquerda(x.pai);
                    w = x.pai.dir;
                }

                if (w.esq.cor == Cor.PRETO && w.dir.cor == Cor.PRETO) {  // caso 2
                    w.cor = Cor.VERMELHO;
                    x = x.pai;
                } else {
                    if (w.dir.cor == Cor.PRETO) {                        // caso 3
                        w.esq.cor = Cor.PRETO;
                        w.cor = Cor.VERMELHO;
                        rotacaoDireita(w);
                        w = x.pai.dir;
                    }
                    w.cor = x.pai.cor;                                   // caso 4
                    x.pai.cor = Cor.PRETO;
                    w.dir.cor = Cor.PRETO;
                    rotacaoEsquerda(x.pai);
                    x = raiz;                           // termina o laco
                }
            } else {
                // Espelho do bloco acima: x e filho direito.
                w = x.pai.esq;

                if (w.cor == Cor.VERMELHO) {                             // caso 1
                    w.cor = Cor.PRETO;
                    x.pai.cor = Cor.VERMELHO;
                    rotacaoDireita(x.pai);
                    w = x.pai.esq;
                }

                if (w.dir.cor == Cor.PRETO && w.esq.cor == Cor.PRETO) {  // caso 2
                    w.cor = Cor.VERMELHO;
                    x = x.pai;
                } else {
                    if (w.esq.cor == Cor.PRETO) {                        // caso 3
                        w.dir.cor = Cor.PRETO;
                        w.cor = Cor.VERMELHO;
                        rotacaoEsquerda(w);
                        w = x.pai.esq;
                    }
                    w.cor = x.pai.cor;                                   // caso 4
                    x.pai.cor = Cor.PRETO;
                    w.esq.cor = Cor.PRETO;
                    rotacaoDireita(x.pai);
                    x = raiz;                           // termina o laco
                }
            }
        }

        x.cor = Cor.PRETO;
    }

    // ---------- Percurso em ordem ----------

    /* Devolve os processos em ordem crescente de (vruntime, pid): o
       primeiro da lista e o proximo a rodar. */
    public List<Processo> percursoEmOrdem() {
        List<Processo> lista = new ArrayList<Processo>();
        percursoEmOrdem(raiz, lista);
        return lista;
    }

    /* Esquerda, no, direita. */
    private void percursoEmOrdem(No x, List<Processo> lista) {
        if (x == nil)
            return;
        percursoEmOrdem(x.esq, lista);
        lista.add(x.processo);
        percursoEmOrdem(x.dir, lista);
    }

    // ---------- Busca por pid ----------

    /* A arvore e ordenada por vruntime, nao por pid. Por isso nao da para
       descer escolhendo um lado: e preciso visitar os nos ate achar, O(n).
       Devolve null se nao achar. */
    public No buscarPorPid(int pid) {
        return buscarPorPid(raiz, pid);
    }

    private No buscarPorPid(No x, int pid) {
        if (x == nil)
            return null;
        if (x.processo.getPid() == pid)
            return x;

        No achou = buscarPorPid(x.esq, pid);
        if (achou != null)
            return achou;
        return buscarPorPid(x.dir, pid);
    }
}
