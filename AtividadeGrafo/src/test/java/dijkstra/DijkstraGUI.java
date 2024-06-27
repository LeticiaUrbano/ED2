package dijkstra;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class DijkstraGUI extends JFrame {

    private Grafo grafo;
    private mxGraph graph;
    private Object parent;
    private JComboBox<String> origemComboBox, destinoComboBox;
    private JButton calcularKmButton, calcularPedagioButton, limparButton, adicionarVerticeButton, removerVerticeButton;
    private JTextField custoTextField;
    private Map<String, Object> vertexMap = new HashMap<>();
    private mxCell selectedVertex; // Armazena o vértice selecionado como mxCell
    
    
    public DijkstraGUI() {
        super("Algoritmo de Dijkstra");
        grafo = new Grafo(20); // Número inicial de vértices no grafo
        inicializarGrafo();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        graph = new mxGraph();
        parent = graph.getDefaultParent();
        
        desenharGrafo();

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        // Aplicar layout para organizar os vértices em um círculo
        mxCircleLayout layout = new mxCircleLayout(graph);
        layout.execute(parent);
        
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

        adicionarVerticeButton = new JButton("Adicionar Vértice");
        removerVerticeButton = new JButton("Remover Vértice");

        controlPanel.add(adicionarVerticeButton);
        controlPanel.add(removerVerticeButton);

        adicionarVerticeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarVerticeDialog();
            }
        });

        removerVerticeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedVertex != null) {
                    graph.getModel().beginUpdate();
                    try {
                        graph.removeCells(new Object[]{selectedVertex});
                        String cidadeRemover = selectedVertex.getValue().toString();
                        if (cidadeRemover != null) {
                            grafo.removerVertice(cidadeRemover);
                            origemComboBox.removeItem(cidadeRemover);
                            destinoComboBox.removeItem(cidadeRemover);
                            // Remover do vertexMap
                            vertexMap.remove(cidadeRemover);
                            
                            //desenharGrafo();
                        }
                    } finally {
                        graph.getModel().endUpdate();
                    }
                }
            }
        });

        calcularKmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCaminho(false);
            }
        });

        calcularPedagioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCaminho(true);
            }
        });

        limparButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparGrafo();
            }
        });

        // Adicionar listener para capturar o clique em vértices
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
                    selectedVertex = (mxCell) cell; // Armazena o vértice clicado
                }
            }
        });
	
	    limparGrafo();
	
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	}


    private void adicionarVerticeDialog() {
        JTextField verticeField = new JTextField(10);
        JTextField conectadoAField = new JTextField(10);
        JTextField distanciaField = new JTextField(10);
        JTextField custoField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(4, 2));
        myPanel.add(new JLabel("Nome do Vértice:"));
        myPanel.add(verticeField);
        myPanel.add(new JLabel("Ligado a:"));
        myPanel.add(conectadoAField);
        myPanel.add(new JLabel("Distância (km):"));
        myPanel.add(distanciaField);
        myPanel.add(new JLabel("Custo (pedágio):"));
        myPanel.add(custoField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Adicionar Vértice e Aresta", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String vertice = verticeField.getText().trim();
            String conectadoA = conectadoAField.getText().trim();
            int distancia = Integer.parseInt(distanciaField.getText().trim());
            int custo = Integer.parseInt(custoField.getText().trim());

            if (!vertice.isEmpty() && !grafo.contemVertice(vertice)) {
                int indice = grafo.adicionarVertice(vertice);
                if (indice != -1) {
                    origemComboBox.addItem(vertice);
                    destinoComboBox.addItem(vertice);

                    // Adicionar o novo vértice ao grafo
                    Object vertex = graph.insertVertex(parent, null, vertice,
                            100, 100, 30, 30, "shape=ellipse");
                    vertexMap.put(vertice, vertex);

                    // Redesenhar apenas a parte do grafo modificada
                    desenharArestasDoVertice(vertice);
                }
            }

            if (!conectadoA.isEmpty() && grafo.contemVertice(conectadoA)) {
                grafo.adicionarAresta(vertice, conectadoA, distancia, custo);
                grafo.adicionarAresta(conectadoA, vertice, distancia, custo); // Adiciona a aresta na direção oposta também

                // Redesenhar apenas a parte do grafo modificada
                desenharArestasDoVertice(vertice);
                desenharArestasDoVertice(conectadoA);
            }
        }
    }

    private void desenharArestasDoVertice(String vertice) {
        int indice = grafo.encontrarIndice(vertice);
        if (indice != -1 && grafo.dadosVertices[indice] != null) {
            Object source = vertexMap.get(vertice);
            for (Grafo.Aresta aresta : grafo.listaAdjacencia.get(indice)) {
                int destinoIndex = aresta.destino;
                if (grafo.dadosVertices[destinoIndex] != null) {
                    Object target = vertexMap.get(grafo.dadosVertices[destinoIndex]);
                    graph.insertEdge(parent, null, aresta.pesoKm + " km / Pedágio: " + aresta.pesoPedagio, source, target);
                }
            }
        }
    }



    private void desenharGrafo() {
        graph.getModel().beginUpdate();
        try {
            graph.removeCells(graph.getChildCells(parent, true, false));
            for (int i = 0; i < grafo.dadosVertices.length; i++) {
                if (grafo.dadosVertices[i] != null) {
                    Object vertex = graph.insertVertex(parent, null, grafo.dadosVertices[i],
                            100 + Math.cos(i * 2 * Math.PI / grafo.dadosVertices.length) * 200,
                            100 + Math.sin(i * 2 * Math.PI / grafo.dadosVertices.length) * 200,
                            30, 30, "shape=ellipse"); // Definir o estilo como elipse (círculo)
                    vertexMap.put(grafo.dadosVertices[i], vertex);
                }
            }

            // Inserir as arestas
            for (int i = 0; i < grafo.dadosVertices.length; i++) {
                if (grafo.dadosVertices[i] != null) {
                    for (Grafo.Aresta aresta : grafo.listaAdjacencia.get(i)) {
                        int destinoIndex = aresta.destino;
                        if (grafo.dadosVertices[destinoIndex] != null) {
                            Object source = vertexMap.get(grafo.dadosVertices[i]);
                            Object target = vertexMap.get(grafo.dadosVertices[destinoIndex]);
                            graph.insertEdge(parent, null, aresta.pesoKm + " km / Pedágio: " + aresta.pesoPedagio, source, target);
                        }
                    }
                }
            }

            // Aplicar o layout para organizar os vértices em um círculo
            mxCircleLayout layout = new mxCircleLayout(graph);
            layout.execute(parent);
        } finally {
            graph.getModel().endUpdate();
        }
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
            distancias = grafo.dijkstraPorPedagio(origem);
        } else {
            distancias = grafo.dijkstraPorKm(origem);
        }
        int indiceDestino = grafo.encontrarIndice(destino);
        int custo = distancias[indiceDestino];

        custoTextField.setText(String.valueOf(custo));
        destacarCaminho(origem, destino);
    }


    private void destacarCaminho(String origem, String destino) {
        graph.getModel().beginUpdate();
        try {
            resetarEstilos();

            int origemIndex = grafo.encontrarIndice(origem);
            int destinoIndex = grafo.encontrarIndice(destino);
            Object origemVertice = vertexMap.get(origem);
            Object destinoVertice = vertexMap.get(destino);
            graph.setCellStyle("shape=ellipse;fillColor=red", new Object[]{origemVertice});
            graph.setCellStyle("shape=ellipse;fillColor=blue", new Object[]{destinoVertice});

            // Obter o caminho mínimo por KM
            List<Integer> caminhoKm = grafo.caminhoMinimoPorKm(origem, destino);
            // Aplicar o estilo às arestas do caminho por KM
            aplicarEstiloArestasCaminho(caminhoKm, "strokeColor=red;strokeWidth=2");

            // Obter o caminho mínimo por Pedágio
            List<Integer> caminhoPedagio = grafo.caminhoMinimoPorPedagio(origem, destino);
            // Aplicar o estilo às arestas do caminho por Pedágio
            aplicarEstiloArestasCaminho(caminhoPedagio, "strokeColor=red;strokeWidth=2");
        } finally {
            graph.getModel().endUpdate();
        }
    }



    private void aplicarEstiloArestasCaminho(List<Integer> caminho, String estilo) {
        if (caminho.size() > 1) {
            for (int i = 1; i < caminho.size(); i++) {
                int origemIndex = caminho.get(i - 1);
                int destinoIndex = caminho.get(i);

                Object source = vertexMap.get(grafo.dadosVertices[origemIndex]);
                Object target = vertexMap.get(grafo.dadosVertices[destinoIndex]);

                // Encontrar a aresta correspondente e aplicar o estilo
                Object[] arestas = graph.getEdgesBetween(source, target);
                if (arestas != null && arestas.length > 0) {
                    for (Object aresta : arestas) {
                        graph.setCellStyle(estilo, new Object[]{aresta});
                    }
                }
            }
        }
    }


    private void resetarEstilos() {
        Object[] vertices = graph.getChildVertices(parent);
        for (Object vertex : vertices) {
            graph.setCellStyle("shape=ellipse", new Object[]{vertex});
        }
    }

    private void limparGrafo() {
        origemComboBox.setSelectedItem(null);
        destinoComboBox.setSelectedItem(null);
        custoTextField.setText("");
        selectedVertex = null;

        graph.getModel().beginUpdate();
        try {
            resetarEstilos();
        } finally {
            graph.getModel().endUpdate();
        }
    }



    private void inicializarGrafo() {
        grafo.adicionarVertice("São Paulo");
        grafo.adicionarVertice("Santos");
        grafo.adicionarVertice("São José dos Campos");
        grafo.adicionarVertice("Sorocaba");
        grafo.adicionarVertice("Campinas");
        grafo.adicionarVertice("Piracicaba");
        grafo.adicionarVertice("Bauru");
        grafo.adicionarVertice("Marilia");
        grafo.adicionarVertice("Araraquara");
        grafo.adicionarVertice("Presidente Prudente");
        grafo.adicionarVertice("Araçatuba");
        grafo.adicionarVertice("São José do Rio Preto");
        grafo.adicionarVertice("Ribeirão Preto");
        grafo.adicionarVertice("Registro");
        grafo.adicionarVertice("Itapetininga");
        grafo.adicionarVertice("Itapeva");
        grafo.adicionarVertice("Avaré");
        grafo.adicionarVertice("Assis");
        grafo.adicionarVertice("Andradina");
        grafo.adicionarVertice("Guaratinguetá");

        grafo.adicionarAresta("São Paulo", "Santos", 85, 21);
        grafo.adicionarAresta("São Paulo", "São José dos Campos", 78, 32);
        grafo.adicionarAresta("São Paulo", "Sorocaba", 112, 26);
        grafo.adicionarAresta("São Paulo", "Campinas", 109, 19);
        grafo.adicionarAresta("Santos", "Registro", 180, 46);
        grafo.adicionarAresta("São José dos Campos", "Guaratinguetá", 91, 12);
        grafo.adicionarAresta("Sorocaba", "Bauru", 244, 61);
        grafo.adicionarAresta("Sorocaba", "Itapetininga", 75, 14);
        grafo.adicionarAresta("Sorocaba", "Avaré", 180, 26);
        grafo.adicionarAresta("Campinas", "Piracicaba", 70, 11);
        grafo.adicionarAresta("Campinas", "Araraquara", 185, 42);
        grafo.adicionarAresta("Campinas", "Ribeirão Preto", 223, 57);
        grafo.adicionarAresta("Bauru", "Marilia", 106, 23);
        grafo.adicionarAresta("Bauru", "Araçatuba", 192, 47);
        grafo.adicionarAresta("Araraquara", "São José do Rio Preto", 168, 36);
        grafo.adicionarAresta("Presidente Prudente", "Assis", 126, 15);
        grafo.adicionarAresta("Araçatuba", "Andradina", 111, 17);
        grafo.adicionarAresta("Ribeirão Preto", "Campinas", 223, 57);
        grafo.adicionarAresta("Registro", "Santos", 180, 46);
        grafo.adicionarAresta("Itapetininga", "Itapeva", 126, 5);
        grafo.adicionarAresta("Avaré", "Assis", 212, 28);
        grafo.adicionarAresta("Andradina", "Araçatuba", 111, 17);
        grafo.adicionarAresta("Guaratinguetá", "São José dos Campos", 91, 12);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DijkstraGUI());
    }
}
