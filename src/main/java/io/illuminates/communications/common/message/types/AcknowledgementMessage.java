package io.illuminates.communications.common.message.types;

import io.illuminates.communications.server.utils.ObjectToJSONString;
import io.illuminates.communications.common.message.base.DefaultMessage;
import io.illuminates.communications.common.message.body.Status;

public class AcknowledgementMessage extends DefaultMessage{

    public AcknowledgementMessage(Status body) {
        super();
        getHeader().setMessageType(this.getClass().getName());
        getHeader().setRecipients(new String[]{});
        getBody().setMsgBody(ObjectToJSONString.convertObj(body));
    }
}
