## 迪小萌X8多功能服务机器人版本说明<br/>

**V1.0.0**<br/>
主要实现讲解机器人的基础功能：广告管理、门岗管理（测温）、智能引领、智能讲解、人脸识别/检测<br/>
<br/>

**V1.1.0**<br/>
主要实现讲解机器人的基础功能：迎宾、业务办理页面、智能问答、人脸识别、检测<br/>
voice_record_command.properties  - voiceRecordType - 0:sendi 1:艾小越<br/>

**V1.2.0**<br/>
主要实现讲解机器人的迎宾管理、轻应用、新增机器人标语字段、Vip人脸识别<br/>
<br/>

**1.3.1注意事项**<br/>
目前debug版应用配置了2个真实的语义模型和2个模拟的语义模型：voiceRecordType
0：aiui
1: 艾小越
2: 模拟1（50%几率回复“test1"，50%几率无回复）
3: 模拟2（100%几率回复“test2"）
配置文件中可以配置模型的优先级，代号位置靠前则优先级高，则答案会被优先选择。目前配置文件中的顺序为2103，可自行修改测试不同的顺序。文件路径为/sdcard/config/voice_record_command.properties
<br/>

**1.3.2注意事项**<br/>
更换了语音合成的厂家--讯飞，目前该讯飞的语音合成所需的文件没有自动写入硬盘中，暂时需要手动放入，放置路径为/sdcard/iflytekAikit，文件见附件
<br/>

**V1.4.0**<br/>
/sdcard/config/voice_record_command.properties 增加了ttsType字段，用于控制tts的类型，0：BAIDU 1：XTTS
更新v1.4.0前需要手动删除原机器人内的/sdcard/config/voice_record_command.properties文件
<br/>
