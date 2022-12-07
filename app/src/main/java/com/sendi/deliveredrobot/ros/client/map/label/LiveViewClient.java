package com.sendi.deliveredrobot.ros.client.map.label;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import sendi_sensor_msgs.InfraredImageRawResponse;


/**
 * 获取实景图
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/3/8
 */
@ServiceClient(ClientConstant.INFRARED_IMAGE_RAW)
public class LiveViewClient extends IAbstractClient {
    private InfraredImageRawResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), InfraredImageRawResponse.class);
        } else {
            response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public InfraredImageRawResponse getResponse() {
        try {
            countDownLatch.await(LIVE_VIEW_TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

    /*public void responseHandle(MessageDto message) {
        Map<String, Object> para = new HashMap<>();
        if (null != getResponse()) {
            int result = response.getResult();
            Image image = response.getInfraredRaw();

            ChannelBuffer data = image.getData();
            Properties properties = RosHelper.getProperties();
            File LiveView = new File(properties.getProperty("LiveView"));
            if (!LiveView.exists()) {
                try {
                    LiveView.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            byte[] pic = data.array();
            //System.out.println(pic.length+">>>>>>>"+image.getWidth()+ ">>>>>>" +image.getHeight());
//                BufferedImage bufferedImage = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(LiveView));
                int step = 45;
                for (int i = 0; i < image.getHeight(); i++) {
                    for (int j = 0; j < image.getWidth(); j++) {
                        int r = pic[step++];
                        int g = pic[step++];
                        int b = pic[step++];
                        int rgb = (r << 16) + (g << 8) + (b) + (0xff000000);
                        bitmap.setPixel(j, i, rgb);
                    }
                }
                FileOutputStream out = new FileOutputStream(LiveView);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            message.setStatus(1);
            para.put("msg", "成功");
            message.setPara(para);

        } else {
            message.setStatus(0);
            para.put("msg", "获取不到图片");
            message.setPara(para);
        }
    }*/
}
