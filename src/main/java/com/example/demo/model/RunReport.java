package com.example.demo.model;

import java.util.List;
import java.time.Instant;

/**
 * RunReport - 一次执行的整体报告
 */
public class RunReport {
    public String runId;
    public Instant startTime;
    public Instant endTime;
    public int total;
    public int passed;
    public int failed;
    public List<TestRunResult> results;
}
