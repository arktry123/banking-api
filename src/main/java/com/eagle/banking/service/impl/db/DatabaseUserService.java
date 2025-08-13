package com.eagle.banking.service.impl.db;

import com.eagle.banking.exception.ResourceNotFoundException;
import com.eagle.banking.model.User;
import com.eagle.banking.repo.UserRepository;
import com.eagle.banking.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Profile("!local")
public class DatabaseUserService implements UserService {

    private final UserRepository userRepository;

    public DatabaseUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        return maskPassword(userRepository.save(user));
    }

    @Override
    public User getById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return maskPassword(user.get());
        } else {
            throw new ResourceNotFoundException("User not found: " + id);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User update(String id, User update) {
        Optional<User> oExistingUser = userRepository.findById(id);
        if (oExistingUser.isEmpty()) throw new ResourceNotFoundException("User not found: " + id);
        User existingUser = oExistingUser.get();
        if (update.getFullName() != null) existingUser.setFullName(update.getFullName());
        if (update.getPassword() != null) existingUser.setPassword(update.getPassword());
        return maskPassword(existingUser);
    }

    @Override
    public void delete(String id) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("User not found: " + id);
        }
    }
}
