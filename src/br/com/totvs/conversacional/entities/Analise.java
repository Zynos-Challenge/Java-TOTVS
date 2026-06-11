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



    public int calcularScore() {
        int score = 50;

        if (sentimento != null) {
            if (sentimento.equals("Satisfeito")) score += 20;
            if (sentimento.equals("Insatisfeito")) score -= 20;
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

    public List<Alerta> detectarAlertas() {
        String texto = reuniao.getTextoOriginal().toLowerCase();

        // Risco de churn
        List<String> sinaisChurn = List.of(
                "cancelar", "cancelamento", "rescindir", "rescisão", "sair do contrato",
                "nao vou renovar", "não vou renovar", "encerrar contrato", "desistir"
        );
        for (String sinal : sinaisChurn) {
            if (texto.contains(sinal)) {
                alertas.add(new Alerta(
                        "CHURN",
                        "Sinal de risco de cancelamento detectado",
                        "ALTA",
                        sinal
                ));
                break; // um alerta de churn é suficiente
            }
        }


        List<String> sinaisConcorrente = List.of(
                "concorrente", "outro sistema", "outra solução", "sap", "oracle",
                "senior", "datasul", "linx", "protheus", "rm ", "fluig"
        );
        for (String sinal : sinaisConcorrente) {
            if (texto.contains(sinal)) {
                alertas.add(new Alerta(
                        "CONCORRENTE",
                        "Menção a sistema ou empresa concorrente",
                        "MEDIA",
                        sinal
                ));
                break;
            }
        }

        // Oportunidade de expansão
        List<String> sinaisOportunidade = List.of(
                "expandir", "ampliar", "novo módulo", "novo modulo", "contratar mais",
                "outros setores", "outras filiais", "crescimento", "implantar mais"
        );
        for (String sinal : sinaisOportunidade) {
            if (texto.contains(sinal)) {
                alertas.add(new Alerta(
                        "OPORTUNIDADE",
                        "Sinal de possibilidade de expansão do contrato",
                        "BAIXA",
                        sinal
                ));
                break;
            }
        }

        return alertas;
    }

    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n╔══════════════════════════════════════════╗");
        sb.append("\n║     RELATÓRIO DETALHADO - VAOX           ║");
        sb.append("\n╚══════════════════════════════════════════╝");

        // ── Dados da Reunião ──
        if (reuniao != null) {
            sb.append("\n\n┌─ DADOS DA REUNIÃO ───────────────────────");
            sb.append("\n  ID           : ").append(reuniao.getId());
            sb.append("\n  Data         : ").append(reuniao.getData() != null ? reuniao.getData() : "Não informado");
            sb.append("\n  Duração      : ").append(reuniao.getDuracao()).append(" min");
            sb.append("\n  Formato      : ").append(reuniao.getFormato() != null ? reuniao.getFormato() : "Não informado");
            sb.append("\n  Externo      : ").append(reuniao.isExterno() ? "Sim" : "Não");

            // Campos opcionais — só exibe se existirem
            if (reuniao.getUf()              != null) sb.append("\n  UF           : ").append(reuniao.getUf());
            if (reuniao.getSegmento()        != null) sb.append("\n  Segmento     : ").append(reuniao.getSegmento());
            if (reuniao.getUnidade()         != null) sb.append("\n  Unidade      : ").append(reuniao.getUnidade());
            if (reuniao.getFaixaFaturamento()!= null) sb.append("\n  Faturamento  : ").append(reuniao.getFaixaFaturamento());
            if (reuniao.getTipoRecurso()     != null) sb.append("\n  Tipo Recurso : ").append(reuniao.getTipoRecurso());
            if (reuniao.getNotaNps()         != null) sb.append("\n  NPS          : ").append(reuniao.getNotaNps()).append("/10");

            sb.append("\n└──────────────────────────────────────────");
        }

        // ── Análise de Sentimento e Score ──
        sb.append("\n\n┌─ ANÁLISE ────────────────────────────────");
        sb.append("\n  Sentimento   : ").append(sentimento != null ? sentimento : "Não detectado");
        sb.append("\n  Tom de Voz   : ").append(tomDeVoz   != null ? tomDeVoz   : "Não detectado");
        sb.append("\n  Score Geral  : ").append(scoreGeral).append("/100");

        int blocos = scoreGeral / 10;
        sb.append("\n  Score Visual : [");
        for (int i = 0; i < 10; i++) sb.append(i < blocos ? "█" : "░");
        sb.append("] ").append(scoreGeral).append("%");
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
            for (String rec : reclamacoes) {
                sb.append("\n  ✗ ").append(rec);
            }
        }
        sb.append("\n└──────────────────────────────────────────");

        sb.append("\n╔══════════════════════════════════════════╗");
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
        } else {
            this.tomDeVoz = "POSITIVO";
        }

        if (this.tomDeVoz.equals("POSITIVO")) {
            this.sentimento = "Satisfeito";
        } else if (this.tomDeVoz.equals("AGRESSIVO")) {
            this.sentimento = "Insatisfeito";
        } else {
            this.sentimento = "Ansioso";
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