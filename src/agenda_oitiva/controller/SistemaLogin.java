package agenda_oitiva.controller;

import java.security.MessageDigest;
import java.util.ArrayList;

import agenda_oitiva.model.FuncionarioDelegacia;

public class SistemaLogin {
	private ArrayList<FuncionarioDelegacia> listaFuncionarios;

	public SistemaLogin() {
		this.listaFuncionarios = new ArrayList<>();
	}

	public void cadastrarFuncionario(FuncionarioDelegacia funcionario) {
		for (FuncionarioDelegacia f : listaFuncionarios) {
			if (f.getLogin().equals(funcionario.getLogin())) {
				throw new IllegalArgumentException("Login já Cadastrado!");
			}

		}
		listaFuncionarios.add(funcionario);
		System.out.println("Funcionário cadastrado com sucesso!");
	}

	public static String gerarHash(String senha) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(senha.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("Erro ao Gerar Hash!", e);
		}
	}

	public FuncionarioDelegacia autenticar(String login, String senha) {
		for (FuncionarioDelegacia f : listaFuncionarios) {
			if (f.getLogin().equals(login) && f.verificarSenha(senha)) {
				return f;
			}

		}
		return null;
	}
	
	public FuncionarioDelegacia buscarPorLogin(String login) {
	    for (FuncionarioDelegacia f : listaFuncionarios) {
	        if (f.getLogin().equals(login)) 
	        return f;
	    }
	    return null;
	}

}
