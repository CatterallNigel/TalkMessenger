package io.illuminates.communications.server.websocket.camel.processor.common;

import io.illuminates.communications.common.message.base.DefaultMessage;
import io.illuminates.communications.server.models.ReQueue;
import io.illuminates.communications.server.models.Registry;
import javafx.collections.ListChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
public class ExpressWebSocketPreProcessor implements ListChangeListener<DefaultMessage> {

    @Autowired
    private WebSocketMessageProcessor webSocketMessageProcessor;

    public ExpressWebSocketPreProcessor(){
        ReQueue.instanceOf().getQueue().clear();
        ReQueue.instanceOf().getQueue().addListener(this);
    }

    private static Logger logger = LoggerFactory.getLogger(ExpressWebSocketPreProcessor.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onChanged(Change<? extends DefaultMessage> c) {
        String[] receipents = Registry.connections.keySet().toArray(new String[0]);
        if(receipents.length == 0){
            c.reset();
            return;
        }
        c.getList().forEach((msg -> {
            DefaultMessage dm = msg;
            List<String> addressedTo = Arrays.asList(dm.getHeader().getRecipients());
            addressedTo.forEach(addr -> {
                //Check the recipient is connected and the message hasn't expired
                if(Arrays.stream(receipents).anyMatch(addr::equals) &&
                        dm.getHeader().getExpires() > Instant.now().toEpochMilli()){
                    //TODO Resend - to this receipant.
                    String msgStr = io.illuminates.communications.server.utils.ObjectToJSONString.convertObj(dm);
                    webSocketMessageProcessor.sendBody(dm.getHeader().getRouteTo(), msgStr, Registry.connections.get(addr));
                }else if(!Arrays.stream(receipents).anyMatch(addr::equals) &&
                        dm.getHeader().getExpires() > Instant.now().toEpochMilli()){
                    //Presently not connected but message hasn't expired
                    //No point expressing it if there is no client connection
                    //so requeue normally
                    ReQueue.instanceOf().addMsgToNormalQueue(dm);
                }
            });
        }));
        c.getList().clear();
        c.reset();
    }

}
