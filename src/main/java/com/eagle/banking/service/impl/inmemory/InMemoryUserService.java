package com.eagle.banking.service.impl.inmemory;

import com.eagle.banking.exception.ConflictException;
import com.eagle.banking.exception.ResourceNotFoundException;
import com.eagle.banking.model.User;
import com.eagle.banking.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("local")
public class InMemoryUserService implements UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public User create(User user) {
        // ensure username uniqueness
        boolean exists = users.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(user.getUsername()));
        if (exists) throw new ConflictException("username already exists");
        users.put(user.getId(), user);
        return maskPassword(user);
    }

    @Override
    public User getById(String id) {
        User u = users.get(id);
        if (u == null) throw new ResourceNotFoundException("User not found: " + id);
        return maskPassword(u);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        users.values().forEach(u -> list.add(maskPassword(u)));
        return list;
    }

    @Override
    public User update(String id, User update) {
        User existing = users.get(id);
        if (existing == null) throw new ResourceNotFoundException("User not found: " + id);
        if (update.getFullName() != null) existing.setFullName(update.getFullName());
        if (update.getPassword() != null) existing.setPassword(update.getPassword());
        return maskPassword(existing);
    }

    @Override
    public void delete(String id) {
        User removed = users.remove(id);
        if (removed == null) throw new ResourceNotFoundException("User not found: " + id);
    }

}
