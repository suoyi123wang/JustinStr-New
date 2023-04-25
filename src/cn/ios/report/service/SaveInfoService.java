package cn.ios.report.service;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.report.vo.ClassInfoVO;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * @author: wangmiaomiao
 * @create: 2022/7/7 10:05
 **/
public class SaveInfoService {
    public static void insertIntoPackageInfo(String packageName) {
        try {
            String sql = "INSERT OR IGNORE INTO package_info VALUES ( '" + packageName + "');";
            Statement statement = GlobalCons.connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertIntoClassInfo(ClassInfoVO classInfoVO){
        try {
            String sql = "INSERT INTO class_info VALUES (NULL, '" + classInfoVO.getFullClassName() + "'," + classInfoVO.getMethodNum() + "," +
                    classInfoVO.getTestMethodNum() + "," + classInfoVO.getTestCaseNum() + "," + classInfoVO.getExecuteTestCaseNum() +  "," +
                    classInfoVO.getSuccessTestCaseNum() +  "," + classInfoVO.getSkipTestCaseNum() +  "," + classInfoVO.getFailTestCaseNum() +  ");";

            Statement statement = GlobalCons.connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertIntoDetailInfo(String testMethodName, String stackDetail, String testMethodBody, String classNameUnderTest, String exceptionName){
        try {
            String[] splitArray = testMethodName.split("_");
            String methodNameUnderTest = splitArray[1];
            String[] stackArray = stackDetail.split("\\n");
            String usualStack = "";
            for (int i = 1; i < stackArray.length - 1; i++) {
                usualStack = usualStack + stackArray[i] + "\n" ;
            }
            int exceptionTypeId = SearchService.getExceptionTypeId(methodNameUnderTest, classNameUnderTest, exceptionName, usualStack);
            if (exceptionTypeId != -1) {
                String sql = "INSERT INTO detail_info VALUES (NULL,?,?,?,?);";
                PreparedStatement preparedStatement = GlobalCons.connection.prepareStatement(sql);
                preparedStatement.setString(1,testMethodName);
                preparedStatement.setString(2,stackDetail);
                preparedStatement.setString(3,testMethodBody);
                preparedStatement.setInt(4,exceptionTypeId);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertIntoExceptionType(String methodNameUnderTest, String classNameUnderTest, String exceptionName, String usualStackDetail){
        try {
            String sql = "INSERT OR IGNORE INTO exception_type VALUES (NULL,?,?,?,?);";
            PreparedStatement preparedStatement = GlobalCons.connection.prepareStatement(sql);
            preparedStatement.setString(1,methodNameUnderTest);
            preparedStatement.setString(2,classNameUnderTest);
            preparedStatement.setString(3,exceptionName);
            preparedStatement.setString(4,usualStackDetail);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

}
