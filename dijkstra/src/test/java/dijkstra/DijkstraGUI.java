package dijkstra;

import javax.swing.*;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DijkstraGUI extends JFrame {
    private static final int NUM_VERTICES = 20; // Número de vértices no grafo
    private Grafo grafo;
    private JComboBox<String> origemComboBox;
    private JComboBox<String> destinoComboBox;
    private JButton calcularKmButton; // Botão para calcular caminho com menor custo em km
    private JButton calcularPedagioButton; // Botão para calcular caminho com menor custo em pedágio
    private JButton limparButton;
    private JTextField custoTextField;
    private mxGraph graph;
    private Object parent;
    private boolean calcularPedagio; // Declaração da variável de instância

    public DijkstraGUI() {
        super("Algoritmo de Dijkstra");
        grafo = new Grafo(NUM_VERTICES);
        inicializarGrafo(); // Método adaptado para inicializar o Grafo com os dados específicos

        setLayout(new BorderLayout());
        graph = new mxGraph();
        parent = graph.getDefaultParent();
        desenharGrafo();

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        add(graphComponent, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        origemComboBox = new JComboBox<>(grafo.dadosVertices);
        destinoComboBox = new JComboBox<>(grafo.dadosVertices);
        calcularKmButton = new JButton("Calcular Caminho (KM)");
        calcularPedagioButton = new JButton("Calcular Caminho (Pedágio)");
        limparButton = new JButton("Limpar");
        custoTextField = new JTextField(10);
        custoTextField.setEditable(false);

        controlPanel.add(new JLabel("Origem:"));
        controlPanel.add(origemComboBox);
        controlPanel.add(new JLabel("Destino:"));
        controlPanel.add(destinoComboBox);
        controlPanel.add(calcularKmButton);
        controlPanel.add(calcularPedagioButton);
        controlPanel.add(limparButton);
        controlPanel.add(new JLabel("Custo:"));
        controlPanel.add(custoTextField);

        add(controlPanel, BorderLayout.SOUTH);

        calcularKmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCaminho(false); // Calcula caminho com menor custo em km
            }
        });

        calcularPedagioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCaminho(true); // Calcula caminho com menor custo em pedágio
            }
        });

        limparButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparGrafo();
            }
        });
        limparGrafo();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
    }

    private void desenharGrafo() {
        graph.getModel().beginUpdate();
        try {
            Object[] vertices = new Object[grafo.tamanho];
            for (int i = 0; i < grafo.tamanho; i++) {
                vertices[i] = graph.insertVertex(parent, null, grafo.dadosVertices[i], 50 + (i * 50), 50, 30, 30, "shape=ellipse");
            }
            for (int i = 0; i < grafo.tamanho; i++) {
                List<Grafo.Aresta> arestas = grafo.listaAdjacencia.get(i);
                for (Grafo.Aresta aresta : arestas) {
                    int j = aresta.destino;
                    String label = aresta.pesoKm + " km | " + aresta.pesoPedagio + " pedágio";
                    graph.insertEdge(parent, null, label, vertices[i], vertices[j], "strokeWidth=1");
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }

        // Layout automático
        mxCircleLayout layout = new mxCircleLayout(graph);
        layout.execute(parent);
    }

    private void calcularCaminho(boolean calcularPedagio) {
        String origem = (String) origemComboBox.getSelectedItem();
        String destino = (String) destinoComboBox.getSelectedItem();
        if (origem == null || destino == null || origem.equals(destino)) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione vértices diferentes para origem e destino.");
            return;
        }
        int[] distancias;
        if (calcularPedagio) {
            distancias = grafo.dijkstraPorPedagio(origem); // Calcula distâncias usando Dijkstra por pedágio
        } else {
            distancias = grafo.dijkstraPorKm(origem); // Calcula distâncias usando Dijkstra por km
        }
        int indiceDestino = grafo.encontrarIndice(destino);
        int custo = distancias[indiceDestino];

        custoTextField.setText(String.valueOf(custo));
        destacarCaminho(origem, destino);
    }

    private void destacarCaminho(String origem, String destino) {
        graph.getModel().beginUpdate();
        try {
            Object[] edges = graph.getChildEdges(parent);
            for (Object edge : edges) {
                // Redefine a cor da borda da aresta para a cor padrão
                //graph.getModel().setStyle(edge, "strokeColor=black;strokeWidth=1");
            }

            int origemIndex = grafo.encontrarIndice(origem);
            int destinoIndex = grafo.encontrarIndice(destino);
            Object origemVertice = graph.getChildVertices(parent)[origemIndex];
            Object destinoVertice = graph.getChildVertices(parent)[destinoIndex];
            graph.setCellStyle("shape=ellipse;fillColor=red", new Object[]{origemVertice});
            graph.setCellStyle("shape=ellipse;fillColor=blue", new Object[]{destinoVertice});

            List<Integer> caminho;
            if (this.calcularPedagio) { // Acesso à variável de instância calcularPedagio
                caminho = grafo.caminhoMinimoPorPedagio(origem, destino); // Caminho mínimo por pedágio
            } else {
                caminho = grafo.caminhoMinimoPorKm(origem, destino); // Caminho mínimo por km
            }
            for (int i = 0; i < caminho.size() - 1; i++) {
                int indiceAtual = caminho.get(i);
                int indiceProximo = caminho.get(i + 1);
                Object edge = encontrarAresta(indiceAtual, indiceProximo);
                if (edge != null) {
                    // Define a cor da borda da aresta como vermelha para destacar o caminho
                    graph.getModel().setStyle(edge, "strokeColor=red;strokeWidth=2");
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }
    }


    private Object encontrarAresta(int indiceOrigem, int indiceDestino) {
        Object[] edges = graph.getChildEdges(parent);
        for (Object edge : edges) {
            Object source = graph.getModel().getTerminal(edge, true);
            Object target = graph.getModel().getTerminal(edge, false);
            int sourceIndex = encontrarIndiceVertice(source);
            int targetIndex = encontrarIndiceVertice(target);
            if ((sourceIndex == indiceOrigem && targetIndex == indiceDestino) ||
                    (sourceIndex == indiceDestino && targetIndex == indiceOrigem)) {
                return edge;
            }
        }
        return null;
    }

    private int encontrarIndiceVertice(Object vertice) {
        Object[] vertices = graph.getChildVertices(parent);
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i] == vertice) {
                return i;
            }
        }
        return -1;
    }

    private void limparGrafo() {
        origemComboBox.setSelectedItem(null);
        destinoComboBox.setSelectedItem(null);
        custoTextField.setText("");

        graph.getModel().beginUpdate();
        try {
            // Redefinir estilos para o estado inicial
            Object[] edges = graph.getChildEdges(parent);
            for (Object edge : edges) {
                graph.setCellStyle("strokeWidth=1", new Object[]{edge});
            }
            Object[] vertices = graph.getChildVertices(parent);
            for (Object vertex : vertices) {
                graph.setCellStyle("shape=ellipse", new Object[]{vertex});
            }
        } finally {
            graph.getModel().endUpdate();
        }
    }


    private void inicializarGrafo() {
        grafo.adicionarDadosVertice(0, "São Paulo");
        grafo.adicionarDadosVertice(1, "Santos");
        grafo.adicionarDadosVertice(2, "São José dos Campos");
        grafo.adicionarDadosVertice(3, "Sorocaba");
        grafo.adicionarDadosVertice(4, "Campinas");
        grafo.adicionarDadosVertice(5, "Piracicaba");
        grafo.adicionarDadosVertice(6, "Bauru");
        grafo.adicionarDadosVertice(7, "Marilia");
        grafo.adicionarDadosVertice(8, "Araraquara");
        grafo.adicionarDadosVertice(9, "Presidente Prudente");
        grafo.adicionarDadosVertice(10, "Araçatuba");
        grafo.adicionarDadosVertice(11, "São José do Rio Preto");
        grafo.adicionarDadosVertice(12, "Ribeirão Preto");
        grafo.adicionarDadosVertice(13, "Registro");
        grafo.adicionarDadosVertice(14, "Itapetininga");
        grafo.adicionarDadosVertice(15, "Itapeva");
        grafo.adicionarDadosVertice(16, "Avaré");
        grafo.adicionarDadosVertice(17, "Assis");
        grafo.adicionarDadosVertice(18, "Andradina");
        grafo.adicionarDadosVertice(19, "Guaratinguetá");

        grafo.adicionarAresta(0, 1, 85, 21);   // São Paulo -> Santos, 85 km, R$ 21,50
        grafo.adicionarAresta(0, 2, 78, 32);   // São Paulo -> São José dos Campos, 78 km, R$ 
        grafo.adicionarAresta(0, 3, 112, 26);  // São Paulo -> Sorocaba, 112 km, R$ 
        grafo.adicionarAresta(0, 4, 109, 19);  // São Paulo -> Campinas, 109 km, R$ 
        grafo.adicionarAresta(1, 0, 85, 21);   // Santos -> São Paulo, 85 km, R$ 
        grafo.adicionarAresta(1, 13, 180, 46); // Santos -> Registro, 180 km, R$ 
        grafo.adicionarAresta(2, 0, 78, 32);   // São José dos Campos -> São Paulo, 78 km, R$ 
        grafo.adicionarAresta(2, 19, 91, 12);  // São José dos Campos -> Guaratinguetá, 91 km, R$ 
        grafo.adicionarAresta(3, 0, 112, 26);  // Sorocaba -> São Paulo, 112 km, R$ 
        grafo.adicionarAresta(3, 6, 244, 61);  // Sorocaba -> Bauru, 244 km, R$ 
        grafo.adicionarAresta(3, 15, 75, 14);  // Sorocaba -> Itapetininga, 75 km, R$ 
        grafo.adicionarAresta(3, 16, 180, 26); // Sorocaba -> Avaré, 180 km, R$ 
        grafo.adicionarAresta(4, 0, 109, 19);  // Campinas -> São Paulo, 109 km, R$ 
        grafo.adicionarAresta(4, 5, 70, 11);   // Campinas -> Piracicaba, 70 km, R$ 
        grafo.adicionarAresta(4, 8, 185, 42);  // Campinas -> Araraquara, 185 km, R$ 
        grafo.adicionarAresta(4, 12, 223, 57); // Campinas -> Ribeirão Preto, 223 km, R$ 
        grafo.adicionarAresta(5, 4, 70, 11);   // Piracicaba -> Campinas, 70 km, R$ 
        grafo.adicionarAresta(6, 3, 244, 61);  // Bauru -> Sorocaba, 244 km, R$ 
        grafo.adicionarAresta(6, 7, 106, 23);  // Bauru -> Marilia, 106 km, R$ 
        grafo.adicionarAresta(6, 10, 192, 47); // Bauru -> Araçatuba, 192 km, R$ 
        grafo.adicionarAresta(7, 6, 106, 23);  // Marilia -> Bauru, 106 km, R$ 
        grafo.adicionarAresta(8, 4, 185, 42);  // Araraquara -> Campinas, 185 km, R$ 
        grafo.adicionarAresta(8, 11, 168, 36); // Araraquara -> São José do Rio Preto, 168 km, R$ 
        grafo.adicionarAresta(9, 17, 126, 15); // Presidente Prudente -> Assis, 126 km, R$ 
        grafo.adicionarAresta(10, 6, 192, 47); // Araçatuba -> Bauru, 192 km, R$ 
        grafo.adicionarAresta(10, 18, 111, 17); // Araçatuba -> Andradina, 111 km, R$ 
        grafo.adicionarAresta(11, 8, 168, 36); // São José do Rio Preto -> Araraquara, 168 km, R$ 
        grafo.adicionarAresta(12, 4, 223, 57); // Ribeirão Preto -> Campinas, 223 km, R$ 
        grafo.adicionarAresta(13, 1, 180, 46); // Registro -> Santos, 180 km, R$ 
        grafo.adicionarAresta(14, 3, 75, 14);  // Itapetininga -> Sorocaba, 75 km, R$ 
        grafo.adicionarAresta(14, 15, 126, 5); // Itapetininga -> Itapeva, 126 km, R$ 
        grafo.adicionarAresta(15, 14, 126, 5); // Itapeva -> Itapetininga, 126 km, R$ 
        grafo.adicionarAresta(16, 3, 180, 26); // Avaré -> Sorocaba, 180 km, R$ 
        grafo.adicionarAresta(16, 17, 212, 28); // Avaré -> Assis, 212 km, R$ 
        grafo.adicionarAresta(17, 16, 212, 28); // Assis -> Avaré, 212 km, R$ 
        grafo.adicionarAresta(17, 9, 126, 15); // Assis -> Presidente Prudente, 126 km, R$ 
        grafo.adicionarAresta(18, 10, 111, 17); // Andradina -> Araçatuba, 111 km, R$ 
        grafo.adicionarAresta(19, 2, 91, 12);  // Guaratinguetá -> São José dos Campos, 91 km, R$ 
    }

    public static void main(String[] args) {
        new DijkstraGUI();
    }
}
