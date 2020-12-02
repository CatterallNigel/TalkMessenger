package io.illuminates.communications.server.transactions;

import io.illuminates.communications.common.message.base.DefaultMessage;

import java.time.Instant;

public class Transaction {

    private String msgUUID;
    private String originator;
    private String recipient;
    private DefaultMessage message;
    private boolean acknowledged = false;
    private long transactionCreated =Instant.now().toEpochMilli();

    public Transaction() {}

    public String getMsgUUID() {
        return msgUUID;
    }

    public void setMsgUUID(String msgUUID) {
        this.msgUUID = msgUUID;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public DefaultMessage getMessage() {
        return message;
    }

    public void setMessage(DefaultMessage message) {
        this.message = message;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public long getTransactionCreated() {
        return transactionCreated;
    }
}
