# Java TOTVS — API de Reuniões

Projeto da **Sprint 4 (Java / DDD)** — Equipe ZYNOX · FIAP.

## Sobre o projeto

API REST desenvolvida em Java com Spring Boot para cadastrar, consultar, atualizar, excluir e **importar em lote** reuniões armazenadas em um banco de dados Oracle (tabela `REUNIAO`), além de **analisar o risco de churn e o tom de voz** de cada conversa. Um frontend em React consome essa API (projeto `totvs-frontend`, entregue junto).

A arquitetura é organizada em camadas, separando controllers, serviços, acesso a dados (DAO com JDBC puro) e entidades.

```text
[ React :5173 ] --fetch--> [ Controller ] -> [ Service ] -> [ DAO ] --JDBC--> [ Oracle ]
```

## Tecnologias utilizadas

- Java 17
- Spring Boot 3.5.0
- Maven
- Oracle Database (JDBC puro + padrão DAO, driver `ojdbc17`)
- JUnit 5, Mockito e MockMvc para testes automatizados
- Postman para testes manuais da API

## Estrutura do projeto

```text
src/
└── br/com/totvs/conversacional/
    ├── conexoes/        ConnectionFactory (lê o db.properties)
    ├── controller/      ReuniaoController, AnaliseController, TesteController, ApiExceptionHandler, CorsConfig
    ├── dao/             ReuniaoDAO (JDBC)
    ├── dto/             AnaliseResponse (resposta da análise), ResultadoImportacao (resposta da importação em lote)
    ├── entities/        Reuniao, Analise, Analisador, Alerta, RiscoChurn e demais entidades do domínio
    ├── exception/       AcessoDadosException
    ├── service/         ReuniaoService (regras de negócio, validações e importação em lote), AnaliseService, LeitorArquivo (importação de arquivos pelo console)
    ├── main/            TesteSistema (execução em console)
    └── ConversacionalApplication.java
test/
└── br/com/totvs/conversacional/
    ├── controller/      ReuniaoControllerTest, AnaliseControllerTest
    ├── entities/        AnaliseTest
    └── service/         ReuniaoServiceTest, AnaliseServiceTest
```

## Configuração do banco de dados

A aplicação usa JDBC com Oracle. Copie `src/main/resources/br/com/totvs/conversacional/resources/db.properties.example`
para `db.properties` (mesma pasta) e preencha `db.url`, `db.usuario` e `db.senha`.
O arquivo `db.properties` está no `.gitignore`.

**Não compartilhe credenciais reais do banco de dados.**

A tabela `REUNIAO` precisa existir no schema configurado (o DDL está na documentação em PDF). **O cache de análises não precisa de tabela nova**. Ela é a única tabela do projeto: não há chaves estrangeiras apontando para ela, então a exclusão de uma reunião (`DELETE`) remove só a linha da própria tabela. A coluna `TEXTO_ORIGINAL` é `CLOB`, então transcrições longas cabem inteiras.

## Como executar

1. Instale o JDK 17 e configure-o como SDK do projeto no IntelliJ (File > Project Structure).
2. Deixe o Maven importar as dependências.
3. Configure o `db.properties`.
4. Execute a classe `ConversacionalApplication`.
5. A API ficará disponível em `http://localhost:8080` — teste em `http://localhost:8080/api/teste`.
6. Para rodar os testes automatizados: `mvn test`.

### Rodando com o frontend

Em outro terminal, na pasta `totvs-frontend`: `npm install` e `npm run dev`, depois abra `http://localhost:5173`.
O CORS para essa origem já está liberado pela classe `CorsConfig`. Se mudar a porta do front, ajuste `allowedOrigins` nela.

