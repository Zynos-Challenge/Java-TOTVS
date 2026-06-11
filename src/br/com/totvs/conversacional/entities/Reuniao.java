package br.com.totvs.conversacional.entities;

import java.time.LocalDateTime;

public class Reuniao {

    private String id;
    private LocalDateTime data;
    private LocalDateTime dataCriacao;
    private int duracao;
    private String textoOriginal;
    private String formato;
    private String status;
    private String codt;
    private boolean externo;


    private String segmento;
    private String unidade;
    private String cnae;
    private String uf;
    private String faixaFaturamento;
    private String tipoRecurso;
    private Integer notaNps;

    public Reuniao() {}




    public Integer getNotaNps() {
        return notaNps;
    }

    public void setNotaNps(Integer notaNps) {
        this.notaNps = notaNps;
    }

    public String getTipoRecurso() {
        return tipoRecurso;
    }

    public void setTipoRecurso(String tipoRecurso) {
        this.tipoRecurso = tipoRecurso;
    }

    public String getFaixaFaturamento() {
        return faixaFaturamento;
    }

    public void setFaixaFaturamento(String faixaFaturamento) {
        this.faixaFaturamento = faixaFaturamento;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCnae() {
        return cnae;
    }

    public void setCnae(String cnae) {
        this.cnae = cnae;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public boolean isExterno() {
        return externo;
    }

    public void setExterno(boolean externo) {
        this.externo = externo;
    }

    public String getCodt() {
        return codt;
    }

    public void setCodt(String codt) {
        this.codt = codt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public void setTextoOriginal(String textoOriginal) {
        this.textoOriginal = textoOriginal;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "\n=== REUNIAO ===" +
                "\nID: " + id +
                "\nData: " + data +
                "\nDuracao: " + duracao + " min" +
                "\nFormato: " + formato +
                "\nStatus: " + status +
                (segmento != null ? "\nSegmento: " + segmento : "") +
                (unidade  != null ? "\nUnidade: "  + unidade  : "") +
                (uf       != null ? "\nUF: "       + uf       : "") +
                (faixaFaturamento != null ? "\nFaixa Faturamento: " + faixaFaturamento : "") +
                (tipoRecurso     != null ? "\nTipo Recurso: " + tipoRecurso : "") +
                (notaNps         != null ? "\nNPS: " + notaNps + "/10" : "");
    }
}