package agenda_oitiva.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.StatusCadastro;

public class FuncionarioDAO {

	private PessoaDAO pessoaDAO = new PessoaDAO();

	public int inserir(FuncionarioDelegacia funcionario) {
		int idPessoa = pessoaDAO.inserir(funcionario);

		String sql = "INSERT INTO funcionario (id_pessoa, login, senha_hash, cargo," + " is_admin, status_cadastro) "
				+ "VALUES (?, ?, ?, ?::cargo_funcional, ?, ?::status_cadastro_enum) RETURNING id_funcionario";

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, idPessoa);
			stmt.setString(2, funcionario.getLogin());
			stmt.setString(3, funcionario.getSenhaHash());
			stmt.setString(4, funcionario.getCargo().name());
			stmt.setBoolean(5, funcionario.isAdmin());
			stmt.setString(6, funcionario.getStatusCadastro().name());

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return rs.getInt("id_funcionario");
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao inserir funcionario: " + e.getMessage());
		}
		return -1;
	}

	public FuncionarioDelegacia buscarPorLogin(String login) {
		String sql = "SELECT f.id_funcionario, p.nome, p.cpf, f.login, f.senha_hash, f.cargo, "
				+ "f.is_admin, f.status_cadastro " + "FROM funcionario f "
				+ "JOIN pessoa p ON f.id_pessoa = p.id_pessoa " + "WHERE f.login = ?";
		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, login);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					CargoFuncional cargo = CargoFuncional.valueOf(rs.getString("cargo"));
					StatusCadastro statusCadastro = StatusCadastro.valueOf(rs.getString("status_cadastro"));
					return new FuncionarioDelegacia(rs.getInt("id_funcionario"), rs.getString("nome"),
							rs.getString("cpf"), cargo, rs.getString("login"), rs.getString("senha_hash").trim(),
							rs.getBoolean("is_admin"), statusCadastro);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao buscar funcionario: " + e.getMessage());
		}
		return null;
	}

	public int buscarIdPorLogin(String login) {
		String sql = "SELECT id_funcionario FROM funcionario WHERE login = ?";

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, login);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return rs.getInt("id_funcionario");
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao buscar id funcionario: " + e.getMessage());
		}
		return -1;
	}

	public FuncionarioDelegacia buscarPorId(int idFuncionario) {
		String sql = "SELECT f.id_funcionario, p.nome, p.cpf, f.login, f.senha_hash, f.cargo, "
				+ "f.is_admin, f.status_cadastro " + "FROM funcionario f "
				+ "JOIN pessoa p ON f.id_pessoa = p.id_pessoa " + "WHERE f.id_funcionario = ?";

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, idFuncionario);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					CargoFuncional cargo = CargoFuncional.valueOf(rs.getString("cargo"));
					StatusCadastro statusCadastro = StatusCadastro.valueOf(rs.getString("status_cadastro"));
					return new FuncionarioDelegacia(rs.getInt("id_funcionario"), rs.getString("nome"),
							rs.getString("cpf"), cargo, rs.getString("login"), rs.getString("senha_hash").trim(),
							rs.getBoolean("is_admin"), statusCadastro);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao buscar funcionário por ID: " + e.getMessage());
		}
		return null;
	}

	public ArrayList<FuncionarioDelegacia> listarPendentes() {
		String sql = "SELECT f.id_funcionario, p.nome, p.cpf, f.login, f.senha_hash, f.cargo, "
				+ "f.is_admin, f.status_cadastro " + "FROM funcionario f "
				+ "JOIN pessoa p ON f.id_pessoa = p.id_pessoa " + "WHERE f.status_cadastro = ?::status_cadastro_enum";

		ArrayList<FuncionarioDelegacia> lista = new ArrayList<>();

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, StatusCadastro.PENDENTE.name());

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					CargoFuncional cargo = CargoFuncional.valueOf(rs.getString("cargo"));
					StatusCadastro statusCadastro = StatusCadastro.valueOf(rs.getString("status_cadastro"));
					lista.add(new FuncionarioDelegacia(rs.getInt("id_funcionario"), rs.getString("nome"),
							rs.getString("cpf"), cargo, rs.getString("login"), rs.getString("senha_hash").trim(),
							rs.getBoolean("is_admin"), statusCadastro));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao listar pendentes: " + e.getMessage());
		}
		return lista;
	}

	public void atualizarStatus(int idFuncionario, StatusCadastro novoStatus) {
		String sql = "UPDATE funcionario SET status_cadastro = ?::status_cadastro_enum " + "WHERE id_funcionario = ?";

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, novoStatus.name());
			stmt.setInt(2, idFuncionario);
			stmt.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Erro ao atualizar status do cadastro: " + e.getMessage());
		}
	}

	public void resetarSenha(int idFuncionario, String novaSenha) {
		String sql = "UPDATE funcionario SET senha_hash = ? WHERE id_funcionario = ?";

		try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, FuncionarioDelegacia.gerarHash(novaSenha));
			stmt.setInt(2, idFuncionario);
			stmt.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException("Erro ao resetar senha: " + e.getMessage());
		}
	}
}