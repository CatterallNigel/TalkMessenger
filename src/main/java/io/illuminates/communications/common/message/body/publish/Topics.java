package io.illuminates.communications.common.message.body.publish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Topics {

    private List<Topic> topicList = new ArrayList<>();
    private Topic[] topics;

    public Topics() { }

    public Topic[] getTopics() {
        return topicList.toArray(new Topic[0]);
    }

    public void setTopics(Topic[] topics) {
        this.topicList = Arrays.asList(topics);
    }

    public List<Topic> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
    }
}
