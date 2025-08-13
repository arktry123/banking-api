package com.eagle.banking;

import com.eagle.banking.dto.AuthRequest;
import com.eagle.banking.dto.AuthResponse;
import com.eagle.banking.dto.TransactionDto;
import com.eagle.banking.model.Account;
import com.eagle.banking.model.Transaction;
import com.eagle.banking.model.TransactionType;
import com.eagle.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TransactionIntegrationTest {

    @LocalServerPort int port;
    @Autowired private TestRestTemplate restTemplate;

    private String baseUrl() { return "http://localhost:" + port + "/v1"; }

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = this.restTemplate.getRestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private String authFor(String username, String password) {
        AuthRequest a = new AuthRequest(); a.setUsername(username); a.setPassword(password);
        return restTemplate.postForEntity(baseUrl() + "/auth/login", a, AuthResponse.class).getBody().getToken();
    }

    @Test
    @DirtiesContext
    void create_transaction() {
        String token = setupUserAndAuthToken();
        Account account = createAccount(token);

        Transaction t = new Transaction();
        t.setAmount(new BigDecimal("200"));
        t.setType(TransactionType.DEPOSIT);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transaction> entity = new HttpEntity<>(t, headers);

        ResponseEntity<TransactionDto> response = restTemplate.exchange(baseUrl() + "/accounts/" + account.getId() + "/transactions", HttpMethod.POST, entity, TransactionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    @DirtiesContext
    void get_transaction_by_id() {
        String token = setupUserAndAuthToken();
        Account account = createAccount(token);
        TransactionDto createdTransaction = createTransaction(token, account.getId(), new BigDecimal("300"), TransactionType.DEPOSIT);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<TransactionDto> response = restTemplate.exchange(baseUrl() + "/accounts/" + account.getId() + "/transactions/" + createdTransaction.getId(), HttpMethod.GET, new HttpEntity<>(headers), TransactionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(createdTransaction.getId());
    }

    @Test
    @DirtiesContext
    void list_transactions() {
        String token = setupUserAndAuthToken();
        Account account = createAccount(token);
        createTransaction(token, account.getId(), new BigDecimal("100"), TransactionType.DEPOSIT);
        createTransaction(token, account.getId(), new BigDecimal("50"), TransactionType.WITHDRAW);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<TransactionDto[]> response = restTemplate.exchange(baseUrl() + "/accounts/" + account.getId() + "/transactions", HttpMethod.GET, new HttpEntity<>(headers), TransactionDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DirtiesContext
    void create_transaction_with_invalid_data() {
        String token = setupUserAndAuthToken();
        Account account = createAccount(token);

        Transaction t = new Transaction(); // Missing required fields

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transaction> entity = new HttpEntity<>(t, headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/accounts/" + account.getId() + "/transactions", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DirtiesContext
    void unauthorized_access() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/accounts/1/transactions", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String setupUserAndAuthToken() {
        User u = new User();
        u.setUsername("testuser");
        u.setFullName("Test User");
        u.setPassword("password");
        restTemplate.postForEntity(baseUrl() + "/users", u, User.class);
        return authFor("testuser", "password");
    }

    private Account createAccount(String token) {
        Account a = new Account();
        a.setAccountType("savings");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Account> entity = new HttpEntity<>(a, headers);

        return restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.POST, entity, Account.class).getBody();
    }

    private TransactionDto createTransaction(String token, String accountId, BigDecimal amount, TransactionType type) {
        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setType(type);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transaction> entity = new HttpEntity<>(t, headers);

        return restTemplate.exchange(baseUrl() + "/accounts/" + accountId + "/transactions", HttpMethod.POST, entity, TransactionDto.class).getBody();
    }
}