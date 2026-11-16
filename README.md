# MiniCFS

Simulador de escalonamento com arvore rubro-negra, ordenada por `(vruntime, pid)`.
A cada rodada, o processo com menor chave sai da arvore, recebe 10 unidades de
vruntime e e reinserido. O pid desempata processos com o mesmo vruntime.

## Executar

Requer JDK 8 ou superior e ambiente grafico para a janela Swing.

```sh
javac *.java
java MiniCFS
```

A interface permite criar processos, executar uma rodada e matar por pid.
A tabela mostra o percurso em ordem e o painel mostra as cores da arvore.
Processos novos entram com o menor vruntime atual. Se o limite de inteiro
for atingido, a rodada e recusada sem modificar a fila.

## Testar

```sh
mkdir -p build
javac -d build *.java tests/TesteMiniCFS.java
java -Djava.awt.headless=true -cp build TesteMiniCFS
```

Os testes verificam propriedades rubro-negras, ordenacao, ponteiros de pai,
insercoes crescentes e decrescentes, empates, busca, remocoes em ordem aleatoria,
3.000 rodadas, fila vazia e overflow. Tambem instanciam o painel sem abrir janela.

## Divisao

- Pessoa A: rotacoes, insercao e correcao, busca pela chave, minimo e rodada.
- Pessoa B: remocao e correcao, percurso em ordem, busca por pid e interface.

Insercao, busca pela chave, minimo e rodada custam O(log n). Busca por pid e
percurso em ordem custam O(n). A interface usa pids unicos e crescentes.
O vruntime so deve ser alterado com o processo fora da arvore.
