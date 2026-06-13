package agenda_oitiva.model;
import java.time.LocalDateTime;

public class Oitiva {
	private Pessoa pessoaIntimada;
	private FuncionarioDelegacia funcionarioResponsavel;
	private ProcedimentoPolicial procedimento;
	private LocalDateTime dataHora;
	private StatusOitiva status;
	private String observacao;
	
	public Oitiva(Pessoa pessoaIntimada, ProcedimentoPolicial procedimento, FuncionarioDelegacia funcionarioResponsavel,
			int dia, int mes, int ano, int hora, int minuto, String observacao) {
		this.pessoaIntimada = pessoaIntimada;
		this.procedimento = procedimento;
		this.funcionarioResponsavel = funcionarioResponsavel;
		this.dataHora = LocalDateTime.of(ano, mes, dia, hora, minuto);
		this.observacao = (observacao != null) ? observacao : "";
		this.status = StatusOitiva.PENDENTE;
	}

	public Pessoa getPessoaIntimada() {
		return pessoaIntimada;
	}

	public ProcedimentoPolicial getProcedimento() {
		return procedimento;
	}
	
	public FuncionarioDelegacia getFuncionarioResponsavel() {
	    return funcionarioResponsavel;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public StatusOitiva getStatus() {
		return status;
	}

	public String getObservacao() {
		return observacao;
	}
	
	public void setStatus(StatusOitiva status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
	    return "=== OITIVA ===" 
	         + "\nData/Hora : " + dataHora.getDayOfMonth() + "/" 
	                            + dataHora.getMonthValue() + "/" 
	                            + dataHora.getYear()
	                            + " às " + dataHora.getHour() 
	                            + "h" + String.format("%02d", dataHora.getMinute())
	         + "\nPessoa    : " + pessoaIntimada.getNome()
	         + "\nFuncionário  : " + funcionarioResponsavel.getNome()
	         + "\nProcedimento: " + procedimento
	         + "\nStatus    : " + status
	         + "\nObs       : " + observacao;
	}

	private int idOitiva;

	public void setIdOitiva(int idOitiva) {
	    this.idOitiva = idOitiva;
	}

	public int getIdOitiva() {
	    return idOitiva;
	}

	

}

