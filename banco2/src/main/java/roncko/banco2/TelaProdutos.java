
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

public class TelaProdutos extends JFrame {
    private final JTextField txtDescricao;
    private final JTextField txtValor;
    private final JTextField txtQuantidade;
    private final JTable tabelaProdutos;
    private final DefaultTableModel modeloTabela;

    public TelaProdutos() {
        setTitle("Tela de Produtos");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Painel principal
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout());

        // Painel de entrada
        JPanel painelEntrada = new JPanel();
        painelEntrada.setLayout(new GridLayout(4, 2));

        // Campos de entrada
        painelEntrada.add(new JLabel("Descrição:"));
        txtDescricao = new JTextField();
        painelEntrada.add(txtDescricao);

        painelEntrada.add(new JLabel("Valor:"));
        txtValor = new JTextField();
        painelEntrada.add(txtValor);

        painelEntrada.add(new JLabel("Quantidade:"));
        txtQuantidade = new JTextField();
        painelEntrada.add(txtQuantidade);

        // Botões
        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarProduto();
            }
        });
        painelEntrada.add(btnAdicionar);

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarProduto();
            }
        });
        painelEntrada.add(btnAtualizar);

        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirProduto();
            }
        });
        painelEntrada.add(btnExcluir);

        // Adiciona o painel de entrada ao painel principal
        painel.add(painelEntrada, BorderLayout.NORTH);

        // Tabela de Produtos
        modeloTabela = new DefaultTableModel(new String[]{"Código", "Descrição", "Valor", "Quantidade"}, 0);
        tabelaProdutos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        painel.add(scrollPane, BorderLayout.CENTER);

        // Adiciona o painel principal à janela
        add(painel);
        carregarProdutos(); // Carrega os produtos ao iniciar
    }

    private void carregarProdutos() {
        modeloTabela.setRowCount(0); // Limpa a tabela
        try (Connection conn = ConexaoBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT pro_codigo, pro_descricao, pro_valor, pro_quantidade FROM tb_produtos")) {
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                        rs.getInt("pro_codigo"),
                        rs.getString("pro_descricao"),
                        rs.getDouble("pro_valor"),
                        rs.getInt("pro_quantidade")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adicionarProduto() {
        String descricao = txtDescricao.getText();
        double valor = Double.parseDouble(txtValor.getText());
        int quantidade = Integer.parseInt(txtQuantidade.getText());

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tb_produtos (pro_descricao, pro_valor, pro_quantidade) VALUES (?, ?, ?)")) {
            pstmt.setString(1, descricao);
            pstmt.setDouble(2, valor);
            pstmt.setInt(3, quantidade);
            pstmt.executeUpdate();
            carregarProdutos(); // Atualiza a tabela após adicionar
            JOptionPane.showMessageDialog(this, "Produto adicionado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private void atualizarProduto() {
        int selectedRow = tabelaProdutos.getSelectedRow();
        if (selectedRow >=  0) {
            String descricao = txtDescricao.getText();
            double valor = Double.parseDouble(txtValor.getText());
            int quantidade = Integer.parseInt(txtQuantidade.getText());

            int codigoSelecionado = (int) modeloTabela.getValueAt(selectedRow, 0); // Obtém o código do produto selecionado

            try (Connection conn = ConexaoBD.conectar();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tb_produtos SET pro_descricao = ?, pro_valor = ?, pro_quantidade = ? WHERE pro_codigo = ?")) {
                pstmt.setString(1, descricao);
                pstmt.setDouble(2, valor);
                pstmt.setInt(3, quantidade);
                pstmt.setInt(4, codigoSelecionado);
                pstmt.executeUpdate();
                carregarProdutos(); // Atualiza a tabela após a atualização
                JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao atualizar produto: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para atualizar.");
        }
    }

    private void excluirProduto() {
        int selectedRow = tabelaProdutos.getSelectedRow();
        if (selectedRow >= 0) {
            int codigoSelecionado = (int) modeloTabela.getValueAt(selectedRow, 0); // Obtém o código do produto selecionado

            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir este produto?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = ConexaoBD.conectar();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tb_produtos WHERE pro_codigo = ?")) {
                    pstmt.setInt(1, codigoSelecionado);
                    pstmt.executeUpdate();
                    carregarProdutos(); // Atualiza a tabela após a exclusão
                    JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erro ao excluir produto: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaProdutos tela = new TelaProdutos();
            tela.setVisible(true);
        });
    }
}
