package asia.asoulcnki.api.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperFactory {
    private static ObjectMapper INSTANCE;

    /**
     * 私有构造方法，防止错误 new
     */
    private ObjectMapperFactory() {
    }

    public static ObjectMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectMapper();
            // 忽略未定义字段
            INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return INSTANCE;
    }
}
