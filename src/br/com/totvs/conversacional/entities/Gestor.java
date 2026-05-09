package br.com.totvs.conversacional.entities;

public class Gestor extends Participante{

    private String nivelAutoridade;
    private boolean focoROI;
    private String resistencia;

    public Gestor(){}

    public Gestor(String nome, String cargo, String empresa, String nivelAutoridade, boolean focoROI, String resistencia) {
        super(nome, cargo, empresa);
        this.nivelAutoridade = nivelAutoridade;
        this.focoROI = focoROI;
        this.resistencia = resistencia;
    }

    public Gestor(String nivelAutoridade, boolean focoROI, String resistencia) {
        this.nivelAutoridade = nivelAutoridade;
        this.focoROI = focoROI;
        this.resistencia = resistencia;
    }

    public String getNivelAutoridade() {
        return nivelAutoridade;
    }

    public void setNivelAutoridade(String nivelAutoridade) {
        this.nivelAutoridade = nivelAutoridade;
    }

    public boolean isFocoROI() {
        return focoROI;
    }

    public void setFocoROI(boolean focoROI) {
        this.focoROI = focoROI;
    }

    public String getResistencia() {
        return resistencia;
    }

    public void setResistencia(String resistencia) {
        this.resistencia = resistencia;
    }

    @Override
    public String apresentar() {
        return "Gestor: " + nome + " | Empresa: " + empresa;
    }

    @Override
    public String toString() {
        return "\n=== GESTOR ===" +
                "\nNome: " + nome +
                "\nCargo: " + cargo +
                "\nEmpresa: " + empresa +
                "\nNivel de Autoridade: " + nivelAutoridade +
                "\nFoco em ROI: " + focoROI +
                "\nResistencia: " + resistencia;
    }
}
