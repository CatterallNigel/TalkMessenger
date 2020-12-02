package io.illuminates.communications.common.message.base;

import io.illuminates.communications.common.utils.Consts;

import java.time.Instant;
import java.util.UUID;

public class DefaultHeader {

    String messageType = "DefaultMessage";
    long timestamp = Instant.now().toEpochMilli();
    long expires = Long.MAX_VALUE; // default never expires
    String msgId = UUID.randomUUID().toString();
    String originator =  Consts.QUALIFIED_NAME_UUID;
    String routeFrom = "direct://default";
    String routeTo = "seda:messages";
    String[] recipients = new String[]{};
    boolean responseRequired = false;

    public DefaultHeader(){}

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getRouteFrom() {
        return routeFrom;
    }

    public void setRouteFrom(String routeFrom) {
        this.routeFrom = routeFrom;
    }

    public String getRouteTo() {
        return routeTo;
    }

    public void setRouteTo(String routeTo) {
        this.routeTo = routeTo;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public void setRecipients(String[] recipients) {
        this.recipients = recipients;
    }

    public boolean isResponseRequired() {
        return responseRequired;
    }

    public void setResponseRequired(boolean responseRequired) {
        this.responseRequired = responseRequired;
    }
}
