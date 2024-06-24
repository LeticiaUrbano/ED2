package dijkstra;

class Aresta {
    int destino;
    int distancia;
    int pedagio;

    public Aresta(int destino, int distancia) {
        this.destino = destino;
        this.distancia = distancia;
        this.pedagio = 0; // Inicializa o pedágio como 0
    }

    public Aresta(int destino, int distancia, int pedagio) {
        this.destino = destino;
        this.distancia = distancia;
        this.pedagio = pedagio;
    }
}
