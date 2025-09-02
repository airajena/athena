package com.webserver.storage;

import com.webserver.model.User;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserStorage() {
        initializeSampleData();
    }

    private void initializeSampleData() {
        createUser(new User(null, "John Doe", "john@example.com", 28));
        createUser(new User(null, "Jane Smith", "jane@example.com", 32));
        createUser(new User(null, "Bob Johnson", "bob@example.com", 25));
    }

    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        System.out.println("� Created user: " + user);
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = users.get(id);
        if (existingUser != null) {
            updatedUser.setId(id);
            updatedUser.setCreatedAt(existingUser.getCreatedAt());
            users.put(id, updatedUser);
            System.out.println("📝 Updated user: " + updatedUser);
            return updatedUser;
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        User deletedUser = users.remove(id);
        if (deletedUser != null) {
            System.out.println("🗑️ Deleted user: " + deletedUser);
            return true;
        }
        return false;
    }

    public int getUserCount() {
        return users.size();
    }
}
