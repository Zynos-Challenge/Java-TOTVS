package br.com.totvs.conversacional.main;

import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Analisador;
import br.com.totvs.conversacional.entities.LeitorArquivo;
import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.entities.Vendedor;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.util.List;

public class TesteSistema {

    private static LeitorArquivo leitor;
    private static Analisador analisador;
    private static List<Reuniao> reunioes;
    private static List<Analise> analises;

    public static void main(String[] args) {

        // ───── Carregamento automático ─────
        leitor = new LeitorArquivo();
        analisador = new Analisador();
        reunioes = leitor.lerArquivoAutomatico();

        if (reunioes.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Arquivo de transcrição não encontrado.\n" +
                            "Coloque o arquivo ANON_transcricao.json em src/resources/\n" +
                            "(essa pasta está no .gitignore para proteger os dados).",
                    "Sistema TOTVS - Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        exibirDialogo("Arquivo carregado com sucesso!\n" + leitor.toString(), "Sistema TOTVS");

        // ───── Menu principal ─────
        boolean rodando = true;
        while (rodando) {
            String[] opcoes = {
                    "1 - Analisar todas as reuniões",
                    "2 - Ver detalhes de uma reunião por ID",
                    "3 - Exibir resumo geral",
                    "4 - Buscar por segmento",
                    "5 - Sair"
            };

            String escolha = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecione uma opção:\n(" + reunioes.size() + " reuniões carregadas)",
                    "Sistema TOTVS - Menu Principal",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (escolha == null) break;

            switch (escolha.charAt(0)) {
                case '1' -> opcaoAnalisarTodas();
                case '2' -> opcaoDetalhesPorId();
                case '3' -> opcaoResumoGeral();
                case '4' -> opcaoBuscarSegmento();
                case '5' -> rodando = false;
            }
        }

        exibirDialogo("Sistema encerrado. Até logo!", "Sistema TOTVS");
    }

    // ───── Opção 1: Analisar todas ─────

    private static void opcaoAnalisarTodas() {
        analises = analisador.analisarReunioes(reunioes);
        exibirDialogo(analises.size() + " reuniões analisadas com sucesso!\n" +
                "Use a opção 3 para ver o resumo geral.", "Análise Concluída");
    }

    // ───── Opção 2: Detalhes completos por ID ─────
    private static void opcaoDetalhesPorId() {
        String entrada = JOptionPane.showInputDialog(
                null,
                "Informe o número da reunião (1 a " + reunioes.size() + "):",
                "Ver Detalhes por ID",
                JOptionPane.QUESTION_MESSAGE
        );

        if (entrada == null || entrada.trim().isEmpty()) return;

        try {
            int id = Integer.parseInt(entrada.trim());

            if (id < 1 || id > reunioes.size()) {
                JOptionPane.showMessageDialog(null,
                        "ID inválido. Informe um número entre 1 e " + reunioes.size(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Reuniao reuniao = reunioes.get(id - 1);

            Analise analise = new Analise();
            analise.setReuniao(reuniao);
            analise.detectarReclamacoes();
            analise.detectarAlertas();
            analise.detectarTomDeVoz();
            analise.calcularScore();

            // Aplica bônus de NPS se disponível
            Integer nps = reuniao.getNotaNps();
            if (nps != null) {
                int score = analise.getScoreGeral();
                if (nps >= 9) score += 10;
                else if (nps <= 6) score -= 10;
                if (score > 100) score = 100;
                if (score <   0) score = 0;
                analise.setScoreGeral(score);
            }

            exibirRelatorio("REUNIÃO #" + id + " — RELATÓRIO DETALHADO", analise.gerarRelatorio());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Entrada inválida. Digite apenas números.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ───── Opção 3: Resumo geral ─────

    private static void opcaoResumoGeral() {
        if (analises == null || analises.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma análise realizada ainda.\nUse a opção 1 primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        exibirRelatorio("Resumo Geral", analisador.toString());
    }

    // ───── Opção 4: Buscar por segmento ─────

    private static void opcaoBuscarSegmento() {
        String segmento = JOptionPane.showInputDialog(
                null,
                "Informe o segmento a buscar:",
                "Buscar por Segmento",
                JOptionPane.QUESTION_MESSAGE
        );

        if (segmento == null || segmento.trim().isEmpty()) return;

        StringBuilder resultado = new StringBuilder();
        int encontrados = 0;

        for (int i = 0; i < reunioes.size(); i++) {
            Reuniao r = reunioes.get(i);
            if (r.getSegmento() != null &&
                    r.getSegmento().toLowerCase().contains(segmento.trim().toLowerCase())) {
                resultado.append("\nReunião #").append(i + 1)
                        .append(" | ID: ").append(r.getId())
                        .append(" | Data: ").append(r.getData())
                        .append(" | UF: ").append(r.getUf() != null ? r.getUf() : "—")
                        .append(" | NPS: ").append(r.getNotaNps() != null ? r.getNotaNps() : "—")
                        .append(" | Duração: ").append(r.getDuracao()).append(" min");
                encontrados++;
            }
        }

        if (encontrados == 0) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma reunião encontrada para o segmento: " + segmento,
                    "Busca por Segmento", JOptionPane.WARNING_MESSAGE);
        } else {
            exibirRelatorio("Busca por Segmento: " + segmento,
                    encontrados + " reunião(ões) encontrada(s):\n" + resultado);
        }
    }

    // ───── Utilitários de exibição ─────

    // Diálogo simples para mensagens curtas
    private static void exibirDialogo(String mensagem, String titulo) {
        JOptionPane.showMessageDialog(null, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    // Diálogo com scroll para relatórios longos
    private static void exibirRelatorio(String titulo, String conteudo) {
        JTextArea textArea = new JTextArea(conteudo);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(620, 480));

        JOptionPane.showMessageDialog(null, scrollPane, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}