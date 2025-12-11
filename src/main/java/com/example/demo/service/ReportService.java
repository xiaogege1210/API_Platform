package com.example.demo.service;

import com.example.demo.dto.TestCaseResultDto;
import com.example.demo.model.RunReport;

import java.util.List;

public interface ReportService {
    RunReport generateReport(List<TestCaseResultDto> results);
}
