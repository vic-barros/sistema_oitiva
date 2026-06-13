package agenda_oitiva;

import java.util.Scanner;

import agenda_oitiva.dao.FuncionarioDAO;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.StatusOitiva;
import agenda_oitiva.model.TipoPessoa;
import agenda_oitiva.model.Depoente;
import agenda_oitiva.model.Oitiva;
import agenda_oitiva.model.ProcedimentoPolicial;
import agenda_oitiva.dao.OitivaDAO;

public class Main {
    public static void main(String[] args) {

        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        OitivaDAO oitivaDAO = new OitivaDAO();

        // Cadastra funcionários no banco se ainda não existirem
        if (funcionarioDAO.buscarPorLogin("admin") == null) {
            FuncionarioDelegacia f1 = new FuncionarioDelegacia(
                "Laíssa Barros", "529.982.247-25",
                CargoFuncional.POLICIAL, "admin", "admin", false
            );
            funcionarioDAO.inserir(f1);
            System.out.println("Funcionário admin cadastrado no banco!");
        }

        if (funcionarioDAO.buscarPorLogin("estagiario") == null) {
            FuncionarioDelegacia f2 = new FuncionarioDelegacia(
                "Jenisson Nascimento", "057.465.365-12",
                CargoFuncional.ESTAGIARIO, "estagiario", "estagiario", false
            );
            funcionarioDAO.inserir(f2);
            System.out.println("Funcionário estagiario cadastrado no banco!");
        }

        // Inicia o servidor HTTP
        try {
            Servidor servidor = new Servidor();
            servidor.iniciar();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }

        // Menu console
        Scanner sc = new Scanner(System.in);
        System.out.println("=== SISTEMA DE OITIVAS ===");
        System.out.print("Login: ");
        String login = sc.nextLine();
        System.out.print("Senha: ");
        String senha = sc.nextLine();

        FuncionarioDelegacia usuarioLogado = funcionarioDAO.buscarPorLogin(login);
        
    }
}
    