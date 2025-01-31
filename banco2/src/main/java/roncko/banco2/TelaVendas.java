
package roncko.banco2;

/**
 *
 * @author Bastos
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class TelaVendas extends JFrame {
    private final JComboBox<String> comboFuncionarios;
    private final JComboBox<String> comboProdutos;
    private final JTextField txtQuantidade;
    private final JTable tabelaItens;
    private final DefaultTableModel modeloTabela;
    private final JLabel lblValorTotal;
    private double valorTotal = 0.0;

    public TelaVendas() {
        setTitle("Tela de Vendas");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Painel principal
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout());

        // Painel de entrada
        JPanel painelEntrada = new JPanel();
        painelEntrada.setLayout(new GridLayout(4, 2));

        // ComboBox de Funcionários
        painelEntrada.add(new JLabel("Funcionário:"));
        comboFuncionarios = new JComboBox<>(carregarFuncionarios());
        painelEntrada.add(comboFuncionarios);

        // ComboBox de Produtos
        painelEntrada.add(new JLabel("Produto:"));
        comboProdutos = new JComboBox<>(carregarProdutos());
        painelEntrada.add(comboProdutos);

        // Campo de Quantidade
        painelEntrada.add(new JLabel("Quantidade:"));
        txtQuantidade = new JTextField();
        painelEntrada.add(txtQuantidade);

        // Botão Adicionar Item
        JButton btnAdicionar = new JButton("Adicionar Item");
        btnAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarItem();
            }
        });
        painelEntrada.add(btnAdicionar);

        // Adiciona o painel de entrada ao painel principal
        painel.add(painelEntrada, BorderLayout.NORTH);

        // Tabela de Itens
        modeloTabela = new DefaultTableModel(new String[]{"Produto", "Quantidade", "Valor Parcial"}, 0);
        tabelaItens = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaItens);
        painel.add(scrollPane, BorderLayout.CENTER);

        // Campo Valor Total
        lblValorTotal = new JLabel("Valor Total: R$ 0.00");
        painel.add(lblValorTotal, BorderLayout.SOUTH);

        // Botão Finalizar Venda
        JButton btnFinalizar = new JButton("Finalizar Venda");
        btnFinalizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarVenda();
            }
        });
        painel.add(btnFinalizar, BorderLayout.SOUTH);

        // Adiciona o painel principal à janela
        add(painel);
    }

    private String[] carregarFuncionarios() {
        ArrayList<String> funcionarios = new ArrayList<>();
        try (Connection conn = ConexaoBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT fun_nome FROM tb_funcionarios")) {
            while (rs.next()) {
                funcionarios.add(rs.getString("fun_nome"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return funcionarios.toArray(new String[0]);
    }

    private String[] carregarProdutos() {
        ArrayList<String> produtos = new ArrayList<>();
        try (Connection conn = ConexaoBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT pro_descricao FROM tb_produtos")) {
            while (rs.next()) {
                produtos.add(rs.getString("pro_descricao"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produtos.toArray(new String[0]);
    }

    private void adicionarItem() {
        String produto = (String) comboProdutos.getSelectedItem();
        if (produto == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto.");
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(txtQuantidade.getText());
            if (quantidade <= 0) {
                JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser um número inteiro.");
            return;
        }

        double valorUnitario = 0.0;

        // Obter o valor do produto
        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement("SELECT pro_valor FROM tb_produtos WHERE pro_descricao = ?")) {
            pstmt.setString(1, produto);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                valorUnitario = rs.getDouble("pro_valor");
            } else {
                JOptionPane.showMessageDialog(this, "Produto não encontrado.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double valorParcial = valorUnitario * quantidade;
        modeloTabela.addRow(new Object[]{produto, quantidade, valorParcial});
        valorTotal += valorParcial;
        lblValorTotal.setText("Valor Total: R$ " + String.format("%.2f", valorTotal));
        txtQuantidade.setText(""); // Limpa o campo de quantidade
    }

    private void finalizarVenda() {
        try (Connection conn = ConexaoBD.conectar()) {
            conn.setAutoCommit(false); // Inicia a transação

            // Insere a venda na tabela tb_vendas
            String sqlVenda = "INSERT INTO tb_vendas (ven_valor_total, tb_funcionarios_fun_codigo) VALUES (?, ?)";

            try (PreparedStatement pstmtVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVenda.setDouble(1, valorTotal);
                pstmtVenda.setInt(2, comboFuncionarios.getSelectedIndex() + 1); // Supondo que o índice corresponde ao ID do funcionário
                pstmtVenda.executeUpdate();

                // Obtém o ID da venda recém-inserida
                ResultSet rs = pstmtVenda.getGeneratedKeys();
                int venCodigo = 0;
                if (rs.next()) {
                    venCodigo = rs.getInt(1);
                }

                // Insere os itens da venda na tabela tb_itens
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    String produto = (String) modeloTabela.getValueAt(i, 0);
                    int quantidade = (int) modeloTabela.getValueAt(i, 1);

                    // Obtém o código do produto
                    int proCodigo = 0;
                    try (PreparedStatement pstmtProduto = conn.prepareStatement("SELECT pro_codigo FROM tb_produtos WHERE pro_descricao = ?")) {
                        pstmtProduto.setString(1, produto);
                        ResultSet rsProduto = pstmtProduto.executeQuery();
                        if (rsProduto.next()) {
                            proCodigo = rsProduto.getInt("pro_codigo");
                        }
                    }

                    // Insere o item na tabela tb_itens
                    String sqlItem = "INSERT INTO tb_itens (ite_quantidade, tb_produtos_pro_codigo, tb_vendas_ven_codigo) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
                        pstmtItem.setInt(1, quantidade);
                        pstmtItem.setInt(2, proCodigo);
                        pstmtItem.setInt(3, venCodigo);
                        pstmtItem.executeUpdate();
                    }

                    // Atualiza o estoque
                    String sqlAtualizaEstoque = "UPDATE tb_produtos SET pro_quantidade = pro_quantidade - ? WHERE pro_codigo = ?";
                    try (PreparedStatement pstmtEstoque = conn.prepareStatement(sqlAtualizaEstoque)) {
                        pstmtEstoque.setInt(1, quantidade);
                        pstmtEstoque.setInt(2, proCodigo);
                        pstmtEstoque.executeUpdate();
                    }
                }

                conn.commit(); // Confirma a transação
                JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso!");
                modeloTabela.setRowCount(0); // Limpa a tabela de itens
                valorTotal = 0.0; // Reseta o valor total
                lblValorTotal.setText("Valor Total: R$ 0.00"); // Reseta o campo de valor total
            } catch (SQLException e) {
                conn.rollback(); // Desfaz a transação em caso de erro
                JOptionPane.showMessageDialog(this, "Erro ao finalizar a venda: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaVendas tela = new TelaVendas();
            tela.setVisible(true);
        });
    }
}