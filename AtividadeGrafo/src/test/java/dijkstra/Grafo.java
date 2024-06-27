package dijkstra;

import java.util.*;

public class Grafo {
    int tamanho;
    String[] dadosVertices;
    List<List<Aresta>> listaAdjacencia;
    Map<String, Integer> mapaVertices; // Mapa para mapear nome da cidade para índice

    public Grafo(int tamanho) {
        this.tamanho = tamanho;
        dadosVertices = new String[tamanho];
        listaAdjacencia = new ArrayList<>(tamanho);
        mapaVertices = new HashMap<>(); // Inicializa o mapa de vértices
        
        for (int i = 0; i < tamanho; ++i) {
            listaAdjacencia.add(new ArrayList<>());
        }
    }

    public void adicionarDadosVertice(int indice, String dado) {
        dadosVertices[indice] = dado;
    }

    public void adicionarAresta(String origem, String destino, int pesoKm, int pesoPedagio) {
        int indiceOrigem = encontrarIndice(origem);
        int indiceDestino = encontrarIndice(destino);
        if (indiceOrigem != -1 && indiceDestino != -1) {
            listaAdjacencia.get(indiceOrigem).add(new Aresta(indiceDestino, pesoKm, pesoPedagio));
            listaAdjacencia.get(indiceDestino).add(new Aresta(indiceOrigem, pesoKm, pesoPedagio)); // Se o grafo for não-direcionado
        }
    }

    public int encontrarIndice(String dado) {
        for (int i = 0; i < dadosVertices.length; i++) {
            if (dado.equals(dadosVertices[i])) {
                return i;
            }
        }
        return -1;
    }

    // Verifica se um vértice está no grafo
    public boolean contemVertice(String vertice) {
        return encontrarIndice(vertice) != -1;
    }

    
    // Adiciona um vértice ao grafo
    public int adicionarVertice(String novaCidade) {
        int indice = encontrarIndiceVazio();
        if (indice == -1) {
            int novoTamanho = dadosVertices.length * 2;
            redimensionarGrafo(novoTamanho);
            indice = encontrarIndiceVazio();
        }
        if (indice != -1) {
            dadosVertices[indice] = novaCidade;
            listaAdjacencia.set(indice, new ArrayList<>()); // Inicializa a lista de adjacência para o novo vértice
        }
        return indice;
    }

    
    
    public void removerVertice(String cidade) {
        int indice = encontrarIndice(cidade);
        if (indice != -1) {
            dadosVertices[indice] = null;
            
            // Limpar as arestas do vértice removido em todas as listas de adjacência
            for (List<Aresta> arestas : listaAdjacencia) {
                // Usar iterator para evitar ConcurrentModificationException
                Iterator<Aresta> iterator = arestas.iterator();
                while (iterator.hasNext()) {
                    Aresta aresta = iterator.next();
                    if (aresta.getDestino() == indice) {
                        iterator.remove(); // Remove a aresta cujo destino é o vértice removido
                    }
                }
            }
        }
    }


    public int encontrarIndiceVazio() {
        for (int i = 0; i < dadosVertices.length; i++) {
            if (dadosVertices[i] == null) {
                return i;
            }
        }
        return -1;
    }


    public void redimensionarGrafo(int novoTamanho) {
        String[] novosDadosVertices = new String[novoTamanho];
        System.arraycopy(dadosVertices, 0, novosDadosVertices, 0, dadosVertices.length);
        dadosVertices = novosDadosVertices;

        while (listaAdjacencia.size() < novoTamanho) {
            listaAdjacencia.add(new ArrayList<>());
        }
        tamanho = novoTamanho;
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

        // Marcar as arestas do caminho mínimo
        List<Integer> caminho = new ArrayList<>();
        for (int i = indiceDestino; i != -1; i = anterior[i]) {
            caminho.add(i);
            if (anterior[i] != -1) {
                // Encontrar a aresta que conecta i e anterior[i]
                for (Aresta aresta : listaAdjacencia.get(anterior[i])) {
                    if (aresta.destino == i) {
                        aresta.marcarComoParteDoCaminho();
                        break;
                    }
                }
            }
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
        if (indiceOrigem == -1) {
            throw new IllegalArgumentException("Vértice de origem não encontrado.");
        }

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

    public int getNumeroVertices() {
        return dadosVertices.length; // Assumindo que dadosVertices seja seu array de vértices
    }
    public int getNumeroArestas() {
        int count = 0;
        for (List<Aresta> adjacencias : listaAdjacencia) {
            count += adjacencias.size();
        }
        return count / 2; // Dividido por 2 se o grafo for não direcionado
    }

    // Classe interna para representar uma aresta com dois pesos
    public static class Aresta {
        int destino;
        int pesoKm;
        int pesoPedagio;
        boolean isParteDoCaminho; // Indica se esta aresta faz parte do menor caminho

        public Aresta(int destino, int pesoKm, int pesoPedagio) {
            this.destino = destino;
            this.pesoKm = pesoKm;
            this.pesoPedagio = pesoPedagio;
            this.isParteDoCaminho = false; // Inicialmente nenhuma aresta faz parte do caminho
        }

        public int getDestino() {
            return destino;
        }

        public void marcarComoParteDoCaminho() {
            this.isParteDoCaminho = true;
        }

        public boolean isParteDoCaminho() {
            return isParteDoCaminho;
        }
    }


    
    
}

