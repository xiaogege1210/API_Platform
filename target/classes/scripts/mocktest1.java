import org.junit.jupiter.api.Test;


/**
 * 无包名的测试类示例
 */
public class mocktest1 { // 类名与文件名一致（mocktest.java → mocktest）

    @Test
    void testBasic() {
        // 简单断言，确保测试用例可执行
        assertTrue(true, "基础测试用例通过");
        System.out.println("这是测试一个测试用例");
    }

//    @Test
//    void testAddition() {
//        int a = 1;
//        int b = 2;
//        assertTrue(a + b == 3, "加法测试用例通过");
//    }
}