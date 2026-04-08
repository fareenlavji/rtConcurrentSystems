import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Standalone analyzer for assembly_log.txt.
 * Runs AFTER the assembly run has finished.
 *
 * @author Lavji, F
 * @version 1.1, Mar 14, 2026
 */
public class LogAnalyzer {

    private static final String LOG_FILE_NAME = "assembly_log.txt";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static class ThreadStats {
        long firstTime = Long.MAX_VALUE;
        long lastTime  = Long.MIN_VALUE;
        long totalWaiting = 0L;
        Long lastWaitingStart = null;
        long totalBusy = 0L;
        Long lastBusyStart = null;
        List<Long> responseTimes = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException, ParseException {
        String logFile = (args != null && args.length > 0
                && args[0] != null && !args[0].trim().isEmpty()) ? args[0].trim() : LOG_FILE_NAME;

        Map<String, ThreadStats> statsMap = new HashMap<>();
        long systemStart = Long.MAX_VALUE;
        long systemEnd   = Long.MIN_VALUE;
        int totalDrones  = 0;
        StringBuilder out = new StringBuilder(); // accumulate all output lines

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                int l = line.indexOf('['), r = line.lastIndexOf(']');
                if (l < 0 || r <= l) continue;
                String content = line.substring(l + 1, r);

                String[] parts = content.split(",", 4);
                if (parts.length < 3) continue;
                for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();

                String timeStr = parts[0];
                String entity  = parts[1];
                String event   = parts[2];
                String extra   = (parts.length == 4) ? parts[3] : "";
                long timeMs = formatter.parse(timeStr).getTime();

                // window (also respects SYSTEM_START/END anchors)
                systemStart = Math.min(systemStart, timeMs);
                systemEnd   = Math.max(systemEnd,   timeMs);
                if (entity.equals("AssemblyLine")) {
                    if (event.equals("SYSTEM_START")) systemStart = Math.min(systemStart, timeMs);
                    else if (event.equals("SYSTEM_END")) systemEnd = Math.max(systemEnd, timeMs);
                }

                Map<String, String> kv = new HashMap<>();
                if (!extra.isEmpty()) {
                    for (String token : extra.split(";")) {
                        token = token.trim();
                        int eq = token.indexOf('=');
                        if (eq > 0 && eq < token.length() - 1) {
                            String k = token.substring(0, eq).trim();
                            String v = token.substring(eq + 1).trim();
                            kv.put(k, v);
                        }
                    }
                }

                if (event.equals("DRONE_ASSEMBLED")) { totalDrones++; }

                if (entity.startsWith("Agent") || entity.startsWith("Technician")) {
                    ThreadStats ts = statsMap.computeIfAbsent(entity, k -> new ThreadStats());
                    ts.firstTime = Math.min(ts.firstTime, timeMs);
                    ts.lastTime  = Math.max(ts.lastTime,  timeMs);

                    if (event.equals("WAIT_START")) { ts.lastWaitingStart = timeMs; }
                    else if (event.equals("WAIT_END") && ts.lastWaitingStart != null) {
                        ts.totalWaiting += (timeMs - ts.lastWaitingStart);
                        ts.lastWaitingStart = null;
                    }

                    String ev = event.toUpperCase(java.util.Locale.ROOT);
                    if (ev.equals("RESPONSE_TIME_MS") || ev.equals("RESPONSE_TIME")) {
                        String dur = kv.get("duration");
                        if (dur == null && extra.contains("=")) {
                            String[] kv2 = extra.split("=");
                            if (kv2.length == 2) dur = kv2[1].trim();
                        }
                        if (dur != null) {
                            try { ts.responseTimes.add(Long.parseLong(dur.replaceAll("[^0-9]", ""))); }
                            catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        }

        // close any open waits at EOF
        for (ThreadStats ts : statsMap.values()) {
            if (ts.lastWaitingStart != null) {
                long end = (systemEnd > 0 && systemEnd != Long.MIN_VALUE) ? systemEnd : ts.lastTime;
                if (end > ts.lastWaitingStart) ts.totalWaiting += (end - ts.lastWaitingStart);
                ts.lastWaitingStart = null;
            }
        }

        out.append("===== METRICS ANALYSIS =====\n");
        out.append(String.format("Log file: %s%n", logFile)); // helpful context

        // Empty-log handling (no timestamps or no drones)
        boolean hasWindow = (systemStart < systemEnd);
        if (!hasWindow) {
            out.append("No valid timestamps found in the log. Is the file empty?\n");
        } else {
            double durationSec = (systemEnd - systemStart) / 1000.0;

            if (totalDrones == 0) {
                out.append("No assemblies detected (0 DRONE_ASSEMBLED events).\n");
                out.append(String.format("Total time observed: %.3f s%n", durationSec));
            } else {
                double throughput = (durationSec > 0) ? (totalDrones / durationSec) : 0.0;
                out.append(String.format("Total drones assembled: %d%n", totalDrones));
                out.append(String.format("Total time: %.3f s%n", durationSec));
                out.append(String.format("Throughput: %.3f drones/s%n", throughput));
            }
        }

        out.append("\n");
        out.append("Per-thread metrics:\n");
        if (statsMap.isEmpty()) {
            out.append("(No Agent/Technician events found.)\n");
        } else {
            for (Map.Entry<String, ThreadStats> entry : statsMap.entrySet()) {
                String threadName = entry.getKey();
                ThreadStats ts = entry.getValue();

                long totalTime = Math.max(0L, ts.lastTime - ts.firstTime);
                long waiting   = Math.min(ts.totalWaiting, totalTime);
                long busyTime  = Math.max(0L, totalTime - waiting);
                double utilization = (totalTime > 0) ? (busyTime / (double) totalTime) : 0.0;

                double avgResp = 0.0;
                if (!ts.responseTimes.isEmpty()) {
                    long sum = 0;
                    for (Long rt : ts.responseTimes) sum += rt;
                    avgResp = sum / (double) ts.responseTimes.size();
                }

                out.append("-------------------------------------\n");
                out.append("Thread: ").append(threadName).append('\n');
                out.append(String.format("Total time: %.3f s%n", totalTime / 1000.0));
                out.append(String.format("Total waiting: %.3f s%n", waiting / 1000.0));
                out.append(String.format("Utilization (busy/total): %.3f%n", utilization));
                out.append(String.format("Average response time: %.3f ms (over %d samples)%n",
                        avgResp, ts.responseTimes.size()));
            }
        }

        // Print to console
        System.out.print(out.toString());

        // Also write the exact same output to metrics.txt
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream("metrics.txt", false),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            pw.write(out.toString());
            pw.flush();
        }
    }
}