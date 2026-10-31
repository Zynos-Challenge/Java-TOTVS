package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dao.ReuniaoDAO;
import br.com.totvs.conversacional.dto.ResultadoImportacao;
import br.com.totvs.conversacional.entities.Reuniao;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReuniaoService {

    /** Máximo de reuniões aceitas por chamada de importação (o front envia em lotes menores). */
    static final int LIMITE_LOTE = 200;

    private final ReuniaoDAO reuniaoDAO;
    private final ArmazenamentoAnalises armazenamento;

    @Autowired
    public ReuniaoService(ReuniaoDAO reuniaoDAO, ArmazenamentoAnalises armazenamento) {
        this.reuniaoDAO = reuniaoDAO;
        this.armazenamento = armazenamento;
    }

    public ReuniaoService(ReuniaoDAO reuniaoDAO) {
        this(reuniaoDAO, null);
    }

    public List<Reuniao> listarTodas() {
        return reuniaoDAO.listarTodas();
    }

    /** Somente as análises carregam as transcrições de todas as reuniões. */
    public List<Reuniao> listarTodasComTexto() {
        return reuniaoDAO.listarTodasComTexto();
    }

    public Optional<Reuniao> buscarPorId(String id) {
        validarId(id);
        return reuniaoDAO.buscarPorId(id.trim());
    }

    public boolean inserir(Reuniao reuniao) {
        validarReuniao(reuniao);

        // O DAO grava data_criacao exatamente como recebe; se o cliente não
        // enviou, o Service preenche com a data/hora atual.
        if (reuniao.getDataCriacao() == null) {
            reuniao.setDataCriacao(LocalDateTime.now());
        }

        return reuniaoDAO.inserir(reuniao);
    }

    /**
     * Importa várias reuniões de uma vez. Cada uma passa pelas mesmas validações do cadastro;
     * as inválidas viram mensagens em "erros" e as demais são gravadas. IDs repetidos são ignorados.
     */
    public ResultadoImportacao importarLote(List<Reuniao> reunioes) {
        if (reunioes == null || reunioes.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma reunião para importar.");
        }
        if (reunioes.size() > LIMITE_LOTE) {
            throw new IllegalArgumentException(
                    "Envie no máximo " + LIMITE_LOTE + " reuniões por vez."
            );
        }

        List<Reuniao> validas = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        for (Reuniao reuniao : reunioes) {
            try {
                validarReuniao(reuniao);
                if (reuniao.getDataCriacao() == null) {
                    reuniao.setDataCriacao(LocalDateTime.now());
                }
                validas.add(reuniao);
            } catch (IllegalArgumentException e) {
                String id = reuniao != null && reuniao.getId() != null ? reuniao.getId() : "(sem ID)";
                erros.add(id + ": " + e.getMessage());
            }
        }

        int inseridas = 0;
        int duplicadas = 0;
        if (!validas.isEmpty()) {
            ReuniaoDAO.ResultadoLote resultado = reuniaoDAO.inserirIgnorandoDuplicadas(validas);
            inseridas = resultado.inseridas();
            duplicadas = resultado.duplicadas();
            erros.addAll(resultado.erros());
        }

        return new ResultadoImportacao(reunioes.size(), inseridas, duplicadas, erros);
    }

    public boolean atualizar(Reuniao reuniao) {
        validarReuniao(reuniao);
        boolean atualizada = reuniaoDAO.atualizar(reuniao);
        if (atualizada && armazenamento != null) armazenamento.apagar(reuniao.getId());
        return atualizada;
    }

    public boolean deletar(String id) {
        validarId(id);
        boolean deletada = reuniaoDAO.deletar(id.trim());
        if (deletada && armazenamento != null) armazenamento.apagar(id.trim());
        return deletada;
    }

    public List<Reuniao> listarPorSegmento(String segmento) {
        if (segmento == null || segmento.isBlank()) {
            throw new IllegalArgumentException(
                    "O segmento é obrigatório."
            );
        }

        return reuniaoDAO.listarPorSegmento(segmento.trim());
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da reunião é obrigatório."
            );
        }
    }

    private void validarReuniao(Reuniao reuniao) {
        if (reuniao == null) {
            throw new IllegalArgumentException(
                    "A reunião não pode ser nula."
            );
        }

        validarId(reuniao.getId());

        if (reuniao.getId().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "O ID deve ter no máximo 100 caracteres."
            );
        }

        if (reuniao.getDuracao() != null
                && reuniao.getDuracao() < 0) {
            throw new IllegalArgumentException(
                    "A duração não pode ser negativa."
            );
        }

        Integer nps = reuniao.getNotaNps();

        if (nps != null && (nps < 0 || nps > 10)) {
            throw new IllegalArgumentException(
                    "A nota NPS deve estar entre 0 e 10."
            );
        }

        if (reuniao.getDuracao() != null
                && reuniao.getDuracao() > 99999) {
            throw new IllegalArgumentException(
                    "A duração máxima é 99999 minutos."
            );
        }

        if (reuniao.getUf() != null
                && reuniao.getUf().trim().length() > 2) {
            throw new IllegalArgumentException(
                    "A UF deve ter no máximo 2 caracteres."
            );
        }
    }
}