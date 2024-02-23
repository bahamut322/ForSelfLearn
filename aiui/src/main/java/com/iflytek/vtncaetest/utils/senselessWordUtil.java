package com.iflytek.vtncaetest.utils;

import com.iflytek.vtncaetest.ContextHolder;

import java.util.Arrays;
import java.util.HashSet;

/**
 * 判断是否有意义词，true为有意义，false为有意义
 * 开发文档参考：https://www.yuque.com/iflyaiui/zzoolv/agg7qu#hGFAI
 */
public class senselessWordUtil {
    // 有意义词库
    private static String[] meaningfulWord = {"ok", "OK", "no", "NO", "hi", "HI", "Hi", "tv", "TV", "cd", "CD"};
    // 无意义词库
    private static String[] senselessWord;
    //无意义请求的sid,用于过滤无效tts
    public static String nonsenseSid = "";

    private static final HashSet<String> meaningfulSet = new HashSet<>();
    private static final HashSet<String> senselessSet = new HashSet<>();

    static {
        //初始化资源路径--方法1：大量文件读取txt文件
        senselessWord = FileUtil.readFile(ContextHolder.getContext(), "localRes/senselessWord.txt", "utf-8").split("\\r?\\n");
        meaningfulSet.addAll(Arrays.asList(meaningfulWord)); // 添加有意义词
        senselessSet.addAll(Arrays.asList(senselessWord)); // 添加无意义词
    }

    /**
     * @param asrResult 识别结果
     * @return true(有意义 ） false ( 无意义)
     */
    public static boolean isMeaningful_filter1word(String asrResult) {
        asrResult = asrResult.trim();
        if (asrResult.length() <= 1) {
            return meaningfulSet.contains(asrResult);//单字，在有意义列表中为true，不在为false
        } else {
            if (senselessSet.contains(asrResult)) {
                return false;//在无意义列表中，不处理
            } else {
                if (asrResult.matches("[a-zA-Z][a-zA-Z]") && !meaningfulSet.contains(asrResult)) {
                    return false;//2字无意义英文，不处理
                } else {
                    return true;//2字，有意义，处理
                }
            }
        }
    }

    public static boolean isMeaningful_filter2word(String asrResult) {
        asrResult = asrResult.trim();
        if (asrResult.length() <= 2) {
            return meaningfulSet.contains(asrResult);//2字,有意义为true，无意义为false
        } else {
            if (senselessSet.contains(asrResult)) {
                return false;//多字，在无意义列表中，不处理
            } else {
                return true; //多字，有意义，处理
            }
        }
    }
}
