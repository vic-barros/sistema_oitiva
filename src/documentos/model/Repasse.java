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
	

}
