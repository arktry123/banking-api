package com.eagle.banking.helper;

import com.eagle.banking.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthHelperTest {

    @Test
    void requireAuth_nullAuthentication_throwsException() {
        assertThrows(InvalidRequestException.class, () -> AuthHelper.requireAuth(null));
    }

    @Test
    void requireAuth_nullNameInAuthentication_throwsException() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn(null);

        assertThrows(InvalidRequestException.class, () -> AuthHelper.requireAuth(auth));
    }

    @Test
    void requireAuth_validAuthentication_doesNotThrowException() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("validUser");

        assertDoesNotThrow(() -> AuthHelper.requireAuth(auth));
    }
}