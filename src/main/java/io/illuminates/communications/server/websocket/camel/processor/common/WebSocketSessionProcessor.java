package io.illuminates.communications.server.websocket.camel.processor.common;

import io.illuminates.communications.server.models.Registry;
import io.illuminates.communications.common.message.types.RegistrationMessage;
import io.illuminates.communications.server.utils.ObjectToJSONString;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.atmosphere.websocket.WebsocketConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.camel.component.atmosphere.websocket.WebsocketConstants.ONCLOSE_EVENT_TYPE;
import static org.apache.camel.component.atmosphere.websocket.WebsocketConstants.ONERROR_EVENT_TYPE;
import static org.apache.camel.component.atmosphere.websocket.WebsocketConstants.ONOPEN_EVENT_TYPE;

@Component
public class WebSocketSessionProcessor implements Processor {
    
    private static Logger logger = LoggerFactory.getLogger(WebSocketSessionProcessor.class);

    public void process(Exchange exchange) throws Exception {
        int eventType = (int) exchange.getIn().getHeader(WebsocketConstants.EVENT_TYPE);
        String connectionKey = (String) exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY);
        logger.info("Event notification from websocket client: " + eventType);

        switch (eventType){
            case ONOPEN_EVENT_TYPE :
                logger.info("Connection has been established successfully for connection key: " + connectionKey);
                RegistrationMessage rMsg = new RegistrationMessage(new String[]{"client-connect"}, connectionKey);
                String registrationMsg = ObjectToJSONString.convertObj(rMsg);
                exchange.getIn().setBody(registrationMsg);
                break;
            case ONCLOSE_EVENT_TYPE :
                logger.info("Connection has been closed successfully for connection key: " + connectionKey);
                //Client has Disconnected - remove them from the registry.
                Registry.cleanRegistry(connectionKey);
                break;
            case ONERROR_EVENT_TYPE :
                logger.info("An error event has been triggered for connection key: " + connectionKey);
                break;
            default:
                logger.info("Event notification from websocket client is Unknown.");
        }
    }
}
