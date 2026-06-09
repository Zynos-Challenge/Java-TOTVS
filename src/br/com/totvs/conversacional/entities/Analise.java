package br.com.totvs.conversacional.entities;

import java.util.ArrayList;
import java.util.List;

public class Analise {

    private Reuniao reuniao;
    private Cliente cliente;
    private String sentimento;
    private String tomDeVoz;
    private int scoreGeral;
    private List<Produto> produtos;
    private List<Alerta> alertas;
    private List<String> reclamacoes;
    private Vendedor vendedor;

    public Analise() {
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

    public Reuniao getReuniao() {
        return reuniao;
    }

    public void setReuniao(Reuniao reuniao) {
        this.reuniao = reuniao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getSentimento() {
        return sentimento;
    }

    public void setSentimento(String sentimento) {
        this.sentimento = sentimento;
    }

    public String getTomDeVoz() {
        return tomDeVoz;
    }

    public void setTomDeVoz(String tomDeVoz) {
        this.tomDeVoz = tomDeVoz;
    }

    public int getScoreGeral() {
        return scoreGeral;
    }

    public void setScoreGeral(int scoreGeral) {
        this.scoreGeral = scoreGeral;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public List<Alerta> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<Alerta> alertas) {
        this.alertas = alertas;
    }

    public List<String> getReclamacoes() {
        return reclamacoes;
    }

    public void setReclamacoes(List<String> reclamacoes) {
        this.reclamacoes = reclamacoes;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    // ───── Métodos ─────

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

    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n╔══════════════════════════════════════════╗");
        sb.append("\n║     RELATÓRIO DETALHADO - TOTVS          ║");
        sb.append("\n╚══════════════════════════════════════════╝");

        // ── Dados da Reunião ──
        if (reuniao != null) {
            sb.append("\n\n┌─ DADOS DA REUNIÃO ───────────────────────");
            sb.append("\n  Data e Hora  : ").append(reuniao.getData() != null ? reuniao.getData() : "Não informado");
            sb.append("\n  Duração      : ").append(reuniao.getDuracao()).append(" minutos");
            sb.append("\n  Tipo Recurso : ").append(reuniao.getTipoRecurso() != null ? reuniao.getTipoRecurso() : "Não informado");
            sb.append("\n  UF           : ").append(reuniao.getUf() != null ? reuniao.getUf() : "Não informado");
            sb.append("\n  Segmento     : ").append(reuniao.getSegmento() != null ? reuniao.getSegmento() : "Não informado");
            sb.append("\n  Faturamento  : ").append(reuniao.getFaixaFaturamento() != null ? reuniao.getFaixaFaturamento() : "Não informado");
            sb.append("\n  NPS          : ").append(reuniao.getNotaNps()).append("/10");
            sb.append("\n  Fala Vendedor: ").append(reuniao.getPFalaVendedor()).append("%");
            sb.append("\n└──────────────────────────────────────────");
        }

        // ── Dados do Vendedor ──
        if (vendedor != null) {
            sb.append("\n\n┌─ VENDEDOR ───────────────────────────────");
            sb.append("\n  Nome         : ").append(vendedor.getNome() != null ? vendedor.getNome() : "Não informado");
            sb.append("\n  Cargo        : ").append(vendedor.getCargo() != null ? vendedor.getCargo() : "Não informado");
            sb.append("\n  Empresa      : ").append(vendedor.getEmpresa() != null ? vendedor.getEmpresa() : "Não informado");
            sb.append("\n  Meta de Vendas: R$ ").append(String.format("%.2f", vendedor.getMetaVendas()));
            sb.append("\n  Tempo de Fala: ").append(vendedor.getTempoFala()).append("s");
            sb.append("\n  Confiança    : ").append(vendedor.getConfiancaCliente()).append("/100");
            sb.append("\n└──────────────────────────────────────────");
        }

        // ── Dados do Cliente ──
        if (cliente != null) {
            sb.append("\n\n┌─ CLIENTE ────────────────────────────────");
            sb.append("\n  Nome         : ").append(cliente.getNome() != null ? cliente.getNome() : "Não informado");
            sb.append("\n  Cargo        : ").append(cliente.getCargo() != null ? cliente.getCargo() : "Não informado");
            sb.append("\n  Empresa      : ").append(cliente.getEmpresa() != null ? cliente.getEmpresa() : "Não informado");
            sb.append("\n  Budget       : R$ ").append(String.format("%.2f", cliente.getBudget()));
            sb.append("\n  Tipo Decisão : ").append(cliente.getTipoDecisao() != null ? cliente.getTipoDecisao() : "Não informado");
            sb.append("\n  Satisfação   : ").append(cliente.getSatisfacao() != null ? cliente.getSatisfacao() : "Não informado");
            sb.append("\n  Cit. Concorrente: ").append(cliente.isMencionouConcorrente() ? "⚠ SIM" : "Não");
            sb.append("\n└──────────────────────────────────────────");
        }

        // ── Análise de Sentimento e Score ──
        sb.append("\n\n┌─ ANÁLISE ────────────────────────────────");
        sb.append("\n  Sentimento   : ").append(sentimento != null ? sentimento : "Não detectado");
        sb.append("\n  Tom de Voz   : ").append(tomDeVoz != null ? tomDeVoz : "Não detectado");
        sb.append("\n  Score Geral  : ").append(scoreGeral).append("/100");

        // Barra visual do score
        int blocos = scoreGeral / 10;
        sb.append("\n  Score Visual : [");
        for (int i = 0; i < 10; i++) sb.append(i < blocos ? "█" : "░");
        sb.append("] ").append(scoreGeral).append("%");
        sb.append("\n└──────────────────────────────────────────");

        // ── Produtos ──
        sb.append("\n\n┌─ PRODUTOS MENCIONADOS ───────────────────");
        if (produtos == null || produtos.isEmpty()) {
            sb.append("\n  Nenhum produto identificado na transcrição.");
        } else {
            for (Produto p : produtos) {
                sb.append("\n  • ").append(p.getNome())
                        .append(" | Categoria: ").append(p.getCategoria())
                        .append(" | Status: ").append(p.getStatus())
                        .append(" | Versão: ").append(p.getVersaoMencao());
            }
        }
        sb.append("\n└──────────────────────────────────────────");

        // ── Alertas ──
        sb.append("\n\n┌─ ALERTAS ────────────────────────────────");
        if (alertas == null || alertas.isEmpty()) {
            sb.append("\n  Nenhum alerta identificado.");
        } else {
            for (Alerta a : alertas) {
                sb.append("\n  ⚑ [").append(a.getUrgencia()).append("] ")
                        .append(a.getTipo()).append(": ").append(a.getDescricao());
                if (a.getTrechoOrigem() != null && !a.getTrechoOrigem().isEmpty()) {
                    sb.append("\n    Trecho: \"").append(a.getTrechoOrigem()).append("\"");
                }
            }
        }
        sb.append("\n└──────────────────────────────────────────");

        // ── Reclamações ──
        sb.append("\n\n┌─ RECLAMAÇÕES DETECTADAS ─────────────────");
        if (reclamacoes == null || reclamacoes.isEmpty()) {
            sb.append("\n  Nenhuma reclamação identificada.");
        } else {
            for (String r : reclamacoes) {
                sb.append("\n  ✗ ").append(r);
            }
        }
        sb.append("\n└──────────────────────────────────────────");

        sb.append("\n\n╔══════════════════════════════════════════╗");
        sb.append("\n║            FIM DO RELATÓRIO              ║");
        sb.append("\n╚══════════════════════════════════════════╝");

        return sb.toString();
    }

    public List<String> detectarReclamacoes() {
        String texto = reuniao.getTextoOriginal().toLowerCase();

        List<String> reclamacoesSuporte = List.of("suporte", "atendimento", "demora", "nao respondeu", "sem retorno");
        List<String> reclamacoesProduto = List.of("trava", "lento", "erro", "bug", "falha", "nao funciona", "travando", "caindo", "instavel");
        List<String> reclamacoesPreco = List.of("caro", "valor alto", "preco", "custo elevado", "muito caro");
        List<String> reclamacoesIntegracao = List.of("nao integra", "nao comunica", "sistema separado", "nao conversa", "isolado");

        for (String sinal : reclamacoesSuporte)   { if (texto.contains(sinal)) reclamacoes.add("SUPORTE: " + sinal); }
        for (String sinal : reclamacoesProduto)   { if (texto.contains(sinal)) reclamacoes.add("PRODUTO: " + sinal); }
        for (String sinal : reclamacoesPreco)     { if (texto.contains(sinal)) reclamacoes.add("PRECO: " + sinal); }
        for (String sinal : reclamacoesIntegracao){ if (texto.contains(sinal)) reclamacoes.add("INTEGRACAO: " + sinal); }

        return reclamacoes;
    }

    public String detectarTomDeVoz() {
        String texto = reuniao.getTextoOriginal().toLowerCase();

        List<String> tomAgressivo = List.of("absurdo", "inaceitavel", "ridiculo", "nao aguento", "revoltado", "uma vergonha", "inadmissivel", "pessimo", "horrivel");
        List<String> tomPositivo  = List.of("otimo", "excelente", "maravilhoso", "perfeito", "feliz", "satisfeito", "contente", "muito bom", "aprovado");
        List<String> tomAnsioso   = List.of("urgente", "preciso logo", "quanto antes", "prazo", "atrasado", "nao pode esperar", "emergencia");

        int pontosAgressivo = 0, pontosPositivo = 0, pontosAnsioso = 0;

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