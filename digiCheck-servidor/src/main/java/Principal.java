import java.io.IOException;

import util.Servidor;
public class Principal {
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        try {
            servidor.iniciar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
