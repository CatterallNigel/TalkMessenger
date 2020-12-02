package io.illuminates.communications.common.resources;

import java.util.HashMap;
import java.util.Map;

public class MessageMap {
    public static Map<String,String> mapping = new HashMap<String,String>(){{
        {
            put("io.illuminates.communications.common.message.types.RegistrationMessage", "RegistrationMessage");
            put("io.illuminates.communications.common.message.types.AcknowledgementMessage","AcknowledgementMessage");
        }
    }};
}