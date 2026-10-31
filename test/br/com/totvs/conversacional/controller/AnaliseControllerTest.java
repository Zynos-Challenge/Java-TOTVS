package br.com.totvs.conversacional.controller;

import br.com.totvs.conversacional.dto.AnaliseResponse;
import br.com.totvs.conversacional.entities.Alerta;
import br.com.totvs.conversacional.service.AnaliseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnaliseControllerTest {

    private AnaliseService analiseService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        analiseService = mock(AnaliseService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AnaliseController(analiseService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar200ComAAnalise() throws Exception {
        AnaliseResponse resposta = new AnaliseResponse(
                "T-1", "ALTO", "Alto",
                List.of("Cliente sinalizou intenção de cancelar ou não renovar o contrato."),
                35, "AGRESSIVO", "Insatisfeito",
                List.of(new Alerta("CHURN", "Sinal de risco", "ALTA", "vou cancelar o contrato")),
                List.of("SUPORTE: suporte"),
                List.of(),
                "Regras por palavra-chave");
        when(analiseService.analisarPorId("T-1")).thenReturn(Optional.of(resposta));

        mockMvc.perform(get("/api/reunioes/T-1/analise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riscoChurn").value("ALTO"))
                .andExpect(jsonPath("$.riscoRotulo").value("Alto"))
                .andExpect(jsonPath("$.alertas[0].tipo").value("CHURN"))
                .andExpect(jsonPath("$.alertas[0].trechoOrigem").value("vou cancelar o contrato"))
                .andExpect(jsonPath("$.scoreGeral").value(35));
    }

    @Test
    void deveRetornar200ComAnaliseDeTodasAsReunioes() throws Exception {
        AnaliseResponse a = new AnaliseResponse("T-1", "ALTO", "Alto", List.of(), 35,
                "AGRESSIVO", "Insatisfeito", List.of(), List.of(), List.of(), "Regras por palavra-chave");
        AnaliseResponse b = new AnaliseResponse("T-2", "BAIXO", "Baixo", List.of(), 70,
                "POSITIVO", "Satisfeito", List.of(), List.of(), List.of(), "Regras por palavra-chave");
        when(analiseService.analisarTodas()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/analises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idReuniao").value("T-1"))
                .andExpect(jsonPath("$[1].riscoChurn").value("BAIXO"));
    }

    @Test
    void deveRetornarSomenteIdRiscoETomNoResumo() throws Exception {
        var resumo = new br.com.totvs.conversacional.dto.ResumoAnaliseResponse("T-1", "ALTO", "AGRESSIVO");
        when(analiseService.analisarTodasResumidas()).thenReturn(List.of(resumo));
        mockMvc.perform(get("/api/analises/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReuniao").value("T-1"))
                .andExpect(jsonPath("$[0].riscoChurn").value("ALTO"))
                .andExpect(jsonPath("$[0].tomDeVoz").value("AGRESSIVO"))
                .andExpect(jsonPath("$[0].alertas").doesNotExist());
    }

    @Test
    void deveRetornar404QuandoReuniaoNaoExiste() throws Exception {
        when(analiseService.analisarPorId("X")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reunioes/X/analise"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoIdForInvalido() throws Exception {
        when(analiseService.analisarPorId(" "))
                .thenThrow(new IllegalArgumentException("O ID da reunião é obrigatório."));

        mockMvc.perform(get("/api/reunioes/{id}/analise", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O ID da reunião é obrigatório."));
    }
}
