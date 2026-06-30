package documentos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.ProcedimentoPolicial;
import agenda_oitiva.model.StatusCadastro;
import documentos.model.Posse;

public class PosseDAO {

	public int inserir(Posse posse) {
		String sql = "INSERT INTO posse (id_procedimento, id_funcionario, data_posse, observacao) "
				+ "VALUES (?, ?, ?, ?) RETURNING id_posse";

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, posse.getProcedimento().getIdProcedimento());
			stmt.setInt(2, posse.getFuncionarioAtual().getIdFuncionario());
			stmt.setTimestamp(3, Timestamp.valueOf(posse.getDataPosse()));
			stmt.setString(4, posse.getObservacao());

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return rs.getInt("id_posse");
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao inserir posse: " + e.getMessage());
		}
		return -1;
	}
	
	public void atualizar(int idProcedimento, int idFuncionarioNovo) {
	    String sql = "UPDATE posse SET id_funcionario = ?, data_posse = ? " +
	                 "WHERE id_procedimento = ?";

	    try (Connection conn = ConexaoBD.conectar();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, idFuncionarioNovo);
	        stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
	        stmt.setInt(3, idProcedimento);
	        stmt.executeUpdate();

	    } catch (Exception e) {
	        throw new RuntimeException("Erro ao atualizar posse: " + e.getMessage());
	    }
	}
	
	public Posse buscarPorProcedimento(int idProcedimento) {
	    String sql = "SELECT po.id_posse, po.data_posse, po.observacao, " +
	                 "pr.id_procedimento, pr.num_ocorrencia, pr.ano_ocorrencia, pr.crime, " +
	                 "f.id_funcionario, pf.nome, pf.cpf, f.login, f.cargo, f.is_admin, f.status_cadastro " +
	                 "FROM posse po " +
	                 "JOIN procedimento pr ON po.id_procedimento = pr.id_procedimento " +
	                 "JOIN funcionario f ON po.id_funcionario = f.id_funcionario " +
	                 "JOIN pessoa pf ON f.id_pessoa = pf.id_pessoa " +
	                 "WHERE po.id_procedimento = ?";

	    try (Connection conn = ConexaoBD.conectar();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, idProcedimento);

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return montarPosse(rs);
	            }
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Erro ao buscar posse: " + e.getMessage());
	    }
	    return null;
	}
	
	private Posse montarPosse(ResultSet rs) throws SQLException {
	    ProcedimentoPolicial proc = new ProcedimentoPolicial(
	        rs.getInt("id_procedimento"),
	        rs.getInt("num_ocorrencia"),
	        rs.getInt("ano_ocorrencia"),
	        rs.getString("crime")
	    );

	    StatusCadastro statusCadastro = StatusCadastro.valueOf(rs.getString("status_cadastro"));

	    FuncionarioDelegacia funcionario = new FuncionarioDelegacia(
	        rs.getInt("id_funcionario"),
	        rs.getString("nome"),
	        rs.getString("cpf"),
	        CargoFuncional.valueOf(rs.getString("cargo")),
	        rs.getString("login"),
	        null, // senha não é necessária aqui, só exibição
	        rs.getBoolean("is_admin"),
	        statusCadastro
	    );

	    return new Posse(
	        rs.getInt("id_posse"),
	        proc,
	        funcionario,
	        rs.getTimestamp("data_posse").toLocalDateTime(),
	        rs.getString("observacao")
	    );
	}
	
	
	
}