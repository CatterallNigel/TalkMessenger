package io.illuminates.communications.common.message.base;

public interface IDefaultMessage {
    DefaultHeader getHeader();

    void setHeader(DefaultHeader header);

    DefaultBody getBody();

    void setBody(DefaultBody body);
}
