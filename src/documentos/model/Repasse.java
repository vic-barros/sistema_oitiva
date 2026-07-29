package documentos.model;

import java.time.LocalDateTime;

import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.ProcedimentoPolicial;

public class Repasse {
	private int idRepasse;
	private ProcedimentoPolicial procedimento;
	private FuncionarioDelegacia funcionarioOrigem;
	private FuncionarioDelegacia funcionarioDestino;
	private LocalDateTime dataSolicitacao;
	private LocalDateTime dataConfirmacao;
	private StatusRepasse status;
	private String observacao;

	public Repasse(ProcedimentoPolicial procedimento, FuncionarioDelegacia funcionarioOrigem,
			FuncionarioDelegacia funcionarioDestino, String observacao) {
		this.procedimento = procedimento;
		this.funcionarioOrigem = funcionarioOrigem;
		this.funcionarioDestino = funcionarioDestino;
		this.dataSolicitacao = LocalDateTime.now();
		this.dataConfirmacao = null;
		this.status = StatusRepasse.PENDENTE;
		this.observacao = observacao;
	}

	// Construtor para reconstrução via DAO — todos os dados já existem no banco
	public Repasse(int idRepasse, ProcedimentoPolicial procedimento, FuncionarioDelegacia funcionarioOrigem,
			FuncionarioDelegacia funcionarioDestino, LocalDateTime dataSolicitacao, LocalDateTime dataConfirmacao,
			StatusRepasse status, String observacao) {
		this.idRepasse = idRepasse;
		this.procedimento = procedimento;
		this.funcionarioOrigem = funcionarioOrigem;
		this.funcionarioDestino = funcionarioDestino;
		this.dataSolicitacao = dataSolicitacao;
		this.dataConfirmacao = dataConfirmacao;
		this.status = status;
		this.observacao = observacao;

	}

	public int getIdRepasse() {
		return idRepasse;
	}

	public ProcedimentoPolicial getProcedimento() {
		return procedimento;
	}

	public void setProcedimento(ProcedimentoPolicial procedimento) {
		this.procedimento = procedimento;
	}

	public FuncionarioDelegacia getFuncionarioOrigem() {
		return funcionarioOrigem;
	}

	public void setFuncionarioOrigem(FuncionarioDelegacia funcionarioOrigem) {
		this.funcionarioOrigem = funcionarioOrigem;
	}

	public FuncionarioDelegacia getFuncionarioDestino() {
		return funcionarioDestino;
	}

	public void setFuncionarioDestino(FuncionarioDelegacia funcionarioDestino) {
		this.funcionarioDestino = funcionarioDestino;
	}

	public LocalDateTime getDataSolicitacao() {
		return dataSolicitacao;
	}

	public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
		if (dataSolicitacao == null) {
			this.dataSolicitacao = LocalDateTime.now();
		} else {
			this.dataSolicitacao = dataSolicitacao;
		}
	}

	public LocalDateTime getDataConfirmacao() {
		return dataConfirmacao;
	}

	public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
		this.dataConfirmacao = dataConfirmacao;
	}

	public StatusRepasse getStatus() {
		return status;
	}

	public void setStatus(StatusRepasse status) {
		this.status = status;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

}
