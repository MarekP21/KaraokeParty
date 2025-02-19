package org.example.projectjava;

import org.example.projectjava.registration.RegistrationController;
import org.example.projectjava.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BindingResult;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@WebMvcTest(RegistrationController.class)  // Testujemy tylko kontroler rejestracji
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private RegistrationController registrationController;

    @Test // Sprawdzenie czy formularz rejestracji jest odpowiednio wyświetlany
    void testShowRegistrationForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())  // Sprawdzenie statusu HTTP
                .andExpect(MockMvcResultMatchers.view().name("registration"));  // Sprawdzenie widoku
    }
    @Test // Sprawdzenie czy następuje rejestracja przy poprawnych danych
    void testRegisterUser() throws Exception {
        User user = new User();
        user.setLogin("MarekTest");
        user.setPassword("Rootpassword15!");
        user.setPhoneNumber("883 263 320");
        user.setEmail("valid-email@gmail.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .flashAttr("user", user))  // Przesyłanie danych użytkownika
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());  // Sprawdzenie konkretnego błędu walidacji
    }
}


