package br.com.totvs.conversacional.entities;

public class Cliente extends Participante {

    private double budget;
    private String tipoDecisao;
    private String satisfacao;
    private boolean mencionouConcorrente;

    public Cliente(){}

    public Cliente(String nome, String cargo, String empresa, double budget, String tipoDecisao, String satisfacao, boolean mencionouConcorrente) {
        super(nome, cargo, empresa);
        this.budget = budget;
        this.tipoDecisao = tipoDecisao;
        this.satisfacao = satisfacao;
        this.mencionouConcorrente = mencionouConcorrente;
    }

    public Cliente(double budget, String tipoDecisao, String satisfacao, boolean mencionouConcorrente) {
        this.budget = budget;
        this.tipoDecisao = tipoDecisao;
        this.satisfacao = satisfacao;
        this.mencionouConcorrente = mencionouConcorrente;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getTipoDecisao() {
        return tipoDecisao;
    }

    public void setTipoDecisao(String tipoDecisao) {
        this.tipoDecisao = tipoDecisao;
    }

    public String getSatisfacao() {
        return satisfacao;
    }

    public void setSatisfacao(String satisfacao) {
        this.satisfacao = satisfacao;
    }

    public boolean isMencionouConcorrente() {
        return mencionouConcorrente;
    }

    public void setMencionouConcorrente(boolean mencionouConcorrente) {
        this.mencionouConcorrente = mencionouConcorrente;
    }

    @Override
    public String apresentar() {
        return "Cliente: " + nome + " | Empresa: " + empresa;
    }

    @Override
    public String toString() {
        return "\n=== CLIENTE ===" +
                "\nNome: " + nome +
                "\nCargo: " + cargo +
                "\nEmpresa: " + empresa +
                "\nBudget: R$ " + budget +
                "\nTipo de Decisao: " + tipoDecisao +
                "\nSatisfacao: " + satisfacao +
                "\nMencionou Concorrente: " + mencionouConcorrente;
    }
}

