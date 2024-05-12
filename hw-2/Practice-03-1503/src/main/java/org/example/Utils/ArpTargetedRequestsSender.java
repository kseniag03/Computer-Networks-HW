package org.example.Utils;

import org.example.Common.Constants;
import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.MacAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ArpTargetedRequestsSender {
    private ArpTargetedRequestsSender() {}

    public static void sendArpTargetedRequest(PcapHandle handle,
                                              InetAddress senderIpAddress,
                                              InetAddress targetIpAddress) {
        try {
            var arpBuilder = new ArpPacket.Builder();

            arpBuilder.hardwareType(ArpHardwareType.ETHERNET)
                    .protocolType(EtherType.IPV4)
                    .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                    .protocolAddrLength((byte) Byte.SIZE)
                    .operation(ArpOperation.REQUEST)
                    .srcHardwareAddr(Constants.DEFAULT_MAC_ADDRESS)
                    .srcProtocolAddr(InetAddress.getByAddress(senderIpAddress.getAddress()))
                    .dstHardwareAddr(Constants.BROADCAST_MAC_ADDRESS)
                    .dstProtocolAddr(InetAddress.getByAddress(targetIpAddress.getAddress()));

            var arpPacket = arpBuilder.build();

            handle.sendPacket(arpPacket);
        } catch (PcapNativeException | NotOpenException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}


/*
import org.example.Common.Constants;
import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.MacAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ArpTargetedRequestsSender {
    private final PcapHandle handle;

    public ArpTargetedRequestsSender(PcapNetworkInterface nif, int snapLen, PcapNetworkInterface.PromiscuousMode mode, int timeout) throws PcapNativeException {
        this.handle = nif.openLive(snapLen, mode, timeout);
    }

    public void sendARPTargetedRequest(InetAddress targetIPAddress) {
        try {
            var arpBuilder = new ArpPacket.Builder();

            arpBuilder.hardwareType(ArpHardwareType.ETHERNET)
                    .protocolType(EtherType.IPV4)
                    .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                    .protocolAddrLength((byte) Byte.SIZE)
                    .operation(ArpOperation.REQUEST)
                    .srcHardwareAddr(Constants.DEFAULT_MAC_ADDRESS)
                    .srcProtocolAddr(InetAddress.getByAddress(InetAddress.getLocalHost().getAddress()))
                    .dstHardwareAddr(Constants.BROADCAST_MAC_ADDRESS)
                    .dstProtocolAddr(InetAddress.getByAddress(targetIPAddress.getAddress()));

            var arpPacket = arpBuilder.build();

            handle.sendPacket(arpPacket);
        } catch (PcapNativeException | NotOpenException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void stopSendingRequests() {
        handle.close();
    }
}
*/