package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "roles")
public class Role {
    @Column(nullable = false)
    @Id
    @SequenceGenerator(name = "RoleSeq",allocationSize = 1,initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "RoleSeq")
    private Long roleId;
    @Column(nullable = false)
    private String roleName;
}
