
package roncko.banco2;

/**
 *
 * @author Bastos
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaPrincipal extends JFrame {
    
    public TelaPrincipal() {
        setTitle("Sistema de Vendas");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Criação do painel principal
        JPanel painel = new JPanel();
        painel.setLayout(new GridLayout(5, 1)); // 5 linhas, 1 coluna

        // Botão Vendas
        JButton btnVendas = new JButton("Vendas");
        btnVendas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abre a tela de vendas
                TelaVendas telaVendas = new TelaVendas();
                telaVendas.setVisible(true);
                dispose(); // Fecha a tela principal
            }
        });
        painel.add(btnVendas);

        // Botão Produtos
        JButton btnProdutos = new JButton("Produtos");
        btnProdutos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abre a tela de produtos
                TelaProdutos telaProdutos = new TelaProdutos();
                telaProdutos.setVisible(true);
                dispose(); // Fecha a tela principal
            }
        });
        painel.add(btnProdutos);

        // Botão Funcionários
        JButton btnFuncionarios = new JButton("Funcionários");
        btnFuncionarios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abre a tela de funcionários
                TelaFuncionarios telaFuncionarios = new TelaFuncionarios();
                telaFuncionarios.setVisible(true);
                dispose(); // Fecha a tela principal
            }
        });
        painel.add(btnFuncionarios);

        // Botão Relatórios
        JButton btnRelatorios = new JButton("Relatórios");
        btnRelatorios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abre a tela de relatórios
//                TelaRelatorios telaRelatorios = new TelaRelatorios();
//                telaRelatorios.setVisible(true);
                dispose(); // Fecha a tela principal
            }
        });
        painel.add(btnRelatorios);

        // Botão Sair
        JButton btnSair = new JButton("Sair");
        btnSair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Encerra a aplicação
            }
        });
        painel.add(btnSair);

        // Adiciona o painel à janela
        add(painel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);
        });
    }
}
