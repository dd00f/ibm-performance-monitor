


REM -host
REM -service
REM -port
REM -usage
REM -enable
REM -disable
REM -clear
REM -dump
REM -dumpCsv
REM -dumpTable


java -cp target/* com.ibm.profiler.client.PerformanceLoggerManagerCLI -host 127.0.0.1 -port 9989 -dumpCsv myFile.csv
java -cp target/* com.ibm.profiler.client.PerformanceLoggerManagerCLI -host 127.0.0.1 -port 9989 -dumpTable myFile.txt
java -cp target/* com.ibm.profiler.client.PerformanceLoggerManagerCLI -host 127.0.0.1 -port 9989 -dump
java -cp target/* com.ibm.profiler.client.PerformanceLoggerManagerCLI -host 127.0.0.1 -port 9989 -clear

echo done !
