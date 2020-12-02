package io.illuminates.communications.server.websocket.camel.processor.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.illuminates.communications.server.models.Registry;
import io.illuminates.communications.server.models.publish.Topic;
import io.illuminates.communications.server.models.publish.Topics;
import io.illuminates.communications.server.models.subscribe.Subscriber;
import io.illuminates.communications.server.transactions.Transaction;
import io.illuminates.communications.server.transactions.TransactionLog;
import io.illuminates.communications.server.utils.AppConsts;
import io.illuminates.communications.common.message.base.DefaultMessage;
import io.illuminates.communications.common.message.body.Key;
import io.illuminates.communications.common.message.body.Status;
import io.illuminates.communications.common.message.types.AcknowledgementMessage;
import io.illuminates.communications.server.utils.ObjectToJSONString;
import io.illuminates.communications.common.message.types.InboundMessage;
import io.illuminates.communications.common.message.types.OutboundMessage;
import io.illuminates.communications.common.utils.Consts;
import org.apache.camel.*;
import org.apache.camel.component.atmosphere.websocket.WebsocketConstants;
import org.apache.tomcat.websocket.WsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class WebSocketMessageProcessor implements Processor {

    private static Logger logger = LoggerFactory.getLogger(WebSocketMessageProcessor.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TransactionLog transactionLog;

    private WsSession session;


    private static final Logger LOG = LoggerFactory.getLogger(WebSocketMessageProcessor.class);
    private static final String NOT_SUPPORTED = "501:  talk.illuminates.one does not support the functionality needed to fulfill the request.";

    public void process(Exchange exchange) throws Exception {

        String clientMessage = exchange.getIn().getBody(String.class);
        try{
            ObjectMapper mapper = new ObjectMapper();
            InboundMessage inMsg = mapper.readValue(clientMessage, InboundMessage.class);

            Map<String, Object> headers = exchange.getIn().getHeaders();
            String uniqueConnectionKey = headers.get("websocket.connectionKey").toString();

            String[] recipients = inMsg.getHeader().getRecipients();
            if(recipients.length == 1 && recipients[0].equalsIgnoreCase(Consts.QUALIFIED_NAME_UUID)){
                //Internal message to process.
                DefaultMessage dm = processMessage(inMsg, uniqueConnectionKey);
                if(dm != null) {
                    String outMsg = ObjectToJSONString.convertObj(dm);
                    exchange.getIn().setBody(outMsg);
                }
            } else {
                // Outgoing Message to process
            }
        } catch(Exception ex){
            exchange.getIn().setHeader("Status", HttpServletResponse.SC_NOT_IMPLEMENTED);
            exchange.getIn().setHeader("Connection", "close"); // Doesn't seem to close connection
            exchange.getIn().setBody(NOT_SUPPORTED);
        }
        LOG.info("Message received from Websocket Client: " + clientMessage);
    }

    private DefaultMessage processMessage(InboundMessage message, String key) {
        String[] path = message.getHeader().getMessageType().split("\\.");
        String msgType = path[path.length - 1];
        ObjectMapper mapper = new ObjectMapper();
        try {
            switch (msgType) {
                case "RegistrationMessage":
                    Key client = mapper.readValue((String) message.getBody().getMsgBody(), Key.class);
                    //TODO Some authentication
                    if(isAuthenticated(client.getKey())) {
                        //TODO WHAT to do with already existing Client Key ???
                        Registry.clients.put(client.getKey(), key);
                        Registry.connections.put(key, client.getKey());
                        if (!message.getHeader().getRouteTo().equalsIgnoreCase(Consts.EMPTY_STRING)) {
                            try {
                                createOutboundMessage(message, new String[]{message.getHeader().getOriginator()});
                            } catch (Exception ex) {
                                LOG.info("Error creating OutboundMessage: " + ex.getMessage());
                            }
                        }
                        return messageAcknowledgement(message, msgType, HttpServletResponse.SC_OK, Consts.OK_MSG_PROCESSED);
                    } else {
                        //TODO UnAuthorized Message - socket disconnect ?? exchange.getIn().setHeader("connectionClose", true) ??
                        return messageAcknowledgement(message, key, HttpServletResponse.SC_UNAUTHORIZED, Consts.UNAUTHORIZED_MSG);
                    }
                case "AcknowledgementMessage":
                    // TODO Match with outgoing message - esp for guaranteed delivery
                    processStatusMessage(message);
                    break;
                case "InboundMessage":
                    if(!message.getHeader().getRouteTo().equalsIgnoreCase(Consts.EMPTY_STRING)) {
                        try {
                            createOutboundMessage(message, message.getHeader().getRecipients());
                        } catch (Exception ex) {
                            LOG.error("Error creating OutboundMessage: " + ex.getMessage());
                        }
                    }
                    break;
                case "Status":
                    //TODO Handel Status responses
                    processStatusMessage(message);
                    break;
                case "FlashMessage":
                    //TODO
                    break;
                default:
                    logger.info("Unknown Message Type: " + path);
                    break;
            }
        }catch(Exception ex){
            //BAD Format Message
            return messageAcknowledgement(message, key, HttpServletResponse.SC_BAD_REQUEST, Consts.BAD_REQUEST_MSG);
        }
        return null;
    }

    private boolean isAuthenticated(String key){
        // //TODO Some authentication check OR login
        return true;
    }

    private void processAcknowledgeMessage(InboundMessage message){
        //AcknowledgeMessage presently always contain a Status Message - however that might change in future
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Not Implemented - AcknowledgeMessage with Body other than Status");
        }
        catch(Exception ex) {

        }
    }

    private void processStatusMessage(InboundMessage message){
        ObjectMapper mapper = new ObjectMapper();
        try {
            Status status = mapper.readValue((String) message.getBody().getMsgBody(), Status.class);
            String statusMsgId = status.getMsgId();

            //Set the transaction in the transaction log to completed - IF exista
            Transaction transaction = transactionLog.getByStatusMsgId(statusMsgId);
            if(transaction != null) {
                LOG.info("Transaction updated Cycle Duration: " + (transaction.getTransactionCreated() - message.getHeader().getTimestamp())/1000 + ":secs");
                transaction.setAcknowledged(true);
            }

            if (status.getStatusCode() == HttpServletResponse.SC_OK) {
                LOG.info("Status all good :" + status.getMessage());
            }
            else if(status.getStatusCode() == HttpServletResponse.SC_CONTINUE) {
                if(message.getHeader().isResponseRequired()) {
                    //TODO requires a response
                    LOG.info("Received Status HTTP CONTINUE MSG - Response Required: " + new Date(message.getHeader().getTimestamp()));
                } else{
                    //TODO something else ??? as no response  is required.
                    LOG.info("Received Status HTTP CONTINUE MSG - Response NOT Required: " + new Date(message.getHeader().getTimestamp()));
                }
            } else if(status.getStatusCode() == HttpServletResponse.SC_NOT_MODIFIED) {
                //TODO - Nothing has changed client is still responsive
                LOG.info("Received Status HTTP NOT MODIFIED MSG - Response NOT Required: " + new Date(message.getHeader().getTimestamp()));
            } else if(status.getStatusCode() == HttpServletResponse.SC_RESET_CONTENT) {
                //TODO - Something has has changed NEED a change request message
                LOG.info("Received Status HTTP RESET CONTENT MSG - Response Required: " + new Date(message.getHeader().getTimestamp()));
            } else if(status.getStatusCode() == HttpServletResponse.SC_EXPECTATION_FAILED) {
                //TODO - The client was unable to decipher the message
                //IF it was a Flash message - resend once, it may have got garbled somewhere
                LOG.info("Received Status HTTP SC EXPECTATION FAILED MSG - Response Required: " + new Date(message.getHeader().getTimestamp()));
            } else{
                //TODO handle other possible Status's
                LOG.info("Received Status HTTP STATUS NOT HANDLED MSG - Response NOT Required: " + new Date(message.getHeader().getTimestamp()));
            }
        }catch( Exception ex){
            LOG.error("Failed to get/process STATUS-MSG: " + ex.getMessage());
            //OK Maybe not a Status message body - try generic/lookup
            processAcknowledgeMessage(message);
        }
    }

    private void processSubscriptionMessage(InboundMessage message){
        ObjectMapper mapper = new ObjectMapper();
        try {
            Subscriber subscriber = mapper.readValue((String)message.getBody().getMsgBody(),Subscriber.class);
            Topic topic = Topics.getTopicByType(subscriber.getTopic());
            if(topic.subscriberAddresses().isEmpty() ||
                    topic.subscriberAddresses().stream().filter(s -> s == subscriber.getQualifiedUUID()).findFirst() == null) {
                topic.addSubscriber(subscriber);
            }
        }catch(Exception ex){
            System.err.println("Unable to update Subscription - Error: " + ex.getMessage());
        }
    }

    private void processPublishMessage(InboundMessage message){
        logger.info("Raw Publish Topic: " + message.getBody().getMsgBody());
        ObjectMapper mapper = new ObjectMapper();
        try {
            Topic topic = mapper.readValue((String)message.getBody().getMsgBody(), Topic.class);
            Topics.addTopic(topic);
            logger.info("Number of listed Topics is now: " + Topics.getTopicList().size());
        }catch(Exception ex) {
            logger.error("Error in processing Topic: " + ex.getMessage());
        }
    }

    private DefaultMessage messageAcknowledgement(InboundMessage message, String type, int statusCode, String reply ){

        Status status = new Status();
        status.setStatusCode(statusCode);
        status.setMsgId(message.getHeader().getMsgId());
        status.setMessage(reply + type);

        AcknowledgementMessage ack = new AcknowledgementMessage(status);
        ack.getHeader().setRouteFrom(message.getHeader().getRouteTo());
        String routeTo = statusCode != HttpServletResponse.SC_OK ? "direct://resend" : "direct://continue";
        ack.getHeader().setRouteTo(routeTo);
        ack.getHeader().setRecipients(new String[]{message.getHeader().getOriginator()});
        return ack;
    }

    private void createOutboundMessage(InboundMessage message, String[] recipients) throws Exception{
        String from = message.getHeader().getRouteFrom();
        String service = Registry.servicesIn.get(from);
        if(service == null){
            throw new Exception("Service NOT found");
        }

        String route = message.getHeader().getRouteTo();

        //Set-up basic message
        DefaultMessage om = new OutboundMessage();
        om.getHeader().setRecipients(recipients);
        om.getHeader().setRouteFrom(Registry.servicesOut.get(service));
        om.getHeader().setRouteTo(route);

        switch(service) {
            case "Services":
                //Send out list of available Services
                Registry.Services rs = Registry.getListedServices();
                om.getBody().setMsgBody(ObjectToJSONString.convertObj(rs));
                om.getHeader().setMessageType(Consts.BASE_MSG_TYPE + Consts.SERVICE_MESSAGE_TYPE);
                break;
            case "Topics":
                //Send out list of registered topics
                //TODO correctly filter topics in getActiveTopics
                Topic[] topics = Topics.getActiveTopics().toArray(new Topic[0]);
                om.getBody().setMsgBody(ObjectToJSONString.convertObj(topics));
                om.getHeader().setMessageType(Consts.BASE_MSG_TYPE + Consts.TOPICS_MESSAGE_TYPE);
                //This need to be sent back to the originator as it is a service request
                recipients = new String[]{message.getHeader().getOriginator()};
                break;
            case "Relay":
                //Forward to message on
                om = message;
                break;
            case "Broadcast":
                //Send to everyone
                break;
            case "Publish":
                //Add to, register a topic
                processPublishMessage(message);
                om = messageAcknowledgement(message, message.getBody().getType(),
                        HttpServletResponse.SC_OK, Consts.OK_MSG_PROCESSED);
                //This need to be sent back to the originator as it is a service request
                recipients = new String[]{message.getHeader().getOriginator()};
                break;
            case "Subscribe":
                //Subscribe to a registered topic
                processSubscriptionMessage(message);
                om = messageAcknowledgement(message,AcknowledgementMessage.class.getName(),
                        HttpServletResponse.SC_OK, AppConsts.SUBSCRIPTION_STATUS);
                //This need to be sent back to the originator as it is a service request
                recipients = new String[]{message.getHeader().getOriginator()};
                break;
            case "Flash":
                //Send to subscribers
                break;
        }

        String body = ObjectToJSONString.convertObj(om);
        List<String> list = Arrays.asList(recipients);
        list.forEach(recipient -> {
            String sendToAddr = Registry.clients.get(recipient) != null ? Registry.clients.get(recipient) : recipient;
            if(sendToAddr != null) {
                sendBody(message.getHeader().getRouteTo(), body, sendToAddr);
            }
        });
    }

    protected void sendBody(String endpointUri, final Object body, String recipient) {
        CamelContext camelContext = applicationContext.getBean(CamelContext.class);
        ProducerTemplate template = camelContext.createProducerTemplate();
        template.send(endpointUri, new Processor() {
            public void process(Exchange exchange) {
                try {
                Message in = exchange.getIn();
                in.setHeader(WebsocketConstants.CONNECTION_KEY, recipient);
                in.setBody(body);
                } catch(Exception ex){
                    logger.error("Error sending message : " + ex.getMessage());
                    //Requeue the message ? May have occurred prior to registering the UUID to the connectionKey?
                }
            }
        });
    }
}
