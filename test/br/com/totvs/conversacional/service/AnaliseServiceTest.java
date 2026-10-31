package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dao.ReuniaoDAO;
import br.com.totvs.conversacional.dto.AnaliseResponse;
import br.com.totvs.conversacional.entities.Reuniao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AnaliseServiceTest {

    /** DAO de mentira: devolve a reunião guardada, ou vazio. */
    private static class DaoFalso extends ReuniaoDAO {
        private final Reuniao reuniao;

        DaoFalso(Reuniao reuniao) {
            this.reuniao = reuniao;
        }

        @Override
        public List<Reuniao> listarTodas() {
            return reuniao != null ? List.of(reuniao) : List.of();
        }

        @Override
        public List<Reuniao> listarTodasComTexto() {
            return listarTodas();
        }

        @Override
        public Optional<Reuniao> buscarPorId(String id) {
            return reuniao != null && reuniao.getId().equals(id)
                    ? Optional.of(reuniao)
                    : Optional.empty();
        }
    }

    private AnaliseService servicoCom(Reuniao reuniao) {
        return new AnaliseService(new ReuniaoService(new DaoFalso(reuniao)));
    }

    @Test
    void deveAnalisarReuniaoExistente() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("A-1");
        reuniao.setNotaNps(4);
        reuniao.setTextoOriginal("Vou cancelar o contrato, o suporte demora demais.");

        Optional<AnaliseResponse> resposta = servicoCom(reuniao).analisarPorId("A-1");

        assertTrue(resposta.isPresent());
        assertEquals("A-1", resposta.get().idReuniao());
        assertEquals("ALTO", resposta.get().riscoChurn());
        assertEquals("Alto", resposta.get().riscoRotulo());
        assertFalse(resposta.get().alertas().isEmpty());
        assertFalse(resposta.get().reclamacoes().isEmpty());
        assertTrue(resposta.get().scoreGeral() >= 0 && resposta.get().scoreGeral() <= 100);
    }

    @Test
    void deveDeixarClaroQueNaoEModeloDeMl() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("A-2");
        reuniao.setTextoOriginal("Tudo certo por aqui.");

        AnaliseResponse resposta = servicoCom(reuniao).analisarPorId("A-2").orElseThrow();

        assertTrue(resposta.metodo().contains("palavra-chave"));
    }

    @Test
    void deveAvisarQuandoNaoHaTranscricaoNemNps() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("A-3");

        AnaliseResponse resposta = servicoCom(reuniao).analisarPorId("A-3").orElseThrow();

        assertEquals("BAIXO", resposta.riscoChurn());
        assertEquals(2, resposta.avisos().size());
    }

    @Test
    void deveDevolverVazioQuandoReuniaoNaoExiste() {
        assertTrue(servicoCom(null).analisarPorId("NAO-EXISTE").isEmpty());
    }

    @Test
    void deveAnalisarTodasAsReunioesDeUmaVez() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("A-4");
        reuniao.setTextoOriginal("Vamos cancelar o contrato.");

        List<AnaliseResponse> respostas = servicoCom(reuniao).analisarTodas();

        assertEquals(1, respostas.size());
        assertEquals("A-4", respostas.get(0).idReuniao());
        assertEquals("ALTO", respostas.get(0).riscoChurn());
    }

    @Test
    void deveDevolverListaVaziaQuandoNaoHaReunioes() {
        assertTrue(servicoCom(null).analisarTodas().isEmpty());
    }

    @Test
    void deveRejeitarIdVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> servicoCom(null).analisarPorId(" "));
    }
}
