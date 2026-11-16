/*
 * No.java - no da arvore vermelha e preta.
 *
 * Parte combinada entre A e B (proposta: revisem juntos).
 *
 * Os campos nao sao "private" para o codigo da arvore ficar parecido com
 * o pseudocodigo do Cormen (x.pai.esq, w.cor, ...).
 */
public class No {
    Processo processo;
    Cor cor;
    No esq;
    No dir;
    No pai;

    No(Processo processo, Cor cor) {
        this.processo = processo;
        this.cor = cor;
    }
}
