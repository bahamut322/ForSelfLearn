package com.sendi.deliveredrobot.ros;

import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.Client;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResult;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;

import org.jetbrains.annotations.NotNull;
import org.ros.internal.message.Message;

/**
 * 客户端发送操作类
 *
 * @author Sunzecong
 * @version 1.0
 * @CreateDate: 2021/5/26
 */
public class ClientManager {
    /**
     * 调用 ros_server - 同步
     *
     * @param serviceClient -包括 url 和 调用参数
     * @return
     */
    public static RosResult sendClientMsg(@NotNull Client serviceClient) {
        // param checking
        if (serviceClient.getService() == null || "".equals(serviceClient.getService())) {
            return RosResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        }
        String url = serviceClient.getService();
        // if url exist
        if (DispatchService.getInstance() == null || !DispatchService.getInstance().hasUrlOrClassName(url)) {
            return RosResultUtil.failure(url, RosResultEnum.INVALID_URL.getMsg());
        }
        IAbstractClient client = DispatchService.getInstance().getBeanByUrl(url);
        if (!client.send(serviceClient.toString())) {
            return RosResultUtil.failure(url, RosResultEnum.ROS_CONNECT_ERROR.getMsg());
        }
        try {
            Object response = client.getResponse();
            if (response == null) {
                return RosResultUtil.failure(url, RosResultEnum.GET_RESPONSE_ERROR.getMsg());
            }
            if (response instanceof Message) {
                return RosResultUtil.success(url, (Message) response);
            }
            if (!(Boolean) response) {
                return RosResultUtil.failure(url, RosResultEnum.GET_RESPONSE_PARSE_ERROR.getMsg());
            }
            return RosResultUtil.success(url);
        } catch (Exception e) {
            return RosResultUtil.failure(url, RosResultEnum.GET_RESPONSE_ERROR.getMsg() + e);
        }
    }

    public static <T> RosResult<T> sendClientMsg(@NotNull Client serviceClient, Class<T> requireType) {
        DispatchService dispatchService = DispatchService.getInstance();
        String url = "";
        try {
            // param checking
            if (serviceClient.getService() == null || "".equals(serviceClient.getService()) || requireType == null) {
                throw new RuntimeException(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
            }
            url = serviceClient.getService();
            // if url exist
            if (dispatchService == null || !dispatchService.hasUrlOrClassName(url)) {
                throw new RuntimeException(RosResultEnum.INVALID_URL.getMsg());
            }
            IAbstractClient client = dispatchService.getBeanByUrl(url);
            if (!client.send(serviceClient.toString())) {
                throw new RuntimeException(RosResultEnum.ROS_CONNECT_ERROR.getMsg());
            }

            Object response = client.getResponse();
            if (response == null || !requireType.isInstance(response)) {
                return RosResultUtil.failure(url, RosResultEnum.GET_RESPONSE_ERROR.getMsg(), requireType);
            }
            return RosResultUtil.success(url, (T) response);
        } catch (Exception e) {
            return RosResultUtil.failure(url, e.getMessage(), requireType);
        }
    }


    /**
     * 调用 ros_server - 异步
     * RosResult.getResponse() 为空，response通过广播推送
     *
     * @param serviceClient -包括 url 和 调用参数
     * @return
     */
    public static RosResult sendAysnClientMsg(@NotNull Client serviceClient) {
        // param checking
        if (serviceClient.getService() == null || "".equals(serviceClient.getService())) {
            return RosResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        }
        String url = serviceClient.getService();
        // if url exist
        if (DispatchService.getInstance() == null || !DispatchService.getInstance().hasUrlOrClassName(url)) {
            return RosResultUtil.failure(url, RosResultEnum.INVALID_URL.getMsg());
        }
        IAbstractClient client = DispatchService.getInstance().getBeanByUrl(url);
        if (!client.send(serviceClient.toString())) {
            return RosResultUtil.failure(url, RosResultEnum.ROS_CONNECT_ERROR.getMsg());
        }
        return RosResultUtil.success(url, RosResultEnum.SEND_MESSAGE_SUCCESS.getMsg());
    }
}
