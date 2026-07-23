package documentos;

import com.sun.net.httpserver.*;
import documentos.model.Posse;
import documentos.model.Repasse;
import documentos.model.StatusPosse;
import documentos.service.RepasseService;
import agenda_oitiva.dao.FuncionarioDAO;
import agenda_oitiva.dao.ProcedimentoDAO;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.ProcedimentoPolicial;
import documentos.dao.PosseDAO;
import documentos.dao.RepasseDAO;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServidorDocumentos {

	private RepasseService repasseService = new RepasseService();
	private PosseDAO posseDAO = new PosseDAO();
	private RepasseDAO repasseDAO = new RepasseDAO();
	private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
	private ProcedimentoDAO procedimentoDAO = new ProcedimentoDAO();

	public void iniciar() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

		server.createContext("/documentos/registrar", this::handleRegistrar);
		server.createContext("/documentos/repassar", this::handleRepassar);
		server.createContext("/documentos/confirmar", this::handleConfirmar);
		server.createContext("/documentos/recusar", this::handleRecusar);
		server.createContext("/documentos/posse", this::handlePosse);
		server.createContext("/documentos/pendentes", this::handlePendentes);
		server.createContext("/documentos/minhaposse", this::handleMinhaPosse);
		server.createContext("/documentos/arquivar", this::handleArquivar);
		server.createContext("/documentos/desarquivar", this::handleDesarquivar);
		server.createContext("/documentos/minhassolicitacoes", this::handleMinhasSolicitacoes);
		server.createContext("/documentos/acervo", this::handleAcervo);

		server.setExecutor(null);
		server.start();
		System.out.println("Servidor de Documentos rodando em http://localhost:8081");
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
		if (idx == -1)
			return "";
		int inicio = json.indexOf(":", idx) + 1;
		char primeiro = json.substring(inicio).trim().charAt(0);
		if (Character.isDigit(primeiro)) {
			int fim = json.indexOf(",", inicio);
			if (fim == -1)
				fim = json.indexOf("}", inicio);
			return json.substring(inicio, fim).trim();
		}
		int aspasAbrem = json.indexOf("\"", inicio) + 1;
		int aspasFecham = json.indexOf("\"", aspasAbrem);
		return json.substring(aspasAbrem, aspasFecham);
	}

	private String extrairParametroUrl(String query, String parametro) {
		if (query == null)
			return "";
		for (String par : query.split("&")) {
			String[] partes = par.split("=");
			if (partes.length == 2 && partes[0].equals(parametro)) {
				return partes[1];
			}
		}
		return "";
	}

	private String escaparJson(String texto) {
		if (texto == null)
			return "";
		return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}

	// ── ROTAS ────────────────────────────────────────────────

	private void handleRegistrar(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		String corpo = lerCorpo(ex);
		try {
			int numOcorrencia = Integer.parseInt(extrairCampo(corpo, "numOcorrencia"));
			int anoOcorrencia = Integer.parseInt(extrairCampo(corpo, "anoOcorrencia"));
			String crime = extrairCampo(corpo, "crime");
			int idFuncionario = Integer.parseInt(extrairCampo(corpo, "idFuncionario"));
			String observacao = extrairCampo(corpo, "observacao");

			// 1. Cria o procedimento
			ProcedimentoPolicial proc = new ProcedimentoPolicial(numOcorrencia, anoOcorrencia, crime);
			int idProcedimento = procedimentoDAO.inserir(proc);

			// 2. Busca o funcionário que vai ficar com a posse inicial
			FuncionarioDelegacia funcionario = funcionarioDAO.buscarPorId(idFuncionario);
			if (funcionario == null) {
				enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Funcionário não encontrado\"}");
				return;
			}

			// 3. Cria a posse inicial
			proc = new ProcedimentoPolicial(idProcedimento, numOcorrencia, anoOcorrencia, crime);
			Posse posse = new Posse(proc, funcionario, observacao);
			posseDAO.inserir(posse);

			enviarResposta(ex, 200, "{\"sucesso\":true,\"idProcedimento\":" + idProcedimento + "}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handleRepassar(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		String corpo = lerCorpo(ex);
		try {
			int idProcedimento = Integer.parseInt(extrairCampo(corpo, "idProcedimento"));
			int idFuncionarioOrigem = Integer.parseInt(extrairCampo(corpo, "idFuncionarioOrigem"));
			int idFuncionarioDestino = Integer.parseInt(extrairCampo(corpo, "idFuncionarioDestino"));
			String observacao = extrairCampo(corpo, "observacao");

			// Busca os objetos completos pelo ID
			Posse posseAtual = posseDAO.buscarPorProcedimento(idProcedimento);
			if (posseAtual == null) {
				enviarResposta(ex, 400,
						"{\"sucesso\":false,\"erro\":\"Procedimento não registrado no sistema cartorário\"}");
				return;
			}

			FuncionarioDelegacia origem = funcionarioDAO.buscarPorId(idFuncionarioOrigem);
			FuncionarioDelegacia destino = funcionarioDAO.buscarPorId(idFuncionarioDestino);

			if (origem == null || destino == null) {
				enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Funcionário não encontrado\"}");
				return;
			}

			repasseService.solicitaRepasse(posseAtual.getProcedimento(), origem, destino, observacao);
			enviarResposta(ex, 200, "{\"sucesso\":true}");

		} catch (IllegalStateException e) {
			// Erros de regra de negócio (posse errada, procedimento arquivado, etc.)
			enviarResposta(ex, 422, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handleConfirmar(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		String corpo = lerCorpo(ex);
		try {
			int idRepasse = Integer.parseInt(extrairCampo(corpo, "idRepasse"));
			int idFuncionario = Integer.parseInt(extrairCampo(corpo, "idFuncionario"));

			FuncionarioDelegacia funcionario = funcionarioDAO.buscarPorId(idFuncionario);
			if (funcionario == null) {
				enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Funcionário não encontrado\"}");
				return;
			}

			repasseService.confirmarRepasse(idRepasse, funcionario);
			enviarResposta(ex, 200, "{\"sucesso\":true}");

		} catch (IllegalStateException e) {
			enviarResposta(ex, 422, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handleRecusar(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		String corpo = lerCorpo(ex);
		try {
			int idRepasse = Integer.parseInt(extrairCampo(corpo, "idRepasse"));
			int idFuncionario = Integer.parseInt(extrairCampo(corpo, "idFuncionario"));

			FuncionarioDelegacia funcionario = funcionarioDAO.buscarPorId(idFuncionario);
			if (funcionario == null) {
				enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Funcionário não encontrado\"}");
				return;
			}

			repasseService.recusarRepasse(idRepasse, funcionario);
			enviarResposta(ex, 200, "{\"sucesso\":true}");

		} catch (IllegalStateException e) {
			enviarResposta(ex, 422, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handlePosse(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		try {
			// Extrai os parâmetros da URL: /documentos/posse?numOcorrencia=1234&anoOcorrencia=2026
			String query = ex.getRequestURI().getQuery();
			int numOcorrencia = Integer.parseInt(extrairParametroUrl(query, "numOcorrencia"));
			int anoOcorrencia = Integer.parseInt(extrairParametroUrl(query, "anoOcorrencia"));

			Posse posse = posseDAO.buscarPorNumeroOcorrencia(numOcorrencia, anoOcorrencia);

			if (posse == null) {
				enviarResposta(ex, 404,
						"{\"sucesso\":false,\"erro\":\"Procedimento não encontrado no sistema cartorário\"}");
				return;
			}

			String json = "{" + "\"sucesso\":true," + "\"idPosse\":" + posse.getIdPosse() + "," + "\"idProcedimento\":"
					+ posse.getProcedimento().getIdProcedimento() + "," + "\"numOcorrencia\":"
					+ posse.getProcedimento().getNumeroOcorrencia() + "," + "\"anoOcorrencia\":"
					+ posse.getProcedimento().getAnoOcorrencia() + "," + "\"crime\":\""
					+ posse.getProcedimento().getCrime() + "\"," + "\"idFuncionarioAtual\":"
					+ posse.getFuncionarioAtual().getIdFuncionario() + "," + "\"funcionarioAtual\":\""
					+ posse.getFuncionarioAtual().getNome() + "\"," + "\"dataPosse\":\"" + posse.getDataPosse() + "\","
					+ "\"status\":\"" + posse.getStatus() + "\"," + "\"observacao\":\""
					+ (posse.getObservacao() != null ? posse.getObservacao() : "") + "\"" + "}";

			enviarResposta(ex, 200, json);

		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handlePendentes(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		try {
			// Extrai o parâmetro da URL: /documentos/pendentes?idFuncionario=1
			String query = ex.getRequestURI().getQuery();
			int idFuncionario = Integer.parseInt(extrairParametroUrl(query, "idFuncionario"));

			ArrayList<Repasse> pendentes = repasseDAO.listarPendentesPorDestinatario(idFuncionario);

			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < pendentes.size(); i++) {
				if (i > 0)
					sb.append(",");
				Repasse r = pendentes.get(i);
				sb.append("{").append("\"idRepasse\":").append(r.getIdRepasse()).append(",")
						.append("\"numOcorrencia\":").append(r.getProcedimento().getNumeroOcorrencia()).append(",")
						.append("\"anoOcorrencia\":").append(r.getProcedimento().getAnoOcorrencia()).append(",")
						.append("\"crime\":\"").append(r.getProcedimento().getCrime()).append("\",")
						.append("\"funcionarioOrigem\":\"").append(r.getFuncionarioOrigem().getNome()).append("\",")
						.append("\"dataSolicitacao\":\"").append(r.getDataSolicitacao()).append("\",")
						.append("\"observacao\":\"").append(r.getObservacao() != null ? r.getObservacao() : "")
						.append("\"").append("}");
			}
			sb.append("]");

			enviarResposta(ex, 200, sb.toString());

		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handleMinhaPosse(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		try {
			// Extrai o parâmetro da URL: /documentos/minhaposse?idFuncionario=1
			String query = ex.getRequestURI().getQuery();
			int idFuncionario = Integer.parseInt(extrairParametroUrl(query, "idFuncionario"));

			ArrayList<Posse> lista = posseDAO.listarPorFuncionario(idFuncionario);

			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < lista.size(); i++) {
				if (i > 0)
					sb.append(",");
				Posse p = lista.get(i);
				sb.append("{").append("\"idProcedimento\":").append(p.getProcedimento().getIdProcedimento())
						.append(",").append("\"numOcorrencia\":").append(p.getProcedimento().getNumeroOcorrencia())
						.append(",").append("\"anoOcorrencia\":").append(p.getProcedimento().getAnoOcorrencia())
						.append(",").append("\"crime\":\"").append(p.getProcedimento().getCrime()).append("\"")
						.append("}");
			}
			sb.append("]");

			enviarResposta(ex, 200, sb.toString());

		} catch (IllegalStateException e) {
			enviarResposta(ex, 422, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}
	
	private void handleMinhasSolicitacoes(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		try {
			String query = ex.getRequestURI().getQuery();
			int idFuncionario = Integer.parseInt(extrairParametroUrl(query, "idFuncionario"));

			ArrayList<Repasse> lista = repasseDAO.listarPorOrigem(idFuncionario);

			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < lista.size(); i++) {
				if (i > 0)
					sb.append(",");
				Repasse r = lista.get(i);
				sb.append("{").append("\"idRepasse\":").append(r.getIdRepasse()).append(",")
						.append("\"numOcorrencia\":").append(r.getProcedimento().getNumeroOcorrencia()).append(",")
						.append("\"anoOcorrencia\":").append(r.getProcedimento().getAnoOcorrencia()).append(",")
						.append("\"crime\":\"").append(r.getProcedimento().getCrime()).append("\",")
						.append("\"funcionarioDestino\":\"").append(r.getFuncionarioDestino().getNome()).append("\",")
						.append("\"dataSolicitacao\":\"").append(r.getDataSolicitacao()).append("\",")
						.append("\"status\":\"").append(r.getStatus()).append("\",")
						.append("\"observacao\":\"").append(r.getObservacao() != null ? r.getObservacao() : "")
						.append("\"").append("}");
			}
			sb.append("]");

			enviarResposta(ex, 200, sb.toString());

		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}
	
	private void handleArquivar(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		String corpo = lerCorpo(ex);
		try {
			int idProcedimento = Integer.parseInt(extrairCampo(corpo, "idProcedimento"));
			int idFuncionario = Integer.parseInt(extrairCampo(corpo, "idFuncionario"));

			FuncionarioDelegacia funcionario = funcionarioDAO.buscarPorId(idFuncionario);
			if (funcionario == null) {
				enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Funcionário não encontrado\"}");
				return;
			}

			repasseService.arquivarProcedimento(idProcedimento, funcionario);
			enviarResposta(ex, 200, "{\"sucesso\":true}");

		} catch (IllegalStateException e) {
			enviarResposta(ex, 422, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}

	private void handleDesarquivar(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		String corpo = lerCorpo(ex);
		try {
			int idProcedimento = Integer.parseInt(extrairCampo(corpo, "idProcedimento"));
			int idFuncionario = Integer.parseInt(extrairCampo(corpo, "idFuncionario"));

			FuncionarioDelegacia funcionario = funcionarioDAO.buscarPorId(idFuncionario);
			if (funcionario == null) {
				enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"Funcionário não encontrado\"}");
				return;
			}

			repasseService.desarquivarProcedimento(idProcedimento, funcionario);
			enviarResposta(ex, 200, "{\"sucesso\":true}");

		} catch (IllegalStateException e) {
			enviarResposta(ex, 422, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}
	
	private void handleAcervo(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			enviarResposta(ex, 204, "");
			return;
		}

		try {
			ArrayList<Posse> lista = posseDAO.listarTodos();

			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < lista.size(); i++) {
				if (i > 0)
					sb.append(",");
				Posse p = lista.get(i);
				sb.append("{").append("\"idProcedimento\":").append(p.getProcedimento().getIdProcedimento())
						.append(",").append("\"numOcorrencia\":").append(p.getProcedimento().getNumeroOcorrencia())
						.append(",").append("\"anoOcorrencia\":").append(p.getProcedimento().getAnoOcorrencia())
						.append(",").append("\"crime\":\"").append(p.getProcedimento().getCrime()).append("\",")
						.append("\"funcionarioAtual\":\"").append(p.getFuncionarioAtual().getNome()).append("\",")
						.append("\"status\":\"").append(p.getStatus()).append("\"")
						.append("}");
			}
			sb.append("]");

			enviarResposta(ex, 200, sb.toString());

		} catch (Exception e) {
			enviarResposta(ex, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
		}
	}
}