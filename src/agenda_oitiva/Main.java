package agenda_oitiva;

import documentos.ServidorDocumentos;

public class Main {
    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor();
            servidor.iniciar();

            ServidorDocumentos servidorDocumentos = new ServidorDocumentos();
            servidorDocumentos.iniciar();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar servidores: " + e.getMessage());
        }
    }
}