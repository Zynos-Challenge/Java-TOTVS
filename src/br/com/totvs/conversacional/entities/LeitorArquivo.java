package br.com.totvs.conversacional.entities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LeitorArquivo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String caminhoArquivo;
    private List<Reuniao> reunioes;

    public LeitorArquivo() {
        this.caminhoArquivo = "";
        this.reunioes = new ArrayList<>();
    }

    public LeitorArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.reunioes = new ArrayList<>();
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public List<Reuniao> getReunioes() {
        return reunioes;
    }

    public void setReunioes(List<Reuniao> reunioes) {
        this.reunioes = reunioes;
    }

    public List<Reuniao> lerArquivoAutomatico() {
        this.reunioes.clear();

        // Tenta encontrar o arquivo em diferentes caminhos
        String[] caminhos = {
                "src/resources/ANON_transcricao.json",
                "resources/ANON_transcricao.json",
                "ANON_transcricao.json",
                "../src/resources/ANON_transcricao.json"
        };

        java.io.File arquivo = null;
        for (String caminho : caminhos) {
            java.io.File f = new java.io.File(caminho);
            if (f.exists()) {
                arquivo = f;
                this.caminhoArquivo = f.getAbsolutePath();
                break;
            }
        }

        if (arquivo == null) {
            // Último recurso: tenta pelo diretório do .class
            try {
                java.net.URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
                java.io.File base = new java.io.File(url.toURI()).getParentFile();
                while (base != null) {
                    java.io.File tentativa = new java.io.File(base, "src/resources/ANON_transcricao.json");
                    if (tentativa.exists()) {
                        arquivo = tentativa;
                        this.caminhoArquivo = tentativa.getAbsolutePath();
                        break;
                    }
                    tentativa = new java.io.File(base, "resources/ANON_transcricao.json");
                    if (tentativa.exists()) {
                        arquivo = tentativa;
                        this.caminhoArquivo = tentativa.getAbsolutePath();
                        break;
                    }
                    base = base.getParentFile();
                }
            } catch (Exception ignored) {}
        }

        if (arquivo == null) {
            System.err.println("Arquivo não encontrado.");
            return reunioes;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                Reuniao reuniao = parsearLinha(linha);
                if (reuniao != null) reunioes.add(reuniao);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }

        return reunioes;
    }

    private Reuniao parsearLinha(String json) {
        try {
            Reuniao r = new Reuniao();


            r.setId(extrairString(json, "ID_MEETING"));
            r.setTextoOriginal(extrairString(json, "ANON_TRANSCRICAO"));
            r.setFormato(extrairString(json, "FORMATO_MEETING"));
            r.setStatus(extrairString(json, "STATUS_MEETING"));
            r.setCodt(extrairString(json, "CODT"));
            r.setExterno(extrairBoolean(json, "FLG_EXTERNO"));

            String dtMeeting = extrairString(json, "DT_MEETING");
            if (dtMeeting != null && !dtMeeting.isEmpty())
                r.setData(LocalDateTime.parse(dtMeeting, FMT));

            String dtCriacao = extrairString(json, "DT_CRIACAO");
            if (dtCriacao != null && !dtCriacao.isEmpty())
                r.setDataCriacao(LocalDateTime.parse(dtCriacao, FMT));

            String durStr = extrairString(json, "DURACAO_MEETING");
            if (durStr != null && !durStr.isEmpty()) {
                String[] p = durStr.split(":");
                int h = Integer.parseInt(p[0]);
                int m = Integer.parseInt(p[1]);
                int s = p.length > 2 ? Integer.parseInt(p[2]) : 0;
                r.setDuracao((h * 60) + m + (s >= 30 ? 1 : 0));
            }


            r.setSegmento(extrairOpcional(json, "NOME_SEGMENTO"));
            r.setUnidade(extrairOpcional(json, "NOME_UNIDADE"));
            r.setCnae(extrairOpcional(json, "CNAE"));
            r.setUf(extrairOpcional(json, "UF"));
            r.setFaixaFaturamento(extrairOpcional(json, "FAIXA_FATURAMENTO_CLIENTE_EC"));
            r.setTipoRecurso(extrairOpcional(json, "TP_RECURSO"));


            String npsStr = extrairOpcional(json, "NOTA_NPS");
            if (npsStr != null && !npsStr.isEmpty()) {
                r.setNotaNps((int) Double.parseDouble(npsStr));
            }

            return r;

        } catch (Exception e) {
            System.err.println("Erro ao parsear linha: " + e.getMessage());
            return null;
        }
    }


    private String extrairString(String json, String chave) {
        String v = extrairRaw(json, chave);
        return v != null ? v : "";
    }


    private String extrairOpcional(String json, String chave) {
        return extrairRaw(json, chave);
    }


    private boolean extrairBoolean(String json, String chave) {
        String busca = "\"" + chave + "\"";
        int idx = json.indexOf(busca);
        if (idx == -1) return false;
        int inicio = json.indexOf(":", idx) + 1;
        while (inicio < json.length() && json.charAt(inicio) == ' ') inicio++;
        return json.startsWith("true", inicio);
    }


    private String extrairRaw(String json, String chave) {
        String busca = "\"" + chave + "\"";
        int idx = json.indexOf(busca);
        if (idx == -1) return null;   // chave ausente = campo não existe neste registro

        int inicio = json.indexOf(":", idx) + 1;
        while (inicio < json.length() && json.charAt(inicio) == ' ') inicio++;

        if (json.startsWith("null", inicio)) return null;

        if (json.charAt(inicio) == '"') {
            StringBuilder sb = new StringBuilder();
            int i = inicio + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '"' && json.charAt(i - 1) != '\\') break;
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case 'n'  -> { sb.append('\n'); i += 2; continue; }
                        case 't'  -> { sb.append('\t'); i += 2; continue; }
                        case '"'  -> { sb.append('"');  i += 2; continue; }
                        case '\\' -> { sb.append('\\'); i += 2; continue; }
                    }
                }
                sb.append(c);
                i++;
            }
            return sb.toString();
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