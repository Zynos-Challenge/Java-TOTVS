package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dto.AnaliseResponse;
import br.com.totvs.conversacional.entities.Reuniao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ArmazenamentoAnalisesTest {

    @TempDir Path pasta;

    private static AnaliseResponse resposta(Reuniao reuniao) {
        return new AnaliseResponse(reuniao.getId(), "BAIXO", "Baixo", List.of(),
                75, "NEUTRO", "Neutro", List.of(), List.of(), List.of(), "Regras");
    }

    @Test
    void reutilizaAnaliseSalvaMesmoAposReiniciarEInvalidaSeTextoOuNpsMudar() throws Exception {
        Reuniao r = new Reuniao();
        r.setId("R/1:inseguro");
        r.setTextoOriginal("Transcricao original");
        r.setNotaNps(8);

        AtomicInteger calculos = new AtomicInteger();
        ArmazenamentoAnalises armazenamento = new ArmazenamentoAnalises(pasta.toString(), new ObjectMapper());
        var calcular = (java.util.function.Supplier<AnaliseResponse>) () -> {
            calculos.incrementAndGet();
            return resposta(r);
        };

        armazenamento.obterOuCalcular(r, calcular);
        armazenamento.obterOuCalcular(r, calcular);
        assertEquals(1, calculos.get());

        // Novo objeto simula outra execução do backend, sem cache em memória.
        armazenamento = new ArmazenamentoAnalises(pasta.toString(), new ObjectMapper());
        armazenamento.obterOuCalcular(r, calcular);
        assertEquals(1, calculos.get());

        r.setNotaNps(2);
        armazenamento.obterOuCalcular(r, calcular);
        assertEquals(2, calculos.get());
        r.setTextoOriginal("Texto alterado");
        armazenamento.obterOuCalcular(r, calcular);
        assertEquals(3, calculos.get());

        try (var arquivos = Files.list(pasta)) {
            assertEquals(1, arquivos.count());
        }
        armazenamento.apagar(r.getId());
        try (var arquivos = Files.list(pasta)) {
            assertEquals(0, arquivos.count());
        }
        armazenamento.obterOuCalcular(r, calcular);
        assertEquals(4, calculos.get());
    }
}
