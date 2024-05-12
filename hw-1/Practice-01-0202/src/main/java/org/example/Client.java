package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
    private static final Random random = new Random();

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Not enough args: <IP> <port> <N> <M> <Q> [1 - set TCP_NO_DELAY to true]");
            return;
        }

        var serverAddress = args[0];
        var serverPort = Integer.parseInt(args[1]);
        var N = Integer.parseInt(args[2]);
        var M = Integer.parseInt(args[3]);
        var Q = Integer.parseInt(args[4]);
        var isNoDelay = args.length > 5 && args[5].equals("1");

        List<AutoCloseable> resources = new ArrayList<>();

        try (var outputFile = new FileWriter(String.format("output-%d-%d-%d-%s.csv", N, M, Q, isNoDelay ? "no_delay" : "with_delay"));
             var socket = new Socket(serverAddress, serverPort);
             var input = socket.getInputStream();
             var output = new ObjectOutputStream(socket.getOutputStream())) {
            resources.add(outputFile);
            resources.add(socket);
            resources.add(input);
            resources.add(output);
            outputFile.write("Data Size,Time (ns)\n");

            socket.setTcpNoDelay(isNoDelay);

            System.out.println("Connected to server.");

            // корректное завершения клиента при получения сигнала о завершении JVM
            // например, Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    socket.close();
                    System.out.println("Client stopped.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            for (var k = 0; k < M; ++k) {
                var dataSizeBase = N * k + 8;
                var dataSize = dataSizeBase * 100; // N * k + 8;
                long totalElapsedTime = 0;

                for (var q = 0; q < Q; ++q) {
                    var randomBytes = generateRandomBytes(dataSize);
                    var message = new Message(dataSize, randomBytes);
                    long startTime = System.nanoTime(); // System.currentTimeMillis();

                    // отправляем сообщение
                    output.writeObject(message);
                    output.flush();

                    // ожидаем ответ сервера перед следующей итерацией отсылки
                    var buffer = new byte[dataSize];
                    var bytesRead = input.read(buffer);
                    long endTime = System.nanoTime(); // System.currentTimeMillis();

                    if (bytesRead == -1) {
                        break;
                    }

                    var elapsedTime = endTime - startTime;
                    totalElapsedTime += elapsedTime;
                }

                var averageTime = (double) totalElapsedTime / Q;

                outputFile.write(dataSize + "," + averageTime + "\n");
                System.out.println("Iteration: " + k + ", size = " + dataSize + ", Average Time: " + averageTime + " ns");
            }

            // отправка специального сообщения для указания конца коммуникации
            var endMessage = new Message(0, new byte[0]);

            output.writeObject(endMessage);
            output.flush();

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

    private static byte[] generateRandomBytes(int length) {
        var randomBytes = new byte[length];

        random.nextBytes(randomBytes);

        return randomBytes;
    }
}
