package agenda_oitiva;

import com.sun.net.httpserver.*;

import agenda_oitiva.dao.DepoenteDAO;
import agenda_oitiva.dao.FuncionarioDAO;
import agenda_oitiva.dao.OitivaDAO;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.Depoente;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.Oitiva;
import agenda_oitiva.model.ProcedimentoPolicial;
import agenda_oitiva.model.StatusCadastro;
import agenda_oitiva.model.StatusOitiva;
import agenda_oitiva.model.TipoPessoa;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Servidor {

    private OitivaDAO oitivaDAO = new OitivaDAO();
    private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    public void iniciar() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/login", this::handleLogin);
        server.createContext("/oitivas", this::handleOitivas);
        server.createContext("/status", this::handleStatus);
        server.createContext("/remover", this::handleRemover);
        server.createContext("/funcionarios", this::handleFuncionarios);

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }

    // ── UTILITÁRIOS ──────────────────────────────────────────

    private void enviarResposta(HttpExchange ex, int codigo, String json) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String lerCorpo(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extrairCampo(String json, String campo) {
        String chave = "\"" + campo + "\"";
        int idx = json.indexOf(chave);
        if (idx == -1) return "";
        int inicio = json.indexOf(":", idx) + 1;
        char primeiro = json.substring(inicio).trim().charAt(0);
        if (Character.isDigit(primeiro)) {
            int fim = json.indexOf(",", inicio);
            if (fim == -1) fim = json.indexOf("}", inicio);
            return json.substring(inicio, fim).trim();
        }
        int aspasAbrem = json.indexOf("\"", inicio) + 1;
        int aspasFecham = json.indexOf("\"", aspasAbrem);
        return json.substring(aspasAbrem, aspasFecham);
    }

    private String montarJsonOitiva(Oitiva o, int indice) {
        return "{" +
            "\"indice\":" + indice + "," +
            "\"idOitiva\":" + o.getIdOitiva() + "," +
            "\"nomeDepoente\":\"" + o.getPessoaIntimada().getNome() + "\"," +
            "\"cpfDepoente\":\"" + (o.getPessoaIntimada().getCpf() != null ? o.getPessoaIntimada().getCpf() : "") + "\"," +
            "\"tipoPessoa\":\"" + o.getPessoaIntimada().getTipoDePessoa() + "\"," +
            "\"numOcorrencia\":" + o.getProcedimento().getNumeroOcorrencia() + "," +
            "\"anoOcorrencia\":" + o.getProcedimento().getAnoOcorrencia() + "," +
            "\"crime\":\"" + o.getProcedimento().getCrime() + "\"," +
            "\"dia\":" + o.getDataHora().getDayOfMonth() + "," +
            "\"mes\":" + o.getDataHora().getMonthValue() + "," +
            "\"ano\":" + o.getDataHora().getYear() + "," +
            "\"hora\":" + o.getDataHora().getHour() + "," +
            "\"minuto\":" + o.getDataHora().getMinute() + "," +
            "\"funcionarioResponsavel\":\"" + o.getFuncionarioResponsavel().getNome() + "\"," +
            "\"status\":\"" + o.getStatus() + "\"," +
            "\"observacao\":\"" + o.getObservacao() + "\"" +
            "}";
    }

    // ── ROTAS ────────────────────────────────────────────────

    private void handleLogin(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            enviarResposta(ex, 204, "");
            return;
        }

        String corpo = lerCorpo(ex);
        String login = extrairCampo(corpo, "login");
        String senha = extrairCampo(corpo, "senha");

        FuncionarioDelegacia f = funcionarioDAO.buscarPorLogin(login);

        if (f != null && f.verificarSenha(senha)) {
            if (f.getStatusCadastro() != StatusCadastro.APROVADO) {
                enviarResposta(ex, 403, "{\"sucesso\":false,\"erro\":\"Cadastro ainda nao foi aprovado\"}");
                return;
            }
            String json = "{\"sucesso\":true,\"nome\":\"" + f.getNome()
                    + "\",\"cargo\":\"" + f.getCargo()
                    + "\",\"login\":\"" + f.getLogin() + "\"}";
            enviarResposta(ex, 200, json);
        } else {
            enviarResposta(ex, 401, "{\"sucesso\":false}");
        }
    }

    private void handleOitivas(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            enviarResposta(ex, 204, "");
            return;
        }

        if (ex.getRequestMethod().equalsIgnoreCase("GET")) {
            ArrayList<Oitiva> lista = oitivaDAO.listarTodas();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(montarJsonOitiva(lista.get(i), i));
            }
            sb.append("]");
            enviarResposta(ex, 200, sb.toString());

        } else if (ex.getRequestMethod().equalsIgnoreCase("POST")) {
            String corpo = lerCorpo(ex);
            try {
                String nomeDepoente = extrairCampo(corpo, "nomeDepoente");
                String cpfDepoente  = extrairCampo(corpo, "cpfDepoente");
                String tipoStr      = extrairCampo(corpo, "tipoPessoa");
                int numOc           = Integer.parseInt(extrairCampo(corpo, "numOcorrencia"));
                int anoOc           = Integer.parseInt(extrairCampo(corpo, "anoOcorrencia"));
                String crime        = extrairCampo(corpo, "crime");
                int dia             = Integer.parseInt(extrairCampo(corpo, "dia"));
                int mes             = Integer.parseInt(extrairCampo(corpo, "mes"));
                int ano             = Integer.parseInt(extrairCampo(corpo, "ano"));
                int hora            = Integer.parseInt(extrairCampo(corpo, "hora"));
                int minuto          = Integer.parseInt(extrairCampo(corpo, "minuto"));
                String obs          = extrairCampo(corpo, "observacao");
                String loginResp    = extrairCampo(corpo, "loginResponsavel");

                TipoPessoa tipo = TipoPessoa.valueOf(tipoStr);
                Depoente depoente = new Depoente(
                    nomeDepoente,
                    cpfDepoente.isBlank() ? null : cpfDepoente,
                    tipo
                );
                ProcedimentoPolicial proc = new ProcedimentoPolicial(numOc, anoOc, crime);
                FuncionarioDelegacia resp = funcionarioDAO.buscarPorLogin(loginResp);

                Oitiva oitiva = new Oitiva(depoente, proc, resp, dia, mes, ano, hora, minuto, obs);
                oitivaDAO.inserir(oitiva);
                enviarResposta(ex, 200, "{\"sucesso\":true}");
            } catch (Exception e) {
                enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    private void handleStatus(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            enviarResposta(ex, 204, "");
            return;
        }
        String corpo = lerCorpo(ex);
        int indice = Integer.parseInt(extrairCampo(corpo, "indice"));
        String novoStatus = extrairCampo(corpo, "status");

        ArrayList<Oitiva> lista = oitivaDAO.listarTodas();
        if (indice >= 0 && indice < lista.size()) {
            int idReal = lista.get(indice).getIdOitiva();
            oitivaDAO.alterarStatus(idReal, StatusOitiva.valueOf(novoStatus));
        }
        enviarResposta(ex, 200, "{\"sucesso\":true}");
    }

    private void handleRemover(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            enviarResposta(ex, 204, "");
            return;
        }
        String corpo = lerCorpo(ex);
        int indice = Integer.parseInt(extrairCampo(corpo, "indice"));

        ArrayList<Oitiva> lista = oitivaDAO.listarTodas();
        if (indice >= 0 && indice < lista.size()) {
            int idReal = lista.get(indice).getIdOitiva();
            oitivaDAO.remover(idReal);
        }
        enviarResposta(ex, 200, "{\"sucesso\":true}");
    }

    private void handleFuncionarios(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            enviarResposta(ex, 204, "");
            return;
        }

        if (ex.getRequestMethod().equalsIgnoreCase("POST")) {
            String corpo = lerCorpo(ex);
            try {
                String nome  = extrairCampo(corpo, "nome");
                String cpf   = extrairCampo(corpo, "cpf");
                String login = extrairCampo(corpo, "login");
                String senha = extrairCampo(corpo, "senha");
                String cargoStr = extrairCampo(corpo, "cargo");

                if (funcionarioDAO.buscarPorLogin(login) != null) {
                    enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Login ja cadastrado!\"}");
                    return;
                }

                CargoFuncional cargo = CargoFuncional.valueOf(cargoStr);
                FuncionarioDelegacia novoFuncionario = new FuncionarioDelegacia(
                    nome, cpf.isBlank() ? null : cpf, cargo, login, senha
                );
                funcionarioDAO.inserir(novoFuncionario);

                enviarResposta(ex, 200, "{\"sucesso\":true}");
            } catch (Exception e) {
                enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}