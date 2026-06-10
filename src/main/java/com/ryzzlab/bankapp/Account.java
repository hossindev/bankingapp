package com.ryzzlab.bankapp;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
    private Long balance;
    private String iban;
}
