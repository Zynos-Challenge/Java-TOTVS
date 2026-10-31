package br.com.totvs.conversacional.entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnaliseTest {

    private Analise analisar(String texto, Integer nps) {
        Reuniao reuniao = new Reuniao();
        reuniao.setId("T-ANALISE");
        reuniao.setTextoOriginal(texto);
        reuniao.setNotaNps(nps);
        return new Analisador().analisarReunioes(List.of(reuniao)).get(0);
    }

    private boolean temAlerta(Analise analise, String tipo) {
        return analise.getAlertas().stream().anyMatch(a -> tipo.equals(a.getTipo()));
    }

    @Test
    void deveClassificarRiscoAltoQuandoHaAlertaDeChurn() {
        Analise analise = analisar(
                "Se continuar assim vou cancelar o contrato com voces, ja decidi.", 8);

        assertTrue(temAlerta(analise, "CHURN"));
        assertEquals(RiscoChurn.ALTO, analise.getRiscoChurn());
    }

    @Test
    void deveClassificarRiscoAltoComTomAgressivoENpsBaixo() {
        Analise analise = analisar("Isso e um absurdo, o servico e pessimo.", 5);

        assertEquals("AGRESSIVO", analise.getTomDeVoz());
        assertEquals(RiscoChurn.ALTO, analise.getRiscoChurn());
    }

    @Test
    void deveClassificarRiscoMedioQuandoMencionaConcorrente() {
        Analise analise = analisar("Estamos comparando com a SAP para o financeiro.", 9);

        assertTrue(temAlerta(analise, "CONCORRENTE"));
        assertEquals(RiscoChurn.MEDIO, analise.getRiscoChurn());
    }

    @Test
    void deveClassificarRiscoMedioComDuasOuMaisReclamacoes() {
        Analise analise = analisar("O sistema trava e o suporte demora para responder.", 9);

        assertTrue(analise.getReclamacoes().size() >= 2);
        assertEquals(RiscoChurn.MEDIO, analise.getRiscoChurn());
    }

    @Test
    void deveClassificarRiscoMedioComNpsBaixo() {
        Analise analise = analisar("Reuniao tranquila sobre o novo relatorio.", 6);

        assertEquals(RiscoChurn.MEDIO, analise.getRiscoChurn());
    }

    @Test
    void deveClassificarRiscoBaixoSemSinais() {
        Analise analise = analisar("Reuniao tranquila sobre o novo relatorio.", 9);

        assertEquals(RiscoChurn.BAIXO, analise.getRiscoChurn());
        assertEquals("NEUTRO", analise.getTomDeVoz());
        assertTrue(analise.getAlertas().isEmpty());
    }

    @Test
    void deveAceitarTextoOriginalNulo() {
        Analise analise = analisar(null, null);

        assertEquals(RiscoChurn.BAIXO, analise.getRiscoChurn());
        assertTrue(analise.getAlertas().isEmpty());
        assertTrue(analise.getReclamacoes().isEmpty());
    }

    @Test
    void deveDetectarTermosComAcentoEmTextoComAcento() {
        Analise analise = analisar("O preço está muito caro e o sistema não funciona.", 8);

        assertTrue(analise.getReclamacoes().contains("PRECO: preco"));
        assertTrue(analise.getReclamacoes().contains("PRODUTO: nao funciona"));
    }

    @Test
    void deveDetectarRescisaoComAcento() {
        Analise analise = analisar("Estamos pensando em uma rescisão antecipada.", 8);

        assertTrue(temAlerta(analise, "CHURN"));
    }

    @Test
    void naoDeveCasarTermoDentroDeOutraPalavra() {
        Analise analise = analisar("Preciso de informacao sobre o formato do arquivo do sapateiro.", 9);

        assertFalse(temAlerta(analise, "CONCORRENTE"));
    }

    @Test
    void deveCasarConcorrenteQuandoForPalavraInteira() {
        Analise analise = analisar("Hoje usamos o Oracle no financeiro.", 9);

        assertTrue(temAlerta(analise, "CONCORRENTE"));
    }

    @Test
    void naoDeveTratarProdutoDaTotvsComoConcorrente() {
        Analise analise = analisar(
                "Rodamos o Protheus, o Datasul, o RM e o Fluig sem problemas.", 9);

        assertFalse(temAlerta(analise, "CONCORRENTE"));
        assertEquals(RiscoChurn.BAIXO, analise.getRiscoChurn());
    }

    @Test
    void insatisfeitoNaoDeveContarComoPositivo() {
        Analise analise = analisar("Estou insatisfeito com a entrega.", 9);

        assertNotEquals("POSITIVO", analise.getTomDeVoz());
    }

    @Test
    void alertaDeveTrazerOTrechoQueDisparou() {
        Analise analise = analisar(
                "Conversamos bastante sobre o projeto, mas no fim o cliente disse que vai cancelar o contrato.", 8);

        Alerta alerta = analise.getAlertas().get(0);
        assertEquals("CHURN", alerta.getTipo());
        assertTrue(alerta.getTrechoOrigem().contains("cancelar o contrato"));
    }

    @Test
    void trechoLongoDeveSerRecortadoComReticencias() {
        String texto = "a".repeat(200) + " vou cancelar agora " + "b".repeat(200);
        Analise analise = analisar(texto, 8);

        String trecho = analise.getAlertas().get(0).getTrechoOrigem();
        assertTrue(trecho.startsWith("..."));
        assertTrue(trecho.endsWith("..."));
        assertTrue(trecho.length() < texto.length());
    }

    @Test
    void chamarDeteccaoDuasVezesNaoDeveDuplicarAlertas() {
        Analise analise = analisar("Vamos cancelar o contrato.", 8);

        analise.detectarAlertas();
        analise.detectarReclamacoes();

        assertEquals(1, analise.getAlertas().size());
    }

    @Test
    void deveExplicarOsMotivosDoRisco() {
        Analise analise = analisar("Vamos cancelar o contrato.", 8);

        assertFalse(analise.getMotivosRisco().isEmpty());
    }

    @Test
    void trechoNaoDeveCortarPalavraNoMeio() {
        String texto = "Conversa longa sobre implementacao e treinamento das equipes de campo. "
                + "No fim o cliente disse que vai cancelar o contrato por causa do atendimento recebido nos ultimos meses.";
        Analise analise = analisar(texto, 8);

        String trecho = analise.getAlertas().get(0).getTrechoOrigem();
        String semReticencias = trecho.replaceAll("^\\.{3}", "").replaceAll("\\.{3}$", "");
        assertTrue(texto.contains(semReticencias));
        assertTrue(trecho.startsWith("..."));
        assertFalse(semReticencias.startsWith(" "));
        for (String palavra : semReticencias.split(" ")) {
            assertTrue(texto.contains(" " + palavra + " ") || texto.startsWith(palavra + " ")
                    || texto.endsWith(" " + palavra) || texto.contains(" " + palavra + "."));
        }
    }

    @Test
    void negacaoDePalavraNegativaNaoDeveVirarTomAgressivo() {
        Analise analise = analisar("O sistema não é péssimo, na verdade está ótimo.", 9);

        assertEquals("POSITIVO", analise.getTomDeVoz());
    }

    @Test
    void elogioNegadoDeveContarComoInsatisfacao() {
        Analise analise = analisar("Não estou satisfeito com o resultado.", 9);

        assertEquals("AGRESSIVO", analise.getTomDeVoz());
        assertEquals("Insatisfeito", analise.getSentimento());
    }

    @Test
    void deveReconhecerInsatisfacaoComPalavrasAlemDaListaOriginal() {
        Analise analise = analisar("Estou muito insatisfeito, decepcionado e frustrado com vocês.", 9);

        assertEquals("AGRESSIVO", analise.getTomDeVoz());
    }

    @Test
    void prazoComumNaoDeveSerTomAnsioso() {
        Analise analise = analisar("Vamos alinhar o prazo de implantação do módulo fiscal.", 9);

        assertEquals("NEUTRO", analise.getTomDeVoz());
    }

    @Test
    void prazoApertadoDeveSerTomAnsioso() {
        Analise analise = analisar("Temos um prazo apertado e a entrega é urgente.", 9);

        assertEquals("ANSIOSO", analise.getTomDeVoz());
    }

    @Test
    void elogioAoSuporteNaoDeveVirarReclamacao() {
        Analise analise = analisar("O suporte foi excelente e o atendimento também.", 9);

        assertTrue(analise.getReclamacoes().isEmpty());
        assertEquals("POSITIVO", analise.getTomDeVoz());
        assertEquals(RiscoChurn.BAIXO, analise.getRiscoChurn());
    }

    @Test
    void reclamacaoDeSuporteContinuaSendoDetectada() {
        Analise analise = analisar("O suporte é lento e o atendimento é ruim.", 9);

        assertTrue(analise.getReclamacoes().contains("SUPORTE: suporte"));
        assertTrue(analise.getReclamacoes().contains("SUPORTE: atendimento"));
    }

    @Test
    void problemaNegadoNaoDeveVirarReclamacao() {
        Analise analise = analisar("O sistema não trava e não tem bug nenhum.", 9);

        assertTrue(analise.getReclamacoes().isEmpty());
    }

    @Test
    void cancelamentoNegadoNaoDeveGerarAlertaDeChurn() {
        Analise analise = analisar("Não vamos cancelar o contrato, estamos felizes.", 9);

        assertFalse(temAlerta(analise, "CHURN"));
        assertEquals(RiscoChurn.BAIXO, analise.getRiscoChurn());
    }

    @Test
    void deveEncontrarTermosEmMaiusculasEComAcentoDecomposto() {
        // "ÓTIMO" em maiúsculas e "satisfeito" com o acento digitado separado (NFD)
        Analise analise = analisar("ÓTIMO atendimento! Estamos muito feli\u0301zes, tudo PERFEITO.", 9);

        assertEquals("POSITIVO", analise.getTomDeVoz());
    }

    @Test
    void deveAnalisarTranscricaoGrandeSemFalharERapido() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            texto.append("Bom dia, o suporte é lento mas o time é ótimo. 😀 İstanbul. ");
        }
        long inicio = System.nanoTime();

        Analise analise = analisar(texto.toString(), 7);

        assertTrue((System.nanoTime() - inicio) / 1_000_000_000L < 20);
        assertNotNull(analise.getRiscoChurn());
        assertNotNull(analise.getTomDeVoz());
    }
}
