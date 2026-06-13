# 🚔 Sistema de Gerenciamento de Oitivas de Delegacia

Sistema desenvolvido em Java para gerenciamento de oitivas policiais, com autenticação de usuários, interface web e persistência em banco de dados PostgreSQL.

---

## 📋 Sobre o Projeto

Este sistema permite o gerenciamento completo de oitivas em uma delegacia, controlando depoentes, procedimentos policiais, funcionários responsáveis e o status de cada oitiva. Desenvolvido como projeto acadêmico do curso de **Análise e Sistemas de Desenvolvimento (ADS)** no **Instituto Federal de Sergipe**.

---

## 🛠️ Tecnologias Utilizadas

| Camada | Tecnologia |
|--------|------------|
| Back end | Java 21 |
| Servidor HTTP | `com.sun.net.httpserver` (nativo do JDK) |
| Banco de Dados | PostgreSQL |
| Conexão BD | JDBC |
| Segurança | SHA-256 (hash de senhas) |
| Front end | HTML5, CSS3, JavaScript |

---

## 🗂️ Estrutura do Projeto

```
agenda_oitiva/
├── src/
│   ├── agenda_oitiva/
│   │   ├── Main.java               # Ponto de entrada
│   │   └── Servidor.java           # Servidor HTTP (porta 8080)
│   │
│   ├── agenda_oitiva.config/
│   │   └── ConexaoBD.java          # Configuração do banco de dados
│   │
│   ├── agenda_oitiva.dao/
│   │   ├── PessoaDAO.java          # CRUD da tabela pessoa
│   │   ├── DepoenteDAO.java        # CRUD da tabela depoente
│   │   ├── FuncionarioDAO.java     # CRUD da tabela funcionario
│   │   ├── ProcedimentoDAO.java    # CRUD da tabela procedimento
│   │   └── OitivaDAO.java          # CRUD da tabela oitiva
│   │
│   └── agenda_oitiva.model/
│       ├── Pessoa.java             # Classe abstrata base
│       ├── Depoente.java           # Subclasse de Pessoa
│       ├── FuncionarioDelegacia.java # Subclasse de Pessoa
│       ├── Oitiva.java             # Entidade principal
│       ├── ProcedimentoPolicial.java
│       ├── CargoFuncional.java     # Enum: POLICIAL, ESTAGIARIO
│       ├── StatusOitiva.java       # Enum: PENDENTE, AGENDADA...
│       └── TipoPessoa.java         # Enum: VITIMA, SUSPEITO, TESTEMUNHA
│
└── frontend/
    └── sistema_oitivas.html        # Interface web completa
```

---

## 🗄️ Modelagem do Banco de Dados

```sql
pessoa          → dados base (nome, cpf)
├── depoente    → tipo_pessoa (VITIMA, SUSPEITO, TESTEMUNHA)
└── funcionario → login, senha_hash, cargo (POLICIAL, ESTAGIARIO)

procedimento    → num_ocorrencia, ano_ocorrencia, crime

oitiva          → liga depoente + procedimento + funcionario
                  data_hora, status, observacao
```

---

## ⚙️ Pré-requisitos

- Java JDK 21+
- PostgreSQL 15+
- Driver JDBC PostgreSQL (`postgresql-42.x.x.jar`)
- Navegador moderno
- VS Code com extensão **Live Server** (para o front end)

---

## 🚀 Como Executar

### 1. Clonar o repositório
```bash
git clone https://github.com/seu-usuario/sistema-oitivas.git
```

### 2. Criar o banco de dados no PostgreSQL
Execute o script abaixo no pgAdmin ou psql:

