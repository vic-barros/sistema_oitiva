package agenda_oitiva.model;

public class Depoente extends Pessoa {
	private TipoPessoa tipo;

	public Depoente(String nome, String cpf, TipoPessoa tipo) {
		super(nome, cpf);
		this.tipo = tipo;
	}

	@Override
	public TipoPessoa getTipoDePessoa() {
		return tipo;
	}

	@Override
	public String toString() {
	    return "Depoente "
	    	 + "| Nome: " + getNome()
	         + "| CPF: " + getCpf()
	         + "| Tipo de Pessoa: " + tipo;
	}
	
	

}
