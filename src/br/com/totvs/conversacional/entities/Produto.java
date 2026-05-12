package br.com.totvs.conversacional.entities;

public class Produto {

    public String nome;
    public String categoria;
    public String status;
    public String versaoMencao;

    public Produto(){}

    public Produto(String nome, String categoria, String status, String versaoMencao) {
        this.nome = nome;
        this.categoria = categoria;
        this.status = status;
        this.versaoMencao = versaoMencao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersaoMencao() {
        return versaoMencao;
    }

    public void setVersaoMencao(String versaoMencao) {
        this.versaoMencao = versaoMencao;
    }

    @Override
    public String toString() {
        return "\n=== PRODUTO ===" +
                "\nNome: " + nome +
                "\nCategoria: " + categoria +
                "\nStatus: " + status +
                "\nVersao Mencao: " + versaoMencao;
    }
}
