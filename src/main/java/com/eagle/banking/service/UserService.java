package com.eagle.banking.service;

import com.eagle.banking.model.User;

import java.util.Optional;

public interface UserService {
    User create(User user);

    User getById(String id);

    Optional<User> findByUsername(String username);

    User update(String id, User update);

    void delete(String id);

    default User maskPassword(User u) {
        User copy = new User();
        copy.setId(u.getId());
        copy.setUsername(u.getUsername());
        copy.setFullName(u.getFullName());
        copy.setPassword(null);
        return copy;
    }
}
