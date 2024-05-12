package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    private static final int SERVER_PORT = 10000;

    public static void main(String[] args) {
        List<AutoCloseable> resources = new ArrayList<>();

        try (var serverSocket = new ServerSocket(SERVER_PORT)) {
            resources.add(serverSocket);
            System.out.println("Server is running. Waiting for a connection...");

            // корректное завершения сервера при получения сигнала о завершении JVM
            // например, Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverSocket.close();
                    System.out.println("Server stopped.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            while (true) {
                var clientSocket = serverSocket.accept();

                System.out.println("Client connected: " + clientSocket.getInetAddress());

                var clientHandler = new Thread(() -> handleClient(clientSocket));

                clientHandler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // закрываем все ресурсы
            for (var resource : resources) {
                try {
                    resource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            var input = new ObjectInputStream(clientSocket.getInputStream());
            var output = clientSocket.getOutputStream();

            while (true) {
                // получаем сообщение, если длина == 0 -- знак конца коммуникации
                var message = (Message) input.readObject();

                if (message.length() == 0) {
                    break;
                }

                var currentTime = getCurrentDateTime();

                output.write(currentTime.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCurrentDateTime() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }
}
