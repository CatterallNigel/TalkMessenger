package io.illuminates.communications.server.models.subscribe;

public class Subscriber {

    private String qualifiedName;
    private String qualifiedUUID;
    private String topic;
    private boolean subscribed = true;

    public Subscriber(){}

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedUUID() {
        return qualifiedUUID;
    }

    public void setQualifiedUUID(String qualifiedUUID) {
        this.qualifiedUUID = qualifiedUUID;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
