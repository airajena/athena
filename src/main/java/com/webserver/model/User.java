// src/main/java/com/webserver/model/User.java
package com.webserver.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private Long id;
    private String name;
    private String email;
    private int age;
    private long createdAt;

    // Default constructor for Jackson
    public User() {
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor with Jackson annotations for JSON parsing
    @JsonCreator
    public User(@JsonProperty("id") Long id,
                @JsonProperty("name") String name,
                @JsonProperty("email") String email,
                @JsonProperty("age") int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Validation method
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                email != null && email.contains("@") &&
                age > 0 && age < 150;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', age=%d}",
                id, name, email, age);
    }
}
