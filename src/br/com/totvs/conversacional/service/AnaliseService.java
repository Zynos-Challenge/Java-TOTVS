package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dto.AnaliseResponse;
import br.com.totvs.conversacional.dto.ResumoAnaliseResponse;
import br.com.totvs.conversacional.entities.Analise;
import br.com.totvs.conversacional.entities.Analisador;
import br.com.totvs.conversacional.entities.Reuniao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Analisa uma reunião (risco de churn, alertas, reclamações, tom de voz e score)
 * usando o Analisador. Os resultados são salvos em disco e reutilizados
 * até a transcrição, o NPS ou a versão das regras mudar.
 */
@Service
public class AnaliseService {

    static final String METODO =
            "Regras por palavra-chave aplicadas ao texto da reunião; não é modelo estatístico nem de machine learning.";

    private static final Logger logger = LoggerFactory.getLogger(AnaliseService.class);

    private final ReuniaoService reuniaoService;
    private final ArmazenamentoAnalises armazenamento;

    @Autowired
    public AnaliseService(ReuniaoService reuniaoService, ArmazenamentoAnalises armazenamento) {
        this.reuniaoService = reuniaoService;
        this.armazenamento = armazenamento;
    }

    // Construtor usado pelos testes de regra de negócio (sem acesso ao disco).
    public AnaliseService(ReuniaoService reuniaoService) {
        this(reuniaoService, null);
    }

    public Optional<AnaliseResponse> analisarPorId(String id) {
        return reuniaoService.buscarPorId(id).map(this::analisarComCache);
    }

    /**
     * Uma consulta ao banco traz os textos somente no servidor. Processamento
     * sequencial evita saturar os núcleos de CPU quando chegam muitos pedidos;
     * arquivos já salvos são reutilizados. Falhas individuais não derrubam o lote.
     */
    public List<AnaliseResponse> analisarTodas() {
        return reuniaoService.listarTodasComTexto().stream()
                .map(this::analisarSemFalhar)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Mesmo conjunto, mas só os campos exibidos no painel (resposta muito menor). */
    public List<ResumoAnaliseResponse> analisarTodasResumidas() {
        return reuniaoService.listarTodasComTexto().stream()
                .map(this::analisarSemFalhar)
                .filter(Objects::nonNull)
                .map(ResumoAnaliseResponse::de)
                .toList();
    }

    private AnaliseResponse analisarSemFalhar(Reuniao reuniao) {
        try {
            return analisarComCache(reuniao);
        } catch (RuntimeException e) {
            logger.warn("Nao foi possivel analisar a reuniao {}: {}",
                    reuniao == null ? null : reuniao.getId(), e.toString());
            return null;
        }
    }

    private AnaliseResponse analisarComCache(Reuniao reuniao) {
        return armazenamento == null
                ? analisar(reuniao)
                : armazenamento.obterOuCalcular(reuniao, () -> analisar(reuniao));
    }

    AnaliseResponse analisar(Reuniao reuniao) {
        Analisador analisador = new Analisador();
        Analise analise = analisador.analisarReunioes(List.of(reuniao)).get(0);

        List<String> avisos = new ArrayList<>();
        String texto = reuniao.getTextoOriginal();
        if (texto == null || texto.isBlank()) {
            avisos.add("Reunião sem transcrição: a análise só considera o NPS.");
        }
        if (reuniao.getNotaNps() == null) {
            avisos.add("Reunião sem NPS informado: as regras de NPS não foram aplicadas.");
        }

        return new AnaliseResponse(
                reuniao.getId(),
                analise.getRiscoChurn().name(),
                analise.getRiscoChurn().getRotulo(),
                analise.getMotivosRisco(),
                analise.getScoreGeral(),
                analise.getTomDeVoz(),
                analise.getSentimento(),
                analise.getAlertas(),
                analise.getReclamacoes(),
                avisos,
                METODO
        );
    }
}
