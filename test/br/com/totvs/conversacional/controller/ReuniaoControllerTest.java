package br.com.totvs.conversacional.controller;

import br.com.totvs.conversacional.dto.ResultadoImportacao;
import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.service.ReuniaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReuniaoControllerTest {

    private static final String JSON_VALIDO =
            "{\"id\":\"T-1\",\"duracao\":30,\"segmento\":\"AGRO\",\"notaNps\":8}";

    private ReuniaoService reuniaoService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        reuniaoService = mock(ReuniaoService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReuniaoController(reuniaoService), new TesteController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void deveResponderApiOnline() throws Exception {
        mockMvc.perform(get("/api/teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    void deveListarReunioes() throws Exception {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("T-1");
        when(reuniaoService.listarTodas()).thenReturn(List.of(reuniao));

        mockMvc.perform(get("/api/reunioes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("T-1"));
    }

    @Test
    void deveRetornar200QuandoEncontrarPorId() throws Exception {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("T-1");
        when(reuniaoService.buscarPorId("T-1")).thenReturn(Optional.of(reuniao));

        mockMvc.perform(get("/api/reunioes/T-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("T-1"));
    }

    @Test
    void deveRetornar404QuandoIdNaoExiste() throws Exception {
        when(reuniaoService.buscarPorId("NAO-EXISTE")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reunioes/NAO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveFiltrarPorSegmento() throws Exception {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("T-1");
        reuniao.setSegmento("AGRO");
        when(reuniaoService.listarPorSegmento("AGRO")).thenReturn(List.of(reuniao));

        mockMvc.perform(get("/api/reunioes/segmento/AGRO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].segmento").value("AGRO"));
    }

    @Test
    void deveRetornar201AoCadastrar() throws Exception {
        when(reuniaoService.inserir(any(Reuniao.class))).thenReturn(true);

        mockMvc.perform(post("/api/reunioes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("T-1"));
    }

    @Test
    void deveRetornar409QuandoNaoInserir() throws Exception {
        when(reuniaoService.inserir(any(Reuniao.class))).thenReturn(false);

        mockMvc.perform(post("/api/reunioes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoValidacaoFalhar() throws Exception {
        when(reuniaoService.inserir(any(Reuniao.class)))
                .thenThrow(new IllegalArgumentException("A nota NPS deve estar entre 0 e 10."));

        mockMvc.perform(post("/api/reunioes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"T-1\",\"notaNps\":15}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A nota NPS deve estar entre 0 e 10."));
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido() throws Exception {
        mockMvc.perform(post("/api/reunioes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json quebrado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar204AoAtualizar() throws Exception {
        when(reuniaoService.atualizar(any(Reuniao.class))).thenReturn(true);

        mockMvc.perform(put("/api/reunioes/T-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar400QuandoIdDaUriForDiferenteDoCorpo() throws Exception {
        mockMvc.perform(put("/api/reunioes/OUTRO-ID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O ID da URI deve ser igual ao ID do corpo."));
    }

    @Test
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        when(reuniaoService.atualizar(any(Reuniao.class))).thenReturn(false);

        mockMvc.perform(put("/api/reunioes/T-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar204AoDeletar() throws Exception {
        when(reuniaoService.deletar("T-1")).thenReturn(true);

        mockMvc.perform(delete("/api/reunioes/T-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar404AoDeletarInexistente() throws Exception {
        when(reuniaoService.deletar("T-1")).thenReturn(false);

        mockMvc.perform(delete("/api/reunioes/T-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar405QuandoMetodoNaoForSuportado() throws Exception {
        mockMvc.perform(delete("/api/reunioes"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void deveImportarLoteERetornar200ComOResumo() throws Exception {
        when(reuniaoService.importarLote(any()))
                .thenReturn(new ResultadoImportacao(2, 1, 1, List.of()));

        mockMvc.perform(post("/api/reunioes/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"id\":\"L-1\"},{\"id\":\"L-2\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recebidas").value(2))
                .andExpect(jsonPath("$.inseridas").value(1))
                .andExpect(jsonPath("$.duplicadas").value(1));
    }

    @Test
    void deveRetornar400QuandoLoteForInvalido() throws Exception {
        when(reuniaoService.importarLote(any()))
                .thenThrow(new IllegalArgumentException("Nenhuma reunião para importar."));

        mockMvc.perform(post("/api/reunioes/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Nenhuma reunião para importar."));
    }
}
