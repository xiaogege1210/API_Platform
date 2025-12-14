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
    public CoverageReport AiGenerateCoverageReport(String apiDoc, String extraScene, List<String> generatedTestScenarios) {
        CoverageReport report = new CoverageReport();
        List<String> totalTheoreticalScenarios = apiSceneAnalyzerService.analyze(apiDoc,extraScene);

        for(String scene: totalTheoreticalScenarios){
            System.out.println(scene);
        }

        int totalCases = totalTheoreticalScenarios.size();
        report.setTotalCases(totalCases);

        List<String> validGeneratedScenarios = CollectionUtils.isEmpty(generatedTestScenarios)
                ? new ArrayList<>()
                : generatedTestScenarios.stream()
                .map(String::trim)
                .filter(scene -> !scene.isEmpty())
                .collect(Collectors.toList());

        // 3. 核心优化：仅调用一次大模型，获取匹配结果数组
        List<Integer> matchResultArray = aiScenarioMatchUtils.getMatchResultArray(totalTheoreticalScenarios, validGeneratedScenarios);

        // 4. 基于同一数组计算覆盖数和遗漏场景（无DTO，直接计算）
        int coveredCount = aiScenarioMatchUtils.calculateCoveredCount(matchResultArray);
        List<String> missingScenarios = aiScenarioMatchUtils.calculateMissingScenarios(matchResultArray, totalTheoreticalScenarios);

        // 5. 计算覆盖度（修复数值转换错误）
        double coverageRate = totalCases == 0 ? 0 : (double) coveredCount / totalCases*100;
        report.setCoverageScore(coverageRate);

        // 6. 填充结果
        report.setTestedCases(coveredCount);
        report.setMissingScenarios(missingScenarios);
        System.out.println("**********");
        for(String scene: missingScenarios){
            System.out.println(scene);
        }
        report.setMissingScenarios(missingScenarios);

        return report;
    }

}