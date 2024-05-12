package org.example.Utils.Dns;

public class ShutdownHandlerDns extends Thread {
    private final DnsCapture dnsCapture;
    private final RootServerHandler rootServerHandler;

    public ShutdownHandlerDns(DnsCapture dnsCapture, RootServerHandler rootServerHandler) {
        this.dnsCapture = dnsCapture;
        this.rootServerHandler = rootServerHandler;
    }

    @Override
    public void run() {
        System.out.println("Shutdown handler triggered. Closing resources...");

        try {
            dnsCapture.stopCapturing();
            rootServerHandler.stopHandle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
