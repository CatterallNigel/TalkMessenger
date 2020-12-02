package io.illuminates.communications.server.models;

import io.illuminates.communications.common.message.base.DefaultMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReQueue {

    private static Logger logger = LoggerFactory.getLogger(ReQueue.class);

    private static List<DefaultMessage> normal;
    private static ObservableList<DefaultMessage> priority;
    private static ReQueue queue;

    private ReQueue(){
        if(queue == null) {
            logger.info("Creating Queue-Out Service");
            ReQueue.normal = new ArrayList<>();
            ReQueue.priority = FXCollections.observableArrayList();
            ReQueue.queue = this;
        }
    }

    public static ReQueue instanceOf(){
        return queue;
    }

    public void addMsgToNormalQueue(DefaultMessage message){
        ReQueue.normal.add(message);
    }
    public void addMsgToPriorityQueue(DefaultMessage message) { ReQueue.priority.add(message); }


    public int noOfMessages(){
        return ReQueue.normal.size();
    }

    public ObservableList<DefaultMessage> getQueue() {
        return  priority;
    }

    public DefaultMessage popFirst(){
        DefaultMessage dm = ReQueue.normal.get(0);
        ReQueue.normal.remove(dm);
        return dm;
    }
}
