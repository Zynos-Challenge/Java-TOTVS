package br.com.totvs.conversacional.main;

import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.interfaces.IAnalisavel;
import br.com.totvs.conversacional.repository.LeitorArquivo;
import br.com.totvs.conversacional.services.Analisador;
import br.com.totvs.conversacional.services.AnalisadorIA;

import javax.swing.JOptionPane;
import java.util.List;

public class TesteSistema {

    private static LeitorArquivo leitor;
    private static Analisador analisador;
    private static List<Reuniao> reunioes;
    private static List<Analise> analises;
    private static boolean modoIA = false;

    public static void main(String[] args) {

        // ───── Inicialização ─────
        String caminho = JOptionPane.showInputDialog(
                null,
                "Informe o caminho do arquivo NDJSON:",
                "Sistema TOTVS - Inteligência Conversacional",
                JOptionPane.QUESTION_MESSAGE
        );

        if (caminho == null || caminho.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Caminho inválido. Encerrando sistema.");
            return;
        }

        leitor = new LeitorArquivo(caminho.trim());
        analisador = new Analisador();
        reunioes = leitor.lerArquivo();

        if (reunioes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nenhuma reunião encontrada no arquivo. Encerrando sistema.");
            return;
        }

        JOptionPane.showMessageDialog(null,
                "Arquivo carregado com sucesso!\n" + leitor.toString(),
                "Sistema TOTVS",
                JOptionPane.INFORMATION_MESSAGE
        );

        // ───── Escolha do modo de análise ─────
        String[] modos = {
                "1 - Análise local (palavras-chave)",
                "2 - Análise com IA (Gemini)"
        };

        String modo = (String) JOptionPane.showInputDialog(
                null,
                "Selecione o modo de análise:",
                "Sistema TOTVS - Modo de Análise",
                JOptionPane.PLAIN_MESSAGE,
                null,
                modos,
                modos[0]
        );

        if (modo == null) return;
        modoIA = modo.charAt(0) == '2';

        JOptionPane.showMessageDialog(null,
                modoIA ? "Modo IA ativado! Usando Gemini 1.5 Flash." : "Modo local ativado! Usando palavras-chave.",
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
                    "Selecione uma opção: [Modo: " + (modoIA ? "IA Gemini" : "Local") + "]",
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

    // ───── Opção 1: Analisar todas ─────

    private static void opcaoAnalisarTodas() {
        if (modoIA) {
            JOptionPane.showMessageDialog(null,
                    "O modo IA analisa uma reunião por vez.\nUse a opção 2 para analisar por ID.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        analises = analisador.analisarReunioes(reunioes);

        JOptionPane.showMessageDialog(null,
                analises.size() + " reuniões analisadas com sucesso!\n" +
                        "Use a opção 3 para ver o resumo geral.",
                "Análise Concluída",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ───── Opção 2: Analisar por ID ─────

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
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Reuniao reuniao = reunioes.get(id - 1);
            IAnalisavel analisavel;
            String resultado;

            if (modoIA) {
                // Modo IA - usa Gemini
                analisavel = new AnalisadorIA(reuniao);
                analisavel.calcularScore();
                resultado = reuniao.toString() + "\n\n=== RELATORIO IA ===\n" + analisavel.gerarRelatorio();
            } else {
                // Modo local - usa palavras-chave
                Analise analise = new Analise();
                analise.setReuniao(reuniao);
                analise.detectarReclamacoes();
                analise.detectarTomDeVoz();
                analise.calcularScore();
                analisavel = analise;
                resultado = reuniao.toString() + "\n" + analise.toString();
            }

            JOptionPane.showMessageDialog(null,
                    resultado,
                    "Reunião #" + id + (modoIA ? " [IA Gemini]" : " [Local]"),
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Entrada inválida. Digite apenas números.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ───── Opção 3: Resumo geral ─────

    private static void opcaoResumoGeral() {
        if (modoIA) {
            JOptionPane.showMessageDialog(null,
                    "O resumo geral está disponível apenas no modo local.\nUse a opção 1 primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (analises == null || analises.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma análise realizada ainda.\nUse a opção 1 primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(null,
                analisador.toString(),
                "Resumo Geral",
                JOptionPane.INFORMATION_MESSAGE
        );
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
                    "Busca por Segmento",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(null,
                    encontrados + " reunião(ões) encontrada(s) para: " + segmento +
                            "\n\n" + resultado,
                    "Busca por Segmento",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}