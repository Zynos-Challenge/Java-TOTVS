package br.com.totvs.conversacional.entities;

import java.time.LocalDateTime;

public class Reuniao {

    private LocalDateTime data;
    private int duracao;
    private String textoOriginal;
    private int pFalaVendedor;

    public Reuniao(){}

    public Reuniao(LocalDateTime data, int duracao, String textoOriginal, int pFalaVendedor) {
        this.data = data;
        this.duracao = duracao;
        this.textoOriginal = textoOriginal;
        this.pFalaVendedor = pFalaVendedor;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public void setTextoOriginal(String textoOriginal) {
        this.textoOriginal = textoOriginal;
    }

    public int getpFalaVendedor() {
        return pFalaVendedor;
    }

    public void setpFalaVendedor(int pFalaVendedor) {
        this.pFalaVendedor = pFalaVendedor;
    }

    @Override
    public String toString() {
        return "\n=== REUNIAO ===" +
                "\nData: " + data +
                "\nDuracao: " + duracao + " min" +
                "\nPercentual Fala Vendedor: " + pFalaVendedor + "%" +
                "\nTexto Original: " + textoOriginal;
    }
}
