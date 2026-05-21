package br.com.totvs.conversacional.services;

import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.interfaces.IAnalisavel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AnalisadorIA implements IAnalisavel {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    private Reuniao reuniao;
    private Analise analise;

    public AnalisadorIA() {}

    public AnalisadorIA(Reuniao reuniao) {
        this.reuniao = reuniao;
        this.analise = new Analise();
        this.analise.setReuniao(reuniao);
    }

    public Reuniao getReuniao() { return reuniao; }
    public void setReuniao(Reuniao reuniao) { this.reuniao = reuniao; }

    public Analise getAnalise() { return analise; }
    public void setAnalise(Analise analise) { this.analise = analise; }

    // ───── IAnalisavel ─────

    @Override
    public int calcularScore() {
        String prompt = """
                Você é um analista de vendas da TOTVS.
                Com base na transcrição abaixo, retorne APENAS um número inteiro de 0 a 100
                representando o score geral da reunião (0 = péssima, 100 = excelente).
                Não escreva mais nada além do número.
                
                Transcrição:
                """ + truncarTexto(reuniao.getTextoOriginal(), 3000);

        String resposta = chamarGemini(prompt);

        try {
            int score = Integer.parseInt(resposta.trim());
            if (score < 0) score = 0;
            if (score > 100) score = 100;
            analise.setScoreGeral(score);
            return score;
        } catch (NumberFormatException e) {
            analise.setScoreGeral(50);
            return 50;
        }
    }

    @Override
    public String gerarRelatorio() {
        String prompt = """
                Você é um analista de vendas da TOTVS.
                Analise a transcrição abaixo e gere um relatório estruturado contendo:
                - Sentimento geral (POSITIVO, NEUTRO ou NEGATIVO)
                - Tom de voz (POSITIVO, NEUTRO, ANSIOSO ou AGRESSIVO)
                - Produtos TOTVS mencionados (Protheus, RM, Fluig, TOTVS CRM, Datasul)
                - Concorrentes mencionados (SAP, Senior, Linx, Oracle)
                - Alertas identificados (CHURN, CONCORRENTE, OPORTUNIDADE)
                - Reclamações (SUPORTE, PRODUTO, PRECO, INTEGRACAO)
                - Resumo executivo em 3 linhas
                
                Segmento: """ + reuniao.getSegmento() + """
                
                UF: """ + reuniao.getUf() + """
                
                NPS: """ + reuniao.getNotaNps() + """
                
                Transcrição:
                """ + truncarTexto(reuniao.getTextoOriginal(), 3000);

        return chamarGemini(prompt);
    }

    // ───── Chamada HTTP para o Gemini ─────

    private String chamarGemini(String prompt) {
        try {
            String corpo = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": "%s"
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(escaparJson(prompt));

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return extrairResposta(response.body());

        } catch (Exception e) {
            return "Erro ao chamar IA: " + e.getMessage();
        }
    }

    // ───── Extrai o texto da resposta JSON do Gemini ─────

    private String extrairResposta(String json) {
        String marcador = "\"text\": \"";
        int inicio = json.indexOf(marcador);
        if (inicio == -1) return "Sem resposta da IA.";

        inicio += marcador.length();
        int fim = json.indexOf("\"", inicio);
        if (fim == -1) return "Erro ao parsear resposta.";

        return json.substring(inicio, fim)
                .replace("\\n", "\n")
                .replace("\\t", "\t");
    }

    // ───── Utilitários ─────

    private String truncarTexto(String texto, int limite) {
        if (texto == null) return "";
        return texto.length() > limite ? texto.substring(0, limite) + "..." : texto;
    }

    private String escaparJson(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return "\n=== ANALISADOR IA ===" +
                "\nModelo: Gemini 1.5 Flash" +
                "\nReuniao: " + (reuniao != null ? reuniao.getData() : "N/A") +
                "\nScore: " + (analise != null ? analise.getScoreGeral() : "N/A");
    }
}