```sql
CREATE TYPE tipo_pessoa AS ENUM('VITIMA', 'SUSPEITO', 'TESTEMUNHA');
CREATE TYPE cargo_funcional AS ENUM('POLICIAL', 'ESTAGIARIO');
CREATE TYPE status_oitiva AS ENUM('PENDENTE', 'AGENDADA', 'REMARCADA', 'CANCELADA', 'REALIZADA');

CREATE TABLE pessoa (
    id_pessoa SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE
);

CREATE TABLE depoente (
    id_depoente SERIAL PRIMARY KEY,
    id_pessoa INT NOT NULL REFERENCES pessoa(id_pessoa),
    tipo_pessoa tipo_pessoa NOT NULL
);

CREATE TABLE funcionario (
    id_funcionario SERIAL PRIMARY KEY,
    id_pessoa INT NOT NULL REFERENCES pessoa(id_pessoa),
    login VARCHAR(30) NOT NULL,
    senha_hash CHAR(64) NOT NULL,
    cargo cargo_funcional NOT NULL
);

CREATE TABLE procedimento (
    id_procedimento SERIAL PRIMARY KEY,
    num_ocorrencia INT NOT NULL,
    ano_ocorrencia INT NOT NULL,
    crime VARCHAR(50)
);

CREATE TABLE oitiva (
    id_oitiva SERIAL PRIMARY KEY,
    id_depoente INT NOT NULL REFERENCES depoente(id_depoente),
    id_procedimento INT NOT NULL REFERENCES procedimento(id_procedimento),
    id_funcionario INT NOT NULL REFERENCES funcionario(id_funcionario),
    data_hora TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status status_oitiva NOT NULL DEFAULT 'PENDENTE',
    observacao TEXT
);
```

### 3. Configurar a conexão
Edite o arquivo `ConexaoBD.java` com suas credenciais:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/nome_do_banco";
private static final String USUARIO = "postgres";
private static final String SENHA = "sua_senha";
```

### 4. Adicionar o driver JDBC
- Baixe em [jdbc.postgresql.org](https://jdbc.postgresql.org/download/)
- No Eclipse: botão direito no projeto → `Build Path → Add External JARs`
- Selecione o arquivo `.jar` baixado

### 5. Executar o projeto
- Rode o `Main.java` no Eclipse
- Aguarde a mensagem no console:
```
Servidor rodando em http://localhost:8080
```

### 6. Abrir o front end
- Abra a pasta `frontend` no VS Code
- Clique com botão direito em `sistema_oitivas.html`
- Selecione **Open with Live Server**

### 7. Fazer login
| Login | Senha | Cargo |
|-------|-------|-------|
| admin | admin | Policial |
| estagiario | estagiario | Estagiário |

> ⚠️ Os usuários são cadastrados automaticamente no banco na primeira execução.

---

## 🔐 Funcionalidades por Cargo

| Funcionalidade | Policial | Estagiário |
|----------------|----------|------------|
| Cadastrar oitiva | ✅ | ❌ |
| Listar oitivas | ✅ | ✅ |
| Filtrar por status | ✅ | ✅ |
| Buscar por pessoa | ✅ | ✅ |
| Alterar status | ✅ | ❌ |
| Remover oitiva | ✅ | ❌ |

---

## 🔒 Segurança

- Senhas armazenadas com hash **SHA-256**
- Proteção contra **SQL Injection** via `PreparedStatement`
- Controle de acesso por cargo (`POLICIAL` / `ESTAGIARIO`)
- Validação de CPF com cálculo de dígitos verificadores

---

## 📚 Conceitos Aplicados

- **POO**: Herança, Polimorfismo, Encapsulamento, Classes Abstratas
- **Estrutura de Dados**: ArrayList como estrutura de armazenamento
- **Padrão DAO**: separação entre lógica de negócio e acesso a dados
- **JDBC**: conexão Java com banco de dados relacional
- **HTTP**: servidor e cliente se comunicando via JSON
- **Banco de Dados**: modelagem relacional, normalização, ENUMs, FKs

---

## 👩‍💻 Autora

Desenvolvido por **Victoria** — 2º Período de ADS · IFS · 2026

---

## 📄 Licença

Este projeto é de uso acadêmico.
