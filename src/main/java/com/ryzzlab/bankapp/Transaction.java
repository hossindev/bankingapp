package com.ryzzlab.bankapp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    @JsonIgnore
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnore
    private User user;
    private Integer amount;
    private String fromIban;
    private String toIban;
    private LocalDateTime createdAt;
}
