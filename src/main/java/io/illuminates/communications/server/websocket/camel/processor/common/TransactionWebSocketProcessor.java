package io.illuminates.communications.server.websocket.camel.processor.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.illuminates.communications.common.message.base.DefaultHeader;
import io.illuminates.communications.common.message.base.DefaultMessage;
import io.illuminates.communications.common.message.types.InboundMessage;
import io.illuminates.communications.server.models.ReQueue;
import io.illuminates.communications.server.transactions.Transaction;
import io.illuminates.communications.server.transactions.TransactionLog;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class TransactionWebSocketProcessor implements Processor {

    private static Logger logger = LoggerFactory.getLogger(TransactionWebSocketProcessor.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TransactionLog transactionLog;
    private static final Logger LOG = LoggerFactory.getLogger(TimerWebSocketProcessor.class);

    //Go thorough the transaction log - deleted old heartbeats, archive completed and resend outstanding
    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("No of transaction messaages : " + transactionLog.noOfTransactions());
        transactionLog.removeAllAcknowledgedStatusMsgs();
        LOG.info("No of transaction AFTER Status Complete messaage removal : " + transactionLog.noOfTransactions());
        transactionLog.archiveCompletedTransactionMsgs();
        LOG.info("No of transaction AFTER Archive Completed messaage removal : " + transactionLog.noOfTransactions());
        transactionLog.archiveExpiredTransactionMsgs();
        LOG.info("No of transaction AFTER Archive Expired messaage removal : " + transactionLog.noOfTransactions());
        List<Transaction> transactionList = transactionLog.getInCompleteTransactions();
        reQueueMessages(transactionList);
    }

    private void reQueueMessages(List<Transaction> transactionList) {
        try {
            LOG.info("Incomplete Message Transactions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            transactionList.forEach(transaction -> {
                DefaultMessage msg = transaction.getMessage();
                //TODO Do we need to requeue messages that don't require a response
                //in other words if they don't need a response, do they care that they were delivered ?
                //so on messages which don't require a response, the expiry should be set to a short timespan ??
                if(msg.getHeader().getExpires() > Instant.now().toEpochMilli()) {
                    ReQueue.instanceOf().addMsgToNormalQueue(msg);
                }
                LOG.info("Transaction ID:" + transaction.getMsgUUID() + " Sent: " + new Date(transaction.getTransactionCreated()));
            });
            LOG.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        } catch(Exception ex ){
            logger.error("Unable to re-queue message BAD Format Error: " + ex.getMessage());
        }
    }
}
