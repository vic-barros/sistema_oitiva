package agenda_oitiva.dao;

import java.sql.*;
import java.util.ArrayList;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.*;

public class OitivaDAO {

    private DepoenteDAO depoenteDAO = new DepoenteDAO();
    private ProcedimentoDAO procedimentoDAO = new ProcedimentoDAO();
    private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    public void inserir(Oitiva oitiva) {
        int idDepoente = depoenteDAO.inserir((Depoente) oitiva.getPessoaIntimada());
        int idProcedimento = procedimentoDAO.inserir(oitiva.getProcedimento());
        int idFuncionario = funcionarioDAO.buscarIdPorLogin(
            oitiva.getFuncionarioResponsavel().getLogin()
        );

        String sql = "INSERT INTO oitiva " +
                     "(id_depoente, id_procedimento, id_funcionario, " +
                     "data_hora, status, observacao) " +
                     "VALUES (?, ?, ?, ?, ?::status_oitiva, ?)";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDepoente);
            stmt.setInt(2, idProcedimento);
            stmt.setInt(3, idFuncionario);
            stmt.setTimestamp(4, Timestamp.valueOf(oitiva.getDataHora()));
            stmt.setString(5, oitiva.getStatus().name());
            stmt.setString(6, oitiva.getObservacao());
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir oitiva: " + e.getMessage());
        }
    }

    public ArrayList<Oitiva> listarTodas() {
        String sql = "SELECT o.id_oitiva, p.nome, p.cpf, d.tipo_pessoa, " +
                     "pr.num_ocorrencia, pr.ano_ocorrencia, pr.crime, " +
                     "pf.nome AS nome_funcionario, pf.cpf AS cpf_funcionario, " +
                     "f.login, f.cargo, " +
                     "o.data_hora, o.status, o.observacao " +
                     "FROM oitiva o " +
                     "JOIN depoente d ON o.id_depoente = d.id_depoente " +
                     "JOIN pessoa p ON d.id_pessoa = p.id_pessoa " +
                     "JOIN procedimento pr ON o.id_procedimento = pr.id_procedimento " +
                     "JOIN funcionario f ON o.id_funcionario = f.id_funcionario " +
                     "JOIN pessoa pf ON f.id_pessoa = pf.id_pessoa " +
                     "ORDER BY o.data_hora";

        ArrayList<Oitiva> lista = new ArrayList<>();

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Depoente depoente = new Depoente(
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    TipoPessoa.valueOf(rs.getString("tipo_pessoa"))
                );

                ProcedimentoPolicial proc = new ProcedimentoPolicial(
                    rs.getInt("num_ocorrencia"),
                    rs.getInt("ano_ocorrencia"),
                    rs.getString("crime")
                );

                FuncionarioDelegacia funcionario = new FuncionarioDelegacia();
                funcionario.setNome(rs.getString("nome_funcionario"));
                funcionario.setCpf(rs.getString("cpf_funcionario"));
                funcionario.setCargo(CargoFuncional.valueOf(rs.getString("cargo")));
                funcionario.setLogin(rs.getString("login"));

                Oitiva oitiva = new Oitiva(
                    depoente, proc, funcionario,
                    rs.getTimestamp("data_hora").toLocalDateTime().getDayOfMonth(),
                    rs.getTimestamp("data_hora").toLocalDateTime().getMonthValue(),
                    rs.getTimestamp("data_hora").toLocalDateTime().getYear(),
                    rs.getTimestamp("data_hora").toLocalDateTime().getHour(),
                    rs.getTimestamp("data_hora").toLocalDateTime().getMinute(),
                    rs.getString("observacao")
                );

                oitiva.setStatus(StatusOitiva.valueOf(rs.getString("status")));
                oitiva.setIdOitiva(rs.getInt("id_oitiva"));
                lista.add(oitiva);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar oitivas: " + e.getMessage());
        }
        return lista;
    }

    public ArrayList<Oitiva> buscarPorPessoa(String nome) {
        String sql = "SELECT o.id_oitiva, p.nome, p.cpf, d.tipo_pessoa, " +
                     "pr.num_ocorrencia, pr.ano_ocorrencia, pr.crime, " +
                     "pf.nome AS nome_funcionario, pf.cpf AS cpf_funcionario, " +
                     "f.login, f.cargo, " +
                     "o.data_hora, o.status, o.observacao " +
                     "FROM oitiva o " +
                     "JOIN depoente d ON o.id_depoente = d.id_depoente " +
                     "JOIN pessoa p ON d.id_pessoa = p.id_pessoa " +
                     "JOIN procedimento pr ON o.id_procedimento = pr.id_procedimento " +
                     "JOIN funcionario f ON o.id_funcionario = f.id_funcionario " +
                     "JOIN pessoa pf ON f.id_pessoa = pf.id_pessoa " +
                     "WHERE LOWER(p.nome) LIKE LOWER(?) " +
                     "ORDER BY o.data_hora";

        ArrayList<Oitiva> lista = new ArrayList<>();

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarOitiva(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar por pessoa: " + e.getMessage());
        }
        return lista;
    }

    public ArrayList<Oitiva> listarPorStatus(StatusOitiva status) {
        String sql = "SELECT o.id_oitiva, p.nome, p.cpf, d.tipo_pessoa, " +
                     "pr.num_ocorrencia, pr.ano_ocorrencia, pr.crime, " +
                     "pf.nome AS nome_funcionario, pf.cpf AS cpf_funcionario, " +
                     "f.login, f.cargo, " +
                     "o.data_hora, o.status, o.observacao " +
                     "FROM oitiva o " +
                     "JOIN depoente d ON o.id_depoente = d.id_depoente " +
                     "JOIN pessoa p ON d.id_pessoa = p.id_pessoa " +
                     "JOIN procedimento pr ON o.id_procedimento = pr.id_procedimento " +
                     "JOIN funcionario f ON o.id_funcionario = f.id_funcionario " +
                     "JOIN pessoa pf ON f.id_pessoa = pf.id_pessoa " +
                     "WHERE o.status = ?::status_oitiva " +
                     "ORDER BY o.data_hora";

        ArrayList<Oitiva> lista = new ArrayList<>();

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarOitiva(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar por status: " + e.getMessage());
        }
        return lista;
    }

    public void alterarStatus(int idOitiva, StatusOitiva novoStatus) {
        String sql = "UPDATE oitiva SET status = ?::status_oitiva WHERE id_oitiva = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus.name());
            stmt.setInt(2, idOitiva);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao alterar status: " + e.getMessage());
        }
    }

    public void remover(int idOitiva) {
        String sql = "DELETE FROM oitiva WHERE id_oitiva = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOitiva);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover oitiva: " + e.getMessage());
        }
    }

    // Método auxiliar para montar objeto Oitiva a partir do ResultSet
    private Oitiva montarOitiva(ResultSet rs) throws SQLException {
        Depoente depoente = new Depoente(
            rs.getString("nome"),
            rs.getString("cpf"),
            TipoPessoa.valueOf(rs.getString("tipo_pessoa"))
        );

        ProcedimentoPolicial proc = new ProcedimentoPolicial(
            rs.getInt("num_ocorrencia"),
            rs.getInt("ano_ocorrencia"),
            rs.getString("crime")
        );

        FuncionarioDelegacia funcionario = new FuncionarioDelegacia();
        funcionario.setNome(rs.getString("nome_funcionario"));
        funcionario.setCpf(rs.getString("cpf_funcionario"));
        funcionario.setCargo(CargoFuncional.valueOf(rs.getString("cargo")));
        funcionario.setLogin(rs.getString("login"));

        Oitiva oitiva = new Oitiva(
            depoente, proc, funcionario,
            rs.getTimestamp("data_hora").toLocalDateTime().getDayOfMonth(),
            rs.getTimestamp("data_hora").toLocalDateTime().getMonthValue(),
            rs.getTimestamp("data_hora").toLocalDateTime().getYear(),
            rs.getTimestamp("data_hora").toLocalDateTime().getHour(),
            rs.getTimestamp("data_hora").toLocalDateTime().getMinute(),
            rs.getString("observacao")
        );

        oitiva.setStatus(StatusOitiva.valueOf(rs.getString("status")));
        oitiva.setIdOitiva(rs.getInt("id_oitiva"));
        return oitiva;
    }
}