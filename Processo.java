/*
 * Processo.java - dados de um processo do escalonador.
 *
 * Parte combinada entre A e B (proposta: revisem juntos).
 *
 * A arvore e ordenada por vruntime; em caso de empate, por pid.
 */
public class Processo {
    private final int pid;
    private final String nome;
    private int vruntime;

    public Processo(int pid, String nome, int vruntime) {
        this.pid = pid;
        this.nome = nome;
        this.vruntime = vruntime;
    }

    public int getPid() {
        return pid;
    }

    public String getNome() {
        return nome;
    }

    public int getVruntime() {
        return vruntime;
    }

    /* So mude o vruntime com o processo FORA da arvore: ele e a chave de
       ordenacao, entao muda-lo com o processo dentro bagunca a arvore. */
    public void setVruntime(int vruntime) {
        this.vruntime = vruntime;
    }
}
