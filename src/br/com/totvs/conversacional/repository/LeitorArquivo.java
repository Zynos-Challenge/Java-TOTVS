package br.com.totvs.conversacional.repository;

import br.com.totvs.conversacional.entities.Reuniao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LeitorArquivo {

    private String caminhoArquivo;
    private List<Reuniao> reunioes;


    public LeitorArquivo(){

    }

    public LeitorArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.reunioes = new ArrayList<>();
    }



    public String getCaminhoArquivo() { return caminhoArquivo; }
    public void setCaminhoArquivo(String caminhoArquivo) { this.caminhoArquivo = caminhoArquivo; }

    public List<Reuniao> getReunioes() { return reunioes; }
    public void setReunioes(List<Reuniao> reunioes) { this.reunioes = reunioes; }



    public List<Reuniao> lerArquivo() {
        this.reunioes.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                Reuniao reuniao = parsearLinha(linha);
                if (reuniao != null) {
                    reunioes.add(reuniao);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }

        return reunioes;
    }



    private Reuniao parsearLinha(String json) {
        try {
            Reuniao reuniao = new Reuniao();

            reuniao.setTextoOriginal(extrairValor(json, "ANON_TRANSCRICAO"));
            reuniao.setSegmento(extrairValor(json, "NOME_SEGMENTO"));
            reuniao.setUf(extrairValor(json, "UF"));
            reuniao.setFaixaFaturamento(extrairValor(json, "FAIXA_FATURAMENTO_CLIENTE_EC"));
            reuniao.setTipoRecurso(extrairValor(json, "TP_RECURSO"));

            // Duração
            String duracaoStr = extrairValor(json, "DURACAO_MEETING");
            if (duracaoStr != null && !duracaoStr.isEmpty()) {
                reuniao.setDuracao((int) Double.parseDouble(duracaoStr));
            }

            // NPS
            String npsStr = extrairValor(json, "NOTA_NPS");
            if (npsStr != null && !npsStr.isEmpty()) {
                reuniao.setNotaNps((int) Double.parseDouble(npsStr));
            }

            // Data
            String dataStr = extrairValor(json, "DT_MEETING");
            if (dataStr != null && !dataStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                reuniao.setData(LocalDateTime.parse(dataStr, formatter));
            }

            return reuniao;

        } catch (Exception e) {
            System.err.println("Erro ao parsear linha: " + e.getMessage());
            return null;
        }
    }



    private String extrairValor(String json, String chave) {
        String busca = "\"" + chave + "\"";
        int idx = json.indexOf(busca);
        if (idx == -1) return "";

        int inicio = json.indexOf(":", idx) + 1;


        while (inicio < json.length() && json.charAt(inicio) == ' ') inicio++;


        if (json.startsWith("null", inicio)) return "";


        if (json.charAt(inicio) == '"') {
            int fim = inicio + 1;
            while (fim < json.length()) {
                if (json.charAt(fim) == '"' && json.charAt(fim - 1) != '\\') break;
                fim++;
            }
            return json.substring(inicio + 1, fim);
        }

        int fim = inicio;
        while (fim < json.length() && json.charAt(fim) != ',' && json.charAt(fim) != '}') fim++;
        return json.substring(inicio, fim).trim();
    }


    @Override
    public String toString() {
        return "\n=== LEITOR ARQUIVO ===" +
                "\nCaminho: " + caminhoArquivo +
                "\nReunioes carregadas: " + reunioes.size();
    }
}
