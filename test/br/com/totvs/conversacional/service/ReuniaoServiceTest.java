package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dao.ReuniaoDAO;
import br.com.totvs.conversacional.dto.ResultadoImportacao;
import br.com.totvs.conversacional.entities.Reuniao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReuniaoServiceTest {

    private ReuniaoDAO reuniaoDAO;
    private ReuniaoService reuniaoService;

    @BeforeEach
    void configurar() {
        reuniaoDAO = mock(ReuniaoDAO.class);
        reuniaoService = new ReuniaoService(reuniaoDAO);
    }

    @Test
    void deveRejeitarIdVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.buscarPorId(" ")
        );

        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveRejeitarNotaNpsAcimaDeDez() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-NPS");
        reuniao.setNotaNps(15);

        assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(reuniao)
        );

        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveRejeitarDuracaoNegativa() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-DURACAO");
        reuniao.setDuracao(-10);

        assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(reuniao)
        );

        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveBuscarReuniaoPorId() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-001");

        when(reuniaoDAO.buscarPorId("TESTE-001"))
                .thenReturn(Optional.of(reuniao));

        Optional<Reuniao> resultado =
                reuniaoService.buscarPorId("TESTE-001");

        assertTrue(resultado.isPresent());
        assertEquals("TESTE-001", resultado.get().getId());

        verify(reuniaoDAO).buscarPorId("TESTE-001");
    }

    @Test
    void deveRejeitarNotaNpsNegativa() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-NPS-NEG");
        reuniao.setNotaNps(-1);

        assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(reuniao)
        );

        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveRejeitarReuniaoNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(null)
        );

        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveAceitarNpsNosLimitesZeroEDez() {
        Reuniao zero = new Reuniao();
        zero.setId("NPS-ZERO");
        zero.setNotaNps(0);

        Reuniao dez = new Reuniao();
        dez.setId("NPS-DEZ");
        dez.setNotaNps(10);

        when(reuniaoDAO.inserir(zero)).thenReturn(true);
        when(reuniaoDAO.inserir(dez)).thenReturn(true);

        assertTrue(reuniaoService.inserir(zero));
        assertTrue(reuniaoService.inserir(dez));
    }

    @Test
    void deveInserirReuniaoValida() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-OK");
        reuniao.setDuracao(30);
        reuniao.setNotaNps(8);

        when(reuniaoDAO.inserir(reuniao)).thenReturn(true);

        assertTrue(reuniaoService.inserir(reuniao));
        verify(reuniaoDAO).inserir(reuniao);
    }

    @Test
    void deveAtualizarReuniaoValida() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-UPD");
        reuniao.setNotaNps(9);

        when(reuniaoDAO.atualizar(reuniao)).thenReturn(true);

        assertTrue(reuniaoService.atualizar(reuniao));
        verify(reuniaoDAO).atualizar(reuniao);
    }

    @Test
    void deveDeletarRemovendoEspacosDoId() {
        when(reuniaoDAO.deletar("TESTE-DEL")).thenReturn(true);

        assertTrue(reuniaoService.deletar("  TESTE-DEL  "));
        verify(reuniaoDAO).deletar("TESTE-DEL");
    }

    @Test
    void deveRejeitarSegmentoVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.listarPorSegmento("  ")
        );

        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveListarPorSegmento() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("AGRO-1");
        reuniao.setSegmento("AGRO");

        when(reuniaoDAO.listarPorSegmento("AGRO")).thenReturn(List.of(reuniao));

        List<Reuniao> resultado = reuniaoService.listarPorSegmento(" AGRO ");

        assertEquals(1, resultado.size());
        assertEquals("AGRO", resultado.get(0).getSegmento());
    }

    @Test
    void deveRejeitarUfComMaisDeDoisCaracteres() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-UF");
        reuniao.setUf("SPX");

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(reuniao)
        );

        assertEquals("A UF deve ter no máximo 2 caracteres.", erro.getMessage());
        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveRejeitarDuracaoAcimaDoLimite() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-DURACAO-MAX");
        reuniao.setDuracao(100000);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(reuniao)
        );

        assertEquals("A duração máxima é 99999 minutos.", erro.getMessage());
        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveRejeitarIdComMaisDeCemCaracteres() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("A".repeat(101));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> reuniaoService.inserir(reuniao)
        );

        assertEquals("O ID deve ter no máximo 100 caracteres.", erro.getMessage());
        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void devePreencherDataCriacaoQuandoNaoInformada() {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-DATA");

        when(reuniaoDAO.inserir(reuniao)).thenReturn(true);

        assertTrue(reuniaoService.inserir(reuniao));
        assertNotNull(reuniao.getDataCriacao());
        verify(reuniaoDAO).inserir(reuniao);
    }

    @Test
    void deveManterDataCriacaoQuandoInformada() {
        LocalDateTime data = LocalDateTime.of(2026, 10, 1, 9, 30, 0);

        Reuniao reuniao = new Reuniao();
        reuniao.setId("TESTE-DATA-FIXA");
        reuniao.setDataCriacao(data);

        when(reuniaoDAO.inserir(reuniao)).thenReturn(true);

        assertTrue(reuniaoService.inserir(reuniao));
        assertEquals(data, reuniao.getDataCriacao());
    }

    @Test
    void deveImportarLoteSeparandoValidasInvalidasEDuplicadas() {
        Reuniao ok1 = new Reuniao();
        ok1.setId("L-1");
        Reuniao ok2 = new Reuniao();
        ok2.setId("L-2");
        Reuniao invalida = new Reuniao();
        invalida.setId("L-3");
        invalida.setNotaNps(15);

        when(reuniaoDAO.inserirIgnorandoDuplicadas(anyList()))
                .thenReturn(new ReuniaoDAO.ResultadoLote(1, 1, List.of()));

        ResultadoImportacao resultado = reuniaoService.importarLote(List.of(ok1, ok2, invalida));

        assertEquals(3, resultado.recebidas());
        assertEquals(1, resultado.inseridas());
        assertEquals(1, resultado.duplicadas());
        assertEquals(1, resultado.erros().size());
        assertTrue(resultado.erros().get(0).startsWith("L-3"));
        assertNotNull(ok1.getDataCriacao());
        verify(reuniaoDAO).inserirIgnorandoDuplicadas(argThat(l -> l.size() == 2));
    }

    @Test
    void naoDeveChamarODaoQuandoTodasAsReunioesDoLoteSaoInvalidas() {
        Reuniao invalida = new Reuniao();
        invalida.setId(" ");

        ResultadoImportacao resultado = reuniaoService.importarLote(List.of(invalida));

        assertEquals(0, resultado.inseridas());
        assertEquals(1, resultado.erros().size());
        verifyNoInteractions(reuniaoDAO);
    }

    @Test
    void deveRejeitarLoteVazioOuAcimaDoLimite() {
        assertThrows(IllegalArgumentException.class, () -> reuniaoService.importarLote(List.of()));
        assertThrows(IllegalArgumentException.class, () -> reuniaoService.importarLote(null));

        List<Reuniao> grande = new java.util.ArrayList<>();
        for (int i = 0; i <= ReuniaoService.LIMITE_LOTE; i++) {
            Reuniao r = new Reuniao();
            r.setId("G-" + i);
            grande.add(r);
        }
        assertThrows(IllegalArgumentException.class, () -> reuniaoService.importarLote(grande));
        verifyNoInteractions(reuniaoDAO);
    }
}
