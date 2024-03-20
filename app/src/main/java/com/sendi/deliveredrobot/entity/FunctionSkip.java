package com.sendi.deliveredrobot.entity;

import com.sendi.deliveredrobot.entity.entitySql.QuerySql;

import java.util.Objects;

public class FunctionSkip {

    public static int selectFunction() {
        int itemNum = -1;
        String defaultValue = QuerySql.QueryBasic().getDefaultValue();
        if (defaultValue!=null && defaultValue.split(" ").length == 1) {
            if (Objects.equals(defaultValue, "智能引领 ")){
                itemNum = 0;
            }else if(Objects.equals(defaultValue, "智能讲解 ")){
                itemNum = 1;
            }else if(Objects.equals(defaultValue, "智能问答 ")){
                itemNum = 2;
            }else if (Objects.equals(defaultValue, "更多服务 ")){
                itemNum = 3;
            }else if(Objects.equals(defaultValue, "业务办理 ")){
                itemNum = 5;
            }
        } else{
            itemNum = 4;
        }
        return itemNum;
    }
}
