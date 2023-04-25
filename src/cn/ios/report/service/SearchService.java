package cn.ios.report.service;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.util.StringUtil;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author: wangmiaomiao
 * @create: 2022/7/7 14:32
 **/
public class SearchService {

    public void getAllResult() throws Exception {
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.setSharedCache(true);
        sqLiteConfig.enableRecursiveTriggers(true);
        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource(sqLiteConfig);
        sqLiteDataSource.setUrl("jdbc:sqlite:sample.db");
        GlobalCons.connection = sqLiteDataSource.getConnection();
        String packageName = "";
        String fullClassName = "";
        Map<String, List<Map<String, String>>> responseResult = new HashMap<>();
        responseResult.put("classInfo", getClassInfo(packageName, fullClassName));
        responseResult.put("detailInfo", getDetailInfo(packageName, fullClassName));
        responseResult.put("exceptionInfo", getExceptionInfo(packageName, fullClassName));
    }

    public Map<String, List<String>> getSearchName() {
        Map<String, List<String>> result = new HashMap<>();
        List<String> packageNameList = new ArrayList<>();
        List<String> classNameList = new ArrayList<>();
        Statement statement = null;
        try {
            String sql = "SELECT package_name from package_info;";
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                packageNameList.add(resultSet.getString("package_name"));
            }
            sql = "SELECT class_name from class_info;";
            statement = GlobalCons.connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                classNameList.add(resultSet.getString("class_name"));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        result.put("packageNameList", packageNameList);
        result.put("classNameList", classNameList);
        return result;
    }

