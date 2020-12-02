package io.illuminates.communications.server.websocket.camel.processor.common;

import io.illuminates.communications.common.message.body.subscribe.SubscriptionPing;
import io.illuminates.communications.server.models.ReQueue;
import io.illuminates.communications.server.models.Registry;
import io.illuminates.communications.server.models.publish.Topic;
import io.illuminates.communications.server.models.publish.Topics;
import io.illuminates.communications.server.transactions.Transaction;
import io.illuminates.communications.server.transactions.TransactionLog;
import io.illuminates.communications.server.utils.AppConsts;
import io.illuminates.communications.server.utils.ObjectToJSONString;
import io.illuminates.communications.common.message.base.DefaultMessage;
import io.illuminates.communications.common.message.body.Status;
import io.illuminates.communications.common.message.types.OutboundMessage;
import io.illuminates.communications.common.utils.Consts;
import org.apache.camel.*;
import org.apache.camel.component.atmosphere.websocket.WebsocketConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class TimerWebSocketProcessor  implements Processor {

    private static Logger logger = LoggerFactory.getLogger(TimerWebSocketProcessor.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TransactionLog transactionLog;
    private static final Logger LOG = LoggerFactory.getLogger(TimerWebSocketProcessor.class);


    @Override
    public void process(Exchange exchange) throws Exception {
        //Create Heatbeat Status Message requires ACK ..
        String[] receipents = Registry.connections.keySet().toArray(new String[0]);
        if(receipents.length == 0){
            return;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //Resend Undelivered Messages
        resendUnDeliveredMessages(receipents);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //Send Status Message/Request
        Status status = new Status();
        status.setStatusCode(HttpServletResponse.SC_CONTINUE);
        status.setMsgId(UUID.randomUUID().toString());
        status.setMessage(AppConsts.UPDATE_STATUS_MSG);
        try{
            createOutboundMessage(status, Status.class.getTypeName(),receipents);
        }catch(Exception ex) {
            LOG.error("Timer Send Message Error: " + ex.getMessage());
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //Send Subscription PINGs
        sendPingFlashForSubscription();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    private void resendUnDeliveredMessages(String[] receipents) {
        ReQueue queue = ReQueue.instanceOf();
        List<DefaultMessage> noAddressToSend = new ArrayList<DefaultMessage>() ;
        while(queue.noOfMessages() != 0){
            DefaultMessage dm = queue.popFirst();
            List<String> addressedTo = Arrays.asList(dm.getHeader().getRecipients());
            addressedTo.forEach(addr -> {
                //Check the recipient is connected and the message hasn't expired
                if(Arrays.stream(receipents).anyMatch(addr::equals) &&
                        dm.getHeader().getExpires() > Instant.now().toEpochMilli()){
                    //TODO Resend - to this receipant.
                    String msg = ObjectToJSONString.convertObj(dm);
                    sendBody(dm.getHeader().getRouteTo(), msg, Registry.connections.get(addr));
                }else if(!Arrays.stream(receipents).anyMatch(addr::equals) &&
                        dm.getHeader().getExpires() > Instant.now().toEpochMilli()){
                    //Presently not connected but message hasn't expired
                    noAddressToSend.add(dm);
                }
            });
        }
        //Add all those messages for unconnected clients back to the queue
        queue.getQueue().addAll(noAddressToSend);
    }

    private void sendPingFlashForSubscription(){

        //Probably want to move these out so we can use other routes
        String routeTo = "direct://status/out";
        String routeFrom = Registry.servicesOut.get("Flash");

        DefaultMessage om = new OutboundMessage();

        om.getHeader().setOriginator(Consts.QUALIFIED_NAME_UUID);
        om.getHeader().setRouteFrom(routeFrom);
        om.getHeader().setRouteTo(routeTo);
        om.getHeader().setMessageType(Consts.BASE_MSG_TYPE + Consts.FLASH_MESSAGE_TYPE);
        om.getBody().setType(Consts.SUBSCRIBER_PING);

        //Clone the topic list - in case of changes
        List<Topic> topicsList = new ArrayList<Topic>(Topics.getTopicList());
        topicsList.stream().filter(topic -> topic.subscriberAddresses().size() > 0).forEach(topic -> {
            om.getBody().setMsgBody(subscriptionPing(topic.getTitle()));
            String body = ObjectToJSONString.convertObj(om);
            topic.subscriberAddresses().forEach(sub -> {
                String recipient = Registry.clients.get(sub);
                if(recipient != null) {
                    if(topic.isMustAcknowledge()){
                        om.getHeader().setResponseRequired(true);
                        logTransaction(om, sub);
                    }
                    om.getHeader().setRecipients(new String[]{sub});
                    String msg = ObjectToJSONString.convertObj(om);
                    sendBody(routeTo, msg, recipient);
                }
            });
        });
    }

    private String subscriptionPing(String title){
        SubscriptionPing sp = new SubscriptionPing();
        sp.setName(title);
        sp.setValue(String.valueOf(Instant.now().toEpochMilli()));
        return ObjectToJSONString.convertObj(sp);
    }

    private void createOutboundMessage(Object body,String msgType, String[] recipients) throws Exception{

        //Probably want to move these out so we can use other routes
        String routeTo = "direct://status/out";
        String routeFrom = "direct://status/in";

        //Set-up basic message
        DefaultMessage om = new OutboundMessage();
        om.getHeader().setOriginator(Consts.QUALIFIED_NAME_UUID);
        om.getHeader().setRouteFrom(routeFrom);
        om.getHeader().setRouteTo(routeTo);
        om.getHeader().setResponseRequired(true);
        om.getHeader().setMessageType(msgType);
        om.getBody().setMsgBody(ObjectToJSONString.convertObj(body));


        Registry.clients.forEach((uuid,recipient) -> {
            String sendToAddr = recipient;
            if(sendToAddr != null) {
                om.getHeader().setRecipients(new String[]{uuid});
                logTransaction(om, uuid);
                String msg = ObjectToJSONString.convertObj(om);
                sendBody(routeTo, msg, recipient);
            }
        });
    }

    private void logTransaction(DefaultMessage msg, String recipient){
        Transaction transaction = new Transaction();
        transaction.setMsgUUID(msg.getHeader().getMsgId());
        transaction.setOriginator(msg.getHeader().getOriginator());
        transaction.setRecipient(recipient);
        transaction.setMessage(msg);
        transactionLog.addTransaction(transaction);
    }

    private void sendBody(String endpointUri, final Object body, String recipient) {
        CamelContext camelContext = applicationContext.getBean(CamelContext.class);
        ProducerTemplate template = camelContext.createProducerTemplate();
        template.send(endpointUri, new Processor() {
            public void process(Exchange exchange) {
                Message in = exchange.getIn();
                in.setHeader(WebsocketConstants.CONNECTION_KEY, recipient);
                in.setBody(body);
            }
        });
    }
}
