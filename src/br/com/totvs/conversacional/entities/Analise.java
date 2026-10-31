package br.com.totvs.conversacional.entities;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Analise {

    private Reuniao reuniao;
    private Cliente cliente;
    private String sentimento;
    private String tomDeVoz;
    private int scoreGeral;
    private List<Produto> produtos;
    private List<Alerta> alertas;
    private List<String> reclamacoes;
    private Vendedor vendedor;
    private RiscoChurn riscoChurn;
    private List<String> motivosRisco;

    // Palavras-chave do tom de voz (minúsculas e sem acento, como o texto normalizado)
    private static final List<String> TOM_AGRESSIVO = List.of(
            "absurdo", "inaceitavel", "ridiculo", "nao aguento", "revoltado", "revoltada", "uma vergonha",
            "inadmissivel", "pessimo", "pessima", "horrivel", "insatisfeito", "insatisfeita", "insatisfacao",
            "decepcionado", "decepcionada", "decepcionante", "frustrado", "frustrada", "frustrante",
            "irritado", "irritada", "chateado", "chateada", "indignado", "indignada", "furioso", "furiosa",
            "descaso", "desrespeito", "falta de respeito", "insuportavel", "cansado de", "cansada de");
    private static final List<String> TOM_POSITIVO = List.of(
            "otimo", "otima", "otimos", "excelente", "excelentes", "maravilhoso", "maravilhosa",
            "perfeito", "perfeita", "feliz", "felizes", "satisfeito", "satisfeita", "satisfeitos",
            "contente", "contentes", "muito bom", "muito boa", "aprovado", "adorei", "gostei",
            "gostamos", "parabens", "recomendo", "funciona bem");
    private static final List<String> TOM_ANSIOSO = List.of(
            "urgente", "urgentemente", "urgencia", "preciso logo", "quanto antes", "o mais rapido possivel",
            "prazo curto", "prazo apertado", "estourou o prazo", "estourar o prazo", "perder o prazo",
            "fora do prazo", "atrasado", "atrasada", "atrasados", "nao pode esperar", "emergencia", "pressa");

    // Termos que são só um assunto ("suporte", "preço"): só viram reclamação se a frase não for elogio
    private static final Set<String> TERMOS_AMBIGUOS = Set.of("suporte", "atendimento", "preco");
    private static final Set<String> NEGADORES = Set.of("nao", "nunca", "jamais", "nem", "sem", "nada");

    public Analise() {
        this.motivosRisco = new ArrayList<>();
        this.produtos = new ArrayList<>();
        this.alertas = new ArrayList<>();
        this.reclamacoes = new ArrayList<>();
    }

    public Analise(Reuniao reuniao, Cliente cliente, String sentimento, String tomDeVoz, int scoreGeral) {
        this.reuniao = reuniao;
        this.cliente = cliente;
        this.sentimento = sentimento;
        this.tomDeVoz = tomDeVoz;
        this.scoreGeral = scoreGeral;
        this.motivosRisco = new ArrayList<>();
        this.produtos = new ArrayList<>();
        this.alertas = new ArrayList<>();
        this.reclamacoes = new ArrayList<>();
    }

    public Reuniao getReuniao() {
        return reuniao;
    }

    public void setReuniao(Reuniao reuniao) {
        this.reuniao = reuniao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getSentimento() {
        return sentimento;
    }

    public void setSentimento(String sentimento) {
        this.sentimento = sentimento;
    }

    public String getTomDeVoz() {
        return tomDeVoz;
    }

    public void setTomDeVoz(String tomDeVoz) {
        this.tomDeVoz = tomDeVoz;
    }

    public int getScoreGeral() {
        return scoreGeral;
    }

    public void setScoreGeral(int scoreGeral) {
        this.scoreGeral = scoreGeral;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public List<Alerta> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<Alerta> alertas) {
        this.alertas = alertas;
    }

    public List<String> getReclamacoes() {
        return reclamacoes;
    }

    public void setReclamacoes(List<String> reclamacoes) {
        this.reclamacoes = reclamacoes;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }



    public RiscoChurn getRiscoChurn() {
        return riscoChurn;
    }

    public List<String> getMotivosRisco() {
        return motivosRisco;
    }

    public int calcularScore() {
        int score = 50;

        if (sentimento != null) {
            if (sentimento.equals("Satisfeito")) score += 20;
            if (sentimento.equals("Insatisfeito")) score -= 20;
        }

        for (Alerta alertaAtual : alertas) {
            if (alertaAtual.getTipo().equals("CHURN")) score -= 15;
            if (alertaAtual.getTipo().equals("CONCORRENTE")) score -= 10;
            if (alertaAtual.getTipo().equals("OPORTUNIDADE")) score += 10;
        }

        if (score > 100) score = 100;
        if (score < 0) score = 0;

        this.scoreGeral = score;
        return score;
    }

    public List<Alerta> detectarAlertas() {
        alertas.clear();
        String original = textoDaReuniao();
        String texto = textoNormalizado(original);

        // Risco de churn
        List<String> sinaisChurn = List.of(
                "cancelar", "cancelamento", "rescindir", "rescisão", "sair do contrato",
                "nao vou renovar", "não vou renovar", "encerrar contrato", "desistir"
        );
        adicionarAlerta("CHURN", "Sinal de risco de cancelamento detectado", "ALTA",
                sinaisChurn, original, texto);

        List<String> sinaisConcorrente = List.of(
                "concorrente", "outro sistema", "outra solução", "sap", "oracle",
                "senior", "linx"
        );
        adicionarAlerta("CONCORRENTE", "Menção a sistema ou empresa concorrente", "MEDIA",
                sinaisConcorrente, original, texto);

        List<String> sinaisOportunidade = List.of(
                "expandir", "ampliar", "novo módulo", "novo modulo", "contratar mais",
                "outros setores", "outras filiais", "crescimento", "implantar mais"
        );
        adicionarAlerta("OPORTUNIDADE", "Sinal de possibilidade de expansão do contrato", "BAIXA",
                sinaisOportunidade, original, texto);

        return alertas;
    }

    /**
     * Calcula o risco de churn por regras de palavra-chave (não é modelo estatístico):
     * ALTO  - há alerta de CHURN, ou tom AGRESSIVO com NPS até 6;
     * MEDIO - há alerta de CONCORRENTE, ou 2 ou mais reclamações, ou NPS até 6;
     * BAIXO - nenhum dos casos acima.
     * Deve ser chamado depois de detectarReclamacoes, detectarAlertas e detectarTomDeVoz.
     */
    public RiscoChurn calcularRisco() {
        motivosRisco.clear();
        Integer nps = reuniao != null ? reuniao.getNotaNps() : null;
        boolean npsBaixo = nps != null && nps <= 6;

        boolean alto = false;
        boolean medio = false;

        if (temAlerta("CHURN")) {
            alto = true;
            motivosRisco.add("Cliente sinalizou intenção de cancelar ou não renovar o contrato.");
        }
        if ("AGRESSIVO".equals(tomDeVoz) && npsBaixo) {
            alto = true;
            motivosRisco.add("Tom de voz agressivo combinado com NPS baixo (" + nps + ").");
        }
        if (temAlerta("CONCORRENTE")) {
            medio = true;
            motivosRisco.add("Cliente citou um sistema ou empresa concorrente.");
        }
        if (reclamacoes.size() >= 2) {
            medio = true;
            motivosRisco.add("Foram detectadas " + reclamacoes.size() + " reclamações na conversa.");
        }
        if (npsBaixo) {
            medio = true;
            motivosRisco.add("NPS baixo (" + nps + " de 10).");
        }

        if (alto) {
            riscoChurn = RiscoChurn.ALTO;
        } else if (medio) {
            riscoChurn = RiscoChurn.MEDIO;
        } else {
            riscoChurn = RiscoChurn.BAIXO;
            motivosRisco.add("Nenhum sinal de risco encontrado pelas regras.");
        }
        return riscoChurn;
    }

    private boolean temAlerta(String tipo) {
        for (Alerta alerta : alertas) {
            if (tipo.equals(alerta.getTipo())) return true;
        }
        return false;
    }

    private String textoDaReuniao() {
        if (reuniao == null || reuniao.getTextoOriginal() == null) return "";
        return reuniao.getTextoOriginal();
    }

    // Normalizar um texto de milhares de caracteres custa caro: faz uma vez só por análise.
    private String cacheOriginal;
    private String cacheNormalizado;

    private String textoNormalizado(String original) {
        if (cacheNormalizado == null || cacheOriginal != original) {
            cacheNormalizado = normalizar(original);
            cacheOriginal = original;
        }
        return cacheNormalizado;
    }

    private void adicionarAlerta(String tipo, String descricao, String urgencia,
                                 List<String> sinais, String original, String texto) {
        for (String sinal : sinais) {
            int posicao = "CHURN".equals(tipo) ? encontrarNaoNegado(texto, sinal) : encontrar(texto, sinal);
            if (posicao >= 0) {
                alertas.add(new Alerta(tipo, descricao, urgencia,
                        extrairTrecho(original, posicao, sinal.length())));
                return;
            }
        }
    }

    /**
     * Minúsculas e sem acentos, mantendo exatamente o mesmo tamanho do texto original
     * (cada caractere vira um caractere), para que as posições encontradas no texto
     * normalizado valham também no texto original.
     */
    static String normalizar(String texto) {
        if (texto == null) return "";
        char[] saida = new char[texto.length()];
        for (int i = 0; i < saida.length; i++) {
            saida[i] = normalizarChar(texto.charAt(i));
        }
        return new String(saida);
    }

    // Cada caractere só passa pelo Normalizer uma vez; as demais vezes vêm desta tabela.
    private static final char SEM_VALOR = '\uFFFF';
    private static final char[] MAPA_NORMAL = novoMapa();

    private static char[] novoMapa() {
        char[] mapa = new char[65536];
        Arrays.fill(mapa, SEM_VALOR);
        return mapa;
    }

    private static char normalizarChar(char c) {
        if (c < 128) return Character.toLowerCase(c);
        char r = MAPA_NORMAL[c];
        if (r == SEM_VALOR) {
            String decomposto = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
            r = Character.toLowerCase(decomposto.charAt(0));
            MAPA_NORMAL[c] = r;
        }
        return r;
    }

    // Termo da lista já normalizado (minúsculo, sem acento, sem espaços nas pontas)
    private static final ConcurrentHashMap<String, String> TERMOS = new ConcurrentHashMap<>();

    private static String termoNormalizado(String termo) {
        return TERMOS.computeIfAbsent(termo, t -> normalizar(t).trim());
    }

    private static boolean parteDePalavra(int codePoint) {
        if (Character.isLetter(codePoint) || Character.isDigit(codePoint)) return true;
        int tipo = Character.getType(codePoint);
        return tipo == Character.NON_SPACING_MARK || tipo == Character.ENCLOSING_MARK
                || tipo == Character.COMBINING_SPACING_MARK || tipo == Character.LETTER_NUMBER
                || tipo == Character.OTHER_NUMBER;
    }

    private static boolean palavraInteira(String texto, int inicio, int fim) {
        boolean antesOk = inicio == 0 || !parteDePalavra(texto.codePointBefore(inicio));
        boolean depoisOk = fim >= texto.length() || !parteDePalavra(texto.codePointAt(fim));
        return antesOk && depoisOk;
    }

    /** Posição da primeira ocorrência do termo como palavra inteira, ou -1. */
    static int encontrar(String textoNormalizado, String termo) {
        String t = termoNormalizado(termo);
        if (t.isEmpty()) return -1;
        for (int i = textoNormalizado.indexOf(t); i >= 0; i = textoNormalizado.indexOf(t, i + 1)) {
            if (palavraInteira(textoNormalizado, i, i + t.length())) return i;
        }
        return -1;
    }

    private static boolean contem(String textoNormalizado, String termo) {
        return encontrar(textoNormalizado, termo) >= 0;
    }

    /** Posições de todas as ocorrências do termo como palavra inteira. */
    private static List<Integer> posicoes(String textoNormalizado, String termo) {
        List<Integer> lista = new ArrayList<>();
        String t = termoNormalizado(termo);
        if (t.isEmpty()) return lista;
        for (int i = textoNormalizado.indexOf(t); i >= 0; i = textoNormalizado.indexOf(t, i + 1)) {
            if (palavraInteira(textoNormalizado, i, i + t.length())) lista.add(i);
        }
        return lista;
    }

    /** Primeira ocorrência do termo que NÃO vem negada ("não é péssimo", "sem bugs"), ou -1. */
    static int encontrarNaoNegado(String textoNormalizado, String termo) {
        String t = termoNormalizado(termo);
        if (t.isEmpty()) return -1;
        for (int i = textoNormalizado.indexOf(t); i >= 0; i = textoNormalizado.indexOf(t, i + 1)) {
            if (palavraInteira(textoNormalizado, i, i + t.length()) && !negado(textoNormalizado, i)) return i;
        }
        return -1;
    }

    /** Há um "não", "nunca", "sem"... nas 3 palavras anteriores, na mesma oração? */
    static boolean negado(String textoNormalizado, int posicao) {
        String antes = textoNormalizado.substring(Math.max(0, posicao - 40), posicao);
        int corte = -1;
        for (char c : new char[]{'.', '!', '?', ';', ',', '\n'}) {
            corte = Math.max(corte, antes.lastIndexOf(c));
        }
        if (corte >= 0) antes = antes.substring(corte + 1);
        String[] palavras = antes.trim().split("[^\\p{L}\\p{N}]+");
        for (int i = Math.max(0, palavras.length - 3); i < palavras.length; i++) {
            if (NEGADORES.contains(palavras[i])) return true;
        }
        return false;
    }

    /** A frase em volta da posição tem algum termo de tom positivo (não negado)? */
    private static boolean frasePositiva(String textoNormalizado, int posicao) {
        String delimitadores = ".!?;\n";
        int inicio = posicao;
        while (inicio > 0 && posicao - inicio < 120 && delimitadores.indexOf(textoNormalizado.charAt(inicio - 1)) < 0) {
            inicio--;
        }
        int fim = posicao;
        while (fim < textoNormalizado.length() && fim - posicao < 120
                && delimitadores.indexOf(textoNormalizado.charAt(fim)) < 0) {
            fim++;
        }
        String frase = textoNormalizado.substring(inicio, fim);
        for (String positivo : TOM_POSITIVO) {
            if (encontrarNaoNegado(frase, positivo) >= 0) return true;
        }
        return false;
    }

    /** Reclamação de verdade: termo não negado e, nos termos ambíguos, fora de uma frase de elogio. */
    private static boolean temReclamacao(String textoNormalizado, String sinal) {
        for (int posicao : posicoes(textoNormalizado, sinal)) {
            if (negado(textoNormalizado, posicao)) continue;
            if (TERMOS_AMBIGUOS.contains(sinal) && frasePositiva(textoNormalizado, posicao)) continue;
            return true;
        }
        return false;
    }

    /** Recorte do texto original ao redor do termo que disparou o alerta. */
    static String extrairTrecho(String original, int posicao, int tamanhoTermo) {
        int inicio = Math.max(0, posicao - 50);
        int fim = Math.min(original.length(), posicao + tamanhoTermo + 50);

        // Não corta palavra no meio: avança o início e recua o fim até um espaço
        // (sem nunca passar do termo que disparou o alerta).
        while (inicio > 0 && inicio < posicao && !Character.isWhitespace(original.charAt(inicio - 1))) {
            inicio++;
        }
        while (fim < original.length() && fim > posicao + tamanhoTermo
                && !Character.isWhitespace(original.charAt(fim))) {
            fim--;
        }

        String trecho = original.substring(inicio, fim).replaceAll("\\s+", " ").trim();
        return (inicio > 0 ? "..." : "") + trecho + (fim < original.length() ? "..." : "");
    }

    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n╔══════════════════════════════════════════╗");
        sb.append("\n║     RELATÓRIO DETALHADO - VAOX           ║");
        sb.append("\n╚══════════════════════════════════════════╝");


        if (reuniao != null) {
            sb.append("\n\n┌─ DADOS DA REUNIÃO ───────────────────────");
            sb.append("\n  ID           : ").append(reuniao.getId());
            sb.append("\n  Data         : ").append(reuniao.getData() != null ? reuniao.getData() : "Não informado");
            sb.append("\n  Duração      : ").append(reuniao.getDuracao()).append(" min");
            sb.append("\n  Formato      : ").append(reuniao.getFormato() != null ? reuniao.getFormato() : "Não informado");
            sb.append("\n  Externo      : ").append(reuniao.isExterno() ? "Sim" : "Não");


            if (reuniao.getUf()              != null) sb.append("\n  UF           : ").append(reuniao.getUf());
            if (reuniao.getSegmento()        != null) sb.append("\n  Segmento     : ").append(reuniao.getSegmento());
            if (reuniao.getUnidade()         != null) sb.append("\n  Unidade      : ").append(reuniao.getUnidade());
            if (reuniao.getFaixaFaturamento()!= null) sb.append("\n  Faturamento  : ").append(reuniao.getFaixaFaturamento());
            if (reuniao.getTipoRecurso()     != null) sb.append("\n  Tipo Recurso : ").append(reuniao.getTipoRecurso());
            if (reuniao.getNotaNps()         != null) sb.append("\n  NPS          : ").append(reuniao.getNotaNps()).append("/10");

            sb.append("\n└──────────────────────────────────────────");
        }


        sb.append("\n\n┌─ ANÁLISE ────────────────────────────────");
        sb.append("\n  Sentimento   : ").append(sentimento != null ? sentimento : "Não detectado");
        sb.append("\n  Tom de Voz   : ").append(tomDeVoz   != null ? tomDeVoz   : "Não detectado");
        sb.append("\n  Score Geral  : ").append(scoreGeral).append("/100");

        int blocos = scoreGeral / 10;
        sb.append("\n  Score Visual : [");
        for (int i = 0; i < 10; i++) sb.append(i < blocos ? "█" : "░");
        sb.append("] ").append(scoreGeral).append("%");
        sb.append("\n└──────────────────────────────────────────");


        sb.append("\n\n┌─ ALERTAS ────────────────────────────────");
        if (alertas == null || alertas.isEmpty()) {
            sb.append("\n  Nenhum alerta identificado.");
        } else {
            for (Alerta a : alertas) {
                sb.append("\n  ⚑ [").append(a.getUrgencia()).append("] ")
                        .append(a.getTipo()).append(": ").append(a.getDescricao());
                if (a.getTrechoOrigem() != null && !a.getTrechoOrigem().isEmpty()) {
                    sb.append("\n    Trecho: \"").append(a.getTrechoOrigem()).append("\"");
                }
            }
        }
        sb.append("\n└──────────────────────────────────────────");


        sb.append("\n\n┌─ RECLAMAÇÕES DETECTADAS ─────────────────");
        if (reclamacoes == null || reclamacoes.isEmpty()) {
            sb.append("\n  Nenhuma reclamação identificada.");
        } else {
            for (String rec : reclamacoes) {
                sb.append("\n  ✗ ").append(rec);
            }
        }
        sb.append("\n└──────────────────────────────────────────");

        sb.append("\n╔══════════════════════════════════════════╗");
        sb.append("\n║            FIM DO RELATÓRIO              ║");
        sb.append("\n╚══════════════════════════════════════════╝");

        return sb.toString();
    }

    public List<String> detectarReclamacoes() {
        reclamacoes.clear();
        String texto = textoNormalizado(textoDaReuniao());

        List<String> reclamacoesSuporte = List.of("suporte", "atendimento", "demora", "nao respondeu", "sem retorno");
        List<String> reclamacoesProduto = List.of("trava", "lento", "erro", "erros", "bug", "bugs", "falha", "falhas", "nao funciona", "travando", "caindo", "instavel");
        List<String> reclamacoesPreco = List.of("caro", "valor alto", "preco", "custo elevado", "muito caro");
        List<String> reclamacoesIntegracao = List.of("nao integra", "nao comunica", "sistema separado", "nao conversa", "isolado");

        for (String sinal : reclamacoesSuporte)   { if (temReclamacao(texto, sinal)) reclamacoes.add("SUPORTE: " + sinal); }
        for (String sinal : reclamacoesProduto)   { if (temReclamacao(texto, sinal)) reclamacoes.add("PRODUTO: " + sinal); }
        for (String sinal : reclamacoesPreco)     { if (temReclamacao(texto, sinal)) reclamacoes.add("PRECO: " + sinal); }
        for (String sinal : reclamacoesIntegracao){ if (temReclamacao(texto, sinal)) reclamacoes.add("INTEGRACAO: " + sinal); }

        return reclamacoes;
    }

    public String detectarTomDeVoz() {
        String texto = textoNormalizado(textoDaReuniao());

        int pontosAgressivo = 0, pontosPositivo = 0, pontosAnsioso = 0;

        // Termo negado ("não é péssimo") não conta; elogio negado ("não estou satisfeito") conta como insatisfação.
        for (String sinal : TOM_AGRESSIVO) { if (encontrarNaoNegado(texto, sinal) >= 0) pontosAgressivo++; }
        for (String sinal : TOM_POSITIVO) {
            if (encontrarNaoNegado(texto, sinal) >= 0) pontosPositivo++;
            else if (contem(texto, sinal)) pontosAgressivo++;
        }
        for (String sinal : TOM_ANSIOSO)   { if (encontrarNaoNegado(texto, sinal) >= 0) pontosAnsioso++; }

        int maior = Math.max(pontosAgressivo, Math.max(pontosPositivo, pontosAnsioso));

        if (maior == 0) {
            this.tomDeVoz = "NEUTRO";
            this.sentimento = "Neutro";
        } else if (pontosAgressivo == maior) {
            this.tomDeVoz = "AGRESSIVO";
            this.sentimento = "Insatisfeito";
        } else if (pontosAnsioso == maior) {
            this.tomDeVoz = "ANSIOSO";
            this.sentimento = "Ansioso";
        } else {
            this.tomDeVoz = "POSITIVO";
            this.sentimento = "Satisfeito";
        }

        return this.tomDeVoz;
    }

    @Override
    public String toString() {
        return "\n=== ANALISE ===" +
                "\nCliente: " + (cliente != null ? cliente.getNome() : "N/A") +
                "\nSentimento: " + sentimento +
                "\nTom de Voz: " + tomDeVoz +
                "\nScore Geral: " + scoreGeral +
                "\nProdutos Identificados: " + produtos +
                "\nAlertas: " + alertas +
                "\nReclamacoes: " + reclamacoes;
    }
}