package br.com.totvs.conversacional.main;

import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Analisador;
import br.com.totvs.conversacional.entities.LeitorArquivo;
import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.dao.ReuniaoDAO;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TesteSistema {

    private static LeitorArquivo leitor;
    private static Analisador analisador;
    private static List<Reuniao> reunioes = new ArrayList<>();
    private static List<Analise> analises;

    public static void main(String[] args) {

        leitor = new LeitorArquivo();
        analisador = new Analisador();

        boolean rodando = true;

        while (rodando) {
            String[] opcoes = {
                    "1 - Selecionar / Importar Arquivo JSON",
                    "2 - Analisar todas as reuniões",
                    "3 - Ver detalhes de uma reunião por ID",
                    "4 - Exibir resumo geral",
                    "5 - Buscar por segmento",
                    "6 - Operações de Banco de Dados",
                    "7 - Sair"
            };

            String escolha = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecione uma opção:\n(" + reunioes.size() + " reuniões carregadas na memória)",
                    "Sistema TOTVS - Menu Principal",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (escolha == null) break;

            switch (escolha.charAt(0)) {
                case '1' -> opcaoCarregarArquivo();
                case '2' -> opcaoAnalisarTodas();
                case '3' -> opcaoDetalhesPorId();
                case '4' -> opcaoResumoGeral();
                case '5' -> opcaoBuscarSegmento();
                case '6' -> opcaoMenuBanco();
                case '7' -> rodando = false;
            }
        }

        exibirDialogo("Sistema encerrado. Até logo!", "Sistema TOTVS");
    }

    private static void opcaoCarregarArquivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecione o arquivo JSON de reuniões");

        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos JSON (*.json)", "json"));

        int resultado = chooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = chooser.getSelectedFile();

            if (!arquivoSelecionado.exists() || arquivoSelecionado.length() == 0) {
                JOptionPane.showMessageDialog(null,
                        "O arquivo selecionado é inválido ou está vazio.",
                        "Sistema TOTVS - Erro",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            reunioes = leitor.lerArquivo(arquivoSelecionado);

            if (reunioes == null || reunioes.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "O arquivo selecionado não pôde ser lido ou não contém reuniões válidas.",
                        "Sistema TOTVS - Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                exibirDialogo("Arquivo JSON carregado com sucesso!\n" +
                        "Total de " + reunioes.size() + " reuniões prontas para análise.", "Sistema TOTVS");
            }
        }
    }

    private static void opcaoAnalisarTodas() {
        if (reunioes == null || reunioes.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma reunião carregada.\nUse a opção 1 para carregar um arquivo JSON primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        analises = analisador.analisarReunioes(reunioes);
        exibirDialogo(analises.size() + " reuniões analisadas com sucesso!\n" +
                "Use a opção 4 para ver o resumo geral.", "Análise Concluída");
    }

    private static void opcaoDetalhesPorId() {
        if (reunioes == null || reunioes.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma reunião carregada.\nUse a opção 1 para carregar um arquivo JSON primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

    private static void opcaoResumoGeral() {
        if (analises == null || analises.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma análise realizada ainda.\nUse a opção 2 primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        exibirRelatorio("Resumo Geral", analisador.toString());
    }

    private static void opcaoBuscarSegmento() {
        if (reunioes == null || reunioes.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma reunião carregada.\nUse a opção 1 para carregar um arquivo JSON primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

    private static void opcaoMenuBanco() {
        ReuniaoDAO dao = new ReuniaoDAO();
        boolean testando = true;

        while (testando) {
            String[] opcoes = {
                    "1 - Inserir uma Reunião Manualmente",
                    "2 - Contar total de reuniões no Banco",
                    "3 - Buscar Reunião por ID no Banco",
                    "4 - Deletar Reunião por ID no Banco",
                    "5 - Voltar ao Menu Principal"
            };

            String escolha = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecione a operação de banco de dados para testar:",
                    "Menu DAO - Teste de Conexão",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (escolha == null || escolha.charAt(0) == '5') {
                testando = false;
                continue;
            }

            switch (escolha.charAt(0)) {
                case '1' -> {
                    String id = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite o ID da Reunião:",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "1247082"
                    );
                    if (id == null || id.trim().isEmpty()) break;

                    String codt = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite o código do tenant/cliente (codt):",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "2"
                    );
                    if (codt == null) break;

                    String segmento = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite o segmento de mercado:",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "SAUDE"
                    );
                    if (segmento == null) break;

                    String formato = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite o formato da reunião:",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "VIDEO"
                    );
                    if (formato == null) break;

                    String externoStr = (String) JOptionPane.showInputDialog(
                            null,
                            "Envolve participante externo? Digite S ou N:",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "S"
                    );
                    if (externoStr == null) break;

                    String duracaoStr = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite a duração em minutos:",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "45"
                    );
                    if (duracaoStr == null) break;

                    int duracao = 0;
                    try {
                        duracao = Integer.parseInt(duracaoStr.trim());
                    } catch (NumberFormatException ignored) {}

                    String npsStr = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite a nota NPS (0 a 10) ou deixe em branco:",
                            "Inserção Manual",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "8"
                    );
                    if (npsStr == null) break;

                    Integer notaNps = null;
                    try {
                        if (!npsStr.trim().isEmpty()) {
                            notaNps = Integer.parseInt(npsStr.trim());
                        }
                    } catch (NumberFormatException ignored) {}

                    Reuniao manual = new Reuniao();
                    manual.setId(id.trim());
                    manual.setCodt(codt.trim());
                    manual.setSegmento(segmento.trim());
                    manual.setFormato(formato.trim().toUpperCase());
                    manual.setExterno(externoStr.trim().equalsIgnoreCase("S"));
                    manual.setDuracao(duracao);
                    manual.setNotaNps(notaNps);
                    manual.setData(LocalDateTime.now());
                    manual.setDataCriacao(LocalDateTime.now());
                    manual.setStatus("COMPLETED");
                    manual.setTextoOriginal("Transcrição de teste inserida manualmente seguindo o padrão estrutural.");

                    boolean sucesso = dao.inserir(manual);
                    if (sucesso) {
                        exibirDialogo("Reunião '" + id + "' inserida manualmente com sucesso!", "Inserção Manual");
                    } else {
                        exibirDialogo("Falha ao inserir reunião. Verifique se o ID já existe no banco.", "Erro");
                    }
                }
                case '2' -> {
                    int total = dao.contarTotal();
                    exibirDialogo("Conexão OK!\nTotal de registros na tabela 'reuniao': " + total, "Teste de Contagem");
                }
                case '3' -> {
                    String id = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite o ID da Reunião que deseja buscar no Oracle:",
                            "Buscar no Banco",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            "1247082"
                    );
                    if (id != null && !id.trim().isEmpty()) {
                        Optional<Reuniao> optReuniao = dao.buscarPorId(id.trim());
                        if (optReuniao.isPresent()) {
                            Reuniao r = optReuniao.get();
                            String dados = String.format("Registro Encontrado no Banco:\n\nID: %s\nData: %s\nSegmento: %s\nDuração: %d min\nNPS: %s",
                                    r.getId(), r.getData(), r.getSegmento(), r.getDuracao(), (r.getNotaNps() != null ? r.getNotaNps() : "N/A"));
                            exibirRelatorio("Busca no Banco - Sucesso", dados);
                        } else {
                            exibirDialogo("Nenhum registro encontrado no banco com o ID: " + id, "Busca sem resultados");
                        }
                    }
                }
                case '4' -> {
                    String id = (String) JOptionPane.showInputDialog(
                            null,
                            "Digite o ID da Reunião que deseja DELETAR do banco:",
                            "Deletar Registro",
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            null,
                            "1247082"
                    );
                    if (id != null && !id.trim().isEmpty()) {
                        boolean excluiu = dao.deletar(id.trim());
                        if (excluiu) {
                            exibirDialogo("Reunião com ID " + id + " deletada do banco com sucesso!", "Teste de Exclusão");
                        } else {
                            exibirDialogo("Não foi possível deletar. O ID pode não existir no banco.", "Erro na Exclusão");
                        }
                    }
                }
            }
        }
    }

    private static void exibirDialogo(String mensagem, String titulo) {
        JOptionPane.showMessageDialog(null, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

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