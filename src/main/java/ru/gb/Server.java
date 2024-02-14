package ru.gb;

import lombok.Getter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    public static int PORT = 8181;
    private static long clientIDCounter = 1l;
    private static Map<Long, SocketWrapper> clients = new HashMap<>();
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                final Socket client = server.accept();
                final long clientId = clientIDCounter++;
                SocketWrapper wrapper = new SocketWrapper(clientId, client);
                System.out.println("Подключился новый клиент[" + wrapper + "]\nСписок всех клиентов: " + clients);
                clients.put(clientId, wrapper);
                clients.values().forEach(it -> it.getOutput().println("Клиент[" + clientId + "] подключился\nСписок всех клиентов: " + clients));

                new Thread(() -> {
                    try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {

                        while (true) {
                            String clientInput = input.nextLine();
                            if (Objects.equals("q", clientInput)) {
                                clients.remove(clientId);
                                clients.values().forEach(it -> it.getOutput().println("Клиент[" + clientId + "] отключился"));
                                System.out.println("Клиент[" + clientId + "] отключился\nСписок всех клиентов: " + clients);
                                break;
                            }
                            // admin commands
                            if(clientInput.equals("admin")){
                                output.println("Please enter password: ");
                                String password = input.nextLine();
                                if(password.equals("12345")){
                                    output.println("Please enter command: ");
                                    String command = input.nextLine();
                                    if(command.substring(0,4).equals("kick")){
                                        long destinationClientId = Long.parseLong(command.substring(4));
                                        SocketWrapper destinationKick = clients.get(destinationClientId);
                                        try {
                                            clients.remove(destinationClientId);
                                            clients.values().forEach(it -> it.getOutput().println("Клиент[" + destinationClientId + "] отключился"));
                                            System.out.println("Клиент[" + destinationClientId + "] отключился\nСписок всех клиентов: " + clients);
                                            output.println("Client "+ destinationKick + " is kicked!");
                                            destinationKick.close();
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }else{
                                    System.out.println("incorrect password");
                                }
                            }

                            // формат сообщения: "@цифра сообщение"
                            if (clientInput.substring(0,1).equals("@")){
                                long destinationId = Long.parseLong(clientInput.substring(1, 2));
                                SocketWrapper destination = clients.get(destinationId);
                                destination.getOutput().println(clientId +": " + clientInput.substring(2));
                            } else if (!clientInput.equals("admin")) {
                                clients.values().stream().filter(it-> it.getId()!= clientId).forEach(it-> it.getOutput().println(clientId +": " +clientInput));
                            }

                        }
                    }
                }).start();
            }
        }
    }
}
@Getter
class SocketWrapper implements AutoCloseable {

    private final long id;
    private final Socket socket;
    private final Scanner input;
    private final PrintWriter output;

    SocketWrapper(long id, Socket socket) throws IOException {
        this.id = id;
        this.socket = socket;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    @Override
    public String toString() {
        return String.format("%s", socket.getInetAddress().toString());
    }
}



