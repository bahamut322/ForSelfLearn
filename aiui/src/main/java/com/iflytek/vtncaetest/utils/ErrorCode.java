package com.iflytek.vtncaetest.utils;


import java.util.HashMap;

/**
 * 常见错误码和解决方式
 */
public class ErrorCode {

    private static HashMap<Integer, String> errorMsg = new HashMap();


    static {
        errorMsg.put(-1, "操作不允许，没有权限");
        errorMsg.put(-9, "坏的文件描述符，销毁录音机后重新初始化");
        errorMsg.put(-16, "多个设备同时录音");
        errorMsg.put(10120, "网络未连接或信号不好");
        errorMsg.put(10103, "应用没有开启语义理解就使用了文本请求，先开启语义理解\n" +
                "1、API对接：修改请求代码中的scene为main_box；\n" +
                "SDK对接：修改aiui.cfg中scene参数值为main_box；\n" +
                "2、aiui平台应用配置技能后发布上线");
        errorMsg.put(10105, "没有权限，检查是否使用的webapi应用，apikey，ip，checksum等授权参数是否正确");
        errorMsg.put(10106, "参数名称错误，检查aiui.cfg配置文件，检查必传参数、格式及编码，常见原因：\n" +
                "(1)配置换行漏了，号\n" +
                "(2)中英文逗号格式\n" +
                "(3)读取配置后，客户做了二次封装\n" +
                "(4)sdk没有设置唯一的sn信息\n" +
                "(5)用户的后处理服务，返回的结果不是json类型， 结果改成 {\"result\":\"error\"} 这种json格式");
        errorMsg.put(10107, "参数取值错误，检查配置文件，检查参数值是否超过范围或不符合要求");
        errorMsg.put(10108, "sdk缺少so库或dll库，1.检查aiui.cfg是否配置了so\n" +
                "2.android：\n" +
                "build.gradle如有以下内容，请去掉\n" +
                "packagingOptions {\n" +
                "    exclude \"**/libvtn_mic2*.so\"\n" +
                "    exclude \"**/libvtn_mic4*.so\"\n" +
                "    exclude \"**/libvtn_mic6*.so\"\n" +
                "}\n" +
                "windows：需要将dll放在当前可执行程序的同级目录");
        errorMsg.put(10110, "AIUI云端引擎授权不足1.在我的应用--》服务统计或设备统计中查看授权次数\n" +
                "2.联系商务开通授权");
        errorMsg.put(10116, "云端无对应的scene场景参数，1.检查账号的scene \n  2.检查aiui.cfg配置的scene和账号的scene是否匹配");
        errorMsg.put(10120, "等待云端结果超时，1.路由器网络信号差，可以先检查网络状况\n" +
                "2.设备wifi模组信号弱。同样环境下手机正常，设备10%几率超时，发现是设备的wifi模组信号能力较弱。可以用以下工具做对比测试\n" +
                "测试工具：https://pan.baidu.com/s/1CzREbrypkgLiRcjAYBav5A?pwd=AIUI ");
        errorMsg.put(10145, "后处理开启，地址未配置，需要关闭后处理或配置处理地址");
        errorMsg.put(10146, "后处理请求超时，需要检查是否开启了后处理");
        errorMsg.put(10200, "动态实体无法配置成wss协议，请在aiui.cfg配置中去掉\"aiui_ssb\":{}");
        errorMsg.put(10201, "日流控超限，联系商务，开通授权");
        errorMsg.put(10407, "APPID和Key不匹配，1.在aiui平台上查询appid和key  2.在aiui.cfg中配置appid和key，有些默认配置了key，切换appid后要显式配置key");
        errorMsg.put(11200, "1.联系商务开通授权，常见为发音人未授权或授权到期\n" +
                "2.如已授权，请在aiui平台先选择其他发音人，再次选择使用的发音人后点击右上角保存");
        errorMsg.put(11201, "日流控超限，联系商务");
        errorMsg.put(11210, "资源appid和应用appid不匹配");
        errorMsg.put(11216, "1. AIUI授权不足，联系商务处理\n" +
                "2. 如果为白名单授权，则当前sn未在白名单范围内");
        errorMsg.put(11217, "1.按次交互时，AIUI交互量已用完，联系商务\n" +
                "2.按台授权时，日流控超过限制，联系商务\n" +
                "3.一个sn与多个设备绑定，云端报错，重新初始化");
        errorMsg.put(11218, "检查AIUI和CAE上传的SN是否正常，长度不能超过32，不能包含特殊字符");
        errorMsg.put(20006, "录音失败，1. 搜索一下日志中的AlsaRecorder，分析具体原因。假如是\"libtinyalsa.so\" not found， 那么这个就是缺少so，把设备中的libtinyalsa.so放到工程的jnilibs/armeabi-v7a路径下\n" +
                "2. 声卡可能接触不良或者坏了，换声卡测试");
        errorMsg.put(21002, "离线tts的jet资源文件和aiui.so都需要绑定appid，请替换aiui.so后重试");
        errorMsg.put(25102, "检查唤醒配置文件vtn.ini及唤醒词bin文件路径是否正确、对应路径下文件是否存在");
        errorMsg.put(25201, "离线能力授权拒绝，离线合成授权用完了，找商务重新申请授权");
        errorMsg.put(25202, "离线sdk的测试版本只能联网测试，离线会报错。正式签订商务后提供完整离线功能");
        errorMsg.put(90003, "读取的aiui.cfg文件为空");
        errorMsg.put(30002, "上传参数有误，请检查resname等必要参数配置\n" +
                "尤其是resname，设置值为namespace.资源名");
        errorMsg.put(40000, "厂商无可用授权数量，联系商务");
        errorMsg.put(40001, "设备激活限制，联系商务");
        errorMsg.put(40002, "激活失败，参数不正确");
        errorMsg.put(40003, "激活失败,设备已经激活");
        errorMsg.put(40004, "设备未激活");
        errorMsg.put(40005, "设备未领取专区权益");
        errorMsg.put(40006, "设备未绑定酷狗用户");
        errorMsg.put(600001, "访问引擎配置文件失败");
        errorMsg.put(600002, "打开vtn.ini配置失败\n" +
                "1.检查系统中的对应路径下有没有文件，Android的某些系统对assets资源文件复制有限制，需要手动将资源文件push到指定路径\n" +
                "2.检查应用是否有读写文件的权限，Android 11高版本下系统做了限制，应用可能没有获取权限");
        errorMsg.put(600013, "1.创建cae句柄出错\n" +
                "2.传入的sn异常\n" +
                "3.没有降噪唤醒授权，联系讯飞开通");
        errorMsg.put(600021, "初始化授权操作失败");
        errorMsg.put(600022, "1.唤醒授权用完,联系商务授权\n" +
                "2.多次重装应用，同一个sn多次刷机触发了防盗刷机制，联系讯飞增加CAE刷机次数");
        errorMsg.put(600100, "1.找不到唤醒资源文件\n" +
                "2.检查vtn.ini中配置的bin文件路径");
        errorMsg.put(600103, "1.唤醒资源版本不对\n" +
                "2.检查唤醒bin文件版本");
    }

    public static String getError(int errorCode) {
        return errorMsg.get(errorCode);
    }
}
