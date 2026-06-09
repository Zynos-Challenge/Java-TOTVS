package br.com.totvs.conversacional.main;

import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Analisador;
import br.com.totvs.conversacional.entities.LeitorArquivo;
import br.com.totvs.conversacional.entities.Reuniao;

import javax.swing.JOptionPane;
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

        JOptionPane.showMessageDialog(null,
                "Arquivo carregado com sucesso!\n" + leitor.toString(),
                "Sistema TOTVS",
                JOptionPane.INFORMATION_MESSAGE
        );

        // ───── Menu principal ─────
        boolean rodando = true;
        while (rodando) {
            String[] opcoes = {
                    "1 - Analisar todas as reuniões",
                    "2 - Analisar reunião por ID",
                    "3 - Exibir resumo geral",
                    "4 - Buscar por segmento",
                    "5 - Sair"
            };

            String escolha = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecione uma opção:",
                    "Sistema TOTVS - Menu Principal",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (escolha == null) break;

            switch (escolha.charAt(0)) {
                case '1' -> opcaoAnalisarTodas();
                case '2' -> opcaoAnalisarPorId();
                case '3' -> opcaoResumoGeral();
                case '4' -> opcaoBuscarSegmento();
                case '5' -> rodando = false;
            }
        }

        JOptionPane.showMessageDialog(null,
                "Sistema encerrado. Até logo!",
                "Sistema TOTVS",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static void opcaoAnalisarTodas() {
        analises = analisador.analisarReunioes(reunioes);
        JOptionPane.showMessageDialog(null,
                analises.size() + " reuniões analisadas com sucesso!\n" +
                        "Use a opção 3 para ver o resumo geral.",
                "Análise Concluída",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static void opcaoAnalisarPorId() {
        String entrada = JOptionPane.showInputDialog(
                null,
                "Informe o número da reunião (1 a " + reunioes.size() + "):",
                "Analisar por ID",
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
            analise.detectarTomDeVoz();
            analise.calcularScore();

            JOptionPane.showMessageDialog(null,
                    reuniao.toString() + "\n" + analise.toString(),
                    "Reunião #" + id,
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Entrada inválida. Digite apenas números.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void opcaoResumoGeral() {
        if (analises == null || analises.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma análise realizada ainda.\nUse a opção 1 primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(null,
                analisador.toString(),
                "Resumo Geral",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

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

        for (Reuniao r : reunioes) {
            if (r.getSegmento() != null &&
                    r.getSegmento().toLowerCase().contains(segmento.trim().toLowerCase())) {
                resultado.append(r.toString()).append("\n");
                encontrados++;
            }
        }

        if (encontrados == 0) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma reunião encontrada para o segmento: " + segmento,
                    "Busca por Segmento", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    encontrados + " reunião(ões) encontrada(s) para: " + segmento +
                            "\n\n" + resultado,
                    "Busca por Segmento", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}