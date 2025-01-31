
package roncko.banco2;

/**
 *
 * @author Bastos
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class TelaRelatorios extends JFrame {
    private final JComboBox<String> comboTipoRelatorio;
    private final JTextField txtDataInicio;
    private final JTextField txtDataFim;
    private final JTextArea areaResultados;

    public TelaRelatorios() {
        setTitle("Tela de Relatórios");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Painel principal
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout());

        // Painel de entrada
        JPanel painelEntrada = new JPanel();
        painelEntrada.setLayout(new GridLayout(4, 2));

        // ComboBox para seleção de tipo de relatório
        painelEntrada.add(new JLabel("Tipo de Relatório:"));
        comboTipoRelatorio = new JComboBox<>(new String[]{"Relatório de Vendas", "Relatório de Produtos", "Relatório de Funcionários"});
        painelEntrada.add(comboTipoRelatorio);

        // Campo de Data Inicial
        painelEntrada.add(new JLabel("Data Inicial (YYYY-MM-DD):"));
        txtDataInicio = new JTextField();
        painelEntrada.add(txtDataInicio);

        // Campo de Data Final
        painelEntrada.add(new JLabel("Data Final (YYYY-MM-DD):"));
        txtDataFim = new JTextField();
        painelEntrada.add(txtDataFim);

        // Botão Gerar Relatório
        JButton btnGerarRelatorio = new JButton("Gerar Relatório");
        btnGerarRelatorio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gerarRelatorio();
            }
        });
        painelEntrada.add(btnGerarRelatorio);

        // Adiciona o painel de entrada ao painel principal
        painel.add(painelEntrada, BorderLayout.NORTH);

        // Área de Resultados
        areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaResultados);
        painel.add(scrollPane, BorderLayout.CENTER);

        // Adiciona o painel principal à janela
        add(painel);
    }

    private void gerarRelatorio() {
        String tipoRelatorio = (String) comboTipoRelatorio.getSelectedItem();
        String dataInicio = txtDataInicio.getText();
        String dataFim = txtDataFim.getText();

        // Validação de dados
        if (!validarDatas(dataInicio, dataFim)) {
            areaResultados.setText("Datas inválidas. Por favor, insira datas no formato correto (YYYY-MM-DD) e verifique se a data inicial é anterior à data final.\n");
            return;
        }

        // Limpa a área de resultados antes de gerar um novo relatório
        areaResultados.setText("Gerando " + tipoRelatorio + " de " + dataInicio + " a " + dataFim + "...\n");

        // Lógica para gerar o relatório com base no tipo selecionado
        try (Connection conn = ConexaoBD.conectar()) {
            String sql = "";
            ResultSet rs = null;

            switch (tipoRelatorio) {
                case "Relatório de Vendas":
                    sql = "SELECT p.pro_descricao, SUM(i.ite_quantidade) AS total_vendas " +
                          "FROM tb_vendas v " +
                          "JOIN tb_itens i ON v.ven_codigo = i.tb_vendas_ven_codigo " +
                          "JOIN tb_produtos p ON i.tb_produtos_pro_codigo = p.pro_codigo " +
                          "WHERE v.ven_data >= ? AND v.ven_data <= ? " +
                          "GROUP BY p.pro_descricao";
                    break;

                case "Relatório de Produtos":
                    sql = "SELECT pro_descricao, pro_quantidade FROM tb_produtos";
                    break;

                case "Relatório de Funcionários":
                    sql = "SELECT f.fun_nome, COUNT(v.ven_codigo) AS total_vendas " +
                          "FROM tb_funcionarios f " +
                          "LEFT JOIN tb_vendas v ON f.fun_codigo = v.tb_funcionarios_fun_codigo " +
                          "GROUP BY f.fun_nome";
                    break;
            }

            if (!sql.isEmpty()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    if (tipoRelatorio.equals("Relatório de Vendas")) {
                        pstmt.setString(1, dataInicio);
                        pstmt.setString(2, dataFim);
                    }
                    rs = pstmt.executeQuery();

                    // Processa os resultados e exibe na área de resultados
                    StringBuilder resultados = new StringBuilder();
                    while (rs.next()) {
                        switch (tipoRelatorio) {
                            case "Relatório de Vendas":
                                resultados.append("Produto: ").append(rs.getString("pro_descricao"))
                                          .append(", Total Vendas: ").append(rs.getInt("total_vendas")).append("\n");
                                break;

                            case "Relatório de Produtos":
                                resultados.append("Produto: ").append(rs.getString("pro_descricao"))
                                          .append(", Quantidade: ").append(rs.getInt("pro_quantidade")).append("\n");
                                break;

                            case "Relatório de Funcionários":
                                resultados.append("Funcionário: ").append(rs.getString("fun_nome"))
                                          .append(", Total Vendas: ").append(rs.getInt("total_vendas")).append("\n");
                                break;
                        }
                    }
                    areaResultados.append(resultados.toString());
                }
            }
        } catch (SQLException e) {
            areaResultados.append("Erro ao gerar relatório: " + e.getMessage() + "\n");
        }
    }

    private boolean validarDatas(String dataInicio, String dataFim) {
        // Implementar a lógica de validação de datas
        if (dataInicio.isEmpty() || dataFim.isEmpty()) {
            return false;
        }
        // Verifica se as datas estão no formato correto
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        if (!dataInicio.matches(regex) || !dataFim.matches(regex)) {
            return false;
        }
        // Verifica se a data inicial é anterior à data final
        return dataInicio.compareTo(dataFim) <= 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaRelatorios tela = new TelaRelatorios();
            tela.setVisible(true);
        });
    }
}
