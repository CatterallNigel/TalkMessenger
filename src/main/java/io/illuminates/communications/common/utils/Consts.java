package io.illuminates.communications.common.utils;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Consts {

    public static String QUALIFIED_NAME;
    public static String QUALIFIED_NAME_UUID;
    public static String ROUTE_CLIENT_HOME;
    public static String BASE_MSG_TYPE;
    public static String SUBSCRIBER_PING;
    public static String BASE_MSG_TOPIC;
    public static String TALK_SERVER_ADDR;
    public static String CLIENT_ENDPOINT;
    public static String WEBSOCKET_PROTOCOL;
    public static long HEARTBEAT_PERIOD;
    public static long HEARTBEAT_TIMEOUT;
    public static long HEARTBEAT_RESET_SESSION;


    public static final String SERVICE_MESSAGE_TYPE = "ServicesMessage";
    public static final String TOPICS_MESSAGE_TYPE = "TopicsMessage";
    public static final String FLASH_MESSAGE_TYPE = "FlashMessage";
    public static final String TOPIC_SERVICE_TITLE = "Topics";
    public static final String SUBSCRIPTION_SERVICE_TITLE = "Subscribe";

    public enum Routes {
        CLIENT(ROUTE_CLIENT_HOME);

        public final String label;

        private Routes(String label) {
            this.label = label;
        }
    }

    public static final String BAD_REQUEST_MSG = "Unable to process request -  ";
    public static final String UNAUTHORIZED_MSG = "You are not authorized for this request";
    public static final String OK_MSG_PROCESSED = "Message processed successfully - ";

    public static final String EMPTY_STRING = "";
}
