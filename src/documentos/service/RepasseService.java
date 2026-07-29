package documentos.service;

import documentos.dao.PosseDAO;
import documentos.dao.RepasseDAO;
import documentos.model.Posse;
import documentos.model.Repasse;
import documentos.model.StatusPosse;
import documentos.model.StatusRepasse;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.ProcedimentoPolicial;

public class RepasseService {
	private RepasseDAO repasseDAO = new RepasseDAO();
	private PosseDAO posseDAO = new PosseDAO();

	public Repasse solicitaRepasse(ProcedimentoPolicial procedimento, FuncionarioDelegacia funcionarioOrigem,
			FuncionarioDelegacia funcionarioDestino, String observacao) {
		Posse posseAtual = posseDAO.buscarPorProcedimento(procedimento.getIdProcedimento());

		if (posseAtual == null) {
			throw new IllegalStateException("Procedimneto não está registrado no sistema");
		}

		if (posseAtual.getStatus() == StatusPosse.ARQUIVADO) {
			throw new IllegalStateException("Procedimento Policial encontra-se arquivado, não pode ser repassado");
		}

		if (posseAtual.getFuncionarioAtual().getIdFuncionario() != funcionarioOrigem.getIdFuncionario()) {
			throw new IllegalStateException("Você não tem a posse desse procedimento");
		}

		if (repasseDAO.existeRepassePendente(procedimento.getIdProcedimento())) {
			throw new IllegalStateException("Já existe um repasse pendente para este procedimento");
		}

		Repasse novoRepasse = new Repasse(procedimento, funcionarioOrigem, funcionarioDestino, observacao);
		repasseDAO.inserir(novoRepasse);
		return novoRepasse;

	}

	public void confirmarRepasse(int idRepasse, FuncionarioDelegacia funcionarioConfirmando) {
		Repasse repasse = repasseDAO.buscarPorId(idRepasse);

		if (repasse == null) {
			throw new IllegalStateException("Repasse não encontrado - nulo");
		}

		if (repasse.getStatus() != StatusRepasse.PENDENTE) {
			throw new IllegalStateException("Este repasse não está mais pendente");
		}

		if (repasse.getFuncionarioDestino().getIdFuncionario() != funcionarioConfirmando.getIdFuncionario()) {
			throw new IllegalStateException("Você não é o funcionário destinatário deste repasse");
		}

		repasseDAO.atualizarStatus(idRepasse, StatusRepasse.CONFIRMADO);

		posseDAO.atualizar(repasse.getProcedimento().getIdProcedimento(), funcionarioConfirmando.getIdFuncionario());
	}

	public void recusarRepasse(int idRepasse, FuncionarioDelegacia funcionarioRecusando) {
		Repasse repasse = repasseDAO.buscarPorId(idRepasse);

		if (repasse == null) {
			throw new IllegalStateException("Repasse não encontrado");
		}

		if (repasse.getStatus() != StatusRepasse.PENDENTE) {
			throw new IllegalStateException("Este repasse não está mais pendente");
		}

		if (repasse.getFuncionarioDestino().getIdFuncionario() != funcionarioRecusando.getIdFuncionario()) {
			throw new IllegalStateException("Você não é o funcionário destinatário deste repasse");
		}

		// Só atualiza o status — a posse NÃO muda
		repasseDAO.atualizarStatus(idRepasse, StatusRepasse.RECUSADO);
	}
	
	public void arquivarProcedimento(int idProcedimento, FuncionarioDelegacia funcionarioSolicitante) {
		Posse posseAtual = posseDAO.buscarPorProcedimento(idProcedimento);
		if (posseAtual == null) {
			throw new IllegalStateException("Procedimento não está registrado no sistema");
		}
		if (posseAtual.getStatus() == StatusPosse.ARQUIVADO) {
			throw new IllegalStateException("Procedimento já está arquivado");
		}
		if (posseAtual.getFuncionarioAtual().getIdFuncionario() != funcionarioSolicitante.getIdFuncionario()) {
			throw new IllegalStateException("Você não tem a posse desse procedimento");
		}
		if (repasseDAO.existeRepassePendente(idProcedimento)) {
			throw new IllegalStateException("Existe um repasse pendente para este procedimento, precisa aceitar/recusar o repasse para arquivar");
		}
		posseDAO.atualizarStatus(idProcedimento, StatusPosse.ARQUIVADO);
	}

	public void desarquivarProcedimento(int idProcedimento, FuncionarioDelegacia funcionarioSolicitante) {
		Posse posseAtual = posseDAO.buscarPorProcedimento(idProcedimento);
		if (posseAtual == null) {
			throw new IllegalStateException("Procedimento não está registrado no sistema");
		}
		if (posseAtual.getStatus() == StatusPosse.ATIVO) {
			throw new IllegalStateException("Procedimento já está ativo");
		}
		if (posseAtual.getFuncionarioAtual().getIdFuncionario() != funcionarioSolicitante.getIdFuncionario()) {
			throw new IllegalStateException("Você não tem a posse desse procedimento");
		}
		posseDAO.atualizarStatus(idProcedimento, StatusPosse.ATIVO);
	}
}
