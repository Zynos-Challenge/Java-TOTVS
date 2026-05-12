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

    @Override
    public String toString() {
        return "\n=== ANALISE ===" +
                "\nCliente: " + cliente.getNome() +
                "\nSentimento: " + sentimento +
                "\nTom de Voz: " + tomDeVoz +
                "\nScore Geral: " + scoreGeral +
                "\nProdutos Identificados: " + produtos +
                "\nAlertas: " + alertas +
                "\nReclamacoes: " + reclamacoes;
    }

}
