package io.illuminates.communications.server.models.publish.test;

import io.illuminates.communications.server.models.publish.Topic;
import io.illuminates.communications.server.models.publish.Topics;
import io.illuminates.communications.common.utils.Consts;
import org.springframework.stereotype.Service;

@Service
public class BasicSubscriberPingTopic {

    private Topic topic;

    private BasicSubscriberPingTopic(){
        this.topic = new Topic();
        this.topic.setTitle("Subscriber Ping Topic");
        this.topic.setPublisher(Consts.QUALIFIED_NAME_UUID);
        this.topic.setType(Consts.SUBSCRIBER_PING);
        this.topic.setMustAcknowledge(true);
        Topics.addTopic(this.topic);
    }
}