    public static List<Map<String, String>> getClassInfo(String packageName, String fullClassName) {
        List<Map<String, String>> result = new ArrayList<>();
        String sql = "SELECT COUNT(*), SUM(method_num), SUM(test_method_num), SUM(execute_case_num), SUM(success_count), SUM(skip_count), SUM(failure_count)FROM class_info ";
        if (fullClassName != null && !fullClassName.isEmpty()) {
            sql += "WHERE class_name = '" + fullClassName + "' ";
        } else if (packageName != null && !packageName.isEmpty()) {
            sql = sql + "WHERE class_name LIKE" + "'" + packageName + "%') ";
        }
        sql += ";";
        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, String> sumMap = new HashMap<>();
                sumMap.put("class_num", StringUtil.formatString(resultSet.getString("COUNT(*)")));
                sumMap.put("method_num", StringUtil.formatString(resultSet.getString("SUM(method_num)")));
                sumMap.put("test_method_num", StringUtil.formatString(resultSet.getString("SUM(test_method_num)")));
                sumMap.put("execute_case_num", StringUtil.formatString(resultSet.getString("SUM(execute_case_num)")));
                sumMap.put("success_count", StringUtil.formatString(resultSet.getString("SUM(success_count)")));
                sumMap.put("skip_count", StringUtil.formatString(resultSet.getString("SUM(skip_count)")));
                sumMap.put("failure_count", StringUtil.formatString(resultSet.getString("SUM(failure_count)")));
                sumMap.put("execute_time", GlobalCons.EXECUTE_TIME);
                result.add(sumMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        return result;
    }

    public static List<Map<String, String>> getClassInfo1() {
        List<Map<String, String>> result = new ArrayList<>();
        String sql = "SELECT COUNT(class_name), SUM(method_num), SUM(test_method_num), SUM(execute_case_num), SUM(success_count), SUM(skip_count), SUM(failure_count)FROM class_info; ";
        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, String> sumMap = new HashMap<>();
                sumMap.put("class_num", resultSet.getString("COUNT(class_name)"));
                sumMap.put("method_num", resultSet.getString("SUM(method_num)"));
                sumMap.put("test_method_num", resultSet.getString("SUM(test_method_num)"));
                sumMap.put("execute_case_num", resultSet.getString("SUM(execute_case_num)"));
                sumMap.put("success_count", resultSet.getString("SUM(success_count)"));
                sumMap.put("skip_count", resultSet.getString("SUM(skip_count)"));
                sumMap.put("failure_count", resultSet.getString("SUM(failure_count)"));
                sumMap.put("execute_time", GlobalCons.EXECUTE_TIME);
                result.add(sumMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        return result;
    }

    public List<Map<String, String>> getExceptionInfo(String packageName, String fullClassName) {
        List<Map<String, String>> result = new ArrayList<>();
        String sql = "select exception_name, count(*) from exception_type ";
        if (fullClassName != null && !fullClassName.isEmpty()) {
            sql = sql + "WHERE class_name_under_test = '" + fullClassName + "' ";
        } else if (packageName != null && !packageName.isEmpty()) {
            sql = sql + "WHERE class_name_under_test LIKE '" + packageName + "%' ";
        }
        sql += "group by exception_name;";
        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, String> exceptionNumMap = new HashMap<>();
                exceptionNumMap.put(resultSet.getString("exception_name"), StringUtil.formatString(resultSet.getString("count(*)")));
                result.add(exceptionNumMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        return result;
    }

    public static Map<String, List<String>> getExceptionInfo1(String packageName, String fullClassName) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> exceptionNameList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();

        String sql = "select exception_name, count(*) from exception_type ";
        if (fullClassName != null && !fullClassName.isEmpty()) {
            sql = sql + "WHERE class_name_under_test = '" + fullClassName + "' ";
        } else if (packageName != null && !packageName.isEmpty()) {
            sql = sql + "WHERE class_name_under_test LIKE '" + packageName + "%' ";
        }
        sql += "group by exception_name;";

        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            Map<String, Integer> tempMap = new HashMap<>();
            while (resultSet.next()) {
                tempMap.put(resultSet.getString("exception_name"), Integer.parseInt(resultSet.getString("count(*)")));
            }

            List<Map.Entry<String, Integer>> listData = new ArrayList<Map.Entry<String, Integer>>(tempMap.entrySet());
            Collections.sort(listData, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return (o2.getValue() - o1.getValue());
                }
            });

            for (int i = 0; i < 8 && i < listData.size(); i++) {
                Map.Entry<String, Integer> stringIntegerEntry = listData.get(i);
                exceptionNameList.add(stringIntegerEntry.getKey());
                valueList.add(String.valueOf(stringIntegerEntry.getValue()));
            }
            int otherNum = 0;
            for (int i = 8; i < listData.size(); i++) {
                Map.Entry<String, Integer> stringIntegerEntry = listData.get(i);
                otherNum += stringIntegerEntry.getValue();
            }
            if (otherNum > 0) {
                Map<String, String> exceptionNumMap = new HashMap<>();
                exceptionNameList.add("other exceptions");
                valueList.add(String.valueOf(otherNum));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        result.put("exceptionNameList", exceptionNameList);
        result.put("valueList", valueList);
        return result;
    }

    public List<Map<String, String>> getDetailInfo(String packageName, String fullClassName) {
        List<Map<String, String>> result = new ArrayList<>();
        String sql = "select a.test_method_name, b.class_name_under_test, b.exception_name from detail_info a, exception_type b WHERE a.exception_type_id = b.id ";
        if (fullClassName != null && !fullClassName.isEmpty()) {
            sql = sql + "AND b.class_name_under_test = '" + fullClassName + "' ";
        } else if (packageName != null && !packageName.isEmpty()) {
            sql = sql + "AND b.class_name_under_test LIKE '" + packageName + "%' ";
        }
        sql += ";";
        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, String> detailMap = new HashMap<>();
                detailMap.put("testMethodName", resultSet.getString("test_method_name"));
                detailMap.put("className", resultSet.getString("class_name_under_test"));
                detailMap.put("exceptionName", resultSet.getString("exception_name"));
                result.add(detailMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        return result;
    }

    public static Map<String, List<String>> getDetailInfo1(String packageName, String fullClassName) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> methodNameListTable = new ArrayList<>();
        List<String> classNameListTable = new ArrayList<>();
        List<String> exceptionNameListTable = new ArrayList<>();


        String sql = "select a.test_method_name, b.class_name_under_test, b.exception_name from detail_info a, exception_type b WHERE a.exception_type_id = b.id ";
        if (fullClassName != null && !fullClassName.isEmpty()) {
            sql = sql + "AND b.class_name_under_test = '" + fullClassName + "' ";
        } else if (packageName != null && !packageName.isEmpty()) {
            sql = sql + "AND b.class_name_under_test LIKE '" + packageName + "%' ";
        }
        sql += ";";

        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Map<String, String> detailMap = new HashMap<>();
                methodNameListTable.add(resultSet.getString("test_method_name"));
                classNameListTable.add(resultSet.getString("class_name_under_test"));
                exceptionNameListTable.add(resultSet.getString("exception_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        result.put("exceptionNameListTable", exceptionNameListTable);
        result.put("methodNameListTable", methodNameListTable);
        result.put("classNameListTable", classNameListTable);
        return result;
    }

    public static int getExceptionTypeId(String methodNameUnderTest, String classNameUnderTest, String exceptionName, String usualStackDetail) {
        String sql = "select id from exception_type where method_name_under_test = '" + methodNameUnderTest + "' and " +
                "class_name_under_test = '" + classNameUnderTest + "' and " +
                "exception_name = '" + exceptionName + "' and " +
                "usual_stack_info = '" + usualStackDetail + "';";
        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                SaveInfoService.insertIntoExceptionType(methodNameUnderTest, classNameUnderTest, exceptionName, usualStackDetail);
                return getExceptionTypeId(methodNameUnderTest, classNameUnderTest, exceptionName, usualStackDetail);
            }
        } catch (SQLException e) {
            // e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                // qlException.printStackTrace();
            }
        }
        return -1;
    }

    public static Map<String, List<String>> getDataForStaticHtml() {
        Map<String, List<String>> result = new HashMap<>();

        String sql1 = "SELECT id, method_name_under_test, class_name_under_test, exception_name from exception_type;";
        Statement statement = null;
        try {
            statement = GlobalCons.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql1);

            while (resultSet.next()) {
                String key = resultSet.getString("id") + "##" + resultSet.getString("method_name_under_test") + "##" +
                        resultSet.getString("class_name_under_test") + "##" + resultSet.getString("exception_name");
                result.put(key, new ArrayList<>());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        statement = null;
        try {
            for (String key : result.keySet()) {
                String id = key.split("##")[0];
                String sql2 = "SELECT test_method_name, test_method_body, stack_detail from detail_info where exception_type_id = " + id + ";";

                statement = GlobalCons.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql2);
                List<String> valueList = new ArrayList<>();
                while (resultSet.next()) {
                    valueList.add(resultSet.getString("test_method_name"));
                    valueList.add(resultSet.getString("test_method_body"));
                    valueList.add(resultSet.getString("stack_detail"));
                }

                result.put(key, valueList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        return result;
    }
}
