package com.example.leave.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private Long managerId;

    @Column
    private String department;

    public void assignManager(Long managerId) {
        this.managerId = managerId;
    }

    public void updateRoleAndDepartment(Role role, String department) {
        if (role == null) throw new IllegalArgumentException("Role is required");
        this.role = role;
        this.department = department;
    }

    public Employee(String email, String password, String name, Role role, String department) {
        this(email, password, name, role);
        this.department = department;
    }

    public Employee(String email, String password, String name, Role role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }
}
