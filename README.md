# 🚔 Sistema de Gerenciamento de Delegacia

Sistema modular desenvolvido em Java para gerenciamento de delegacia de polícia civil, cobrindo agendamento de oitivas e controle de posse/repasse de procedimentos (documentos), com autenticação de usuários, interface web e persistência em PostgreSQL.

---

## 📋 Sobre o Projeto

Este sistema permite o gerenciamento de duas frentes de trabalho de uma delegacia:

- **Módulo Oitivas** — controle de depoentes, procedimentos policiais e agendamento de oitivas.
- **Módulo Documentos** — controle de posse e repasse (cadeia de custódia) de procedimentos policiais entre funcionários, incluindo arquivamento e uma visão consolidada de todo o acervo (aba "Acervo Procedimental").

Desenvolvido como projeto acadêmico do curso de **Análise e Sistemas de Desenvolvimento (ADS)** no **Instituto Federal de Sergipe**, com aplicação real em ambiente de trabalho da autora na Polícia Civil.

---

## 🛠️ Tecnologias Utilizadas

| Camada | Tecnologia |
|--------|------------|
| Back end | Java 21 |
| Servidor HTTP | `com.sun.net.httpserver` (nativo do JDK) |
| Banco de Dados | PostgreSQL |
| Conexão BD | JDBC |
| Segurança | SHA-256 (hash de senhas) |
| Front end | HTML5, CSS3, JavaScript (vanilla) |

Sem frameworks e sem gerenciador de dependências (Maven/Gradle) — projeto construído com JDK puro, por escolha de arquitetura.

---

## 🗂️ Estrutura do Projeto

```
src/
├── agenda_oitiva/           → Main.java, Servidor.java (porta 8080)
├── agenda_oitiva.config/    → ConexaoBD.java (ignorado no Git)
├── agenda_oitiva.dao/       → FuncionarioDAO, OitivaDAO, PessoaDAO, DepoenteDAO, ProcedimentoDAO
├── agenda_oitiva.model/     → FuncionarioDelegacia, Pessoa, Depoente, Oitiva, ProcedimentoPolicial, enums
│
├── documentos/               → ServidorDocumentos.java (porta 8081)
├── documentos.dao/          → PosseDAO, RepasseDAO
├── documentos.model/        → Posse, Repasse, StatusPosse, StatusRepasse
├── documentos.service/      → RepasseService

frontend/
├── index.html                      → login unificado + seleção de módulo
├── oitivas/oitivas.html            → módulo Oitivas
├── documentos/documentos.html      → módulo Documentos / Cartorário
├── admin/admin.html                → aprovação/recusa de cadastros, reset de senha
├── funcionarios/funcionarios.html  → cadastro de funcionário (acesso: POLICIAL)
└── assets/brasao_pcse.png          → identidade visual

database/
└── schema.sql                → schema completo, incluindo status_posse, status em posse,
                                  constraint UNIQUE em procedimento (num_ocorrencia + ano_ocorrencia)
```

`Main.java` inicia os dois servidores (Oitivas e Documentos) numa única execução — não é necessário rodar dois processos separados.

---

## 🗄️ Modelagem do Banco de Dados

```
pessoa          → dados base (nome, cpf)
├── depoente    → tipo_pessoa (VITIMA, SUSPEITO, TESTEMUNHA)
└── funcionario → login, senha_hash, cargo, status_cadastro, is_admin

procedimento    → num_ocorrencia, ano_ocorrencia, crime
                  (UNIQUE em num_ocorrencia + ano_ocorrencia)

oitiva          → liga depoente + procedimento + funcionario
                  data_hora, status, observacao

posse           → controla quem detém fisicamente um procedimento
                  status_posse, data_remessa_final, protocolo_remessa_final (arquivamento)

repasse         → solicitações de transferência de posse entre funcionários
                  status (PENDENTE, CONFIRMADO, RECUSADO)
```

O script completo, com todos os `CREATE TYPE`/`CREATE TABLE`, está em `database/schema.sql`.

---

## 📂 Acervo Procedimental

Aba do módulo Documentos que lista **todos** os procedimentos do sistema (ativos e arquivados), não só os que estão em posse do usuário logado. Pensada como uma visão de consulta ampla sobre o acervo da delegacia, com:

