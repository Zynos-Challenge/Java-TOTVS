package br.com.totvs.conversacional.entities;

import br.com.totvs.conversacional.interfaces.IAnalisavel;
import java.util.ArrayList;
import java.util.List;

public class Analise implements IAnalisavel {

    private Reuniao reuniao;
    private Cliente cliente;
    private String sentimento;
    private String tomDeVoz;
    private int scoreGeral;
    private List<Produto> produtos;
    private List<Alerta> alertas;
    private List<String> reclamacoes;

    public Analise(){
        this.produtos = new ArrayList<>();
        this.alertas = new ArrayList<>();
        this.reclamacoes = new ArrayList<>();
    }

    public Analise(Reuniao reuniao, Cliente cliente, String sentimento, String tomDeVoz, int scoreGeral) {
        this.reuniao = reuniao;
        this.cliente = cliente;
        this.sentimento = sentimento;
        this.tomDeVoz = tomDeVoz;
        this.scoreGeral = scoreGeral;
        this.produtos = new ArrayList<>();
        this.alertas = new ArrayList<>();
        this.reclamacoes = new ArrayList<>();
    }

    public Reuniao getReuniao() { return reuniao; }
    public void setReuniao(Reuniao reuniao) { this.reuniao = reuniao; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getSentimento() { return sentimento; }
    public void setSentimento(String sentimento) { this.sentimento = sentimento; }

    public String getTomDeVoz() { return tomDeVoz; }
    public void setTomDeVoz(String tomDeVoz) { this.tomDeVoz = tomDeVoz; }

    public int getScoreGeral() { return scoreGeral; }
    public void setScoreGeral(int scoreGeral) { this.scoreGeral = scoreGeral; }

    public List<Produto> getProdutos() { return produtos; }
    public void setProdutos(List<Produto> produtos) { this.produtos = produtos; }

    public List<Alerta> getAlertas() { return alertas; }
    public void setAlertas(List<Alerta> alertas) { this.alertas = alertas; }

    public List<String> getReclamacoes() { return reclamacoes; }
    public void setReclamacoes(List<String> reclamacoes) { this.reclamacoes = reclamacoes; }

    // ───── IAnalisavel ─────

    @Override
    public int calcularScore() {
        int score = 50;

        if (sentimento != null) {
            if (sentimento.equals("POSITIVO")) score += 20;
            if (sentimento.equals("NEGATIVO")) score -= 20;
        }

        for (Alerta alertaAtual : alertas) {
            if (alertaAtual.getTipo().equals("CHURN")) score -= 15;
            if (alertaAtual.getTipo().equals("CONCORRENTE")) score -= 10;
            if (alertaAtual.getTipo().equals("OPORTUNIDADE")) score += 10;
        }

        if (score > 100) score = 100;
        if (score < 0) score = 0;

        this.scoreGeral = score;
        return score;
    }

    @Override
    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================");
        sb.append("\n        RELATORIO DE REUNIAO TOTVS      ");
        sb.append("\n========================================");

        // Dados da reunião
        if (reuniao != null) {
            sb.append("\n\n--- DADOS DA REUNIAO ---");
            sb.append("\nData: ").append(reuniao.getData());
            sb.append("\nDuracao: ").append(reuniao.getDuracao()).append(" min");
            sb.append("\nTipo Recurso: ").append(reuniao.getTipoRecurso());
            sb.append("\nUF: ").append(reuniao.getUf());
            sb.append("\nSegmento: ").append(reuniao.getSegmento());
            sb.append("\nFaixa Faturamento: ").append(reuniao.getFaixaFaturamento());
            sb.append("\nNPS: ").append(reuniao.getNotaNps());
        }

        // Dados do cliente
        if (cliente != null) {
            sb.append("\n\n--- DADOS DO CLIENTE ---");
            sb.append("\nNome: ").append(cliente.getNome());
            sb.append("\nCargo: ").append(cliente.getCargo());
            sb.append("\nEmpresa: ").append(cliente.getEmpresa());
        }

        // Análise
        sb.append("\n\n--- ANALISE ---");
        sb.append("\nSentimento: ").append(sentimento);
        sb.append("\nTom de Voz: ").append(tomDeVoz);
        sb.append("\nScore Geral: ").append(scoreGeral).append("/100");

        // Produtos
        sb.append("\n\n--- PRODUTOS IDENTIFICADOS ---");
        if (produtos.isEmpty()) {
            sb.append("\nNenhum produto identificado.");
        } else {
            for (Produto p : produtos) {
                sb.append("\n- ").append(p.getNome())
                        .append(" | ").append(p.getCategoria())
                        .append(" | ").append(p.getStatus());
            }
        }

        // Alertas
        sb.append("\n\n--- ALERTAS ---");
        if (alertas.isEmpty()) {
            sb.append("\nNenhum alerta identificado.");
        } else {
            for (Alerta a : alertas) {
                sb.append("\n- [").append(a.getUrgencia()).append("] ")
                        .append(a.getTipo()).append(": ").append(a.getDescricao());
            }
        }

        // Reclamações
        sb.append("\n\n--- RECLAMACOES ---");
        if (reclamacoes.isEmpty()) {
            sb.append("\nNenhuma reclamacao identificada.");
        } else {
            for (String r : reclamacoes) {
                sb.append("\n- ").append(r);
            }
        }

        sb.append("\n========================================");

        return sb.toString();
    }

