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
    public CoverageReport generateCoverageReport(String apiDoc, List<String> generatedTestScenarios) {
        CoverageReport report = new CoverageReport();

        // 1. 第一步：获取理论总场景列表（大模型分析的应覆盖场景）
        List<String> totalTheoreticalScenarios = apiSceneAnalyzerService.analyze(apiDoc);
        int totalCases = totalTheoreticalScenarios.size();
        report.setTotalCases(totalCases);

        // 2. 第二步：处理生成的测试用例场景（空值保护 + 标准化）
        List<String> validGeneratedScenarios = CollectionUtils.isEmpty(generatedTestScenarios)
                ? new ArrayList<>()
                : generatedTestScenarios.stream()
                .map(String::trim) // 去除首尾空格
                .filter(scene -> !scene.isEmpty()) // 过滤空场景
                .collect(Collectors.toList());

        // 3. 第三步：计算已覆盖的场景数（生成的用例中匹配理论场景的数量）
        // 注意：这里是“匹配”而非直接取size，避免生成的用例包含非理论场景
        List<String> coveredScenarios = validGeneratedScenarios.stream()
                .filter(totalTheoreticalScenarios::contains)
                .collect(Collectors.toList());
        int coveredCaseCount = coveredScenarios.size();
        report.setTestedCases(coveredCaseCount); // 字段名保留，但语义改为“已覆盖场景数”

        // 4. 第四步：计算场景覆盖度（避免除以0）
        double coverageScore = totalCases == 0 ? 0.0 : (double) coveredCaseCount / totalCases;
        coverageScore = Math.round(coverageScore * 10000.0) / 10000.0; // 保留4位小数
        report.setCoverageScore(coverageScore);

        // 5. 第五步：识别缺失场景（理论场景 - 生成的用例覆盖的场景）
        List<String> missingScenarios = totalTheoreticalScenarios.stream()
                .filter(scene -> !coveredScenarios.contains(scene))
                .collect(Collectors.toList());
        report.setMissingScenarios(missingScenarios);

        return report;
    }

    /**
     * 扩展方法：支持模糊匹配（生成的场景名称和理论场景名称略有差异时）
     * 比如：理论场景是“user_id缺失”，生成的用例场景是“用户ID缺失”，可通过关键词匹配
     */
    @Override
    public CoverageReport generateCoverageReportWithFuzzyMatch(String apiDoc, List<String> generatedTestScenarios) {
        CoverageReport report = new CoverageReport();
        List<String> totalTheoreticalScenarios = apiSceneAnalyzerService.analyze(apiDoc);
        int totalCases = totalTheoreticalScenarios.size();
        report.setTotalCases(totalCases);

        List<String> validGeneratedScenarios = CollectionUtils.isEmpty(generatedTestScenarios)
                ? new ArrayList<>()
                : generatedTestScenarios.stream()
                .map(String::trim)
                .filter(scene -> !scene.isEmpty())
                .collect(Collectors.toList());

        // 模糊匹配：忽略大小写 + 关键词匹配
        List<String> coveredScenarios = new ArrayList<>();
        for (String theoreticalScene : totalTheoreticalScenarios) {
            boolean isCovered = validGeneratedScenarios.stream()
                    .anyMatch(generated ->
                            generated.toLowerCase().contains(theoreticalScene.toLowerCase().replace("-", "").replace("：", ""))
                    );
            if (isCovered) {
                coveredScenarios.add(theoreticalScene);
            }
        }

        int coveredCaseCount = coveredScenarios.size();
        report.setTestedCases(coveredCaseCount);
        double coverageScore = totalCases == 0 ? 0.0 : (double) coveredCaseCount / totalCases;
        coverageScore = Math.round(coverageScore * 10000.0) / 10000.0;
        report.setCoverageScore(coverageScore);

        List<String> missingScenarios = totalTheoreticalScenarios.stream()
                .filter(scene -> !coveredScenarios.contains(scene))
                .collect(Collectors.toList());
        report.setMissingScenarios(missingScenarios);

        return report;
    }

    @Override
    public CoverageReport AiGenerateCoverageReport(String apiDoc, List<String> generatedTestScenarios) {
        CoverageReport report = new CoverageReport();
        List<String> totalTheoreticalScenarios = apiSceneAnalyzerService.analyze(apiDoc);
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
        report.setCoverageScore(Math.round(coverageRate * 10000.0) / 100.0);

        // 获取未覆盖场景（AI匹配失败时降级）
        List<String> missingScenarios = aiScenarioMatchUtils.getMissingScenariosByAI(totalTheoreticalScenarios, validGeneratedScenarios);
        report.setMissingScenarios(missingScenarios);

        return report;
    }
}