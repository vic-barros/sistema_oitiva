package agenda_oitiva.model;

import agenda_oitiva.controller.SistemaLogin;

public class FuncionarioDelegacia extends Pessoa {
    private CargoFuncional cargo;
    private String login;
    private String senhaHash;
    

    public FuncionarioDelegacia(String nome, String cpf, CargoFuncional cargo, String login, String senha) {
        super(nome, cpf);
        this.cargo = cargo;
        this.login = login;
        this.senhaHash = SistemaLogin.gerarHash(senha);
    }
    
    public boolean verificarSenha(String senha) {
    	return this.senhaHash.equals(SistemaLogin.gerarHash(senha));
    }

    public CargoFuncional getCargo() {
        return cargo;
    }

    @Override
    public TipoPessoa getTipoDePessoa() {
        return null;
    }
    
    public String getLogin() {
    	return login;
    }

    @Override
    public String toString() {
        return "Funcionário | Nome: " + getNome()
             + " | CPF: " + getCpf()
             + " | Cargo: " + cargo;
    }
}
