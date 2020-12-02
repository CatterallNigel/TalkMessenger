package io.illuminates.communications.server.websocket.camel.processor.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.illuminates.communications.common.message.types.InboundMessage;
import io.illuminates.communications.server.models.ReQueue;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.atmosphere.websocket.WebsocketConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WebSocketNotDeliveredMessageProcessor implements Processor {

    private static Logger logger = LoggerFactory.getLogger(WebSocketNotDeliveredMessageProcessor.class);

    //TODO Something with undelivered
    public void process(Exchange exchange) throws Exception {
        String clientMessage = exchange.getIn().getBody(String.class);
        String connectionKey = (String) exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY);
        logger.info("Message to connection key:" + connectionKey + ", has not been delivered. Message body: " + clientMessage);
        reQueueMessage(clientMessage);
    }

    private void reQueueMessage(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InboundMessage msg = mapper.readValue(message, InboundMessage.class);
            //TODO Do we need to requeue messages that don't require a response
            //in other words if they don't need a response, do they care that they were delivered ?
            //so on messages which don't require a response, the expiry should be set to a short timespan ??
            if(msg.getHeader().getExpires() > Instant.now().toEpochMilli()) {
                ReQueue.instanceOf().addMsgToNormalQueue(msg);
            }
        } catch(Exception ex ){
            logger.error("Unable to re-queue message BAD Format Error: " + ex.getMessage());
        }
    }
}
