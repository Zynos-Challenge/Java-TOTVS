package br.com.totvs.conversacional.entities;

import java.util.ArrayList;
import java.util.List;

public class Historico {

    private String nomeCliente;
    private int totalReunioes;
    private List<Analise> analises;

    public Historico() {
        this.analises = new ArrayList<>();
    }

    public Historico(String nomeCliente) {
        this.nomeCliente = nomeCliente;
        this.totalReunioes = 0;
        this.analises = new ArrayList<>();
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public int getTotalReunioes() {
        return totalReunioes;
    }

    public void setTotalReunioes(int totalReunioes) {
        this.totalReunioes = totalReunioes;
    }

    public List<Analise> getAnalises() {
        return analises;
    }

    public void setAnalises(List<Analise> analises) {
        this.analises = analises;
    }

    //métodos:
    // adiciona uma analise ao historico
    public void adicionarAnalise(Analise analise) {
        this.analises.add(analise);
        this.totalReunioes++;
    }
    // compara as reunioes do historico
    public String compararReunioes() {
        if (analises.isEmpty()) {
            return "Nenhuma reuniao registrada ainda.";
        }

        StringBuilder comparacao = new StringBuilder();
        comparacao.append("\n=== COMPARATIVO DE REUNIOES ===");
        comparacao.append("\nCliente: ").append(nomeCliente);

        for (int i = 0; i < analises.size(); i++) {
            Analise analiseAtual = analises.get(i);
            comparacao.append("\n\nReuniao ").append(i + 1);
            comparacao.append("\n  Data: ").append(analiseAtual.getReuniao().getData());
            comparacao.append("\n  Sentimento: ").append(analiseAtual.getSentimento());
            comparacao.append("\n  Score: ").append(analiseAtual.getScoreGeral());
            comparacao.append("\n  Alertas: ").append(analiseAtual.getAlertas().size());
        }
        return comparacao.toString();
    }
    // mostra tendencia do sentimento ao longo das reunioes
    public String tendenciaSentimento() {
        if (analises.isEmpty()) {
            return "Nenhuma reuniao registrada ainda.";
        }
        int positivos = 0;
        int negativos = 0;

        for (Analise analiseAtual : analises) {
            if (analiseAtual.getSentimento().equals("POSITIVO")) positivos++;
            if (analiseAtual.getSentimento().equals("NEGATIVO")) negativos++;
        }
        if (positivos > negativos) {
            return "TENDENCIA POSITIVA - cliente evoluindo bem!";
        } else if (negativos > positivos) {
            return "TENDENCIA NEGATIVA - atencao com esse cliente!";
        } else {
            return "TENDENCIA NEUTRA - cliente estavel.";
        }
    }

        @Override
    public String toString() {
        return "\n=== HISTORICO ===" +
                "\nCliente: " + nomeCliente +
                "\nTotal de Reunioes: " + totalReunioes +
                "\nAnalises: " + analises;
    }
}
