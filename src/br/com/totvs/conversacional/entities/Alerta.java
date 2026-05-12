package br.com.totvs.conversacional.entities;

public class Alerta {

    private String tipo;
    private String descricao;
    private String urgencia;
    private String trechoOrigem;

    public Alerta(){}

    public Alerta(String tipo, String descricao, String urgencia, String trechoOrigem) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.urgencia = urgencia;
        this.trechoOrigem = trechoOrigem;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public String getTrechoOrigem() {
        return trechoOrigem;
    }

    public void setTrechoOrigem(String trechoOrigem) {
        this.trechoOrigem = trechoOrigem;
    }

    @Override
    public String toString() {
        return "\n=== ALERTA ===" +
                "\nTipo: " + tipo +
                "\nDescricao: " + descricao +
                "\nUrgencia: " + urgencia +
                "\nTrecho de Origem: " + trechoOrigem;
    }
}
