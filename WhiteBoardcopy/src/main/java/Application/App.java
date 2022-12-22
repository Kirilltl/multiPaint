package Application;
import javax.swing.*;
import Application.Client.Client;
import Application.Server.Server;

public class App {
    public static void main(String[] args) {
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("-S")) {
                System.out.println("Запущен сервер");
                Server server = new Server();
            } else if (args[0].equalsIgnoreCase("-C") && args.length == 3) {
                System.out.println("Запущен клиент");
                SwingUtilities.invokeLater(() -> {
                    Client client = new Client(args[1], Integer.parseInt(args[2]));
                });
            } else {
                System.out.println("[тип(-S/-S)] [адрес сервера(опционально)] [порт сервера(опционально)]");
            }
        } else {
            System.out.println("[тип(-S/-C)] [адрес сервера(опционально)] [порт сервера(опционально)]");
        }
    }
}