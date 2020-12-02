package io.illuminates.communications.common.message.base;

public class DefaultBody {

    String type = "text/plain";
    Object msgBody;

    public DefaultBody(){}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(Object body) {
        this.msgBody = body;
    }
}
