package br.com.totvs.conversacional.dao;

import br.com.totvs.conversacional.conexoes.ConnectionFactory;
import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.exception.AcessoDadosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReuniaoDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReuniaoDAO.class);

    /** Código de erro Oracle para violação de restrição de unicidade (ORA-00001). */
    private static final int ORA_UNIQUE_VIOLATION = 1;

    private static final String SQL_INSERT =
            "INSERT INTO reuniao (id_reuniao, data, data_criacao, duracao, texto_original, " +
                    "formato, status, codt, externo, segmento, unidade, cnae, uf, " +
                    "faixa_faturamento, tipo_recurso, nota_nps) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM reuniao WHERE id_reuniao = ?";

    // Evita transferir CLOBs pesados na listagem usada pelo React.
    private static final String COLUNAS_RESUMO =
            "id_reuniao, data, data_criacao, duracao, formato, status, codt, externo, " +
            "segmento, unidade, cnae, uf, faixa_faturamento, tipo_recurso, nota_nps";

    private static final String SQL_SELECT_ALL =
            "SELECT " + COLUNAS_RESUMO + " FROM reuniao ORDER BY data DESC";

    private static final String SQL_SELECT_ALL_COM_TEXTO =
            "SELECT * FROM reuniao ORDER BY data DESC";

    private static final String SQL_SELECT_BY_SEGMENTO =
            "SELECT " + COLUNAS_RESUMO + " FROM reuniao WHERE UPPER(segmento) = UPPER(?) ORDER BY data DESC";

    private static final String SQL_UPDATE =
            "UPDATE reuniao SET data = ?, duracao = ?, texto_original = ?, formato = ?, status = ?, " +
                    "codt = ?, externo = ?, segmento = ?, unidade = ?, cnae = ?, uf = ?, " +
                    "faixa_faturamento = ?, tipo_recurso = ?, nota_nps = ? WHERE id_reuniao = ?";

    private static final String SQL_DELETE =
            "DELETE FROM reuniao WHERE id_reuniao = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM reuniao";

    public boolean inserir(Reuniao reuniao) {
        if (reuniao == null || reuniao.getId() == null) {
            logger.warn("Reuniao invalida para insercao.");
            return false;
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            mapearParaStatement(ps, reuniao);
            ps.executeUpdate();
            logger.info("Reuniao inserida: {}", reuniao.getId());
            return true;

        } catch (SQLException e) {
            if (e.getErrorCode() == ORA_UNIQUE_VIOLATION) {
                logger.warn("Reuniao ja existente: {}", reuniao.getId());
                return false;
            }
            throw new AcessoDadosException("Erro ao inserir reuniao.", e);
        }
    }

    public int inserirLote(List<Reuniao> reunioes) {
        if (reunioes == null || reunioes.isEmpty()) return 0;

        int inseridos = 0;

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
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
                logger.info("Lote inserido: {} reunioes.", inseridos);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao inserir lote de reunioes.", e);
        }

        return inseridos;
    }

    public Optional<Reuniao> buscarPorId(String idReuniao) {
        if (idReuniao == null || idReuniao.isBlank()) return Optional.empty();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setString(1, idReuniao);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearReuniao(rs, true));
            }

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao buscar reuniao por ID.", e);
        }

        return Optional.empty();
    }

    public List<Reuniao> listarTodas() {
        return listarUsando(SQL_SELECT_ALL, false);
    }

    public List<Reuniao> listarTodasComTexto() {
        return listarUsando(SQL_SELECT_ALL_COM_TEXTO, true);
    }

    private List<Reuniao> listarUsando(String sql, boolean comTexto) {
        List<Reuniao> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapearReuniao(rs, comTexto));

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao listar reunioes.", e);
        }

        return lista;
    }


    public List<Reuniao> listarPorSegmento(String segmento) {
        if (segmento == null || segmento.isBlank()) {
            return new ArrayList<>();
        }

        List<Reuniao> lista = new ArrayList<>();
        String segmentoNormalizado = segmento.trim();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_SEGMENTO)) {

            ps.setString(1, segmentoNormalizado);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReuniao(rs, false));
                }
            }

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao buscar reunioes por segmento.", e);
        }

        return lista;
    }

    public int contarTotal() {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao contar reunioes.", e);
        }

        return 0;
    }

    public boolean atualizar(Reuniao reuniao) {
        if (reuniao == null || reuniao.getId() == null) return false;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setTimestamp(1, reuniao.getData() != null ? Timestamp.valueOf(reuniao.getData()) : null);
            if (reuniao.getDuracao() != null) {
                ps.setInt(2, reuniao.getDuracao());
            } else {
                ps.setNull(2, Types.NUMERIC);
            }
            ps.setString(3, reuniao.getTextoOriginal());
            ps.setString(4, reuniao.getFormato());
            ps.setString(5, reuniao.getStatus());
            ps.setString(6, reuniao.getCodt());
            ps.setString(7, reuniao.isExterno() ? "S" : "N");
            ps.setString(8, reuniao.getSegmento());
            ps.setString(9, reuniao.getUnidade());
            ps.setString(10, reuniao.getCnae());
            ps.setString(11, reuniao.getUf());
            ps.setString(12, reuniao.getFaixaFaturamento());
            ps.setString(13, reuniao.getTipoRecurso());
            if (reuniao.getNotaNps() != null)
                ps.setInt(14, reuniao.getNotaNps());
            else
                ps.setNull(14, Types.NUMERIC);
            ps.setString(15, reuniao.getId());

            boolean sucesso = ps.executeUpdate() > 0;
            logger.info("Reuniao {}: {}", sucesso ? "atualizada" : "nao encontrada", reuniao.getId());
            return sucesso;

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao atualizar reuniao.", e);
        }
    }

    public boolean deletar(String idReuniao) {
        if (idReuniao == null || idReuniao.isBlank()) return false;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, idReuniao);
            boolean sucesso = ps.executeUpdate() > 0;
            logger.info("Reuniao {}: {}", sucesso ? "deletada" : "nao encontrada", idReuniao);
            return sucesso;

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao deletar reuniao.", e);
        }
    }

    /** Resultado de uma importação em lote feita pelo DAO. */
    public record ResultadoLote(int inseridas, int duplicadas, List<String> erros) {
    }

    /**
     * Insere várias reuniões numa só conexão e numa só transação.
     * ID que já existe (ORA-00001) é contado como duplicado; qualquer outro erro de
     * uma linha (ex.: valor maior que a coluna) entra em "erros" e não derruba o resto.
     */
    public ResultadoLote inserirIgnorandoDuplicadas(List<Reuniao> reunioes) {
        int inseridas = 0;
        int duplicadas = 0;
        List<String> erros = new ArrayList<>();

        if (reunioes == null || reunioes.isEmpty()) {
            return new ResultadoLote(0, 0, erros);
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                for (Reuniao r : reunioes) {
                    try {
                        mapearParaStatement(ps, r);
                        ps.executeUpdate();
                        inseridas++;
                    } catch (SQLException e) {
                        if (e.getErrorCode() == ORA_UNIQUE_VIOLATION) {
                            duplicadas++;
                        } else {
                            String msg = e.getMessage() == null ? "erro ao inserir" : e.getMessage().split("\\R")[0];
                            erros.add(r.getId() + ": " + msg);
                        }
                    }
                }
                conn.commit();
                logger.info("Importacao em lote: {} inseridas, {} duplicadas, {} com erro.",
                        inseridas, duplicadas, erros.size());

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new AcessoDadosException("Erro ao importar lote de reunioes.", e);
        }

        return new ResultadoLote(inseridas, duplicadas, erros);
    }

    private void mapearParaStatement(PreparedStatement ps, Reuniao r) throws SQLException {
        ps.setString(1, r.getId());
        ps.setTimestamp(2, r.getData() != null ? Timestamp.valueOf(r.getData()) : null);
        ps.setTimestamp(3, r.getDataCriacao() != null ? Timestamp.valueOf(r.getDataCriacao()) : null);
        if (r.getDuracao() != null) {
            ps.setInt(4, r.getDuracao());
        } else {
            ps.setNull(4, Types.NUMERIC);
        }
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

    private Reuniao mapearReuniao(ResultSet rs, boolean comTexto) throws SQLException {
        Reuniao r = new Reuniao();

        r.setId(rs.getString("id_reuniao"));
        int duracao = rs.getInt("duracao");
        if (!rs.wasNull()) {
            r.setDuracao(duracao);
        }
        if (comTexto) r.setTextoOriginal(rs.getString("texto_original"));
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