DRONE ASSEMBLY LINE — LOGGING & METRICS SYSTEM
SYSC 3303A W2026 - Dr. Rami Sabouni - Assignment 4
==================================================

OVERVIEW
- Agent places two components; Technicians assemble drones from the other two.
- AssemblyTable is the monitor (wait/notifyAll) coordinating threads.
- EventLogger is a daemonized, buffered singleton (run-ID log file per run).
- LogAnalyzer computes throughput, utilization, and response-time metrics.

HOW TO RUN (INTELLIJ)
1) Run AssemblyTable.main()
2) Program stops -> logger flushes -> LogAnalyzer.main() runs automatically
3) Open: assembly_log_YYYYMMDD_HHMMSS.txt and metrics.txt

CONFIGURATION
- Max drones: AssemblyTable.getMaxDrones()
- Log file: EventLogger.setLogFileName(String)
- Flush ms: EventLogger.setFlushIntervalMs(long)

LOG FORMAT
Event log: [yyyy-MM-dd HH:mm:ss.SSS, ENTITY, EVENT_CODE, key=value; key=value]
Core events: WAIT_START/WAIT_END, PLACED_COMPONENTS, PICKED_UP, DRONE_ASSEMBLED,
TABLE_FULL, TABLE_EMPTY, WORK_START/WORK_END, RESPONSE_TIME, SYSTEM_START/END.

OUTPUT FILES
- assembly_log_YYYYMMDD_HHMMSS.txt (per-run log)
- metrics.txt (summary report)