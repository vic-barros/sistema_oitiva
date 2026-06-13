package agenda_oitiva.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.ProcedimentoPolicial;

public class ProcedimentoDAO {

    public int inserir(ProcedimentoPolicial proc) {
        String sql = "INSERT INTO procedimento (num_ocorrencia, ano_ocorrencia, crime) " +
                     "VALUES (?, ?, ?) RETURNING id_procedimento";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, proc.getNumeroOcorrencia());
            stmt.setInt(2, proc.getAnoOcorrencia());
            stmt.setString(3, proc.getCrime());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_procedimento");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir procedimento: " + e.getMessage());
        }
        return -1;
    }

    public int buscarIdPorNumeroAno(int numero, int ano) {
        String sql = "SELECT id_procedimento FROM procedimento " +
                     "WHERE num_ocorrencia = ? AND ano_ocorrencia = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            stmt.setInt(2, ano);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_procedimento");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar procedimento: " + e.getMessage());
        }
        return -1;
    }
}