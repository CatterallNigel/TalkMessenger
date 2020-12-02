package io.illuminates.communications.common.message.body.publish;

import java.time.Instant;

public class Topic {

    private String title;
    private String publisher; // Originator UUID
    private String type; // eg: io.illuminates.communications.common.message.types.topics.subject.Xxxxxxx
    private boolean mustAcknowledge = false;
    private boolean deliverOnce = false;
    private boolean guaranteeDelivery = false;
    private boolean isActive = true;
    private long startDate = Instant.now().toEpochMilli();
    private long endDate = Long.MAX_VALUE;

    public Topic() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMustAcknowledge() {
        return mustAcknowledge;
    }

    public void setMustAcknowledge(boolean mustAcknowledge) {
        this.mustAcknowledge = mustAcknowledge;
    }

    public boolean isDeliverOnce() {
        return deliverOnce;
    }

    public void setDeliverOnce(boolean deliverOnce) {
        this.deliverOnce = deliverOnce;
    }

    public boolean isGuaranteeDelivery() {
        return guaranteeDelivery;
    }

    public void setGuaranteeDelivery(boolean guaranteeDelivery) {
        this.guaranteeDelivery = guaranteeDelivery;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    private boolean persisted = false;
}
