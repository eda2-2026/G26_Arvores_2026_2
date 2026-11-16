/*
 * Escalonador.java - logica do escalonador.
 *
 * Pessoa A: execucao de uma rodada com quantum fixo.
 */
public class Escalonador {

    public static final int QUANTUM = 10;

    /*
     * Uma rodada: tira o processo de menor vruntime, soma o quantum e
     * reinsere. Devolve o processo que rodou (ja com o vruntime novo), ou
     * null se a arvore estiver vazia.
     */
    public static Processo executarRodada(ArvoreRubroNegra arvore) {
        if (arvore.estaVazia())
            return null;
        No menor = arvore.minimo(arvore.getRaiz());
        Processo processo = menor.processo;
        // Calcular antes de remover: overflow nao pode alterar a fila.
        int novoVruntime = Math.addExact(processo.getVruntime(), QUANTUM);
        arvore.remover(menor);
        processo.setVruntime(novoVruntime);
        arvore.inserir(processo);
        return processo;
    }
}
