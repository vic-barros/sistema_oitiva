package agenda_oitiva.model;

import com.sun.net.httpserver.*;

import agenda_oitiva.controller.SistemaLogin;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Servidor {

    private Agenda agenda;
    private SistemaLogin sistemaLogin;

    public Servidor(Agenda agenda, SistemaLogin sistemaLogin) {
        this.agenda = agenda;
        this.sistemaLogin = sistemaLogin;
    }

    public void iniciar() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/login", this::handleLogin);
        server.createContext("/oitivas", this::handleOitivas);
        server.createContext("/status", this::handleStatus);
        server.createContext("/remover", this::handleRemover);

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
        // valor numérico
        char primeiro = json.substring(inicio).trim().charAt(0);
        if (Character.isDigit(primeiro)) {
            int fim = json.indexOf(",", inicio);
            if (fim == -1) fim = json.indexOf("}", inicio);
            return json.substring(inicio, fim).trim();
        }
        // valor string
        int aspasAbrem = json.indexOf("\"", inicio) + 1;
        int aspasFecham = json.indexOf("\"", aspasAbrem);
        return json.substring(aspasAbrem, aspasFecham);
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

        FuncionarioDelegacia f = sistemaLogin.autenticar(login, senha);

        if (f != null) {
            String json = "{\"sucesso\":true,\"nome\":\"" + f.getNome()
                    + "\",\"cargo\":\"" + f.getCargo() + "\"}";
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
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < agenda.totalOitivas(); i++) {
                Oitiva o = agenda.getOitiva(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                  .append("\"indice\":").append(i).append(",")
                  .append("\"nomeDepoente\":\"").append(o.getPessoaIntimada().getNome()).append("\",")
                  .append("\"cpfDepoente\":\"").append(o.getPessoaIntimada().getCpf()).append("\",")
                  .append("\"tipoPessoa\":\"").append(o.getPessoaIntimada().getTipoDePessoa()).append("\",")
                  .append("\"numOcorrencia\":").append(o.getProcedimento().getNumeroOcorrencia()).append(",")
                  .append("\"anoOcorrencia\":").append(o.getProcedimento().getAnoOcorrencia()).append(",")
                  .append("\"crime\":\"").append(o.getProcedimento().getCrime()).append("\",")
                  .append("\"dia\":").append(o.getDataHora().getDayOfMonth()).append(",")
                  .append("\"mes\":").append(o.getDataHora().getMonthValue()).append(",")
                  .append("\"ano\":").append(o.getDataHora().getYear()).append(",")
                  .append("\"hora\":").append(o.getDataHora().getHour()).append(",")
                  .append("\"minuto\":").append(o.getDataHora().getMinute()).append(",")
                  .append("\"funcionarioResponsavel\":\"").append(o.getFuncionarioResponsavel().getNome()).append("\",")
                  .append("\"status\":\"").append(o.getStatus()).append("\",")
                  .append("\"observacao\":\"").append(o.getObservacao()).append("\"")
                  .append("}");
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
                Depoente depoente = new Depoente(nomeDepoente, cpfDepoente, tipo);
                ProcedimentoPolicial proc = new ProcedimentoPolicial(numOc, anoOc, crime);
                FuncionarioDelegacia resp = sistemaLogin.buscarPorLogin(loginResp);

                Oitiva oitiva = new Oitiva(depoente, proc, resp, dia, mes, ano, hora, minuto, obs);
                agenda.adicionarOitiva(oitiva);
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
        agenda.alterarStatus(indice, StatusOitiva.valueOf(novoStatus));
        enviarResposta(ex, 200, "{\"sucesso\":true}");
    }

    private void handleRemover(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            enviarResposta(ex, 204, "");
            return;
        }
        String corpo = lerCorpo(ex);
        int indice = Integer.parseInt(extrairCampo(corpo, "indice"));
        agenda.removerOitiva(indice);
        enviarResposta(ex, 200, "{\"sucesso\":true}");
    }
}