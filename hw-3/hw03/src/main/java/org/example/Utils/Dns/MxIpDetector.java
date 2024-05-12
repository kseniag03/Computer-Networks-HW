package org.example.Utils.Dns;

import org.pcap4j.packet.DnsPacket;
import org.pcap4j.packet.DnsRDataMx;
import org.pcap4j.packet.namednumber.DnsResourceRecordType;

import java.util.ArrayList;
import java.util.List;

public class MxIpDetector {
    public static List<String> find(String domain, List<DnsPacket> dnsPackets) {
        List<String> mxRecords = new ArrayList<>();

        try {
            for (var dnsPacket : dnsPackets) {
                if (dnsPacket.getHeader().isResponse() && !dnsPacket.getHeader().isTruncated()) {
                    var answers = dnsPacket.getHeader().getAnswers();

                    for (var answer : answers) {
                        if (answer.getDataType() == DnsResourceRecordType.MX) {
                            mxRecords.add(DnsRDataMx
                                    .newInstance(answer.getRawData(), 0, answer.getRawData().length)
                                    .getExchange()
                                    .decompress(dnsPacket.getHeader().getRawData()));
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (mxRecords.isEmpty()) {
            System.out.println("No MX records found for domain: " + domain);
        }

        return mxRecords;
    }
}
