package dijkstra;

import java.util.*;

public class Grafo {
    int tamanho;
    String[] dadosVertices;
    List<List<Aresta>> listaAdjacencia;

    public Grafo(int tamanho) {
        this.tamanho = tamanho;
        dadosVertices = new String[tamanho];
        listaAdjacencia = new ArrayList<>(tamanho);
        for (int i = 0; i < tamanho; ++i) {
            listaAdjacencia.add(new ArrayList<>());
        }
    }

    public void adicionarDadosVertice(int indice, String dado) {
        dadosVertices[indice] = dado;
    }

    public void adicionarAresta(int origem, int destino, int pesoKm, int pesoPedagio) {
        listaAdjacencia.get(origem).add(new Aresta(destino, pesoKm, pesoPedagio));
        listaAdjacencia.get(destino).add(new Aresta(origem, pesoKm, pesoPedagio)); // Se o grafo for não-direcionado
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

            for (Aresta aresta : listaAdjacencia.get(u)) {
                int v = aresta.destino;
                int peso = aresta.pesoKm;
                if (!visitados[v] && distancias[u] + peso < distancias[v]) {
                    distancias[v] = distancias[u] + peso;
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

            for (Aresta aresta : listaAdjacencia.get(u)) {
                int v = aresta.destino;
                int peso = aresta.pesoKm;
                if (!visitados[v] && distancias[u] + peso < distancias[v]) {
                    distancias[v] = distancias[u] + peso;
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

    public List<Integer> caminhoMinimoPorPedagio(String origem, String destino) {
        int[] distancias = dijkstraPorPedagio(origem);
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

            for (Aresta aresta : listaAdjacencia.get(u)) {
                int v = aresta.destino;
                int peso = aresta.pesoPedagio;
                if (!visitados[v] && distancias[u] + peso < distancias[v]) {
                    distancias[v] = distancias[u] + peso;
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

    public List<Integer> caminhoMinimoPorKm(String origem, String destino) {
        int[] distancias = dijkstraPorKm(origem);
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

            for (Aresta aresta : listaAdjacencia.get(u)) {
                int v = aresta.destino;
                int peso = aresta.pesoKm;
                if (!visitados[v] && distancias[u] + peso < distancias[v]) {
                    distancias[v] = distancias[u] + peso;
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

    // Método auxiliar para Dijkstra baseado no pedágio
    public int[] dijkstraPorPedagio(String origem) {
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

            for (Aresta aresta : listaAdjacencia.get(u)) {
                int v = aresta.destino;
                int peso = aresta.pesoPedagio; // Usar peso de pedágio ao invés de peso de km
                if (!visitados[v] && distancias[u] + peso < distancias[v]) {
                    distancias[v] = distancias[u] + peso;
                    pq.add(v);
                }
            }
        }
        return distancias;
    }

    // Método auxiliar para Dijkstra baseado no km
    public int[] dijkstraPorKm(String origem) {
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

            for (Aresta aresta : listaAdjacencia.get(u)) {
                int v = aresta.destino;
                int peso = aresta.pesoKm; // Usar peso de km
                if (!visitados[v] && distancias[u] + peso < distancias[v]) {
                    distancias[v] = distancias[u] + peso;
                    pq.add(v);
                }
            }
        }
        return distancias;
    }

    // Classe interna para representar uma aresta com dois pesos
    public static class Aresta {
        int destino;
        int pesoKm;
        int pesoPedagio;

        public Aresta(int destino, int pesoKm, int pesoPedagio) {
            this.destino = destino;
            this.pesoKm = pesoKm;
            this.pesoPedagio = pesoPedagio;
        }
    }
}

