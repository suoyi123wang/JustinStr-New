package cn.ios.cmd;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.util.StringUtil;

/**
 * @author: wangmiaomiao
 * @description: TODO
 * @date: 2023/4/19 13:22
 */
public class ProgressBar {
    public static int times = 1;
    public static void showProgress() {
        String finish = "";
        String unFinish = "";
        String target = "";
        while (GlobalCons.ALREADY_DEAL_CLASS_NUM <= 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }

        int total = GlobalCons.TEST_CLASS_NUM;
        int finishNum = 0;
        if (GlobalCons.TEST_CLASS_NUM >= 50) {
            times = GlobalCons.TEST_CLASS_NUM / 50;
            total = total / times;
        } else {
            times = 50 / GlobalCons.TEST_CLASS_NUM;
            total = total * times;
        }
        System.out.println("-------------soot loading completed-------------");
        while (GlobalCons.ALREADY_DEAL_CLASS_NUM < GlobalCons.TEST_CLASS_NUM){
            if (GlobalCons.ALREADY_DEAL_CLASS_NUM > 0) {
                if (GlobalCons.TEST_CLASS_NUM >= 50) {
                    finishNum = GlobalCons.ALREADY_DEAL_CLASS_NUM / times;
                } else {
                    finishNum = GlobalCons.ALREADY_DEAL_CLASS_NUM * times;
                }
                if (finishNum > 0) {
                    finish = getNChar(finishNum, '█');
                    unFinish = getNChar(total - finishNum, '─');
                    target = String.format("%s[%s%s]", StringUtil.getDividePercentage(finishNum, total), finish, unFinish);

                    System.out.println("Progress:" + target);
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }

        // GlobalCons.ALREADY_DEAL_CLASS_NUM >= GlobalCons.TEST_CLASS_NUM
        finish = getNChar(total, '█');
        unFinish = getNChar(0, '─');
        target = String.format("%s[%s%s]", StringUtil.getDividePercentage(GlobalCons.TEST_CLASS_NUM, GlobalCons.TEST_CLASS_NUM), finish, unFinish);
        System.out.println("Progress:" + target);
        System.out.println("-------------generating report-------------");
    }

    private static String getNChar(int num, char ch){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < num; i++){
            builder.append(ch);
        }
        return builder.toString();
    }
}