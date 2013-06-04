package com.sparc.knappsack.util;

import com.sparc.knappsack.models.EmailModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T unmarshall(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Unable to unmarshall json:", json, clazz);
        }
        return null;
    }

    public static String marshall(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("Unable to marshall object:", obj);
        }
        return null;
    }

    public static void main(String[] args) {
        EmailModel emailModel = JsonUtil.unmarshall("{\"eventType\":\"USER_INVITE\",\"params\":{\"userId\":2,\"invitationIds\":[829,828]}}", EmailModel.class);
        convertIntegersToLongs(emailModel);
        Long fromUserId = (Long) emailModel.getParams().get("userId");
        List<Long> invitationIds = (List<Long>) emailModel.getParams().get("invitationIds");

//        SQSResignerModel model = new SQSResignerModel("myBucket", ApplicationType.IOS, "my/file/to/resign.ipa", "the/distribution/cert/dist.cert", "the/distribution/key/key.p12", "superSecretPassword", "the/distribution/profile/profile.mobileprovision", "https://mycallbackural.com/blahblah");
//
//        String json = JsonUtil.marshall(model);
//        System.out.println(json);
    }

    private static void convertIntegersToLongs(EmailModel emailModel) {
        Map<String, Object> params = emailModel.getParams();
        for (String s : params.keySet()) {
            Object value = params.get(s);
            if(value instanceof Integer) {
                params.put(s, Long.valueOf((Integer)value));
            } else if (value instanceof Collection) {
                List<Long> longList = new ArrayList<Long>();
                for (Object elem : (Collection) value) {
                    if (elem instanceof Integer) {
                        longList.add(Long.valueOf((Integer) elem));
                    }
                }
                if (!CollectionUtils.isEmpty(longList)) {
                    params.put(s, longList);
                }
            }
        }
    }

}
