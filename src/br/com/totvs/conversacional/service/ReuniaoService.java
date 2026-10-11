package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dao.ReuniaoDAO;
import br.com.totvs.conversacional.entities.Reuniao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReuniaoService {

    private final ReuniaoDAO reuniaoDAO;

    public ReuniaoService(ReuniaoDAO reuniaoDAO) {
        this.reuniaoDAO = reuniaoDAO;
    }

    public List<Reuniao> listarTodas() {
        return reuniaoDAO.listarTodas();
    }

    public Optional<Reuniao> buscarPorId(String id) {
        validarId(id);
        return reuniaoDAO.buscarPorId(id.trim());
    }

    public boolean inserir(Reuniao reuniao) {
        validarReuniao(reuniao);
        return reuniaoDAO.inserir(reuniao);
    }

    public boolean atualizar(Reuniao reuniao) {
        validarReuniao(reuniao);
        return reuniaoDAO.atualizar(reuniao);
    }

    public boolean deletar(String id) {
        validarId(id);
        return reuniaoDAO.deletar(id.trim());
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
    }
}