## Endpoints disponíveis

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/teste` | Verifica o funcionamento da API |
| GET | `/api/reunioes` | Lista reuniões |
| GET | `/api/reunioes/{id}` | Busca reunião por ID (com a transcrição completa) |
| GET | `/api/reunioes/segmento/{segmento}` | Filtra reuniões por segmento |
| GET | `/api/reunioes/{id}/analise` | Analisa o risco de churn e o tom de voz de uma reunião |
| GET | `/api/analises` | Analisa **todas** as reuniões em uma única chamada |
| POST | `/api/reunioes` | Cadastra reunião |
| POST | `/api/reunioes/lote` | Importa várias reuniões (até 200 por chamada); ID repetido é ignorado |
| PUT | `/api/reunioes/{id}` | Atualiza reunião |
| DELETE | `/api/reunioes/{id}` | Exclui reunião |

## Códigos de status

| Endpoint | Status |
|---|---|
| GET /api/teste | 200 |
| GET /api/reunioes | 200, 500 |
| GET /api/reunioes/{id} | 200, 400, 404 |
| GET /api/reunioes/segmento/{segmento} | 200, 400 |
| GET /api/reunioes/{id}/analise | 200, 400, 404 |
| GET /api/analises | 200, 500 |
| GET /api/analises/resumo | 200, 500 |
| POST /api/reunioes | 201, 400, 409, 500 |
| POST /api/reunioes/lote | 200, 400, 500 |
| PUT /api/reunioes/{id} | 204, 400, 404, 500 |
| DELETE /api/reunioes/{id} | 204, 404, 500 |

Respostas **400 e 500** trazem JSON no formato `{"erro": "mensagem"}`.
Respostas **404, 409 e 204** vêm **sem corpo** (o frontend traduz isso em mensagens para o usuário).
Métodos/rotas não suportados mantêm o status original do Spring (ex.: 405) com corpo `{"erro": "..."}`.

## Cache persistente das análises (sem alterações no Oracle)

A análise **é calculada uma vez por conteúdo** e salva automaticamente em um arquivo JSON por reunião no servidor Java. Por padrão, os arquivos ficam em `dados/analises/` **relativo à pasta de execução do backend** (por exemplo, `Java-TOTVS/dados/analises` quando a API é iniciada a partir de `Java-TOTVS`). Nenhum botão adicional é necessário. Ao fechar/reabrir o React ou reiniciar a API, os arquivos já salvos continuam sendo usados.

- Para escolher uma pasta persistente fora do projeto, defina a variável de ambiente `VAOX_ANALISES_DIRETORIO` **antes** de iniciar o Java; exemplo no PowerShell: `$env:VAOX_ANALISES_DIRETORIO = 'C:\VAOX\analises'`. A pasta precisa permitir criação e gravação de arquivos. **Em Docker/nuvem, use um volume persistente**, senão o cache pode desaparecer na recriação do container.
- O nome dos arquivos é o hash SHA-256 do ID; a aplicação guarda a assinatura SHA-256 da transcrição e do NPS. Alterar qualquer um deles gera automaticamente uma nova análise; **editar ou excluir pela API também apaga o cache da reunião**.
- Quando as regras de análise forem alteradas no código, incremente `VERSAO_REGRAS` na classe `ArmazenamentoAnalises` para invalidar os arquivos antigos.
- O diretório contém resultados de conversas possivelmente confidenciais: **não publique nem faça commit desse diretório** (já está no `.gitignore`); restrinja seu acesso e faça backup quando necessário.
- As APIs `/api/reunioes` e `/api/reunioes/segmento/{segmento}` agora retornam os dados da tabela **sem a transcrição (`textoOriginal: null`)** para acelerar o painel. Use `GET /api/reunioes/{id}` para obter a transcrição completa, inclusive antes de editar.
- `GET /api/analises/resumo` retorna uma lista com apenas `idReuniao`, `riscoChurn` e `tomDeVoz`. O endpoint antigo `GET /api/analises` continua retornando os relatórios completos.

## Análise de churn (`GET /api/reunioes/{id}/analise`)

Devolve, para uma reunião, o **risco de churn** (Alto, Médio ou Baixo), os motivos do risco, os alertas (com o trecho do texto que os disparou), as reclamações, o tom de voz, o sentimento e o score (0 a 100). A análise é recuperada dos arquivos persistentes quando o conteúdo não mudou, ou calculada e salva na primeira vez. Nada é gravado em tabelas novas do Oracle.

| Risco | Quando acontece |
|---|---|
| Alto | Há alerta de CHURN (cancelar, rescindir, não renovar...) **ou** tom de voz agressivo com NPS até 6 |
| Médio | Há alerta de CONCORRENTE **ou** 2 ou mais reclamações **ou** NPS até 6 |
| Baixo | Nenhum dos casos acima |

**É uma regra por palavra-chave** (listas fixas de termos em português), **não é probabilidade nem modelo de machine learning**; a própria resposta traz o campo `metodo` com essa observação.

Cuidados da implementação:

- Texto da reunião nulo é aceito (a resposta traz um aviso); o texto é comparado em minúsculas e sem acentos (assim "preço" casa com "preco"); termos só casam como palavra inteira ("sap" não casa com "sapato").
- A lista de concorrentes (SAP, Oracle, Senior, Linx e termos genéricos como "concorrente") **não inclui produtos da própria TOTVS** (Protheus, Datasul, RM, Fluig).
- **Negação:** um termo precedido de "não", "nunca", "jamais", "nem", "sem" ou "nada" nas 3 palavras anteriores, na mesma oração, não conta ("não é péssimo", "sem bugs", "não vamos cancelar o contrato"). Elogio negado ("não estou satisfeito") conta como insatisfação.
- **Termos ambíguos:** "suporte", "atendimento" e "preço" só viram reclamação se a frase em volta não for um elogio ("o suporte foi excelente" não é reclamação).

### Tom de voz

O tom é escolhido pelo maior número de termos encontrados em três listas fixas. Sem nenhum termo reconhecido, o tom é `NEUTRO`. Em caso de empate, a ordem é Agressivo, depois Ansioso, depois Positivo.

| Tom | Sentimento | Exemplos de termos |
|---|---|---|
| `AGRESSIVO` | Insatisfeito | absurdo, péssimo, insatisfeito, decepcionado, frustrado, descaso |
| `ANSIOSO` | Ansioso | urgente, preciso logo, prazo apertado, atrasado, emergência |
| `POSITIVO` | Satisfeito | ótimo, excelente, perfeito, satisfeito, adorei, gostei |
| `NEUTRO` | Neutro | (nenhum termo reconhecido) |

Exemplo de resposta (200):

```json
{
  "idReuniao": "ZYNOX-001",
  "riscoChurn": "ALTO",
  "riscoRotulo": "Alto",
  "motivosRisco": ["Cliente sinalizou intenção de cancelar ou não renovar o contrato."],
  "scoreGeral": 25,
  "tomDeVoz": "AGRESSIVO",
  "sentimento": "Insatisfeito",
  "alertas": [{"tipo": "CHURN", "descricao": "Sinal de risco de cancelamento detectado",
               "urgencia": "ALTA", "trechoOrigem": "...Vamos cancelar o contrato e olhar a SAP."}],
  "reclamacoes": ["SUPORTE: suporte", "PRECO: caro"],
  "avisos": [],
  "metodo": "Regras por palavra-chave aplicadas ao texto da reunião; não é modelo estatístico nem de machine learning."
}
```

Reunião inexistente retorna **404** (sem corpo) e ID vazio retorna **400**.

## Análise de todas as reuniões (`GET /api/analises`)

Devolve uma lista de objetos no mesmo formato acima, um por reunião da tabela. A análise roda de forma sequencial no servidor para não saturar a CPU; uma reunião que falhar é registrada no log da API e fica de fora da lista, sem derrubar as outras (o frontend mostra essas reuniões como sem análise). O frontend usa essa chamada para preencher o selo de risco, o tom de voz e a barra de risco de todas as reuniões de uma vez (sem uma requisição por reunião). Os resultados salvos são reutilizados após recarregar o painel, reduzindo o custo de CPU de chamadas repetidas. Na primeira análise de uma grande base: em um teste com 1000 transcrições longas (da ordem de dezenas de KB cada, algumas de ~1 MB) a análise levou poucos segundos.

## Importação em lote (`POST /api/reunioes/lote`)

Recebe um **array JSON** de reuniões (mesmo formato do `POST /api/reunioes`), até **200 por chamada** (o frontend envia 50 por vez). Cada reunião passa pelas mesmas validações do cadastro; as inválidas não derrubam as demais.

- ID que já existe (ORA-00001) é **ignorado** e contado como duplicado; dá para reenviar o mesmo arquivo sem duplicar.
- Qualquer outro erro de uma linha (por exemplo, valor maior que a coluna do Oracle) entra na lista `erros`.
- Se `dataCriacao` não vier, o Service preenche com a data/hora atual.
- Lista vazia ou acima de 200 reuniões retorna **400**.

Exemplo de resposta (200):

```json
{
  "recebidas": 50,
  "inseridas": 48,
  "duplicadas": 1,
  "erros": ["ZYNOX-049: A nota NPS deve estar entre 0 e 10."]
}
```

## Validações (ReuniaoService)

| Regra | Mensagem (HTTP 400) |
|---|---|
| ID obrigatório | O ID da reunião é obrigatório. |
| ID com no máximo 100 caracteres | O ID deve ter no máximo 100 caracteres. |
| Duração não negativa | A duração não pode ser negativa. |
| Duração até 99999 | A duração máxima é 99999 minutos. |
| NPS entre 0 e 10 | A nota NPS deve estar entre 0 e 10. |
| UF com no máximo 2 caracteres | A UF deve ter no máximo 2 caracteres. |
| PUT com ID da URI diferente do ID do corpo | O ID da URI deve ser igual ao ID do corpo. |
| Segmento obrigatório no filtro | O segmento é obrigatório. |
| Lote não pode ser vazio | Nenhuma reunião para importar. |
| Lote com no máximo 200 reuniões | Envie no máximo 200 reuniões por vez. |

No lote, as regras de cada reunião (ID, duração, NPS e UF) não geram 400: a mensagem vai para a lista `erros` da resposta.

ID duplicado no POST retorna **409** (violação de unicidade ORA-00001 tratada no DAO).
Se `dataCriacao` não for enviada no cadastro, o Service preenche com a data/hora atual.

## Testes

- **Automatizados (JUnit 5 + Mockito + MockMvc):** `ReuniaoServiceTest` (20), `ReuniaoControllerTest` (17), `AnaliseTest` (29), `AnaliseServiceTest` (7) e `AnaliseControllerTest` (4) — 77 testes no total. Execute com `mvn test`.
- **Manuais (Postman):** cadastro, listagem, consulta, filtro por segmento, atualização, exclusão e validações (evidências na documentação em PDF).

## Limitações conhecidas

- O risco de churn e o tom de voz são estimativas por palavra-chave (listas fixas em português), não um modelo estatístico; podem errar em ironias, gírias e frases que não usam nenhum termo das listas. A negação só é tratada nas 3 palavras anteriores.
- As análises são calculadas a cada consulta e não ficam gravadas no banco.
- `GET /api/reunioes` devolve a transcrição de todas as reuniões; com milhares de reuniões a resposta fica grande.
- No lote, campos de texto acima do tamanho da coluna (por exemplo, `SEGMENTO` com mais de 100 caracteres) são rejeitados pelo Oracle e aparecem em `erros`; no cadastro e na edição individuais, retornam 500.
- Não há autenticação nem paginação.
