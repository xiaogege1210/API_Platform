package com.example.demo.service.impl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCaseManagementServiceImplTest {
    TestCaseManagementServiceImpl testCaseManagementServiceImpl = new TestCaseManagementServiceImpl();
    /**
     * 测试打开主目录下的所有文件
     */
    @Test
    void testBasic1() {
        List<String> list=new ArrayList<>();
        list=testCaseManagementServiceImpl.findTestCaseAll();
        for(String s:list){
            System.out.println(s);
        }

    }
    @Test
    void testBasic2() {
        List<String> list=new ArrayList<>();
        list=testCaseManagementServiceImpl.findTestCaseByDir("scene1");
        for(String s:list){
            System.out.println(s);
        }
    }
    @Test
    void testBasic3() {
        String res=testCaseManagementServiceImpl.readTestCaseContent("scene1/1.txt");
        System.out.println(res);
    }
    @Test
    void testBasic4() {
        testCaseManagementServiceImpl.createTestCaseFile("hh/xixi/test.java","又是一个测试");
    }
    @Test
    void testBasic5() {
        testCaseManagementServiceImpl.deleteTestCaseFile("hh/xixi/test.java");

    }
    @Test
    void testBasic6() {
        testCaseManagementServiceImpl.deleteAllTestCasesInDir("hh/xixi");
    }
    @Test
    void testBasic7() {
        testCaseManagementServiceImpl.updateTestCaseContent("hh/test.java","烦死了");
    }
}
