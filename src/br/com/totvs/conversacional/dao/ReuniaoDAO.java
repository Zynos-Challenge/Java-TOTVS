package br.com.totvs.conversacional.dao;

import br.com.totvs.conversacional.conexoes.ConnectionFactory;
import br.com.totvs.conversacional.entities.Reuniao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReuniaoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO reuniao (id_reuniao, data, data_criacao, duracao, texto_original, " +
                    "formato, status, codt, externo, segmento, unidade, cnae, uf, " +
                    "faixa_faturamento, tipo_recurso, nota_nps) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM reuniao WHERE id_reuniao = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT * FROM reuniao ORDER BY data DESC";

    private static final String SQL_SELECT_BY_SEGMENTO =
            "SELECT * FROM reuniao WHERE UPPER(segmento) = UPPER(?) ORDER BY data DESC";

    private static final String SQL_UPDATE =
            "UPDATE reuniao SET data = ?, duracao = ?, texto_original = ?, formato = ?, status = ?, " +
                    "segmento = ?, unidade = ?, uf = ?, nota_nps = ? WHERE id_reuniao = ?";

    private static final String SQL_DELETE =
            "DELETE FROM reuniao WHERE id_reuniao = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM reuniao";

    private static final String SQL_SELECT_ALTO_RISCO =
            "SELECT r.* FROM reuniao r " +
                    "INNER JOIN analise a ON r.id_reuniao = a.id_reuniao " +
                    "WHERE a.churn_risco >= ? ORDER BY a.churn_risco DESC";


    public boolean inserir(Reuniao reuniao) {
        if (reuniao == null || reuniao.getId() == null) {
            System.err.println("[ReuniaoDAO] Reuniao invalida para insercao.");
            return false;
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            mapearParaStatement(ps, reuniao);
            ps.executeUpdate();
            System.out.println("[ReuniaoDAO] Reuniao inserida: " + reuniao.getId());
            return true;

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao inserir: " + e.getMessage());
            return false;
        }
    }

    public int inserirLote(List<Reuniao> reunioes) {
        if (reunioes == null || reunioes.isEmpty()) return 0;

        int inseridos = 0;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            conn.setAutoCommit(false);

            for (Reuniao r : reunioes) {
                if (r == null || r.getId() == null) continue;
                mapearParaStatement(ps, r);
                ps.addBatch();
                inseridos++;

                if (inseridos % 100 == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }

            ps.executeBatch();
            conn.commit();
            System.out.println("[ReuniaoDAO] Lote inserido: " + inseridos + " reunioes.");

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro no lote: " + e.getMessage());
            inseridos = 0;
        }

        return inseridos;
    }

    public Optional<Reuniao> buscarPorId(String idReuniao) {
        if (idReuniao == null || idReuniao.isBlank()) return Optional.empty();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setString(1, idReuniao);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearReuniao(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao buscar por ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Reuniao> listarTodas() {
        List<Reuniao> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapearReuniao(rs));

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao listar: " + e.getMessage());
        }

        return lista;
    }

    public List<Reuniao> listarPorSegmento(String segmento) {
        if (segmento == null || segmento.isBlank()) return new ArrayList<>();

        List<Reuniao> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_SEGMENTO)) {

            ps.setString(1, segmento);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearReuniao(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao buscar por segmento: " + e.getMessage());
        }

        return lista;
    }

    public List<Reuniao> listarAltoRiscoChurn(double limiteChurn) {
        List<Reuniao> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALTO_RISCO)) {

            ps.setDouble(1, limiteChurn);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearReuniao(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao buscar alto risco: " + e.getMessage());
        }

        return lista;
    }

    public int contarTotal() {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao contar: " + e.getMessage());
        }

        return 0;
    }

    public boolean atualizar(Reuniao reuniao) {
        if (reuniao == null || reuniao.getId() == null) return false;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setTimestamp(1, reuniao.getData() != null ? Timestamp.valueOf(reuniao.getData()) : null);
            ps.setInt(2, reuniao.getDuracao());
            ps.setString(3, reuniao.getTextoOriginal());
            ps.setString(4, reuniao.getFormato());
            ps.setString(5, reuniao.getStatus());
            ps.setString(6, reuniao.getSegmento());
            ps.setString(7, reuniao.getUnidade());
            ps.setString(8, reuniao.getUf());
            if (reuniao.getNotaNps() != null)
                ps.setInt(9, reuniao.getNotaNps());
            else
                ps.setNull(9, Types.NUMERIC);
            ps.setString(10, reuniao.getId());

            boolean sucesso = ps.executeUpdate() > 0;
            System.out.println("[ReuniaoDAO] Reuniao " + (sucesso ? "atualizada" : "nao encontrada") + ": " + reuniao.getId());
            return sucesso;

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao atualizar: " + e.getMessage());
            return false;
        }
    }

    public boolean deletar(String idReuniao) {
        if (idReuniao == null || idReuniao.isBlank()) return false;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, idReuniao);
            boolean sucesso = ps.executeUpdate() > 0;
            System.out.println("[ReuniaoDAO] Reuniao " + (sucesso ? "deletada" : "nao encontrada") + ": " + idReuniao);
            return sucesso;

        } catch (SQLException e) {
            System.err.println("[ReuniaoDAO] Erro ao deletar: " + e.getMessage());
            return false;
        }
    }

    private void mapearParaStatement(PreparedStatement ps, Reuniao r) throws SQLException {
        ps.setString(1, r.getId());
        ps.setTimestamp(2, r.getData() != null ? Timestamp.valueOf(r.getData()) : null);
        ps.setTimestamp(3, r.getDataCriacao() != null ? Timestamp.valueOf(r.getDataCriacao()) : null);
        ps.setInt(4, r.getDuracao());
        ps.setString(5, r.getTextoOriginal());
        ps.setString(6, r.getFormato());
        ps.setString(7, r.getStatus());
        ps.setString(8, r.getCodt());
        ps.setString(9, r.isExterno() ? "S" : "N");
        ps.setString(10, r.getSegmento());
        ps.setString(11, r.getUnidade());
        ps.setString(12, r.getCnae());
        ps.setString(13, r.getUf());
        ps.setString(14, r.getFaixaFaturamento());
        ps.setString(15, r.getTipoRecurso());
        if (r.getNotaNps() != null)
            ps.setInt(16, r.getNotaNps());
        else
            ps.setNull(16, Types.NUMERIC);
    }

    private Reuniao mapearReuniao(ResultSet rs) throws SQLException {
        Reuniao r = new Reuniao();

        r.setId(rs.getString("id_reuniao"));
        r.setDuracao(rs.getInt("duracao"));
        r.setTextoOriginal(rs.getString("texto_original"));
        r.setFormato(rs.getString("formato"));
        r.setStatus(rs.getString("status"));
        r.setCodt(rs.getString("codt"));
        r.setExterno("S".equals(rs.getString("externo")));
        r.setSegmento(rs.getString("segmento"));
        r.setUnidade(rs.getString("unidade"));
        r.setCnae(rs.getString("cnae"));
        r.setUf(rs.getString("uf"));
        r.setFaixaFaturamento(rs.getString("faixa_faturamento"));
        r.setTipoRecurso(rs.getString("tipo_recurso"));

        int nps = rs.getInt("nota_nps");
        if (!rs.wasNull()) r.setNotaNps(nps);

        Timestamp data = rs.getTimestamp("data");
        if (data != null) r.setData(data.toLocalDateTime());

        Timestamp dataCriacao = rs.getTimestamp("data_criacao");
        if (dataCriacao != null) r.setDataCriacao(dataCriacao.toLocalDateTime());

        return r;
    }
}