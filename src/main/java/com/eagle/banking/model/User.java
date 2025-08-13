package com.eagle.banking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    @NotBlank(message = "username is required")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "fullName is required")
    private String fullName;

    @Column(nullable = false)
    @NotBlank(message = "password is required")
    private String password; // plain text for demo only — DON'T do this in prod
}
