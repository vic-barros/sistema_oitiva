package agenda_oitiva;
import java.util.Scanner;

import agenda_oitiva.controller.SistemaLogin;
import agenda_oitiva.model.Agenda;
import agenda_oitiva.model.CargoFuncional;
import agenda_oitiva.model.Depoente;
import agenda_oitiva.model.FuncionarioDelegacia;
import agenda_oitiva.model.Oitiva;
import agenda_oitiva.model.ProcedimentoPolicial;
import agenda_oitiva.model.Servidor;
import agenda_oitiva.model.StatusOitiva;
import agenda_oitiva.model.TipoPessoa;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SistemaLogin sistemaLogin = new SistemaLogin();
        Agenda agenda = new Agenda();
        

        // Funcionários de teste
        FuncionarioDelegacia f1 = new FuncionarioDelegacia(
            "Laíssa Barros", "057.465.365-12",
            CargoFuncional.POLICIAL, "admin", "admin"
        );
        FuncionarioDelegacia f2 = new FuncionarioDelegacia(
            "Jenisson Nascimento", "057.465.355-40",
            CargoFuncional.ESTAGIARIO, "estagiario", "estagiario"
        );
        sistemaLogin.cadastrarFuncionario(f1);
        sistemaLogin.cadastrarFuncionario(f2);

        try {
            Servidor servidor = new Servidor(agenda, sistemaLogin);
            servidor.iniciar();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }
        
        // Login
        System.out.println("=== SISTEMA DE OITIVAS ===");
        System.out.print("Login: ");
        String login = sc.nextLine();
        System.out.print("Senha: ");
        String senha = sc.nextLine();

        FuncionarioDelegacia usuarioLogado = sistemaLogin.autenticar(login, senha);

        if(usuarioLogado == null) {
            System.out.println("Acesso negado! Login ou senha incorretos.");
            sc.close();
            return;
        }

        System.out.println("Bem-vindo(a), " + usuarioLogado.getNome() +
                           " | Cargo: " + usuarioLogado.getCargo());

        // Menu
        int opcao = 0;
        do {
            System.out.println("\n=== MENU ===");
            if(usuarioLogado.getCargo() == CargoFuncional.POLICIAL) {
                System.out.println("1 - Cadastrar Oitiva");
                System.out.println("2 - Listar Todas as Oitivas");
                System.out.println("3 - Listar por Status");
                System.out.println("4 - Buscar por Pessoa");
                System.out.println("5 - Buscar por Procedimento");
                System.out.println("6 - Alterar Status de Oitiva");
                System.out.println("7 - Remover Oitiva");
                System.out.println("0 - Sair");
            } else {
                System.out.println("1 - Listar Todas as Oitivas");
                System.out.println("2 - Listar por Status");
                System.out.println("3 - Buscar por Pessoa");
                System.out.println("4 - Buscar por Procedimento");
                System.out.println("0 - Sair");
            }

            System.out.print("Opção: ");
            opcao = Integer.parseInt(sc.nextLine());

            if(usuarioLogado.getCargo() == CargoFuncional.POLICIAL) {
                switch(opcao) {
                    case 1:
                        System.out.print("Nome do depoente: ");
                        String nomeDepoente = sc.nextLine();
                        System.out.print("CPF do depoente: ");
                        String cpfDepoente = sc.nextLine();
                        System.out.println("Tipo (1-VITIMA, 2-SUSPEITO, 3-TESTEMUNHA): ");
                        int tipoOp = Integer.parseInt(sc.nextLine());
                        TipoPessoa tipo = tipoOp == 1 ? TipoPessoa.VITIMA :
                                          tipoOp == 2 ? TipoPessoa.SUSPEITO :
                                          TipoPessoa.TESTEMUNHA;
                        Depoente depoente = new Depoente(nomeDepoente, cpfDepoente, tipo);

                        System.out.print("Número da ocorrência: ");
                        int numOc = Integer.parseInt(sc.nextLine());
                        System.out.print("Ano da ocorrência: ");
                        int anoOc = Integer.parseInt(sc.nextLine());
                        System.out.print("Crime: ");
                        String crime = sc.nextLine();
                        ProcedimentoPolicial proc = new ProcedimentoPolicial(numOc, anoOc, crime);

                        System.out.print("Dia: ");
                        int dia = Integer.parseInt(sc.nextLine());
                        System.out.print("Mês: ");
                        int mes = Integer.parseInt(sc.nextLine());
                        System.out.print("Ano: ");
                        int ano = Integer.parseInt(sc.nextLine());
                        System.out.print("Hora: ");
                        int hora = Integer.parseInt(sc.nextLine());
                        System.out.print("Minuto: ");
                        int minuto = Integer.parseInt(sc.nextLine());
                        System.out.print("Observação: ");
                        String obs = sc.nextLine();

                        Oitiva oitiva = new Oitiva(depoente, proc, usuarioLogado,
                                                   dia, mes, ano, hora, minuto, obs);
                        agenda.adicionarOitiva(oitiva);
                        break;

                    case 2:
                        agenda.listarOitivas();
                        break;

                    case 3:
                        System.out.println("Status (1-PENDENTE, 2-AGENDADA, 3-REMARCADA, 4-CANCELADA, 5-REALIZADA): ");
                        int stOp = Integer.parseInt(sc.nextLine());
                        StatusOitiva statusBusca = switch(stOp) {
                            case 1 -> StatusOitiva.AGENDADA;
                            case 2 -> StatusOitiva.REMARCADA;
                            case 3 -> StatusOitiva.CANCELADA;
                            case 4 -> StatusOitiva.REALIZADA;
                            default -> throw new IllegalArgumentException("Opção inválida!");
                        };
                        agenda.listarPorStatus(statusBusca);
                        break;

                    case 4:
                        System.out.print("Nome da pessoa: ");
                        String nomeBusca = sc.nextLine();
                        agenda.buscarPorPessoa(nomeBusca);
                        break;

                    case 5:
                        System.out.print("Número da ocorrência: ");
                        int numBusca = Integer.parseInt(sc.nextLine());
                        System.out.print("Ano da ocorrência: ");
                        int anoBusca = Integer.parseInt(sc.nextLine());
                        agenda.buscarPorProcedimento(numBusca, anoBusca);
                        break;

                    case 6:
                        agenda.listarOitivas();
                        System.out.print("Índice da oitiva: ");
                        int idxAlt = Integer.parseInt(sc.nextLine());
                        System.out.println("Novo status (1-AGENDADA, 2-REALIZADA, 3-REMARCADA, 4-CANCELADA): ");
                        int stAlt = Integer.parseInt(sc.nextLine());
                        StatusOitiva novoStatus = switch(stAlt) {
                            case 1 -> StatusOitiva.AGENDADA;
                            case 2 -> StatusOitiva.REALIZADA;
                            case 3 -> StatusOitiva.REMARCADA;
                            case 4 -> StatusOitiva.CANCELADA;
                            default -> throw new IllegalArgumentException("Opção inválida!");
                        };
                        agenda.alterarStatus(idxAlt, novoStatus);
                        break;

                    case 7:
                        agenda.listarOitivas();
                        System.out.print("Índice da oitiva a remover: ");
                        int idxRem = Integer.parseInt(sc.nextLine());
                        agenda.removerOitiva(idxRem);
                        break;

                    case 0:
                        System.out.println("Saindo...");
                        break;

                    default:
                        System.out.println("Opção inválida!");
                }
            } else {
                switch(opcao) {
                    case 1:
                        agenda.listarOitivas();
                        break;

                    case 2:
                        System.out.println("Status (1-PENDENTE, 2-AGENDADA, 3-REMARCADA, 4-CANCELADA, 5-REALIZADA): ");
                        int stOp = Integer.parseInt(sc.nextLine());
                        StatusOitiva statusBusca = switch(stOp) {
                            case 1 -> StatusOitiva.AGENDADA;
                            case 2 -> StatusOitiva.REMARCADA;
                            case 3 -> StatusOitiva.CANCELADA;
                            case 4 -> StatusOitiva.REALIZADA;
                            default -> throw new IllegalArgumentException("Opção inválida!");
                        };
                        agenda.listarPorStatus(statusBusca);
                        break;

                    case 3:
                        System.out.print("Nome da pessoa: ");
                        String nomeBusca = sc.nextLine();
                        agenda.buscarPorPessoa(nomeBusca);
                        break;

                    case 4:
                        System.out.print("Número da ocorrência: ");
                        int numBusca = Integer.parseInt(sc.nextLine());
                        System.out.print("Ano da ocorrência: ");
                        int anoBusca = Integer.parseInt(sc.nextLine());
                        agenda.buscarPorProcedimento(numBusca, anoBusca);
                        break;

                    case 0:
                        System.out.println("Saindo...");
                        break;

                    default:
                        System.out.println("Opção inválida!");
                }
            }

        } while(opcao != 0);

        sc.close();
    }
}