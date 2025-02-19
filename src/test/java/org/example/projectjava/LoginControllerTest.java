package org.example.projectjava;

import org.example.projectjava.login.LoginController;
import org.example.projectjava.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {

    @Autowired
    private LoginController loginController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test // Test, który sprawdza, czy następuje prawidłowe przekierowanie po zalogowaniu
    public void testLogin_validCredentials() throws Exception {
        // Przygotowanie użytkownika
        User user = new User();
        user.setLogin("MarekAdmin");
        user.setPassword("MarekAdmin123!");

        // Test logowania
        mockMvc.perform(post("/login")
                        .param("login", user.getLogin())
                        .param("password", user.getPassword()))
                .andExpect(status().is3xxRedirection()) // Oczekiwane przekierowanie
                .andExpect(redirectedUrl("/welcome"));  // Oczekiwane przekierowanie na stronę powitania
    }
}