    // ───── Demais métodos ─────

    public List<String> detectarReclamacoes() {
        String texto = reuniao.getTextoOriginal().toLowerCase();

        List<String> reclamacoesSuporte = List.of(
                "suporte", "atendimento", "demora", "nao respondeu", "sem retorno"
        );
        List<String> reclamacoesProduto = List.of(
                "trava", "lento", "erro", "bug", "falha", "nao funciona",
                "travando", "caindo", "instavel"
        );
        List<String> reclamacoesPreco = List.of(
                "caro", "valor alto", "preco", "custo elevado", "muito caro"
        );
        List<String> reclamacoesIntegracao = List.of(
                "nao integra", "nao comunica", "sistema separado",
                "nao conversa", "isolado"
        );

        for (String sinal : reclamacoesSuporte) {
            if (texto.contains(sinal)) reclamacoes.add("SUPORTE: " + sinal);
        }
        for (String sinal : reclamacoesProduto) {
            if (texto.contains(sinal)) reclamacoes.add("PRODUTO: " + sinal);
        }
        for (String sinal : reclamacoesPreco) {
            if (texto.contains(sinal)) reclamacoes.add("PRECO: " + sinal);
        }
        for (String sinal : reclamacoesIntegracao) {
            if (texto.contains(sinal)) reclamacoes.add("INTEGRACAO: " + sinal);
        }

        return reclamacoes;
    }

    public String detectarTomDeVoz() {
        String texto = reuniao.getTextoOriginal().toLowerCase();

        List<String> tomAgressivo = List.of(
                "absurdo", "inaceitavel", "ridiculo", "nao aguento", "revoltado",
                "uma vergonha", "inadmissivel", "pessimo", "horrivel"
        );
        List<String> tomPositivo = List.of(
                "otimo", "excelente", "maravilhoso", "perfeito", "feliz",
                "satisfeito", "contente", "muito bom", "aprovado"
        );
        List<String> tomAnsioso = List.of(
                "urgente", "preciso logo", "quanto antes", "prazo",
                "atrasado", "nao pode esperar", "emergencia"
        );

        int pontosAgressivo = 0;
        int pontosPositivo = 0;
        int pontosAnsioso = 0;

        for (String sinal : tomAgressivo) { if (texto.contains(sinal)) pontosAgressivo++; }
        for (String sinal : tomPositivo)  { if (texto.contains(sinal)) pontosPositivo++; }
        for (String sinal : tomAnsioso)   { if (texto.contains(sinal)) pontosAnsioso++; }

        if (pontosAgressivo >= pontosPositivo && pontosAgressivo >= pontosAnsioso) {
            this.tomDeVoz = "AGRESSIVO";
        } else if (pontosAnsioso >= pontosPositivo && pontosAnsioso >= pontosAgressivo) {
            this.tomDeVoz = "ANSIOSO";
        } else if (pontosPositivo > 0) {
            this.tomDeVoz = "POSITIVO";
        } else {
            this.tomDeVoz = "NEUTRO";
        }

        return this.tomDeVoz;
    }

    @Override
    public String toString() {
        return "\n=== ANALISE ===" +
                "\nCliente: " + (cliente != null ? cliente.getNome() : "N/A") +
                "\nSentimento: " + sentimento +
                "\nTom de Voz: " + tomDeVoz +
                "\nScore Geral: " + scoreGeral +
                "\nProdutos Identificados: " + produtos +
                "\nAlertas: " + alertas +
                "\nReclamacoes: " + reclamacoes;
    }
}