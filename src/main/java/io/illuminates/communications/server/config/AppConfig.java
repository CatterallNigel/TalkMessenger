package io.illuminates.communications.server.config;

import io.illuminates.communications.common.utils.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Autowired
    public void setQualifiedNameUUID(@Value("${illuminates.qualified.name.uuid}") String name) {
        Consts.QUALIFIED_NAME_UUID = name;
    }

    @Autowired
    public void setQualifiedName(@Value("${illuminates.qualified.name}") String name) {
        Consts.QUALIFIED_NAME = name;
    }

    @Autowired
    public void setHomeRoute(@Value("${illuminates.home.route}") String route){
        Consts.ROUTE_CLIENT_HOME = route;
    }

    @Autowired
    public void setBaseMessageType(@Value("${illuminates.base.message.type}") String baseMsgType) { Consts.BASE_MSG_TYPE = baseMsgType;}

    @Autowired
    public void setBaseMessageTopic(@Value("${illuminates.base.message.topic}") String baseMsgTopic) { Consts.BASE_MSG_TOPIC = baseMsgTopic;}

    @Autowired
    public void setTalkServerAddress(@Value("${illuminates.talk.server.address}") String serverAddr) { Consts.TALK_SERVER_ADDR = serverAddr;}

    @Autowired
    public void setClientEndpoint(@Value("${illuminates.talk.client.endpoint}") String clientEndpoint) { Consts.CLIENT_ENDPOINT = clientEndpoint;}

    @Autowired
    public void setWebsocketProtocol(@Value("${illuminates.talk.websocket.protocol}") String protocol) { Consts.WEBSOCKET_PROTOCOL = protocol;}

    @Autowired
    public void setHeartbeatPeriod(@Value("${illuminates.talk.heartbeat.period}") long period) { Consts.HEARTBEAT_PERIOD = period;}

    @Autowired
    public void setHeartbeatTimeout(@Value("${illuminates.talk.heartbeat.timeout}") long timeout) { Consts.HEARTBEAT_TIMEOUT = timeout;}

    @Autowired
    public void setHeartbeatResetSession(@Value("${illuminates.talk.reset.session.timeout}") long reset) { Consts.HEARTBEAT_RESET_SESSION = reset;}

    @Autowired
    public void setSubscriberPing(@Value("${illuminates.base.subscription.ping}") String ping) { Consts.SUBSCRIBER_PING = ping;}

    //Used in addition of @PropertySource
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
