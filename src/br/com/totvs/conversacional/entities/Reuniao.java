package br.com.totvs.conversacional.entities;

import java.time.LocalDateTime;

public class Reuniao {

    private LocalDateTime data;
    private int duracao;
    private String textoOriginal;
    private int pFalaVendedor;

    //Campos extras do JSON
    private String segmento;
    private int notaNps;
    private String tipoRecurso;
    private String uf;
    private String faixaFaturamento;


    public Reuniao(){}

    public Reuniao(LocalDateTime data, int duracao, String textoOriginal, int pFalaVendedor, String segmento, int notaNps, String tipoRecurso, String uf, String faixaFaturamento) {
        this.data = data;
        this.duracao = duracao;
        this.textoOriginal = textoOriginal;
        this.pFalaVendedor = pFalaVendedor;
        this.segmento = segmento;
        this.notaNps = notaNps;
        this.tipoRecurso = tipoRecurso;
        this.uf = uf;
        this.faixaFaturamento = faixaFaturamento;
    }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public int getDuracao() { return duracao; }
    public void setDuracao(int duracao) { this.duracao = duracao; }

    public String getTextoOriginal() { return textoOriginal; }
    public void setTextoOriginal(String textoOriginal) { this.textoOriginal = textoOriginal; }

    public int getPFalaVendedor() { return pFalaVendedor; }
    public void setPFalaVendedor(int pFalaVendedor) { this.pFalaVendedor = pFalaVendedor; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }

    public int getNotaNps() { return notaNps; }
    public void setNotaNps(int notaNps) { this.notaNps = notaNps; }

    public String getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(String tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getFaixaFaturamento() { return faixaFaturamento; }
    public void setFaixaFaturamento(String faixaFaturamento) { this.faixaFaturamento = faixaFaturamento; }

    @Override
    public String toString() {
        return "\n=== REUNIAO ===" +
                "\nData: " + data +
                "\nDuracao: " + duracao + " min" +
                "\nPercentual Fala Vendedor: " + pFalaVendedor + "%" +
                "\nSegmento: " + segmento +
                "\nNPS: " + notaNps +
                "\nTipo Recurso: " + tipoRecurso +
                "\nUF: " + uf +
                "\nFaixa Faturamento: " + faixaFaturamento;
    }
}
