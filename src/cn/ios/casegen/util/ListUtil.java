package cn.ios.casegen.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-14 17:10
 **/

public class ListUtil {
    public static <T> T getOneRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int i = new Random().nextInt(list.size());
        return list.get(i);
    }

    public static <T> T getLastElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public static <T> T getFirstElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static void removeLastElement(List<?> list) {
        if (list == null || list.isEmpty()) return;
        list.remove(list.size() - 1);
    }

    public static void removeFirstElement(List<?> list) {
        if (list == null || list.isEmpty()) return;
        list.remove(0);
    }

    public static <T> List<T> reverse(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<T> newList = Lists.newArrayList();
        for (int i = list.size() - 1; i > -1; i--) {
            newList.add(list.get(i));
        }
        return newList;
    }
}