package br.com.totvs.conversacional.dto;

import java.util.List;

/**
 * Resposta do endpoint POST /api/reunioes/lote.
 * recebidas = inseridas + duplicadas + (reuniões com erro).
 */
public record ResultadoImportacao(
        int recebidas,
        int inseridas,
        int duplicadas,
        List<String> erros
) {
}
