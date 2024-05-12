package org.example;

import org.example.Utils.Dns.DnsCapture;
import org.example.Utils.Dns.MxIpDetector;
import org.example.Utils.Dns.RootServerHandler;
import org.example.Utils.Dns.ShutdownHandlerDns;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;

import java.util.List;
import java.util.Scanner;

public class CommandInterpreter {
    private final DnsCapture dnsCapture;
    private final RootServerHandler rootServerHandler;

    public CommandInterpreter(PcapNetworkInterface nif, int snapLen,
                              PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException {
        this.dnsCapture = new DnsCapture(nif, snapLen, mode, timeout);
        this.rootServerHandler = new RootServerHandler(nif, snapLen, mode, timeout);
    }

    public void startCommandInterpreter()
            throws PcapNativeException, NotOpenException {
        var scanner = new Scanner(System.in);
        var running = true;

        ShutdownHandlerDns shutdownHandler = new ShutdownHandlerDns(
                dnsCapture, rootServerHandler);
        Runtime.getRuntime().addShutdownHook(shutdownHandler);

        System.out.println("Welcome to Command Interpreter");

        while (running) {
            System.out.print("Enter a command (type 'help' for available commands): ");

            var command = scanner.nextLine();

            switch (command.toLowerCase()) {
                case "help":
                    printAvailableCommands();
                    break;
                case "dns capture":
                    System.out.println("Starting DNS packet capture...");
                    dnsCapture.startCapturing();
                    System.out.println("DNS packet capture completed");
                    break;
                case "print packets":
                    dnsCapture.printCapturedPacketInfo();
                    break;
                case "find mx":
                    System.out.print("Enter target domain name: ");

                    var targetDomain = scanner.nextLine();

                    MxIpDetector.find(targetDomain, dnsCapture.getCapturedPackets())
                            .forEach(x -> System.out.println(targetDomain + " -> " + x));
                    break;
                case "query root server":
                    System.out.print("Enter target domain name: ");

                    var targetDomains = List.of(scanner.nextLine().split(" "));

                    targetDomains.forEach(rootServerHandler::handleDomain);
                    rootServerHandler.stopHandle();
                    break;
                case "exit":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
                    break;
            }
        }

        dnsCapture.stopCapturing();
        rootServerHandler.stopHandle();
        Runtime.getRuntime().removeShutdownHook(shutdownHandler);
        scanner.close();
    }

    private void printAvailableCommands() {
        System.out.println("Available commands:");
        System.out.println("'dns capture' -- start capturing dns packets");
        System.out.println("'print packets' -- print captured dns packets");
        System.out.println("'find mx' -- get mail service IP (requires to enter domain, " +
                "also you need to capture some packets before this command)");
        System.out.println("'query root server' -- get IP for domains " +
                "(requires to enter root server address and domains)");
        System.out.println("'exit' -- stop interpreter work");
    }
}
