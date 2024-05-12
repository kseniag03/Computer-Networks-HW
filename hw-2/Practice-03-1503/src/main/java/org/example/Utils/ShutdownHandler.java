package org.example.Utils;

public class ShutdownHandler extends Thread {
    private final ArpCapture arpCapture;
    private final MacDiscover macDiscover;
    private final StatisticsCollector statisticsCollector;
    private final DuplicateIpDetector duplicateIPDetector;

    public ShutdownHandler(ArpCapture arpCapture,
                           DuplicateIpDetector duplicateIPDetector,
                           MacDiscover macDiscover,
                           StatisticsCollector statisticsCollector) {
        this.arpCapture = arpCapture;
        this.duplicateIPDetector = duplicateIPDetector;
        this.macDiscover = macDiscover;
        this.statisticsCollector = statisticsCollector;
    }

    @Override
    public void run() {
        System.out.println("Shutdown handler triggered. Closing resources...");

        try {
            arpCapture.stopCapturing();
            duplicateIPDetector.stopDetection();
            macDiscover.stopDiscovering();
            statisticsCollector.stopCollecting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
