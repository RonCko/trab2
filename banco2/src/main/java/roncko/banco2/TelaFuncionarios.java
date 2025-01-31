
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

public class TelaFuncionarios extends JFrame {
    private final JTextField txtNome;
    private final JTextField txtCpf;
    private final JTextField txtSenha;
    private final JTextField txtFuncao;
    private final JTable tabelaFuncionarios;
    private final DefaultTableModel modeloTabela;

    public TelaFuncionarios() {
        setTitle("Tela de Funcionários");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Painel principal
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout());

        // Painel de entrada
        JPanel painelEntrada = new JPanel();
        painelEntrada.setLayout(new GridLayout(5, 2));

        // Campos de entrada
        painelEntrada.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        painelEntrada.add(txtNome);

        painelEntrada.add(new JLabel("CPF:"));
        txtCpf = new JTextField();
        painelEntrada.add(txtCpf);

        painelEntrada.add(new JLabel("Senha:"));
        txtSenha = new JTextField();
        painelEntrada.add(txtSenha);

        painelEntrada.add(new JLabel("Função:"));
        txtFuncao = new JTextField();
        painelEntrada.add(txtFuncao);

        // Botões
        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarFuncionario();
            }
        });
        painelEntrada.add(btnAdicionar);

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarFuncionario();
            }
        });
        painelEntrada.add(btnAtualizar);

        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirFuncionario();
            }
        });
        painelEntrada.add(btnExcluir);

        // Adiciona o painel de entrada ao painel principal
        painel.add(painelEntrada, BorderLayout.NORTH);

        // Tabela de Funcionários
        modeloTabela = new DefaultTableModel(new String[]{"Nome", "CPF", "Função"}, 0);
        tabelaFuncionarios = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaFuncionarios);
        painel.add(scrollPane, BorderLayout.CENTER);

        // Adiciona o painel principal à janela
        add(painel);
        carregarFuncionarios(); // Carrega os funcionários ao iniciar
    }

    private void carregarFuncionarios() {
        modeloTabela.setRowCount(0); // Limpa a tabela
        try (Connection conn = ConexaoBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT fun_nome, fun_cpf, fun_funcao FROM tb_funcionarios")) {
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{rs.getString("fun_nome"), rs.getString("fun_cpf"), rs.getString("fun_funcao")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adicionarFuncionario() {
        String nome = txtNome.getText();
        String cpf = txtCpf.getText();
        String senha = txtSenha.getText();
        String funcao = txtFuncao.getText();

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tb_funcionarios (fun_nome, fun_cpf, fun_senha, fun_funcao) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cpf);
            pstmt.setString(3, senha);
            pstmt.setString(4, funcao);
            pstmt.executeUpdate();
            carregarFuncionarios(); // Atualiza a tabela após adicionar
            JOptionPane.showMessageDialog(this, "Funcionário adicionado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao adicionar funcionário: " + e.getMessage());
        }
    }

    private void atualizarFuncionario() {
        int selectedRow = tabelaFuncionarios.getSelectedRow();
        if (selectedRow >= 0) {
            String nome = txtNome.getText();
            String cpf = txtCpf.getText();
            String senha = txtSenha.getText();
            String funcao = txtFuncao.getText();

            String cpfSelecionado = (String) modeloTabela.getValueAt(selectedRow, 1); // Obtém o CPF do funcionário selecionado

            try (Connection conn = ConexaoBD.conectar();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tb_funcionarios SET fun_nome = ?, fun_cpf = ?, fun_senha = ?, fun_funcao = ? WHERE fun_cpf = ?")) {
                pstmt.setString(1, nome);
                pstmt.setString(2, cpf);
                pstmt.setString(3, senha);
                pstmt.setString(4, funcao);
                pstmt.setString(5, cpfSelecionado);
                pstmt.executeUpdate();
                carregarFuncionarios(); // Atualiza a tabela após a atualização
                JOptionPane.showMessageDialog(this, "Funcionário atualizado com sucesso!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao atualizar funcionário: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um funcionário para atualizar.");
        }
    }

    private void excluirFuncionario() {
        int selectedRow = tabelaFuncionarios.getSelectedRow();
        if (selectedRow >= 0) {
            String cpfSelecionado = (String) modeloTabela.getValueAt(selectedRow, 1); // Obtém o CPF do funcionário selecionado

            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir este funcionário?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = ConexaoBD.conectar();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tb_funcionarios WHERE fun_cpf = ?")) {
                    pstmt.setString(1, cpfSelecionado);
                    pstmt.executeUpdate();
                    carregarFuncionarios(); // Atualiza a tabela após a exclusão
                    JOptionPane.showMessageDialog(this, "Funcionário excluído com sucesso!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erro ao excluir funcionário: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um funcionário para excluir.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaFuncionarios tela = new TelaFuncionarios();
            tela.setVisible(true);
        });
    }
}
