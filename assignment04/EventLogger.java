import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Central event logger for the assembly system.
 * Logs are buffered in memory and periodically flushed
 * to disk by a background daemon thread.
 *
 * @author Lavji, F
 * @version 1.0, March.14.2026
 */
public class EventLogger {

    // CONSTANTS
    private static final String DEFAULT_LOG_FILE_NAME = "assembly_log.txt";
    private volatile String logFileName = DEFAULT_LOG_FILE_NAME;
    private static final long DEFAULT_FLUSH_INTERVAL_MS = 1000L; // 1 second
    private volatile long flushIntervalMs = DEFAULT_FLUSH_INTERVAL_MS;


    private static final EventLogger INSTANCE = new EventLogger();

    private final List<String> buffer = new ArrayList<>();
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private volatile boolean running = false;
    private Thread flusherThread;

    /**
     * Singleton constructor.
     */
    private EventLogger() { }

    /**
     * Returns the singleton instance.
     * @return Instance of EventLogger class object.
     */
    public static EventLogger getInstance() { return INSTANCE; }

    /**
     * Sets the flush interval in milliseconds for fine tuning.
     *
     * @param intervalMs    Sets the flush interval in milliseconds.
     */

    public synchronized void setFlushIntervalMs(long intervalMs) {
        // do not change while running
        if (running) return;
        if (intervalMs > 0) { flushIntervalMs = intervalMs; }
    }

    /**
     * Sets the log file name.
     *
     * @param fileName  The name of the log file.
     */
    public synchronized void setLogFileName(String fileName) {
        if (running) return; // don’t change while running
        if (fileName != null && !fileName.trim().isEmpty()) { this.logFileName = fileName.trim(); }
        // Fallback
        else { this.logFileName = DEFAULT_LOG_FILE_NAME; }
    }

    /**
     * Starts the background daemon flusher thread.
     * Should be called once at program startup.
     */
    public synchronized void start() {
        if (running) { return; }
        running = true;
        flusherThread = new Thread(() -> {
            while (running) {
                try { Thread.sleep(flushIntervalMs); }
                catch (InterruptedException e) { // ignore interruptions; loop checks running flags
                } flushToDisk();
            } // Final flush after loop exits.
        flushToDisk(); }, "EventLogger-Flusher");
        flusherThread.setDaemon(true); // set to daemon thread
        flusherThread.start();
    }

    /**
     * Stops the logger and forces a final flush.
     * To be called near program termination.
     */
    public synchronized void stop() {
        running = false;
        if (flusherThread != null) {
            flusherThread.interrupt();
            try { flusherThread.join(500); } catch (InterruptedException ignored) {}
        } flushToDisk();
    }

    /**
     * Generic log formatter.
     * Event Log: [TIME, ENTITY, EVENT_CODE, additionalData]
     *
     * @param entity    The thread or resource that generated the event.
     * @param eventCode A short identifier describing the event.
     * @param additionalData Extraneous data for calculation of metrics, omitted if null.
     */
    public void logEvent(String entity, String eventCode, String additionalData) {
        String time = LocalDateTime.now().format(TS_FMT);
        StringBuilder sb = new StringBuilder();
        sb.append("Event log: [").append(time).append(" , ").append(entity).append(" , ").append(eventCode);
        if (additionalData != null && !additionalData.isEmpty()) { sb.append(" , ").append(additionalData); }
        sb.append("]");
        synchronized (buffer) { buffer.add(sb.toString()); }
    }

    /**
     * Sanity logger for redundancy.
     *
     * @param entity    The thread or resource that generated the event.
     * @param eventCode A short identifier describing the event.
     */
    public void logEvent(String entity, String eventCode) { logEvent(entity, eventCode, ""); }

    /**
     * Flushes current buffer to file and clears the in-memory buffer.
     */
    private void flushToDisk() {
        List<String> toWrite;
        synchronized (buffer) {
            if (buffer.isEmpty()) { return; }
            toWrite = new ArrayList<>(buffer);
            buffer.clear();
        }
        try (FileWriter fw = new FileWriter(logFileName, true)) {
            for (String line : toWrite) {
                fw.write(line);
                fw.write(System.lineSeparator());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Build a semicolon-separated key=value string from a map, preserving insertion order.
     * Values are written as-is enabling logging "[Frame,ControlFirmware]" for components.
     */
    private static String toAdditionalData(Map<String, String> kv) {
        if (kv == null || kv.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : kv.entrySet()) {
            if (!first) sb.append("; ");
            sb.append(e.getKey()).append("=").append(e.getValue());
            first = false;
        } return sb.toString();
    }

    /**
     * Convenience: supply additional data as varargs: ("k1","v1","k2","v2",...)
     * Throws IllegalArgumentException if odd number of tokens is passed.
     */
    public void logEventKV(String entity, String eventCode, String... kvPairs) {
        if (kvPairs == null || kvPairs.length == 0) {
            logEvent(entity, eventCode, "");
            return;
        }

        if (kvPairs.length % 2 != 0) { throw new IllegalArgumentException("kvPairs must be even-length: key,value,..."); }
        Map<String, String> kv = new LinkedHashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            String k = kvPairs[i] == null ? "" : kvPairs[i].trim();
            String v = kvPairs[i + 1] == null ? "" : kvPairs[i + 1].trim();
            if (!k.isEmpty()) kv.put(k, v);
        } logEvent(entity, eventCode, toAdditionalData(kv));
    }

    /**
     * Convenience: supply additional data as a map (insertion order respected).
     */
    public void logEvent(String entity, String eventCode, Map<String, String> kv) { logEvent(entity, eventCode, toAdditionalData(kv)); }

    /**
     * Wait markers so all threads/classes use the same wording.
     *
     * Emits WAIT_START (no state field forced).
     */
    public void waitStart(String entity) { logEvent(entity, "WAIT_START", ""); }

    /**
     * Wait end marker.
     */
    public void waitEnd(String entity) { logEvent(entity, "WAIT_END", ""); }
}