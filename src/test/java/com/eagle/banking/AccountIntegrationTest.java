package com.eagle.banking;

import com.eagle.banking.dto.AccountDto;
import com.eagle.banking.dto.AuthRequest;
import com.eagle.banking.dto.AuthResponse;
import com.eagle.banking.model.Account;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AccountIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/v1";
    }

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = this.restTemplate.getRestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    @DirtiesContext
    void create_and_list_accounts() {
        User u = new User();
        u.setUsername("accu");
        u.setFullName("Acc U");
        u.setPassword("p");
        User cu = restTemplate.postForEntity(baseUrl() + "/users", u, User.class).getBody();
        String token = authFor("accu", "p");

        Account a = new Account();
        a.setAccountType("savings");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Account> entity = new HttpEntity<>(a, headers);

        ResponseEntity<Account> createResp = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.POST, entity, Account.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Account created = createResp.getBody();
        assertThat(created.getId()).isNotNull();

        ResponseEntity<AccountDto[]> listResp = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.GET, new HttpEntity<>(headers), AccountDto[].class);
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotEmpty();
    }

    @Test
    @DirtiesContext
    void get_account_by_id() {
        String token = setupUserAndAuthToken();

        Account a = new Account();
        a.setAccountType("savings");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Account> entity = new HttpEntity<>(a, headers);

        AccountDto created = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.POST, entity, AccountDto.class).getBody();

        ResponseEntity<AccountDto> getResp = restTemplate.exchange(baseUrl() + "/accounts/" + created.getId(), HttpMethod.GET, new HttpEntity<>(headers), AccountDto.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getId()).isEqualTo(created.getId());
    }

    @Test
    @DirtiesContext
    void update_account() {
        String token = setupUserAndAuthToken();

        Account a = new Account();
        a.setAccountType("savings");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Account> entity = new HttpEntity<>(a, headers);

        AccountDto created = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.POST, entity, AccountDto.class).getBody();

        created.setAccountType("current");
        HttpEntity<AccountDto> updateEntity = new HttpEntity<>(created, headers);

        ResponseEntity<AccountDto> updateResp = restTemplate.exchange(baseUrl() + "/accounts/" + created.getId(), HttpMethod.PATCH, updateEntity, AccountDto.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody().getAccountType()).isEqualTo("current");
    }

    @Test
    @DirtiesContext
    void delete_account() {
        String token = setupUserAndAuthToken();

        Account a = new Account();
        a.setAccountType("savings");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Account> entity = new HttpEntity<>(a, headers);

        AccountDto created = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.POST, entity, AccountDto.class).getBody();

        ResponseEntity<Void> deleteResp = restTemplate.exchange(baseUrl() + "/accounts/" + created.getId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void create_account_with_invalid_data() {
        String token = setupUserAndAuthToken();

        Account a = new Account(); // Missing required fields

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Account> entity = new HttpEntity<>(a, headers);

        ResponseEntity<String> createResp = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.POST, entity, String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DirtiesContext
    void unauthorized_access() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = restTemplate.exchange(baseUrl() + "/accounts", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String setupUserAndAuthToken() {
        User u = new User();
        u.setUsername("testuser");
        u.setFullName("Test User");
        u.setPassword("password");
        restTemplate.postForEntity(baseUrl() + "/users", u, User.class);
        return authFor("testuser", "password");
    }

    private String authFor(String username, String password) {
        AuthRequest a = new AuthRequest();
        a.setUsername(username);
        a.setPassword(password);
        return restTemplate.postForEntity(baseUrl() + "/auth/login", a, AuthResponse.class).getBody().getToken();
    }
}