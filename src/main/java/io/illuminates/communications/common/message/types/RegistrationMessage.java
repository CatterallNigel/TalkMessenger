package io.illuminates.communications.common.message.types;

import io.illuminates.communications.common.message.base.DefaultMessage;
import io.illuminates.communications.common.message.body.Key;
import io.illuminates.communications.common.utils.ObjectToJSONString;

public class RegistrationMessage extends DefaultMessage {

    public RegistrationMessage(String[] recipients, String body){
        super();
        getHeader().setMessageType(this.getClass().getName());
        getHeader().setRouteFrom("direct://registration/out");
        getHeader().setRouteTo("direct://services/in");
        getHeader().setRecipients(recipients);
        getBody().setMsgBody(ObjectToJSONString.convertObj(new Key(body)));
    }
}
