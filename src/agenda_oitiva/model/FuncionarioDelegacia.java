package agenda_oitiva.model;

import java.security.MessageDigest;

public class FuncionarioDelegacia extends Pessoa {
    private CargoFuncional cargo;
    private String login;
    private String senhaHash;

 // Construtor para uso do DAO — recebe hash já pronto
    public FuncionarioDelegacia(String nome, String cpf, CargoFuncional cargo, 
                                 String login, String senhaHash, boolean jaEhHash) {
        super(nome, cpf);
        this.cargo = cargo;
        this.login = login;
        this.senhaHash = jaEhHash ? senhaHash : gerarHash(senhaHash);
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

    public CargoFuncional getCargo() {
        return cargo;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    @Override
    public TipoPessoa getTipoDePessoa() {
        return null;
    }

    @Override
    public String toString() {
        return "Funcionário | Nome: " + getNome()
             + " | CPF: " + getCpf()
             + " | Cargo: " + cargo;
    }
}