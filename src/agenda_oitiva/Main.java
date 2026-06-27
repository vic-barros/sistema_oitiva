package agenda_oitiva;
import agenda_oitiva.dao.FuncionarioDAO;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.dao.OitivaDAO;
public class Main {
    public static void main(String[] args) {
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        OitivaDAO oitivaDAO = new OitivaDAO();

        if (funcionarioDAO.buscarPorLogin("admin") == null) {
            FuncionarioDelegacia f1 = new FuncionarioDelegacia(
                "Laíssa Barros", "011.446.784-63",
                CargoFuncional.POLICIAL, "admin", "admin"
            );
            funcionarioDAO.inserir(f1);
            System.out.println("Funcionário admin cadastrado no banco!");
        }
        if (funcionarioDAO.buscarPorLogin("estagiario") == null) {
            FuncionarioDelegacia f2 = new FuncionarioDelegacia(
                "Jenisson Nascimento", "222.333.444-05",
                CargoFuncional.ESTAGIARIO, "estagiario", "estagiario"
            );
            funcionarioDAO.inserir(f2);
            System.out.println("Funcionário estagiario cadastrado no banco!");
        }

        try {
            Servidor servidor = new Servidor();
            servidor.iniciar();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}