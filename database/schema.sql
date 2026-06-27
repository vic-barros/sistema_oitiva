CREATE TYPE public.cargo_funcional AS ENUM ('POLICIAL', 'ESTAGIARIO');
CREATE TYPE public.status_oitiva AS ENUM ('PENDENTE', 'AGENDADA', 'REMARCADA', 'CANCELADA', 'REALIZADA');
CREATE TYPE public.tipo_pessoa AS ENUM ('VITIMA', 'SUSPEITO', 'TESTEMUNHA');
CREATE TYPE public.status_cadastro_enum AS ENUM ('PENDENTE', 'APROVADO', 'RECUSADO');
CREATE TYPE public.status_repasse AS ENUM ('PENDENTE', 'CONFIRMADO', 'RECUSADO');

CREATE TABLE public.pessoa (
    id_pessoa SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHARACTER(11) UNIQUE NULLS NOT DISTINCT
);

CREATE TABLE public.depoente (
    id_depoente SERIAL PRIMARY KEY,
    id_pessoa INTEGER NOT NULL,
    tipo_pessoa public.tipo_pessoa NOT NULL,
    FOREIGN KEY (id_pessoa) REFERENCES public.pessoa(id_pessoa)
);

CREATE TABLE public.funcionario (
    id_funcionario SERIAL PRIMARY KEY,
    id_pessoa INTEGER NOT NULL,
    login VARCHAR(30) NOT NULL,
    senha_hash CHARACTER(64) NOT NULL,
    cargo public.cargo_funcional NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT false,
    status_cadastro public.status_cadastro_enum NOT NULL DEFAULT 'pendente',
    FOREIGN KEY (id_pessoa) REFERENCES public.pessoa(id_pessoa)
);

CREATE TABLE public.procedimento (
    id_procedimento SERIAL PRIMARY KEY,
    num_ocorrencia INTEGER NOT NULL,
    ano_ocorrencia INTEGER NOT NULL,
    crime VARCHAR(50)
);

CREATE TABLE public.oitiva (
    id_oitiva SERIAL PRIMARY KEY,
    id_depoente INTEGER NOT NULL,
    id_procedimento INTEGER NOT NULL,
    id_funcionario INTEGER NOT NULL,
    data_hora TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    status public.status_oitiva DEFAULT 'PENDENTE'::public.status_oitiva NOT NULL,
    observacao TEXT,
    FOREIGN KEY (id_depoente) REFERENCES public.depoente(id_depoente),
    FOREIGN KEY (id_funcionario) REFERENCES public.funcionario(id_funcionario),
    FOREIGN KEY (id_procedimento) REFERENCES public.procedimento(id_procedimento)
);

CREATE TABLE public.posse (
    id_posse SERIAL PRIMARY KEY,
    id_procedimento INTEGER NOT NULL UNIQUE,
    id_funcionario INTEGER NOT NULL,
    data_posse TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    observacao TEXT,
    FOREIGN KEY (id_procedimento) REFERENCES public.procedimento(id_procedimento),
    FOREIGN KEY (id_funcionario) REFERENCES public.funcionario(id_funcionario)
);

CREATE TABLE public.repasse (
    id_repasse SERIAL PRIMARY KEY,
    id_procedimento INTEGER NOT NULL,
    id_funcionario_origem INTEGER NOT NULL,
    id_funcionario_destino INTEGER NOT NULL,
    data_solicitacao TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    status public.status_repasse NOT NULL DEFAULT 'pendente',
    data_confirmacao TIMESTAMP WITH TIME ZONE,
    observacao TEXT,
    FOREIGN KEY (id_procedimento) REFERENCES public.procedimento(id_procedimento),
    FOREIGN KEY (id_funcionario_origem) REFERENCES public.funcionario(id_funcionario),
    FOREIGN KEY (id_funcionario_destino) REFERENCES public.funcionario(id_funcionario)
);