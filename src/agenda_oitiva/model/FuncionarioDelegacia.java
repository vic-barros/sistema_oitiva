package agenda_oitiva.model;

import java.security.MessageDigest;

public class FuncionarioDelegacia extends Pessoa {
	private CargoFuncional cargo;
	private String login;
	private String senhaHash;
	private boolean isAdmin;
	private StatusCadastro statusCadastro;
	private int idFuncionario;

	// Construtor normal — recebe senha pura e gera hash (servirá para o banco)
	public FuncionarioDelegacia(String nome, String cpf, CargoFuncional cargo, String login, String senha) {
		super(nome, cpf);
		this.cargo = cargo;
		this.login = login;
		this.senhaHash = gerarHash(senha);
		this.isAdmin = false;
		this.statusCadastro = StatusCadastro.PENDENTE;
	}

	// Construtor para uso do DAO — recebe hash já pronto (recebem os valores reais
	// do banco)
	public FuncionarioDelegacia(int idFuncionario, String nome, String cpf, CargoFuncional cargo, String login,
	        String senhaHash, boolean isAdmin, StatusCadastro statusCadastro) {
	    super(nome, cpf);
	    this.idFuncionario = idFuncionario;
	    this.cargo = cargo;
	    this.login = login;
	    this.senhaHash = senhaHash;
	    this.isAdmin = isAdmin;
	    this.statusCadastro = statusCadastro;
	}
	}

	public FuncionarioDelegacia() {
		super();
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
			throw new RuntimeException("Erro ao gerar hash!", e);
		}
	}

	public boolean verificarSenha(String senha) {
		return this.senhaHash.equals(gerarHash(senha));
	}

	@Override
	public TipoPessoa getTipoDePessoa() {
		return null;
	}

	public CargoFuncional getCargo() {
		return cargo;
	}

	public void setCargo(CargoFuncional cargo) {
		this.cargo = cargo;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public StatusCadastro getStatusCadastro() {
		return statusCadastro;
	}

	public void setStatusCadastro(StatusCadastro statusCadastro) {
		this.statusCadastro = statusCadastro;
	}
	
	public int getIdFuncionario() {
	    return idFuncionario;
	}

	@Override
	public String toString() {
		return "Funcionário | Nome: " + getNome() + " | CPF: " + getCpf() + " | Cargo: " + cargo + " | Adminstrador: "
				+ isAdmin + " | Status do Cadastro: " + statusCadastro;
	}
}