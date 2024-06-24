package dijkstra;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DijkstraGUI extends JFrame {
    private Grafo grafo;
    private JComboBox<String> origemComboBox;
    private JComboBox<String> destinoComboBox;
    private JButton calcularButton;
    private JButton limparButton;
    private JTextField custoTextField;
    private mxGraph graph;
    private Object parent;

    public DijkstraGUI() {
        super("Algoritmo de Dijkstra");
        grafo = new Grafo(20);
        inicializarGrafo(grafo);

        setLayout(new BorderLayout());
        graph = new mxGraph();
        parent = graph.getDefaultParent();
        desenharGrafo();

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        add(graphComponent, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        origemComboBox = new JComboBox<>(grafo.dadosVertices);
        destinoComboBox = new JComboBox<>(grafo.dadosVertices);
        calcularButton = new JButton("Calcular Caminho");
        limparButton = new JButton("Limpar");
        custoTextField = new JTextField(10);
        custoTextField.setEditable(false);

        controlPanel.add(new JLabel("Origem:"));
        controlPanel.add(origemComboBox);
        controlPanel.add(new JLabel("Destino:"));
        controlPanel.add(destinoComboBox);
        controlPanel.add(calcularButton);
        controlPanel.add(limparButton);
        controlPanel.add(new JLabel("Custo (km):"));
        controlPanel.add(custoTextField);

        add(controlPanel, BorderLayout.SOUTH);

        calcularButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCaminho();
            }
        });

        limparButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparGrafo();
            }
        });

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
                for (int j = i + 1; j < grafo.tamanho; j++) {
                    if (grafo.matrizAdjacencia[i][j] > 0) {
                        graph.insertEdge(parent, null, grafo.matrizAdjacencia[i][j], vertices[i], vertices[j], "strokeWidth=1");
                    }
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }

        // Layout automático
        mxCircleLayout layout = new mxCircleLayout(graph);
        layout.execute(parent);
    }

    private void calcularCaminho() {
        String origem = (String) origemComboBox.getSelectedItem();
        String destino = (String) destinoComboBox.getSelectedItem();
        if (origem == null || destino == null || origem.equals(destino)) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione vértices diferentes para origem e destino.");
            return;
        }
        int[] distancias = grafo.dijkstra(origem);
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
                // Defina a cor da borda da aresta como vermelha
                graph.getModel().setStyle(edge, "strokeColor=red;strokeWidth=2");
            }

            int origemIndex = grafo.encontrarIndice(origem);
            int destinoIndex = grafo.encontrarIndice(destino);
            Object origemVertice = graph.getChildVertices(parent)[origemIndex];
            Object destinoVertice = graph.getChildVertices(parent)[destinoIndex];
            graph.setCellStyle("shape=ellipse;fillColor=red", new Object[]{origemVertice});
            graph.setCellStyle("shape=ellipse;fillColor=blue", new Object[]{destinoVertice});

            List<Integer> caminho = grafo.caminhoMinimo(origem, destino);
            for (int i = 0; i < caminho.size() - 1; i++) {
                int indiceAtual = caminho.get(i);
                int indiceProximo = caminho.get(i + 1);
                Object edge = encontrarAresta(indiceAtual, indiceProximo);
                if (edge != null) {
                    // Defina a cor da borda da aresta como vermelha
                    graph.getModel().setStyle(edge, "strokeColor=red;strokeWidth=2");
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }
    }



    private void resetEstilosGrafo() {
        Object[] vertices = graph.getChildVertices(parent);
        Object[] arestas = graph.getChildEdges(parent);

        for (Object vertex : vertices) {
            graph.setCellStyle("shape=ellipse", new Object[]{vertex});
        }

        for (Object edge : arestas) {
            graph.setCellStyle("strokeWidth=1", new Object[]{edge});
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

    private void inicializarGrafo(Grafo grafo) {
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

        grafo.adicionarAresta(0, 1, 85);   // São Paulo -> Santos, 85 km
        grafo.adicionarAresta(0, 2, 78);   // São Paulo -> São José dos Campos, 78 km
        grafo.adicionarAresta(0, 3, 112);  // São Paulo -> Sorocaba, 112 km
        grafo.adicionarAresta(0, 4, 109);  // São Paulo -> Campinas, 109 km
        grafo.adicionarAresta(1, 0, 85);   // Santos -> São Paulo, 85 km
        grafo.adicionarAresta(1, 13, 180); // Santos -> Registro, 180 km
        grafo.adicionarAresta(2, 0, 78);   // São José dos Campos -> São Paulo, 78 km
        grafo.adicionarAresta(2, 19, 91);  // São José dos Campos -> Guaratinguetá, 91 km
        grafo.adicionarAresta(3, 0, 112);  // Sorocaba -> São Paulo, 112 km
        grafo.adicionarAresta(3, 6, 244);  // Sorocaba -> Bauru, 244 km
        grafo.adicionarAresta(3, 15, 75);  // Sorocaba -> Itapetininga, 75 km
        grafo.adicionarAresta(3, 16, 180); // Sorocaba -> Avaré, 180 km
        grafo.adicionarAresta(4, 0, 109);  // Campinas -> São Paulo, 109 km
        grafo.adicionarAresta(4, 5, 70);   // Campinas -> Piracicaba, 70 km
        grafo.adicionarAresta(4, 8, 185);  // Campinas -> Araraquara, 185 km
        grafo.adicionarAresta(4, 12, 223); // Campinas -> Ribeirão Preto, 223 km
        grafo.adicionarAresta(5, 4, 70);   // Piracicaba -> Campinas, 70 km
        grafo.adicionarAresta(6, 3, 244);  // Bauru -> Sorocaba, 244 km
        grafo.adicionarAresta(6, 7, 106);  // Bauru -> Marilia, 106 km
        grafo.adicionarAresta(6, 10, 192); // Bauru -> Araçatuba, 192 km
        grafo.adicionarAresta(7, 6, 106);  // Marilia -> Bauru, 106 km
        grafo.adicionarAresta(8, 4, 185);  // Araraquara -> Campinas, 185 km
        grafo.adicionarAresta(8, 11, 168); // Araraquara -> São José do Rio Preto, 168 km
        grafo.adicionarAresta(9, 17, 126); // Presidente Prudente -> Assis, 126 km
        grafo.adicionarAresta(10, 6, 192); // Araçatuba -> Bauru, 192 km
        grafo.adicionarAresta(10, 18, 111); // Araçatuba -> Andradina, 111 km
        grafo.adicionarAresta(11, 8, 168); // São José do Rio Preto -> Araraquara, 168 km
        grafo.adicionarAresta(12, 4, 223); // Ribeirão Preto -> Campinas, 223 km
        grafo.adicionarAresta(13, 1, 180); // Registro -> Santos, 180 km
        grafo.adicionarAresta(14, 3, 75);  // Itapetininga -> Sorocaba, 75 km
        grafo.adicionarAresta(14, 15, 126); // Itapetininga -> Itapeva, 126 km
        grafo.adicionarAresta(15, 14, 126); // Itapeva -> Itapetininga, 126 km
        grafo.adicionarAresta(16, 3, 180); // Avaré -> Sorocaba, 180 km
        grafo.adicionarAresta(16, 17, 212); // Avaré -> Assis, 212 km
        grafo.adicionarAresta(17, 16, 212); // Assis -> Avaré, 212 km
        grafo.adicionarAresta(17, 9, 126); // Assis -> Presidente Prudente, 126 km
        grafo.adicionarAresta(18, 10, 111); // Andradina -> Araçatuba, 111 km
        grafo.adicionarAresta(19, 2, 91);  // Guaratinguetá -> São José dos Campos, 91 km
    }

    public static void main(String[] args) {
        new DijkstraGUI();
    }
}
