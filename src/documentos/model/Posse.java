package documentos.model;

import java.time.LocalDateTime;

import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.ProcedimentoPolicial;

public class Posse {
	private int idPosse;
	private ProcedimentoPolicial procedimento;
	private FuncionarioDelegacia funcionarioAtual;
	private LocalDateTime dataPosse;
	private String observacao;

	public Posse(ProcedimentoPolicial procedimento, FuncionarioDelegacia funcionario, String observacao) {
		this.procedimento = procedimento;
		this.funcionarioAtual = funcionario;
		this.dataPosse = LocalDateTime.now();
		this.observacao = observacao;
	}

	public Posse(int idPosse, ProcedimentoPolicial procedimento, FuncionarioDelegacia funcionarioAtual,
			LocalDateTime dataPosse, String observacao) {
		this.idPosse = idPosse;
		this.procedimento = procedimento;
		this.funcionarioAtual = funcionarioAtual;
		this.dataPosse = dataPosse;
		this.observacao = observacao;
	}

	public int getIdPosse() {
		return idPosse;
	}

	public void setIdPosse(int idPosse) {
		this.idPosse = idPosse;
	}

	public ProcedimentoPolicial getProcedimento() {
		return procedimento;
	}

	public void setProcedimento(ProcedimentoPolicial procedimento) {
		this.procedimento = procedimento;
	}

	public FuncionarioDelegacia getFuncionarioAtual() {
		return funcionarioAtual;
	}

	public void setFuncionarioAtual(FuncionarioDelegacia funcionarioAtual) {
		this.funcionarioAtual = funcionarioAtual;
	}

	public LocalDateTime getDataPosse() {
		return dataPosse;
	}

	public void setDataPosse(LocalDateTime dataPosse) {
		if (dataPosse == null) {
			this.dataPosse = LocalDateTime.now();
		} else {
			this.dataPosse = dataPosse;
		}

	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

}
