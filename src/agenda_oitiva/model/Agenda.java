package agenda_oitiva.model;

import java.util.ArrayList;

public class Agenda {
	private ArrayList<Oitiva> oitivas;

	private boolean indiceValido(int indice) {
		return indice >= 0 && indice < oitivas.size();
	}

	public Agenda() {
		this.oitivas = new ArrayList<>();
	}

	public void adicionarOitiva(Oitiva oitiva) {
		oitivas.add(oitiva);
		System.out.println("Oitiva Agendada com Sucesso!");
	}

	public void listarOitivas() {
		if (oitivas.isEmpty()) {
			System.out.println("Nenhuma Oitiva Agendada");
			return;
		}
		System.out.println("=== OITIVAS AGENDADAS ===");
		 for(int i = 0; i < oitivas.size(); i++) {
		        System.out.println("[" + i + "] " + oitivas.get(i));
		}
	}

	public void alterarStatus(int indice, StatusOitiva novoStatus) {
		if (!indiceValido(indice)) {
			throw new IllegalArgumentException("índice Inválido");
		}
		oitivas.get(indice).setStatus(novoStatus);
		System.out.println("Status da Oitiva Atualizado com Sucesso");
	}

	public void removerOitiva(int indice) {
		if (!indiceValido(indice)) {
			throw new IllegalArgumentException("índice Inválido");
		}
		oitivas.remove(indice);
		System.out.println("Oitiva Removida com Sucesso");
	}

	public int totalOitivas() {
		return oitivas.size();
	}

	public void buscarPorPessoa(String nome) {
		for (Oitiva oitiva : oitivas) {
			if (oitiva.getPessoaIntimada().getNome().toLowerCase().contains(nome.toLowerCase())) {
				System.out.println(oitiva);
			}
		}
	}

	public void buscarPorProcedimento(int numeroOcorrencia, int anoOcorrencia) {
		for (Oitiva oitiva : oitivas) {
			if (oitiva.getProcedimento().getNumeroOcorrencia() == numeroOcorrencia
					&& oitiva.getProcedimento().getAnoOcorrencia() == anoOcorrencia) {
				System.out.println(oitiva);
			}
		}

	}

	public void listarPorStatus(StatusOitiva status) {
		boolean encontrou = false;
		for (Oitiva oitiva : oitivas) {
			if (oitiva.getStatus() == status) {
				System.out.println(oitiva);
				encontrou = true;
			}
		}
		if (!encontrou) {
			System.out.println("Nenhuma Oitiva com Status: " + status);
		}

	}

	public Oitiva getOitiva(int indice) {
		return oitivas.get(indice);
	}

}
