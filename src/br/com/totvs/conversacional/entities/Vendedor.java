package br.com.totvs.conversacional.entities;

public class Vendedor extends Participante {

    private double metaVendas;
    private int tempoFala;
    private int confiancaCliente;

    public Vendedor() {
    }

    public Vendedor(double metaVendas, int tempoFala, int confiancaCliente) {
        this.metaVendas = metaVendas;
        this.tempoFala = tempoFala;
        this.confiancaCliente = confiancaCliente;
    }

    public Vendedor(String nome, String cargo, String empresa, double metaVendas, int tempoFala, int confiancaCliente) {
        super(nome, cargo, empresa);
        this.metaVendas = metaVendas;
        this.tempoFala = tempoFala;
        this.confiancaCliente = confiancaCliente;
    }

    public double getMetaVendas() {
        return metaVendas;
    }

    public void setMetaVendas(double metaVendas) {
        this.metaVendas = metaVendas;
    }

    public int getTempoFala() {
        return tempoFala;
    }

    public void setTempoFala(int tempoFala) {
        this.tempoFala = tempoFala;
    }

    public int getConfiancaCliente() {
        return confiancaCliente;
    }

    public void setConfiancaCliente(int confiancaCliente) {
        this.confiancaCliente = confiancaCliente;
    }

    @Override
    public String apresentar() {
        return "Vendedor: " + nome + " | Empresa: " + empresa;
    }

    @Override
    public String toString() {
        return "\n=== VENDEDOR ===" +
                "\nNome: " + nome +
                "\nCargo: " + cargo +
                "\nEmpresa: " + empresa +
                "\nMeta de Vendas: " + metaVendas +
                "\nTempo de Fala: " + tempoFala + "s" +
                "\nConfianca do Cliente: " + confiancaCliente;
    }
}

