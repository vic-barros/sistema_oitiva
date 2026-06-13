package agenda_oitiva.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.Pessoa;

public class PessoaDAO {

    /**
     * Insere uma nova pessoa no banco e retorna o id gerado.
     */
    public int inserir(Pessoa pessoa) {
        String sql = "INSERT INTO pessoa (nome, cpf) " +
                     "VALUES (?, ?) RETURNING id_pessoa";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getCpf());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_pessoa");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir pessoa: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Verifica se já existe uma pessoa com aquele CPF no banco.
     */
    public boolean existePorCpf(String cpf) {
        String sql = "SELECT id_pessoa FROM pessoa WHERE cpf = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf.replaceAll("\\D", ""));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar pessoa: " + e.getMessage());
        }
    }

    /**
     * Busca o id de uma pessoa pelo CPF.
     * Útil para evitar duplicatas antes de inserir.
     */
    public int buscarIdPorCpf(String cpf) {
        String sql = "SELECT id_pessoa FROM pessoa WHERE cpf = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf.replaceAll("\\D", ""));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_pessoa");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar id pessoa: " + e.getMessage());
        }
        return -1;
    }
}