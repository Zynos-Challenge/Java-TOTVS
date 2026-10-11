package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dao.ReuniaoDAO;
import br.com.totvs.conversacional.entities.Reuniao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
