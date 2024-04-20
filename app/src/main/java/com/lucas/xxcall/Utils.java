package com.lucas.xxcall;

import java.util.UUID;

public class Utils {
    public static Long getUUID() {
        // 生成 UUID
        UUID uuid = UUID.randomUUID();

        // 获取 UUID 的高64位和低64位
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();

        // 将两个64位的值组合成一个长整型
        long uuidLong = mostSignificantBits ^ leastSignificantBits;
        return  uuidLong;
    }

}
