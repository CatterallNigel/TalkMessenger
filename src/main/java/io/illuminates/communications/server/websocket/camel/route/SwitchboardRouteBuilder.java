package io.illuminates.communications.server.websocket.camel.route;

import io.illuminates.communications.server.websocket.camel.processor.common.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.atmosphere.websocket.WebsocketConstants;
import org.apache.camel.model.language.SimpleExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.server.ServerEndpoint;

@Component("client-in")
@ServerEndpoint("atmosphere-websocket:///client/in")
public class SwitchboardRouteBuilder extends RouteBuilder  {

    private static Logger logger = LoggerFactory.getLogger(SwitchboardRouteBuilder.class);

    @Autowired
    private WebSocketSessionProcessor webSocketSessionProcessor;
    @Autowired
    private WebSocketMessageProcessor webSocketMessageProcessor;
    @Autowired
    private WebSocketNotDeliveredMessageProcessor webSocketNotDeliveredMessageProcessor;
    @Autowired
    private TimerWebSocketProcessor timerWebSocketProcessor;
    @Autowired
    private TransactionWebSocketProcessor transactionWebSocketProcessor;

    @Override
    public void configure() throws Exception {
        //Main handler for all incoming messages
        from("atmosphere-websocket:///client/in")
                .choice()
                    .when(header(WebsocketConstants.EVENT_TYPE).isEqualTo(WebsocketConstants.ONOPEN_EVENT_TYPE))
                        .process(webSocketSessionProcessor)
                        .to("direct://delay")
                    .when(header(WebsocketConstants.EVENT_TYPE).isEqualTo(WebsocketConstants.ONCLOSE_EVENT_TYPE))
                        .process(webSocketSessionProcessor)
                    .when(header(WebsocketConstants.EVENT_TYPE).isEqualTo(WebsocketConstants.ONERROR_EVENT_TYPE))
                        .process(webSocketSessionProcessor)
                    .when(header(WebsocketConstants.ERROR_TYPE).isEqualTo(WebsocketConstants.MESSAGE_NOT_SENT_ERROR_TYPE))
                        .process(webSocketNotDeliveredMessageProcessor).endChoice()
                    .otherwise() // TODO handel with Ststus message..
                        .process(webSocketMessageProcessor).endChoice(); //.transform(new SimpleExpression("${body}"))
                        //.to("atmosphere-websocket:///client/in");

        //Direct to Client - sent by connection key. Recycled message
        from("direct:client/out").to("atmosphere-websocket:///client/in?sendToAll=false").end();
        //Registration Acknowledgement Message
        from("direct://delay").delay(100).to("atmosphere-websocket:///client/in");
        //Heartbeat Time - send Status message and expects a reply
        from("timer://heartbeat/status?delay=30000&period=30000").process(timerWebSocketProcessor).end(); //.to("seda:messages");
        //Send Status Heartbeat Messages - processed from timer://heartbeat/status
        //Send all NEW outgoing messages to any registered client.
        from("direct://status/out").to("atmosphere-websocket:///client/in").end();
        //Timer to archive, remove and resend completed/uncompleted message transactions
        from("timer://housekeeping?delay=180000&period=60000").process(transactionWebSocketProcessor).end();
    }
}
