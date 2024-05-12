package org.example.Utils;

import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;

import java.io.EOFException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class DuplicateIpDetector {
    private final int timeout;
    private final PcapHandle handle;
    private final InetAddress curIpAddress;

    public DuplicateIpDetector(PcapNetworkInterface nif, int snapLen,
                               PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException, UnknownHostException {
        this.timeout = timeout;
        this.handle = nif.openLive(snapLen, mode, timeout);
        this.curIpAddress = InetAddress.getLocalHost();

        var filter = "arp";

        try {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public boolean waitForDuplicateIp() {
        var startTime = System.currentTimeMillis();
        var endTime = startTime + timeout;

        while (System.currentTimeMillis() < endTime) {
            if (isDuplicateIpInNetwork()) {
                return true;
            }
        }

        return false;
    }

    public void stopDetection() {
        handle.close();
    }

    private boolean isDuplicateIpInNetwork() {
        try {
            sendArpRequest();

            var packet = handle.getNextPacketEx();

            if (packet != null) {
                var arpPacket = packet.get(ArpPacket.class);

                if (arpPacket != null && arpPacket.getHeader() != null && arpPacket.getHeader().getSrcProtocolAddr() != null) {
                    if (arpPacket.getHeader().getSrcProtocolAddr().equals(curIpAddress)) {
                        return true;
                    }
                }
            }
        } catch (PcapNativeException | TimeoutException | EOFException | NotOpenException | UnknownHostException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void sendArpRequest()
            throws PcapNativeException, NotOpenException, UnknownHostException {
        ArpTargetedRequestsSender.sendArpTargetedRequest(handle, curIpAddress, curIpAddress);
    }
}
