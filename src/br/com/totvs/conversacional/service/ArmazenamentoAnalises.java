package br.com.totvs.conversacional.service;

import br.com.totvs.conversacional.dto.AnaliseResponse;
import br.com.totvs.conversacional.entities.Reuniao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Persistência de análises em disco, independente do banco Oracle e do navegador.
 * O nome do arquivo é um hash do ID (impede path traversal); a assinatura do
 * conteúdo garante que NPS/texto alterados nunca usem uma análise antiga.
 * A pasta deve estar num volume persistente e com acesso restrito ao servidor.
 */
@Component
public class ArmazenamentoAnalises {

    private static final Logger logger = LoggerFactory.getLogger(ArmazenamentoAnalises.class);
    // Incremente ao mudar as regras em Analise/Analisador/AnaliseService.
    private static final String VERSAO_REGRAS = "1";

    private final Path diretorio;
    private final ObjectMapper mapper;
    private final Map<String, AnaliseSalva> memoria = new ConcurrentHashMap<>();
    private final Map<String, Object> travas = new ConcurrentHashMap<>();

    public ArmazenamentoAnalises(
            @Value("${vaox.analises.diretorio:${VAOX_ANALISES_DIRETORIO:./dados/analises}}") String pasta,
            ObjectMapper mapper) {
        this.diretorio = Path.of(pasta).toAbsolutePath().normalize();
        this.mapper = mapper;
    }

    public AnaliseResponse obterOuCalcular(Reuniao reuniao, Supplier<AnaliseResponse> calcular) {
        String id = reuniao.getId();
        String assinatura = assinatura(reuniao);
        synchronized (travas.computeIfAbsent(id, ignored -> new Object())) {
            AnaliseSalva salva = memoria.get(id);
            if (valida(salva, assinatura)) return salva.analise();

            Path arquivo = arquivo(id);
            if (Files.isRegularFile(arquivo)) {
                try {
                    salva = mapper.readValue(arquivo.toFile(), AnaliseSalva.class);
                    if (valida(salva, assinatura)) {
                        memoria.put(id, salva);
                        return salva.analise();
                    }
                } catch (IOException e) {
                    logger.warn("Cache de analise invalido para {}. Recalculando: {}", id, e.toString());
                }
            }

            AnaliseResponse resultado = calcular.get();
            AnaliseSalva nova = new AnaliseSalva(VERSAO_REGRAS, assinatura, resultado);
            gravar(arquivo, nova);
            memoria.put(id, nova);
            return resultado;
        }
    }

    public void apagar(String id) {
        synchronized (travas.computeIfAbsent(id, ignored -> new Object())) {
            memoria.remove(id);
            try {
                Files.deleteIfExists(arquivo(id));
            } catch (IOException e) {
                logger.warn("Nao foi possivel apagar cache da reuniao {}: {}", id, e.toString());
            }
        }
    }

    private boolean valida(AnaliseSalva salva, String assinatura) {
        return salva != null && VERSAO_REGRAS.equals(salva.versao())
                && assinatura.equals(salva.assinatura()) && salva.analise() != null;
    }

    private Path arquivo(String id) {
        return diretorio.resolve(hash(id.getBytes(StandardCharsets.UTF_8)) + ".json");
    }

    private static String assinatura(Reuniao reuniao) {
        // Apenas campos efetivamente utilizados pelo analisador.
        String nps = reuniao.getNotaNps() == null ? "sem-nps" : reuniao.getNotaNps().toString();
        String texto = reuniao.getTextoOriginal() == null ? "" : reuniao.getTextoOriginal();
        MessageDigest digest = sha256();
        digest.update(VERSAO_REGRAS.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        digest.update(nps.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        digest.update(texto.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String hash(byte[] bytes) {
        return HexFormat.of().formatHex(sha256().digest(bytes));
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nao disponivel", e);
        }
    }

    private void gravar(Path destino, AnaliseSalva analise) {
        Path temporario = null;
        try {
            Files.createDirectories(diretorio);
            temporario = Files.createTempFile(diretorio, "analise-", ".tmp");
            mapper.writeValue(temporario.toFile(), analise);
            try {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao salvar a analise da reuniao no diretorio " + diretorio, e);
        } finally {
            if (temporario != null) {
                try { Files.deleteIfExists(temporario); } catch (IOException ignored) { }
            }
        }
    }

    public record AnaliseSalva(String versao, String assinatura, AnaliseResponse analise) { }
}
