package dijkstra;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Grafo {
    int tamanho;
    String[] dadosVertices;
    int[][] matrizAdjacencia;

    public Grafo(int tamanho) {
        this.tamanho = tamanho;
        dadosVertices = new String[tamanho];
        matrizAdjacencia = new int[tamanho][tamanho];
    }

    public void adicionarDadosVertice(int indice, String dado) {
        dadosVertices[indice] = dado;
    }

    public void adicionarAresta(int origem, int destino, int peso) {
        matrizAdjacencia[origem][destino] = peso;
        matrizAdjacencia[destino][origem] = peso; // Se o grafo for não-direcionado
    }

    public int encontrarIndice(String dado) {
        for (int i = 0; i < dadosVertices.length; i++) {
            if (dadosVertices[i].equals(dado)) {
                return i;
            }
        }
        return -1;
    }

    public int[] dijkstra(String origem) {
        int indiceOrigem = encontrarIndice(origem);
        int[] distancias = new int[tamanho];
        boolean[] visitados = new boolean[tamanho];
        Arrays.fill(distancias, Integer.MAX_VALUE);
        distancias[indiceOrigem] = 0;

        PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> distancias[a] - distancias[b]);
        pq.add(indiceOrigem);

        while (!pq.isEmpty()) {
            int u = pq.poll();
            if (visitados[u]) continue;
            visitados[u] = true;

            for (int v = 0; v < tamanho; v++) {
                if (matrizAdjacencia[u][v] > 0 && !visitados[v] && distancias[u] + matrizAdjacencia[u][v] < distancias[v]) {
                    distancias[v] = distancias[u] + matrizAdjacencia[u][v];
                    pq.add(v);
                }
            }
        }
        return distancias;
    }

    public List<Integer> caminhoMinimo(String origem, String destino) {
        int[] distancias = dijkstra(origem);
        int indiceOrigem = encontrarIndice(origem);
        int indiceDestino = encontrarIndice(destino);
        
        boolean[] visitados = new boolean[tamanho];
        int[] anterior = new int[tamanho];
        Arrays.fill(anterior, -1);

        PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> distancias[a] - distancias[b]);
        pq.add(indiceOrigem);

        while (!pq.isEmpty()) {
            int u = pq.poll();
            if (visitados[u]) continue;
            visitados[u] = true;

            for (int v = 0; v < tamanho; v++) {
                if (matrizAdjacencia[u][v] > 0 && !visitados[v] && distancias[u] + matrizAdjacencia[u][v] < distancias[v]) {
                    distancias[v] = distancias[u] + matrizAdjacencia[u][v];
                    anterior[v] = u;
                    pq.add(v);
                }
            }
        }

        List<Integer> caminho = new ArrayList<>();
        for (int i = indiceDestino; i != -1; i = anterior[i]) {
            caminho.add(i);
        }
        Collections.reverse(caminho);
        return caminho;
    }
}
