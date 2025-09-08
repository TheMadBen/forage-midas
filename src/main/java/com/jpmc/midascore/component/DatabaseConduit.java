package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseConduit {

    private final UserRepository userRepository;
    private final TransactionRecordRepository txRepository;

    public DatabaseConduit(UserRepository userRepository,
                           TransactionRecordRepository txRepository) {
        this.userRepository = userRepository;
        this.txRepository = txRepository;
    }

    // --- used by tests to seed users ---
    public UserRecord save(UserRecord user) {
        return userRepository.save(user);
    }

    public UserRecord findUser(long id) {
        return userRepository.findById(id);
    }

    // --- validation separated from orchestration ---
    public boolean isValid(long senderId, long recipientId, float amount) {
        UserRecord sender = userRepository.findById(senderId);
        UserRecord recipient = userRepository.findById(recipientId);
        if (sender == null || recipient == null) return false;
        return sender.getBalance() >= amount;
    }

    // --- single atomic DB update (debit sender, credit recipient with amount+incentive, persist tx record) ---
    @Transactional
    public void saveTransaction(long senderId, long recipientId, float amount, float incentive) {
        UserRecord sender = userRepository.findById(senderId);
        UserRecord recipient = userRepository.findById(recipientId);
        // assume caller already validated existence and balance

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentive);

        userRepository.save(sender);
        userRepository.save(recipient);

        txRepository.save(new TransactionRecord(sender, recipient, amount, incentive));
    }
}
