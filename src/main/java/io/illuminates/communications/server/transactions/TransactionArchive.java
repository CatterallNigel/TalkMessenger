package io.illuminates.communications.server.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionArchive {

    private static Logger logger = LoggerFactory.getLogger(TransactionArchive.class);

    private static List<Transaction> transactions = new ArrayList<>();

    public void addToArchive(Transaction transaction){
        TransactionArchive.transactions.add(transaction);
    }

    public void addAllToAchive(List<Transaction> transactionList){
        TransactionArchive.transactions.addAll(transactionList);
    }

    public void archiveToColdStorage(){
        //TODO We should store to DB or like - presently delete everything a week old.
        long now = Instant.now().toEpochMilli();
        long week = 7 * 24 * 60 * 60 * 1000;
        long store = now - week;
        List<Transaction> transactionList = TransactionArchive.transactions.stream()
                .filter(t -> t.getTransactionCreated() < store).collect(Collectors.toList());
        logger.info("Deleting " + transactionList.size() + " records from Archive");
        TransactionArchive.transactions.removeAll(transactionList);
    }
}
