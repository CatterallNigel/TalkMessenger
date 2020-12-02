package io.illuminates.communications.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectToJSONString {

    private static Logger logger = LoggerFactory.getLogger(ObjectToJSONString.class);

    public static String convertObj(Object obj){
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(obj);
        }
        catch(Exception ex){
            //TODO something
        }
        return json;
    }
}
