package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transactions")
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "sender_id")
    private UserRecord sender;

    @ManyToOne(optional = false) @JoinColumn(name = "recipient_id")
    private UserRecord recipient;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private float incentive;   // <— NEW

    protected TransactionRecord() {} // JPA needs no-args ctor

    // Keep the old 3-arg ctor for any existing calls
    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount) {
        this(sender, recipient, amount, 0f);
    }

    // NEW 4-arg ctor used by your DatabaseConduit
    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    // getters/setters
    public Long getId() { return id; }
    public UserRecord getSender() { return sender; }
    public UserRecord getRecipient() { return recipient; }
    public float getAmount() { return amount; }
    public float getIncentive() { return incentive; }
    public void setId(Long id) { this.id = id; }
    public void setSender(UserRecord sender) { this.sender = sender; }
    public void setRecipient(UserRecord recipient) { this.recipient = recipient; }
    public void setAmount(float amount) { this.amount = amount; }
    public void setIncentive(float incentive) { this.incentive = incentive; }
}
