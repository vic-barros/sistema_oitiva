package documentos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import agenda_oitiva.config.ConexaoBD;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.ProcedimentoPolicial;
import agenda_oitiva.model.StatusCadastro;
import documentos.model.Repasse;
import documentos.model.StatusRepasse;

public class RepasseDAO {

    public int inserir(Repasse repasse) {
        String sql = "INSERT INTO repasse (id_procedimento, id_funcionario_origem, "
                + "id_funcionario_destino, data_solicitacao, observacao) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_repasse";

        try (Connection conn = ConexaoBD.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, repasse.getProcedimento().getIdProcedimento());
            stmt.setInt(2, repasse.getFuncionarioOrigem().getIdFuncionario());
            stmt.setInt(3, repasse.getFuncionarioDestino().getIdFuncionario());
            stmt.setTimestamp(4, Timestamp.valueOf(repasse.getDataSolicitacao()));
            stmt.setString(5, repasse.getObservacao());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_repasse");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir repasse: " + e.getMessage());
        }
        return -1;
    }

    public Repasse buscarPorId(int idRepasse) {
        String sql = "SELECT r.id_repasse, r.data_solicitacao, r.data_confirmacao, r.status, r.observacao, " +
                     "pr.id_procedimento, pr.num_ocorrencia, pr.ano_ocorrencia, pr.crime, " +
                     "f_orig.id_funcionario AS id_origem, p_orig.nome AS nome_origem, " +
                     "p_orig.cpf AS cpf_origem, f_orig.login AS login_origem, " +
                     "f_orig.cargo AS cargo_origem, f_orig.is_admin AS admin_origem, " +
                     "f_orig.status_cadastro AS status_origem, " +
                     "f_dest.id_funcionario AS id_destino, p_dest.nome AS nome_destino, " +
                     "p_dest.cpf AS cpf_destino, f_dest.login AS login_destino, " +
                     "f_dest.cargo AS cargo_destino, f_dest.is_admin AS admin_destino, " +
                     "f_dest.status_cadastro AS status_destino " +
                     "FROM repasse r " +
                     "JOIN procedimento pr ON r.id_procedimento = pr.id_procedimento " +
                     "JOIN funcionario f_orig ON r.id_funcionario_origem = f_orig.id_funcionario " +
                     "JOIN pessoa p_orig ON f_orig.id_pessoa = p_orig.id_pessoa " +
                     "JOIN funcionario f_dest ON r.id_funcionario_destino = f_dest.id_funcionario " +
                     "JOIN pessoa p_dest ON f_dest.id_pessoa = p_dest.id_pessoa " +
                     "WHERE r.id_repasse = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRepasse);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return montarRepasse(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar repasse: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Repasse> listarPendentesPorDestinatario(int idFuncionarioDestino) {
        String sql = "SELECT r.id_repasse, r.data_solicitacao, r.data_confirmacao, r.status, r.observacao, " +
                     "pr.id_procedimento, pr.num_ocorrencia, pr.ano_ocorrencia, pr.crime, " +
                     "f_orig.id_funcionario AS id_origem, p_orig.nome AS nome_origem, " +
                     "p_orig.cpf AS cpf_origem, f_orig.login AS login_origem, " +
                     "f_orig.cargo AS cargo_origem, f_orig.is_admin AS admin_origem, " +
                     "f_orig.status_cadastro AS status_origem, " +
                     "f_dest.id_funcionario AS id_destino, p_dest.nome AS nome_destino, " +
                     "p_dest.cpf AS cpf_destino, f_dest.login AS login_destino, " +
                     "f_dest.cargo AS cargo_destino, f_dest.is_admin AS admin_destino, " +
                     "f_dest.status_cadastro AS status_destino " +
                     "FROM repasse r " +
                     "JOIN procedimento pr ON r.id_procedimento = pr.id_procedimento " +
                     "JOIN funcionario f_orig ON r.id_funcionario_origem = f_orig.id_funcionario " +
                     "JOIN pessoa p_orig ON f_orig.id_pessoa = p_orig.id_pessoa " +
                     "JOIN funcionario f_dest ON r.id_funcionario_destino = f_dest.id_funcionario " +
                     "JOIN pessoa p_dest ON f_dest.id_pessoa = p_dest.id_pessoa " +
                     "WHERE r.id_funcionario_destino = ? AND r.status = ?::status_repasse " +
                     "ORDER BY r.data_solicitacao";

        ArrayList<Repasse> lista = new ArrayList<>();

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionarioDestino);
            stmt.setString(2, StatusRepasse.PENDENTE.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarRepasse(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar repasses pendentes: " + e.getMessage());
        }
        return lista;
    }

    private Repasse montarRepasse(ResultSet rs) throws SQLException {
        ProcedimentoPolicial proc = new ProcedimentoPolicial(
            rs.getInt("id_procedimento"),
            rs.getInt("num_ocorrencia"),
            rs.getInt("ano_ocorrencia"),
            rs.getString("crime")
        );

        FuncionarioDelegacia origem = new FuncionarioDelegacia(
            rs.getInt("id_origem"),
            rs.getString("nome_origem"),
            rs.getString("cpf_origem"),
            CargoFuncional.valueOf(rs.getString("cargo_origem")),
            rs.getString("login_origem"),
            null,
            rs.getBoolean("admin_origem"),
            StatusCadastro.valueOf(rs.getString("status_origem"))
        );

        FuncionarioDelegacia destino = new FuncionarioDelegacia(
            rs.getInt("id_destino"),
            rs.getString("nome_destino"),
            rs.getString("cpf_destino"),
            CargoFuncional.valueOf(rs.getString("cargo_destino")),
            rs.getString("login_destino"),
            null,
            rs.getBoolean("admin_destino"),
            StatusCadastro.valueOf(rs.getString("status_destino"))
        );

        Timestamp tsConfirmacao = rs.getTimestamp("data_confirmacao");

        return new Repasse(
            rs.getInt("id_repasse"),
            proc,
            origem,
            destino,
            rs.getTimestamp("data_solicitacao").toLocalDateTime(),
            tsConfirmacao != null ? tsConfirmacao.toLocalDateTime() : null,
            StatusRepasse.valueOf(rs.getString("status")),
            rs.getString("observacao")
        );
        
    }
    
    public void atualizarStatus(int idRepasse, StatusRepasse novoStatus) {
        String sql = "UPDATE repasse SET status = ?::status_repasse, data_confirmacao = ? " +
                     "WHERE id_repasse = ?";

        try (Connection conn = ConexaoBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, idRepasse);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar status do repasse: " + e.getMessage());
        }
    }
}