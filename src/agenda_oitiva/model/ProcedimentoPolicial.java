package agenda_oitiva.model;
import java.time.LocalDateTime;

public class ProcedimentoPolicial {
	private int numeroOcorrencia;
	private int anoOcorrencia;
	private String crime;

	public ProcedimentoPolicial(int numeroOcorrencia, int anoOcorrencia, String crime) {
		setNumeroOcorrencia(numeroOcorrencia);
		setAnoOcorrencia(anoOcorrencia);
		setCrime(crime);

	}

	public int getNumeroOcorrencia() {
		return numeroOcorrencia;
	}
	
	public void setNumeroOcorrencia(int numeroOcorrencia) {
		if(numeroOcorrencia <= 0) {
			throw new IllegalArgumentException("Número do Ocorrência Negativo ou Zerado!");
		}else {
			this.numeroOcorrencia = numeroOcorrencia;
		}
	}
	

	public int getAnoOcorrencia() {
		return anoOcorrencia;
	}
	
	public void setAnoOcorrencia(int anoOcorrencia) {
		int anoAtual = LocalDateTime.now().getYear();
		if(anoOcorrencia > anoAtual || anoOcorrencia < 1980) {
			throw new IllegalArgumentException("Digite um Ano Válido!");
		}else {
			this.anoOcorrencia = anoOcorrencia;
		}
	}

	public String getCrime() {
		return crime;
	}
	
	public void setCrime(String crime) {
		if(crime == null || crime.strip().isEmpty()) {
			throw new IllegalArgumentException("Crime Nulo ou Vazio");
		}else {
			this.crime = crime;
		}
	}

	@Override
    public String toString() {
        return "Procedimento Policial"
        	+ " | Número da Ocorrência: " + numeroOcorrencia
            + " | Ano: " + anoOcorrencia
            + " | Crime: " + crime;
    }
}
