package io.illuminates.communications.server.transactions;

import io.illuminates.communications.common.message.body.Status;
import io.illuminates.communications.common.message.types.AcknowledgementMessage;
import io.illuminates.communications.common.utils.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionLog {

    private static Logger logger = LoggerFactory.getLogger(TransactionLog.class);
    private static final String FLASH_MESSAGE = Consts.BASE_MSG_TYPE + Consts.FLASH_MESSAGE_TYPE;

    @Autowired
    private TransactionArchive transactionArchive;
    private static final Logger LOG = LoggerFactory.getLogger(TransactionLog.class);

    private static List<Transaction> transactions = new ArrayList<>();

    public static List<Transaction> getTransactions() {
        return transactions;
    }

    public static void setTransactions(List<Transaction> transactions) {
        TransactionLog.transactions = transactions;
    }

    public void addTransaction(Transaction transaction){
        synchronized (TransactionLog.transactions) {
            TransactionLog.transactions.add(transaction);
        }
    }

    public int noOfTransactions(){
        return TransactionLog.transactions.size();
    }

    public Transaction getTransactionByMsgId(String msgId){
        Optional<Transaction> transaction = TransactionLog.transactions.stream().filter(t -> t.getMsgUUID().equalsIgnoreCase(msgId)).findFirst();
        if(transaction.isPresent()){
            return transaction.get();
        }
        return null;
    }

    public List<Transaction> getInCompleteTransactions(){
        //TODO this may error on the 'cast'
        return TransactionLog.transactions.stream().filter(t -> t.isAcknowledged() == false).collect(Collectors.toList());
    }

    public Transaction getByStatusMsgId(String statusId){
        Optional<Transaction> transaction = TransactionLog.transactions.stream()
                .filter(t -> t.getMessage().getHeader().getMessageType().equalsIgnoreCase(Status.class.getName()))
                .filter(m -> m.getMessage().getBody().getMsgBody().toString().contains(statusId))
                .findFirst();
        if(transaction.isPresent()){
            return transaction.get();
        }else { // It might not be a Status message  - so check for others
            transaction = TransactionLog.transactions.stream().filter(t -> t.getMsgUUID().equalsIgnoreCase(statusId)).findFirst();
            if(transaction.isPresent()){
                return transaction.get();
            }
        }
        return null;
    }

    public boolean removeAllAcknowledgedStatusMsgs(){
        synchronized (TransactionLog.transactions) {
            try {
                List<Transaction> completed = TransactionLog.transactions.stream()
                        .filter(m -> m.isAcknowledged()).collect(Collectors.toList());
                TransactionLog.transactions.removeAll(completed);
                return true;
            } catch (Exception ex) {
                LOG.error("Unable to remove Completed Message transactions: " + ex.getMessage());
                return false;
            }
        }
    }

    public void archiveCompletedTransactionMsgs(){
        synchronized (TransactionLog.transactions) {
            List<Transaction> transactionList = TransactionLog.transactions.stream()
                    .filter(t -> t.isAcknowledged() || (!t.isAcknowledged() && !t.getMessage().getHeader().isResponseRequired()))
                    .collect(Collectors.toList());
            transactionArchive.archiveToColdStorage();
            transactionArchive.addAllToAchive(transactionList);
            TransactionLog.transactions.removeAll(transactionList);
        }
    }

    public void archiveExpiredTransactionMsgs(){
        synchronized (TransactionLog.transactions) {
            List<Transaction> transactionList = TransactionLog.transactions.stream()
                    .filter(t -> t.getMessage().getHeader().getExpires() < Instant.now().toEpochMilli())
                    .collect(Collectors.toList());
            transactionArchive.archiveToColdStorage();
            transactionArchive.addAllToAchive(transactionList);
            TransactionLog.transactions.removeAll(transactionList);
        }
    }
}
