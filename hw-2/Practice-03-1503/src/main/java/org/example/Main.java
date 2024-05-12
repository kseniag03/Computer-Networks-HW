package org.example;

import org.example.Common.Constants;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args)
            throws UnknownHostException {
        try {
            var address = InetAddress.getByName("192.168.0.110");
            var nif = Pcaps.getDevByAddress(address);
            var timeout = getTimeInMilliseconds(parseInputTime());
            var commandInterpreter = new CommandInterpreter(nif, 65536,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);

            commandInterpreter.startCommandInterpreter();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }

    }

    private static Date parseInputTime() {
        System.out.println("Input time in format HH:mm:ss: ");

        var input = new Scanner(System.in).nextLine();
        var timeFormat = new SimpleDateFormat("HH:mm:ss");

        try {
            var parsedTime = timeFormat.parse(input);

            System.out.println("Input time: " + parsedTime);

            return parsedTime;
        } catch (ParseException e) {
            System.out.println("Time parse error.");
            e.printStackTrace();
        }

        return null;
    }

    private static int getTimeInMilliseconds(Date date) {
        if (date == null) {
            return Constants.DEFAULT_TIMEOUT;
        }

        return (int) date.getTime();
    }
}
