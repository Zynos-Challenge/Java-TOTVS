package br.com.totvs.conversacional.entities;

public abstract class Participante {

    protected String nome;
    protected String cargo;
    protected String empresa;

    public Participante() {
    }

    public Participante(String nome, String cargo, String empresa) {
        this.nome = nome;
        this.cargo = cargo;
        this.empresa = empresa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    //metodo abstrato para cada filho implementar de um jeito
    public abstract String apresentar();

    @Override
    public String toString() {
        return  "\nNome: " + nome +
                "\nCargo: " + cargo +
                "\nEmpresa: " + empresa;
    }
}
