
package com.eagle.banking;

import com.eagle.banking.dto.AuthRequest;
import com.eagle.banking.dto.AuthResponse;
import com.eagle.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/v1";
    }

    @BeforeEach
    void cleanup() {
        // nothing special - H2 is dropped between runs because of create-drop
    }

    @Test
    void createUser_success() {
        User u = new User();
        u.setUsername("alice");
        u.setFullName("Alice A");
        u.setPassword("pass");

        ResponseEntity<User> resp = restTemplate.postForEntity(baseUrl() + "/users", u, User.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isNotNull();
        assertThat(resp.getBody().getUsername()).isEqualTo("alice");
    }

    @Test
    void createUser_missingField_badRequest() {
        User u = new User();
        u.setUsername("bob");
        // missing fullName and password

        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl() + "/users", u, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void authenticate_and_fetch_self() {
        // create user
        User u = new User();
        u.setUsername("charlie");
        u.setFullName("Charlie C");
        u.setPassword("pwd");
        ResponseEntity<User> createResp = restTemplate.postForEntity(baseUrl() + "/users", u, User.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String userId = createResp.getBody().getId();

        // login
        AuthRequest auth = new AuthRequest();
        auth.setUsername("charlie");
        auth.setPassword("pwd");
        ResponseEntity<AuthResponse> tokenResp = restTemplate.postForEntity(baseUrl() + "/auth/login", auth, AuthResponse.class);
        assertThat(tokenResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = tokenResp.getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<User> meResp = restTemplate.exchange(baseUrl() + "/users/" + userId, HttpMethod.GET, entity, User.class);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResp.getBody().getUsername()).isEqualTo("charlie");
    }

    @Test
    void fetch_other_user_forbidden() {
        // create user1
        User u1 = new User();
        u1.setUsername("d1");
        u1.setFullName("D One");
        u1.setPassword("p");
        User cu1 = restTemplate.postForEntity(baseUrl() + "/users", u1, User.class).getBody();

        // create user2
        User u2 = new User();
        u2.setUsername("d2");
        u2.setFullName("D Two");
        u2.setPassword("p2");
        User cu2 = restTemplate.postForEntity(baseUrl() + "/users", u2, User.class).getBody();

        // login as user1
        AuthRequest auth = new AuthRequest();
        auth.setUsername("d1");
        auth.setPassword("p");
        String token = restTemplate.postForEntity(baseUrl() + "/auth/login", auth, AuthResponse.class).getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(baseUrl() + "/users/" + cu2.getId(), HttpMethod.GET, entity, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
