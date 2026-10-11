# Java TOTVS — API de Reuniões

## Sobre o projeto

API REST desenvolvida em Java com Spring Boot para consultar e gerenciar reuniões armazenadas em um banco de dados Oracle.

O projeto utiliza uma arquitetura organizada em camadas, separando controllers, serviços, acesso a dados e entidades.

## Tecnologias utilizadas

- Java 17
- Spring Boot
- Maven
- Oracle Database
- JDBC
- Postman para testes manuais da API

## Estrutura do projeto

```text
src/
└── br/com/totvs/conversacional/
    ├── conexoes/
    ├── controller/
    ├── dao/
    ├── entities/
    ├── service/
    └── ConversacionalApplication.java
```

## Configuração do banco de dados

A aplicação utiliza uma conexão JDBC com o banco Oracle.

Configure as propriedades de conexão localmente no arquivo `db.properties`, conforme o exemplo disponibilizado em `db.properties.example`.

**Não compartilhe credenciais reais do banco de dados.**

## Como executar

1. Instale o Java 17 ou compatível com a configuração do projeto.
2. Configure a conexão com o banco Oracle.
3. Abra o projeto no IntelliJ IDEA.
4. Execute a classe principal `ConversacionalApplication`.
5. A API estará disponível localmente na porta 8080, conforme a configuração atual.

## Endpoints disponíveis

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/reunioes` | Lista reuniões |
| GET | `/api/reunioes/{id}` | Busca reunião por ID |
| POST | `/api/reunioes` | Cadastra reunião |
| PUT | `/api/reunioes/{id}` | Atualiza reunião |
| DELETE | `/api/reunioes/{id}` | Exclui reunião |
| GET | `/api/reunioes/segmento/{segmento}` | Filtra reuniões por segmento |
| GET | `/api/teste` | Verifica o funcionamento da API |

## Testes realizados

Foram realizados testes manuais no Postman para verificar:

- Listagem e consulta de reuniões.
- Cadastro de reuniões e validação de dados.
- Rejeição de notas NPS fora do intervalo de 0 a 10.
- Rejeição de duração negativa.
- Atualização e exclusão de reuniões.
- Filtragem por segmento.
- Funcionamento do endpoint de teste.

A compilação e o empacotamento do projeto foram concluídos com sucesso usando o Maven integrado ao IntelliJ IDEA.

## Observações

A execução dos endpoints depende da disponibilidade do banco Oracle e de uma configuração válida de conexão.

Os testes descritos foram realizados manualmente no Postman. Testes automatizados ainda precisam ser adicionados, caso sejam exigidos pelos critérios de avaliação.
