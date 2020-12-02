package io.illuminates.communications.server.models.publish;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class Topics {

    private static List<Topic> topicList = new ArrayList<>();

    public Topics() { }

    public static List<Topic> getTopicList() {
        synchronized (topicList) {

            return topicList;
        }
    }

    public static void setTopicList(List<Topic> topicList) {
        synchronized (topicList) {
            Topics.topicList = topicList;
        }
    }

    public static List<Topic> getActiveTopics() { //TODO check Active Status etc return a message.body.type
        return Topics.topicList;
    }

    public static Topic getTopicById(String topicId) {
        Optional<Topic> _topic = Topics.topicList.stream().filter(t -> t.getTopicId().equalsIgnoreCase(topicId)).findFirst();
        if(_topic.isPresent()){
            return _topic.get();
        }
        return null;
    }

    public static Topic getTopicByType(String topicType){
        Optional<Topic> _topic = Topics.topicList.stream().filter(t -> t.getType().equalsIgnoreCase(topicType)).findFirst();
        if(_topic.isPresent()){
            return _topic.get();
        }
        return null;
    }

    public static boolean addTopic(Topic topic){
        synchronized (topicList) {
            Optional<Topic> _topic = Topics.topicList.stream().filter(t -> t.getTopicId().equalsIgnoreCase(topic.getTopicId())).findFirst();
            if (_topic.isPresent()) {
                return false;
            } else {
                Topics.topicList.add(topic);
            }
            return true;
        }
    }

    public static boolean removeTopic(Topic topic){
        synchronized (topicList) {
            Optional<Topic> _topic = Topics.topicList.stream().filter(t -> t.getTopicId().equalsIgnoreCase(topic.getTopicId())).findFirst();
            if (_topic.isPresent()) {
                Topics.topicList.remove(topic);
                return true;
            }
            return false;
        }
    }
}


