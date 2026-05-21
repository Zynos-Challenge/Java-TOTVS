package br.com.totvs.conversacional.services;

import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Reuniao;

import java.util.ArrayList;
import java.util.List;

public class Analisador {

    private List<Analise> analises;

    public Analisador() {
        this.analises = new ArrayList<>();
    }


    public List<Analise> getAnalises() {
        return analises;
    }

    public void setAnalises(List<Analise> analises) {
        this.analises = analises;
    }



    public List<Analise> analisarReunioes(List<Reuniao> reunioes) {
        this.analises.clear();

        for (Reuniao reuniao : reunioes) {
            Analise analise = new Analise();
            analise.setReuniao(reuniao);

            analise.detectarReclamacoes();
            analise.detectarTomDeVoz();
            analise.calcularScore();

            aplicarBonusNps(analise);

            this.analises.add(analise);
        }

        return this.analises;
    }


    private void aplicarBonusNps(Analise analise) {
        int nps = analise.getReuniao().getNotaNps();
        int score = analise.getScoreGeral();

        if (nps >= 9) {
            score += 10;
        } else if (nps <= 6) {
            score -= 10;
        }

        if (score > 100) score = 100;
        if (score < 0) score = 0;

        analise.setScoreGeral(score);
    }


    @Override
    public String toString() {
        int total = analises.size();
        int positivos = 0;
        int agressivos = 0;
        int ansiosos = 0;
        int criticos = 0;

        for (Analise a : analises) {
            String tom = a.getTomDeVoz();
            if (tom == null) continue;
            switch (tom) {
                case "POSITIVO"  -> positivos++;
                case "AGRESSIVO" -> agressivos++;
                case "ANSIOSO"   -> ansiosos++;
            }
            if (a.getScoreGeral() < 40) criticos++;
        }

        return "\n=== RESUMO GERAL ===" +
                "\nTotal de reunioes analisadas: " + total +
                "\nTom Positivo: " + positivos +
                "\nTom Agressivo: " + agressivos +
                "\nTom Ansioso: " + ansiosos +
                "\nTom Neutro: " + (total - positivos - agressivos - ansiosos) +
                "\nScore critico (abaixo de 40): " + criticos;
    }
}