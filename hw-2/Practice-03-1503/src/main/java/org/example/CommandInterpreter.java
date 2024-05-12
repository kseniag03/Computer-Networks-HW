package org.example;

import org.example.Utils.*;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;

import java.net.UnknownHostException;
import java.util.Scanner;

public class CommandInterpreter {
    private final ArpCapture arpCapture;
    private final DuplicateIpDetector duplicateIPDetector;
    private final MacDiscover macDiscover;
    private final StatisticsCollector statisticsCollector;

    public CommandInterpreter(PcapNetworkInterface nif, int snapLen,
                              PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException, UnknownHostException {
        this.arpCapture = new ArpCapture(nif, snapLen, mode, timeout);
        this.duplicateIPDetector = new DuplicateIpDetector(nif, snapLen, mode, timeout);
        this.macDiscover = new MacDiscover(nif, snapLen, mode, timeout);
        this.statisticsCollector = new StatisticsCollector(nif, snapLen, mode, timeout);
    }

    public void startCommandInterpreter() throws PcapNativeException {
        var scanner = new Scanner(System.in);
        var running = true;

        ShutdownHandler shutdownHandler = new ShutdownHandler(
                arpCapture,
                duplicateIPDetector,
                macDiscover,
                statisticsCollector);
        Runtime.getRuntime().addShutdownHook(shutdownHandler);

        System.out.println("Welcome to Command Interpreter");

        while (running) {
            System.out.print("Enter a command (type 'help' for available commands): ");

            var command = scanner.nextLine();

            switch (command.toLowerCase()) {
                case "help":
                    printAvailableCommands();
                    break;
                case "arp capture":
                    arpCapture.startCapturing();
                    break;
                case "print packets":
                    arpCapture.printCapturedPackets();
                    break;
                case "find ip duplicate":
                    System.out.println(duplicateIPDetector.waitForDuplicateIp());
                    break;
                case "get mac":
                    macDiscover.printMacAddress();
                    break;
                case "get mac ip":
                    System.out.print("Enter target IP address: ");

                    var targetIp = scanner.nextLine();

                    macDiscover.printMacAddressByIp(targetIp);
                    break;
                case "collect statistics":
                    statisticsCollector.collectStatistics();
                    break;
                case "print statistics":
                    statisticsCollector.printStatistics();
                    break;
                case "exit":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
                    break;
            }
        }

        arpCapture.stopCapturing();
        duplicateIPDetector.stopDetection();
        macDiscover.stopDiscovering();
        statisticsCollector.stopCollecting();
        Runtime.getRuntime().removeShutdownHook(shutdownHandler);
        scanner.close();
    }

    private void printAvailableCommands() {
        System.out.println("Available commands:");
        System.out.println("'arp capture' -- start capturing arp packets");
        System.out.println("'print packets' -- print captured arp packets");
        System.out.println("'find ip duplicate' -- get boolean value if current IP exists in network");
        System.out.println("'get mac' -- get MAC address of current device");
        System.out.println("'get mac ip' -- get MAC address by ip of target device");
        System.out.println("'collect statistics' -- start to collect statistics");
        System.out.println("'print statistics'- print collected statistics");
        System.out.println("'exit' -- stop interpreter work");
    }
}
