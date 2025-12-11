package com.example.demo.service.impl;

import com.example.demo.model.CoverageReport;
import com.example.demo.service.ApiSceneAnalyzerService;
import com.example.demo.service.CoverageAnalysisService;
import com.example.demo.utils.AIScenarioMatchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口场景覆盖度分析核心服务（修正版）
 * 核心逻辑：覆盖度 = 生成的测试用例覆盖的场景数 / 理论总场景数
 */
@Service
public class CoverageAnalysisServiceImpl implements CoverageAnalysisService {

    @Autowired
    private ApiSceneAnalyzerService apiSceneAnalyzerService;

    @Autowired
    private AIScenarioMatchUtils aiScenarioMatchUtils;


    /**
     * 核心方法：生成接口场景覆盖度报告（正确逻辑）
     * @param apiDoc 接口文档（用于生成理论总场景）
     * @param generatedTestScenarios 实际生成的测试用例对应的场景列表（即已覆盖的场景）
     * @return 完整的覆盖度报告
     */
    @Override
    public CoverageReport AiGenerateCoverageReport(String apiDoc,String extraScene, List<String> generatedTestScenarios) {
        CoverageReport report = new CoverageReport();
        List<String> totalTheoreticalScenarios = apiSceneAnalyzerService.analyze(apiDoc,extraScene);
        int totalCases = totalTheoreticalScenarios.size();
        report.setTotalCases(totalCases);

        List<String> validGeneratedScenarios = CollectionUtils.isEmpty(generatedTestScenarios)
                ? new ArrayList<>()
                : generatedTestScenarios.stream()
                .map(String::trim)
                .filter(scene -> !scene.isEmpty())
                .collect(Collectors.toList());

        // 核心：使用AI匹配计算覆盖数（失败时自动降级为手动规则）

        int coveredCount = aiScenarioMatchUtils.countMatchedScenariosByAI(totalTheoreticalScenarios, validGeneratedScenarios);
//        report.setCoveredCases(coveredCount);

        // 计算覆盖度（保留2位小数）
        double coverageRate = totalTheoreticalScenarios.size() == 0 ? 0 : (double) coveredCount / totalTheoreticalScenarios.size();
        report.setCoverageScore(coverageRate);

        // 获取未覆盖场景（AI匹配失败时降级）
        List<String> missingScenarios = aiScenarioMatchUtils.getMissingScenariosByAI(totalTheoreticalScenarios, validGeneratedScenarios);
        report.setMissingScenarios(missingScenarios);

        return report;
    }
}