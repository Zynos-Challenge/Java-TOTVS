package br.com.totvs.conversacional.entities;

/**
 * Nível de risco de cancelamento (churn) estimado por regras de palavra-chave.
 */
public enum RiscoChurn {
    ALTO("Alto"),
    MEDIO("Médio"),
    BAIXO("Baixo");

    private final String rotulo;

    RiscoChurn(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
