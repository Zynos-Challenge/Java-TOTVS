package br.com.totvs.conversacional.dto;

import br.com.totvs.conversacional.entities.Alerta;

import java.util.List;

/**
 * Resposta do endpoint GET /api/reunioes/{id}/analise.
 * O resultado pode ser recuperado da persistência no servidor.
 */
public record AnaliseResponse(
        String idReuniao,
        String riscoChurn,
        String riscoRotulo,
        List<String> motivosRisco,
        int scoreGeral,
        String tomDeVoz,
        String sentimento,
        List<Alerta> alertas,
        List<String> reclamacoes,
        List<String> avisos,
        String metodo
) {
}
