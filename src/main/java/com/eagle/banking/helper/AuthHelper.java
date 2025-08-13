package com.eagle.banking.helper;

import com.eagle.banking.exception.InvalidRequestException;
import org.springframework.security.core.Authentication;

public class AuthHelper {
    public static void requireAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new InvalidRequestException("authentication required");
    }
}
