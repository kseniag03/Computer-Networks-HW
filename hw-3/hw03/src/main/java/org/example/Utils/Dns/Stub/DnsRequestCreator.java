package org.example.Utils.Dns.Stub;

import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;

import java.util.Collections;
import java.util.Random;

public class DnsRequestCreator {
    private static final Random random = new Random();

    public static byte[] createDnsRequest(String domain) {
        var dnsPacketBuilder = new DnsPacket.Builder();
        var questionBuilder = new DnsQuestion.Builder();

        try {
            // there are troubles
            var dnsDomainName = DnsDomainName.newInstance(domain.getBytes(), 0, domain.getBytes().length);

            questionBuilder
                    .qName(dnsDomainName)
                    .qType(DnsResourceRecordType.A)
                    .qClass(DnsClass.IN);

            var question = questionBuilder.build();
            var id = random.nextInt(65535);

            dnsPacketBuilder
                    .id((short) id)
                    .questions(Collections.singletonList(question));

            var build = dnsPacketBuilder.build();

            return build.getRawData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
