import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TesteMiniCFS {
    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Falha na verificacao");
    }

    private static int validarNo(ArvoreRubroNegra a, No n) {
        if (n == a.getNil()) return 1;
        verificar(n.cor != null);
        if (n.esq != a.getNil()) verificar(n.esq.pai == n);
        if (n.dir != a.getNil()) verificar(n.dir.pai == n);
        if (n.cor == Cor.VERMELHO)
            verificar(n.esq.cor == Cor.PRETO && n.dir.cor == Cor.PRETO);
        int esquerda = validarNo(a, n.esq);
        int direita = validarNo(a, n.dir);
        verificar(esquerda == direita);
        return esquerda + (n.cor == Cor.PRETO ? 1 : 0);
    }

    private static void validar(ArvoreRubroNegra a, int quantidade) {
        verificar(a.getNil().cor == Cor.PRETO);
        verificar(a.getNil().esq == a.getNil() && a.getNil().dir == a.getNil());
        verificar(a.getRaiz().cor == Cor.PRETO);
        if (!a.estaVazia()) verificar(a.getRaiz().pai == a.getNil());
        validarNo(a, a.getRaiz());
        List<Processo> fila = a.percursoEmOrdem();
        verificar(fila.size() == quantidade);
        for (int i = 1; i < fila.size(); i++) {
            Processo anterior = fila.get(i - 1), atual = fila.get(i);
            verificar(anterior.getVruntime() < atual.getVruntime()
                    || (anterior.getVruntime() == atual.getVruntime()
                    && anterior.getPid() < atual.getPid()));
        }
        verificar(a.minimo(a.getRaiz()) == (fila.isEmpty() ? null
                : a.buscar(fila.get(0).getVruntime(), fila.get(0).getPid())));
    }

    private static void insercoesERemocoes() {
        for (int modo = 0; modo < 4; modo++) {
            ArvoreRubroNegra a = new ArvoreRubroNegra();
            Random sorteio = new Random(42);
            List<Processo> processos = new ArrayList<Processo>();
            for (int i = 0; i < 500; i++) {
                int tempo = modo == 0 ? i : modo == 1 ? 500 - i
                        : modo == 2 ? 0 : sorteio.nextInt(100);
                Processo p = new Processo(i, "p" + i, tempo);
                processos.add(p);
                a.inserir(p);
                verificar(a.buscar(tempo, i).processo == p);
                validar(a, i + 1);
            }
            verificar(a.buscar(-1, -1) == null);
            try {
                a.inserir(processos.get(0));
                throw new AssertionError("Aceitou chave duplicada");
            } catch (IllegalArgumentException esperado) {
                validar(a, 500);
            }
            Collections.shuffle(processos, sorteio);
            int quantidade = processos.size();
            for (Processo p : processos) {
                a.remover(a.buscar(p.getVruntime(), p.getPid()));
                verificar(a.buscar(p.getVruntime(), p.getPid()) == null);
                validar(a, --quantidade);
            }
        }
    }

    private static void rodadas() {
        ArvoreRubroNegra a = new ArvoreRubroNegra();
        verificar(Escalonador.executarRodada(a) == null);
        for (int i = 30; i >= 1; i--) a.inserir(new Processo(i, "p" + i, 0));
        for (int i = 0; i < 3000; i++) {
            Processo esperado = a.percursoEmOrdem().get(0);
            int tempo = esperado.getVruntime();
            Processo executado = Escalonador.executarRodada(a);
            verificar(executado == esperado);
            verificar(executado.getPid() == i % 30 + 1);
            verificar(executado.getVruntime() == tempo + Escalonador.QUANTUM);
            validar(a, 30);
        }
        a = new ArvoreRubroNegra();
        Processo limite = new Processo(1, "limite", Integer.MAX_VALUE);
        a.inserir(limite);
        try {
            Escalonador.executarRodada(a);
            throw new AssertionError("Nao detectou overflow");
        } catch (ArithmeticException esperado) {
            verificar(limite.getVruntime() == Integer.MAX_VALUE);
            validar(a, 1);
        }
        a.remover(a.getRaiz());
        validar(a, 0);
        a.inserir(new Processo(Integer.MIN_VALUE, "min", Integer.MIN_VALUE));
        a.inserir(new Processo(Integer.MAX_VALUE, "max", Integer.MAX_VALUE));
        validar(a, 2);
    }

    public static void main(String[] args) throws Exception {
        insercoesERemocoes();
        rodadas();
        javax.swing.SwingUtilities.invokeAndWait(() -> new MiniCFS());
        System.out.println("OK: insercao, busca, minimo, remocao, rodadas e painel.");
    }
}
