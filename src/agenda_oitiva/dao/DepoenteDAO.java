package agenda_oitiva.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.Depoente;

public class DepoenteDAO {

    private PessoaDAO pessoaDAO = new PessoaDAO();

    public int inserir(Depoente depoente) {
        int idPessoa = pessoaDAO.inserir(depoente);

        String sql = "INSERT INTO depoente (id_pessoa, tipo_pessoa) " +
                     "VALUES (?, ?::tipo_pessoa) RETURNING id_depoente";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPessoa);
            stmt.setString(2, depoente.getTipoDePessoa().name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_depoente");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir depoente: " + e.getMessage());
        }
        return -1;
    }

    public int buscarIdPorCpf(String cpf) {
        String sql = "SELECT d.id_depoente FROM depoente d " +
                     "JOIN pessoa p ON d.id_pessoa = p.id_pessoa " +
                     "WHERE p.cpf = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf.replaceAll("\\D", ""));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_depoente");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar depoente: " + e.getMessage());
        }
        return -1;
    }
}