package com.sparc.knappsack.util;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.models.SQSResignerModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T unmarshall(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.info("Unable to unmarshall json:", json, clazz);
        }
        return null;
    }

    public static String marshall(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.info("Unable to marshall object:", obj);
        }
        return null;
    }

    public static void main(String[] args) {
        SQSResignerModel model = new SQSResignerModel("myBucket", ApplicationType.IOS, "my/file/to/resign.ipa", "the/distribution/cert/dist.cert", "the/distribution/key/key.p12", "superSecretPassword", "the/distribution/profile/profile.mobileprovision", "https://mycallbackural.com/blahblah");

        String json = JsonUtil.marshall(model);
        System.out.println(json);
    }

}
