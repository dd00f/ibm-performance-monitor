Why is the IBM performance monitor useful ?

# Profile production systems with low overhead

In-Memory metrics can be enabled with low overhead to monitor the performance of a production system. Paired with tools like [Grafana](https://github.com/dd00f/ibm-performance-monitor/wiki/Integration-with-Graphite-&-Grafana) & [Kibana](https://github.com/dd00f/ibm-performance-monitor/wiki/Integration-with-Kibana), those metrics can show the top 10 JDBC statements by execution count and duration to quickly identify what consumes the most resources.

# Easily inject code metrics and/or Trace Logs with AspectJ

Using [AspectJ](https://github.com/dd00f/ibm-performance-monitor/wiki/AspectJ-Instrumentation), you can instrument a large amount of code quickly without changing your existing code.

The instrumentation is used to both monitor the code performance and/or generate trace logs to troubleshoot problems.

# Automated Caching Recommendations

Generate [reports](https://github.com/dd00f/ibm-performance-monitor/wiki/Analyzing-Performance-Reports) that show exactly which parts of your code could benefit the most from caching in terms of time saved and required cache size.

# Smart Code Profiling

[Stack Reports](https://github.com/dd00f/ibm-performance-monitor/wiki/Stack-Reports) show examples of the anatomy of your code so you may discover the source of unexpected overhead with the right level of details.