- Filtro parcial por número de ocorrência (busca por trecho, não precisa ser exato)
- Filtro por status (Todos / Ativo / Arquivado)
- Filtragem 100% client-side — os dados são carregados de uma vez (`GET /documentos/acervo`) e os filtros atuam sobre esse conjunto já em memória, sem nova chamada ao servidor a cada busca

Acesso restrito a `POLICIAL`.

---

## ⚙️ Pré-requisitos

- Java JDK 21+
- PostgreSQL 15+
- Driver JDBC PostgreSQL (`postgresql-42.x.x.jar`)
- Navegador moderno

---

## 🚀 Como Executar

### 1. Clonar o repositório
```bash
git clone [LINK_DO_REPOSITORIO]
```

### 2. Criar o banco de dados
Execute o script `database/schema.sql` no pgAdmin ou psql para criar todas as tabelas, tipos ENUM e constraints.

### 3. Configurar a conexão com o banco
Edite `ConexaoBD.java` (pacote `agenda_oitiva.config`) com as credenciais do seu PostgreSQL local.

> ⚠️ Este arquivo está no `.gitignore` por conter credenciais — cada instalação precisa configurar o seu.

### 4. Adicionar o driver JDBC
- Baixe em [jdbc.postgresql.org](https://jdbc.postgresql.org/download/)
- No Eclipse: botão direito no projeto → `Build Path → Add External JARs`
- Selecione o `.jar` baixado

### 5. Criar o primeiro usuário administrador
Como o sistema não vem com nenhum admin pré-cadastrado, o primeiro precisa ser promovido manualmente:

1. Cadastre um usuário normalmente pela tela de cadastro (`funcionarios.html`)
2. Rode no banco:
   ```sql
   UPDATE funcionario
   SET status_cadastro = 'APROVADO', is_admin = true
   WHERE login = 'SEU_LOGIN_AQUI';
   ```
3. A partir daí, esse usuário pode aprovar os demais cadastros pela tela de Admin.

### 6. Executar o projeto
Rode `Main.java` no Eclipse. O console deve mostrar os dois servidores no ar:
```
Servidor de Oitivas rodando em http://localhost:8080
Servidor de Documentos rodando em http://localhost:8081
```

### 7. Abrir o front end
Abra `frontend/index.html` diretamente no navegador (login unificado, com seleção de módulo em seguida).

---

## 🔐 Controle de Acesso

| Funcionalidade | Policial | Estagiário |
|----------------|----------|------------|
| Módulo Oitivas — cadastrar/alterar/remover | ✅ | ❌ |
| Módulo Oitivas — listar, filtrar, buscar | ✅ | ✅ |
| Módulo Documentos — posse, repasse, arquivamento | ✅ | ✅ (conforme regra de negócio) |
| Cadastro de novo funcionário | ✅ | ❌ |
| Painel Admin (aprovar/recusar cadastros) | Somente `is_admin` | ❌ |
| Aba "Acervo Procedimental" | ✅ | ❌ |

Cada tela do front end valida o acesso de forma independente, com base nos dados salvos em `sessionStorage` após o login (`idFuncionario`, `cargo`, `isAdmin`).

---

## 🔒 Segurança

- Senhas armazenadas com hash **SHA-256**
- Proteção contra **SQL Injection** via `PreparedStatement`
- Controle de acesso por cargo (`POLICIAL` / `ESTAGIARIO`) e por flag `is_admin`
- Fluxo de aprovação de cadastro (novos funcionários entram como `PENDENTE` até aprovação de um admin)

---

## 📚 Conceitos Aplicados

- **POO**: Herança, Polimorfismo, Encapsulamento, Classes Abstratas
- **Padrão DAO**: separação entre lógica de negócio e acesso a dados
- **Camada de Service**: regras de negócio de repasse/arquivamento isoladas em `RepasseService`
- **JDBC**: conexão Java com banco de dados relacional
- **HTTP**: dois servidores nativos do JDK, comunicação via JSON
- **Banco de Dados**: modelagem relacional, normalização, ENUMs, FKs, constraints de unicidade

---

## 🗺️ Roadmap

- Importação de histórico via CSV
- Exportação de relatórios (XLSX/PDF)
- Notificações no cabeçalho (oitivas próximas, repasses recebidos/aceitos/recusados)
- Migração da versão JDBC/DAO atual para JPA + Spring Boot

---

## 👩‍💻 Autora

Desenvolvido por **Victoria** — 3º Período de ADS · IFS · 2026.

---

## 📄 Licença

Este projeto é de uso acadêmico.