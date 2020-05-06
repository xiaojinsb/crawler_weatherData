package util;

/**
 * @Author: Avalon
 * @Date: 20/4/23 11:31
 * @Description: 一般工具
 */
public class CommonUtil {
    /**
     * 暂停
     *
     * @param time
     */
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            System.exit(0);
        }
    }
}
