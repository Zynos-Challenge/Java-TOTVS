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

    @Override
    public String toString() {
        return "\n=== HISTORICO ===" +
                "\nCliente: " + nomeCliente +
                "\nTotal de Reunioes: " + totalReunioes +
                "\nAnalises: " + analises;
    }
}
