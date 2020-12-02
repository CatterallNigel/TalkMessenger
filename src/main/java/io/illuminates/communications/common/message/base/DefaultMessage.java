package io.illuminates.communications.common.message.base;

public abstract class DefaultMessage implements IDefaultMessage {

    DefaultHeader header = new DefaultHeader();
    DefaultBody body = new DefaultBody();

    @Override
    public DefaultHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(DefaultHeader header) {
        this.header = header;
    }

    @Override
    public DefaultBody getBody() {
        return body;
    }

    @Override
    public void setBody(DefaultBody body) {
        this.body = body;
    }

}
