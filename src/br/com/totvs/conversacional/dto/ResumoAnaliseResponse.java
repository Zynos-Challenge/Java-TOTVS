package br.com.totvs.conversacional.dto;

/** Resposta leve usada só pela tabela, sem motivos, alertas e transcrição. */
public record ResumoAnaliseResponse(String idReuniao, String riscoChurn, String tomDeVoz) {
    public static ResumoAnaliseResponse de(AnaliseResponse completa) {
        return new ResumoAnaliseResponse(completa.idReuniao(), completa.riscoChurn(), completa.tomDeVoz());
    }
}
