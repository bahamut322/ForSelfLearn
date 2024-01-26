package com.sendi.deliveredrobot.ros;

import android.content.Context;

import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import cn.hutool.core.thread.NamedThreadFactory;
import dalvik.system.DexFile;


/**
 * 维护rosBridge单利和自动注入
 *
 * @author Sunzecong
 */
public class DispatchService {
    /**
     * ioc容器
     */
    private final Map<String, IAbstractClient> mapping = new ConcurrentHashMap<>();
    /**
     * 单例对象
     */
    private static volatile DispatchService INSTANCE;
    /**
     * 状态位
     */
    public static AtomicBoolean isDispatchInit = new AtomicBoolean(false);
    public static AtomicBoolean isSubPreTopicList = new AtomicBoolean(false);
    public static List<String> preTopicList;
    public static ExecutorService scheduledThreadPool =  Executors.newSingleThreadExecutor();
    /**
     * rosBridge 初始化方法
     *
     * @param packageName ros报名
     * @param context     上下文
     */
    public static boolean initRosBridge(String packageName, Context context) {
        ReentrantLock lock = new ReentrantLock();
        LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* 初始化开始 *=*=*=*=*=*=*=*");
        try {
            lock.lock();
            // RosWebSocketManager初始化
            if (!RosWebSocketManager.isConnect.get()) {
                LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* RosBridge 初始化 *=*=*=*=*=*=*=*");
                RosWebSocketManager.getInstance().init();
            }
            // DispatchService初始化
            if (!DispatchService.isDispatchInit.get()) {
                LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* DispatchService 初始化 *=*=*=*=*=*=*=*");
//                long start = System.currentTimeMillis();
                createInstance(packageName, context);
//                long end = System.currentTimeMillis();
//                LogUtil.INSTANCE.i("bean init time:" + (end - start)); 1.9s
            }
            lock.unlock();
//            // 订阅自启动需要订阅的topic
//            if (!DispatchService.isSubPreTopicList.get()) {
//                LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* Ros topic 初始化 *=*=*=*=*=*=*=*");
//                List<String> subResults;
//                subResults = SubManager.subTopics(preTopicList);
//                if (subResults.size() == 0) {
//                    isSubPreTopicList.compareAndSet(false, true);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
            LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* 初始化结束 *=*=*=*=*=*=*=*");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* 初始化失败 *=*=*=*=*=*=*=*");
            return false;
        }
    }

    /**
     * subInitTopic 初始化订阅Topic
     *
     * @param preTopicList 需要开机订阅的topic
     */
    public static boolean subInitTopic(List<String> preTopicList) {
        boolean res = false;
        // 订阅自启动需要订阅的topic
        if (!DispatchService.isSubPreTopicList.get()) {
            if (RosWebSocketManager.isConnect.get()) {
                LogUtil.INSTANCE.i("*=*=*=*=*=*=*=* Ros topic 初始化 *=*=*=*=*=*=*=*");
                List<String> subResults;
                subResults = SubManager.subTopics(preTopicList);
                if (subResults.size() == 0) {
                    isSubPreTopicList.compareAndSet(false, true);
                    res = true;
                    DispatchService.preTopicList = preTopicList;
                }
            }
        }
        return res;
    }

    /**
     * 获取bean
     *
     * @param url url/类名
     */
    public <T extends IAbstractClient> T getBeanByUrl(String url) {
        return (T) mapping.get(url);
    }

    public <T extends IAbstractClient> T getBeanByName(String className) {
        return (T) mapping.get(className);
    }

    /**
     * 是否有对应的单例存在
     *
     * @param key ClientConstant
     */
    public boolean hasUrlOrClassName(String key) {
        return mapping.containsKey(key) && mapping.get(key) != null;
    }

    //获取单例
    public static DispatchService getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化，需要应用启动第一时间调用
     *
     * @param packageName "com.sendi.deliveredrobot"
     * @param context     上下文
     */
    public static void createInstance(String packageName, Context context) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (null == INSTANCE) {
            synchronized (DispatchService.class) {
                if (null == INSTANCE) {
                    INSTANCE = new DispatchService(packageName, context);
                }
            }
        }
    }

    /**
     * 私有构造方法
     */
    private DispatchService(String packgeName, Context context) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        boolean injectionResult = autoInjection(packgeName, context);
        // autoLoadBean();
        isDispatchInit.compareAndSet(false, injectionResult);
    }

    private boolean autoInjection(String packgeName, Context context) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return reader(packgeName, context);
    }

    // 配合手动加载类load
    /*
    private void autoLoadBean() {
        //对象注入
        for (Object object : mapping.values()) {
            if (null == object) {
                System.out.println("=============对象为空=============");
                continue;
            }
            Class clazz = object.getClass();
            if (clazz.isAnnotationPresent(Controller.class)) {
                //遍历属性，自动注入
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Autowired autowired = field.getAnnotation(Autowired.class);
                        String beanName = autowired.value();
                        if ("".equals(beanName)) {
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
//                        LogUtil.e("field: " + field.getName());
                        try {
                            field.set(object, mapping.get(beanName));
                        } catch (Exception e) {
//                            LogUtil.e("属性注入失败请检查检查: " + clazz.getName());
                            e.printStackTrace();
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
    }*/

    /**
     * 获取应用程序下的所有Dex文件
     *
     * @param packageCodePath 包路径
     * @return Set<DexFile>
     */
    public static Set<DexFile> applicationDexFile(String packageCodePath) {
        File dir = new File(packageCodePath).getParentFile();
        if (dir == null) return Collections.emptySet();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return Collections.emptySet();
        Set<DexFile> dexFiles = new HashSet<>();
        for (File file : files) {
            try {
                String absolutePath = file.getAbsolutePath();
                if (!absolutePath.contains(".")) continue;
                String suffix = absolutePath.substring(absolutePath.lastIndexOf("."));
                if (!suffix.equals(".apk")) continue;
                DexFile dexFile = createDexFile(file.getAbsolutePath());
                if (dexFile == null) continue;
                dexFiles.add(dexFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dexFiles;
    }

    /**
     * 创建DexFile文件
     *
     * @param path 路径
     * @return DexFile
     */
    public static DexFile createDexFile(String path) {
        try {
            return new DexFile(path);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * 读取类路径下的所有类
     *
     * @param packageName 包名
     * @param context     上下文
     */
    private boolean reader(String packageName, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String packageCodePath = context.getPackageCodePath();
        Set<DexFile> dexFiles = applicationDexFile(packageCodePath);
        if (dexFiles.isEmpty()) return false;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (DexFile dexFile : dexFiles) {
            if (dexFile == null) continue;
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String currentClassPath = entries.nextElement();
                if (currentClassPath == null || currentClassPath.isEmpty() || currentClassPath.indexOf(packageName) != 0) {
                    continue;
                }
                Class<?> clazz = Class.forName(currentClassPath, true, classLoader);
                String className = clazz.getName();
                //初始化controller
                if (clazz.isAnnotationPresent(ServiceClient.class)) {
                    ServiceClient serviceClient = clazz.getAnnotation(ServiceClient.class);
                    if (serviceClient == null) continue;
                    String url = serviceClient.value();
                    if (mapping.containsKey(url) || mapping.containsKey(className)) {
                        throw new RuntimeException("Bean Repeat! class: " + className);
                    }
                    IAbstractClient o = (IAbstractClient) clazz.newInstance();
                    if (!"".equals(url)) {
                        mapping.put(url, o);
                    }
                    mapping.put(className, o);
                } else if (clazz.isAnnotationPresent(Subscribe.class)) {
                    // 添加到 Bean 容器中
                    Subscribe subscribe = clazz.getAnnotation(Subscribe.class);
                    if (subscribe == null) continue;
                    String url = subscribe.value();
                    if (mapping.containsKey(url) || mapping.containsKey(className)) {
                        throw new RuntimeException("Bean Repeat! class: " + className);
                    }
                    IAbstractClient o = (IAbstractClient) clazz.newInstance();
                    if (!"".equals(url)) {
                        mapping.put(url, o);
                        // 添加到 SubManage 容器中
                        SubManager.addSub(url);
                    }
                    mapping.put(className, o);
                }
            }
        }
        return true;
    }

    /**
     * rosbridge 回复消息处理
     *
     * @param text rosResult
     */
    public void messageHandler(String text) {
        try {
            scheduledThreadPool.execute(()-> {
                String serviceOpStr = "\"op\": \"service_response\"";
                String serviceStr = "\"service\": \"";
                String topicOpStr = "\"op\": \"publish\"";
                String topicStr = "\"topic\": \"";
                // JSONObject jsonObject = JSONObject.parseObject(text);
                // String op = jsonObject.getString(JRosbridgeConstant.FIELD_OP);
                if (text.lastIndexOf(serviceOpStr) > 0) {
                    if (text.length() > 1000) {
                        LogUtil.INSTANCE.i("【RESPONSE】 " + text.substring(0, 999));
                    } else {
                        LogUtil.INSTANCE.i("【RESPONSE】 " + text);
                    }
                    //service 回复
                    int serviceStart = text.lastIndexOf(serviceStr) + serviceStr.length();
                    int serviceEnd = text.indexOf("\"", serviceStart);
                    if (serviceStart < serviceStr.length() || serviceEnd < 0) {
                        LogUtil.INSTANCE.e("【RESPONSE ERROR】 json格式不正确");
                        return;
                    }
                    String service = text.substring(serviceStart, serviceEnd);
//                jsonObject.getString(JRosbridgeConstant.FIELD_SERVICE);
                    IAbstractClient serviceBean = INSTANCE.getBeanByUrl(service);
                    if (serviceBean == null) {
                        LogUtil.INSTANCE.e("【RESPONSE ERROR】 service处理类不存在！service: " + service);
                        return;
                    }
                    try {
                        serviceBean.callbackMessageHandle(text);
                    } catch (Exception e) {
                        LogUtil.INSTANCE.e("【RESPONSE ERROR】 service:" + service + "\n" + e);
                    }
                } else if (text.lastIndexOf(topicOpStr) > 0) {
                    // ==============================================================
                    if (!checkNeedLogOut(text)) {
                        LogUtil.INSTANCE.i("【TOPIC】 " + text);
                    }
                    // ==============================================================
                    //topic 上送的数据
                    //service 回复
                    int topicStart = text.lastIndexOf(topicStr) + topicStr.length();
                    int topicEnd = text.indexOf("\"", topicStart);
                    if (topicStart < topicStr.length() || topicEnd < 0) {
                        LogUtil.INSTANCE.e("【TOPIC ERROR】 json格式不正确");
                        return;
                    }
                    String topic = text.substring(topicStart, topicEnd);
//                String topic = jsonObject.getString(JRosbridgeConstant.FIELD_TOPIC);
                    IAbstractClient topicBean = INSTANCE.getBeanByUrl(topic);
                    if (topicBean == null) {
                        LogUtil.INSTANCE.e("【TOPIC ERROR】 topic处理类不存在！topic: " + topic);
                        return;
                    }
                    try {
                        topicBean.callbackMessageHandle(text);
                    } catch (Exception e) {
                        LogUtil.INSTANCE.e("【TOPIC ERROR】 topic:" + topic + "\n" + e);
                    }
                } else {
                    LogUtil.INSTANCE.e("【ROS ERROR】 Ros上报处理消息没有对应的类型\ntext: " + text);
                }
            });
        } catch (Exception ignored) {
        }
    }


    private boolean checkNeedLogOut(String text) {
        return text.contains(ClientConstant.BATTERY_STATE) ||
                text.contains(ClientConstant.SUB_MAP_INFO) ||
                text.contains(ClientConstant.PAUSE_CHECK) ||
                text.contains(ClientConstant.SAFE_STATE_TOPIC) ||
                text.contains(ClientConstant.VOICE_PROMPT_TOPIC) ||
                text.contains(ClientConstant.LASER_SCAN) ||
                text.contains(ClientConstant.ROBOT_POSE) ||
                text.contains(ClientConstant.LABEL_LIST);
    }
}