package com.eagle.banking.service.impl.db;

import com.eagle.banking.exception.ResourceNotFoundException;
import com.eagle.banking.model.User;
import com.eagle.banking.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseUserServiceTest {

    private UserRepository userRepository;
    private DatabaseUserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new DatabaseUserService(userRepository);
    }

    @Test
    void create_ShouldSaveUserAndMaskPassword() {
        User user = new User();
        user.setPassword("secret");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.create(user);

        assertNull(result.getPassword(), "Password should be masked");
        verify(userRepository).save(user);
    }

    @Test
    void getById_ShouldReturnMaskedUser_WhenFound() {
        User user = new User();
        user.setId("u1");
        user.setPassword("secret");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        User result = userService.getById("u1");

        assertEquals("u1", result.getId());
        assertNull(result.getPassword());
        verify(userRepository).findById("u1");
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getById("u1"));
        assertEquals("User not found: u1", ex.getMessage());
    }

    @Test
    void findByUsername_ShouldReturnOptionalUser() {
        User user = new User();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("john");

        assertTrue(result.isPresent());
        verify(userRepository).findByUsername("john");
    }

    @Test
    void update_ShouldUpdateFullNameAndPassword_WhenProvided() {
        User existingUser = new User();
        existingUser.setId("u1");
        existingUser.setFullName("Old Name");
        existingUser.setPassword("oldpass");

        User update = new User();
        update.setFullName("New Name");
        update.setPassword("newpass");

        when(userRepository.findById("u1")).thenReturn(Optional.of(existingUser));

        User result = userService.update("u1", update);

        assertEquals("New Name", existingUser.getFullName());
        assertEquals("newpass", existingUser.getPassword());
        assertNull(result.getPassword(), "Password should be masked");
    }

    @Test
    void update_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.update("u1", new User()));
        assertEquals("User not found: u1", ex.getMessage());
    }

    @Test
    void update_ShouldOnlyUpdateFullName_WhenPasswordIsNull() {
        User existingUser = new User();
        existingUser.setId("u1");
        existingUser.setFullName("Old Name");
        existingUser.setPassword("oldpass");

        User update = new User();
        update.setFullName("New Name");

        when(userRepository.findById("u1")).thenReturn(Optional.of(existingUser));

        User result = userService.update("u1", update);

        assertEquals("New Name", existingUser.getFullName());
        assertEquals("oldpass", existingUser.getPassword());
        assertNull(result.getPassword());
    }

    @Test
    void update_ShouldOnlyUpdatePassword_WhenFullNameIsNull() {
        User existingUser = new User();
        existingUser.setId("u1");
        existingUser.setFullName("Old Name");
        existingUser.setPassword("oldpass");

        User update = new User();
        update.setPassword("newpass");

        when(userRepository.findById("u1")).thenReturn(Optional.of(existingUser));

        User result = userService.update("u1", update);

        assertEquals("Old Name", existingUser.getFullName());
        assertEquals("newpass", existingUser.getPassword());
        assertNull(result.getPassword());
    }

    @Test
    void delete_ShouldDelete_WhenUserExists() {
        User user = new User();
        user.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        userService.delete("u1");

        verify(userRepository).deleteById("u1");
    }

    @Test
    void delete_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.delete("u1"));
        assertEquals("User not found: u1", ex.getMessage());
    }
}
