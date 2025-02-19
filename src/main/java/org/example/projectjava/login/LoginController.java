package org.example.projectjava.login;

import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.example.projectjava.user.User;
import org.example.projectjava.user.UserList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import static org.example.projectjava.user.PasswordHasher.hashPassword;

@Controller
public class LoginController {

    // ImieAdmin123!
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_SECONDS = 300;
    private int loginAttempts = 0;

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        if (isUserLoggedIn(session)) {
            return "redirect:/welcome";  // Przekierowanie dla zalogowanego użytkownika
        }
        model.addAttribute("user", new User());
        model.addAttribute("message", "Please enter your login credentials.");
        model.addAttribute("messageType", "normal");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedIn");
        session.removeAttribute("loggedInUser");
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute User user, BindingResult result, Model model, HttpSession session) {
        if (isUserLoggedIn(session)) {
            return "redirect:/welcome"; // Przekierowanie dla zalogowanego użytkownika
        }
        Instant lockoutEndTime = (Instant) session.getAttribute("lockoutEndTime");
        if (lockoutEndTime != null && Instant.now().isBefore(lockoutEndTime)) {
            model.addAttribute("message", "You are locked out. Please try again later.");
            model.addAttribute("messageType", "error");
            return "login";
        }

        // Walidacja pól
        boolean hasErrors = false;
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            result.rejectValue("login", "error.user", "Login is required.");
            hasErrors = true;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            result.rejectValue("password", "error.user", "Password is required.");
            hasErrors = true;
        }

        if (hasErrors) {
            model.addAttribute("message", "Please correct the errors below.");
            model.addAttribute("messageType", "error");
            return "login";
        }

        try {
            UserList userList = loadUserList(new File("users.xml"));
            User existingUser = userList.getUsers().stream()
                .filter(u -> u.getLogin().equals(user.getLogin()))
                .findFirst()
                .orElse(null);

            if (existingUser != null && verifyPassword(user.getPassword(), existingUser.getPassword(), existingUser.getSalt())) {
                loginAttempts = 0;
                session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
                session.setAttribute("loggedInUser", existingUser); // Zapisanie użytkownika
                return "redirect:/welcome";
            } else {
                loginAttempts++;
                if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                    lockoutEndTime = Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS);
                    session.setAttribute("lockoutEndTime", lockoutEndTime);
                    loginAttempts = 0; // Reset liczby prób
                    model.addAttribute("message", "Too many failed attempts. Try again later.");
                    model.addAttribute("messageType", "error");
                } else {
                    model.addAttribute("message", "Invalid login or password. Attempt "
                        + loginAttempts + " of " + MAX_LOGIN_ATTEMPTS);
                    model.addAttribute("messageType", "error");
                }
            }
        } catch (JAXBException | NoSuchAlgorithmException e) {
            model.addAttribute("message", "Error processing login. Please try again later.");
            model.addAttribute("messageType", "error");
        }

        return "login";
    }

    private UserList loadUserList(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(UserList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (UserList) unmarshaller.unmarshal(file);
    }


    private boolean verifyPassword(String rawPassword, String storedHashedPassword, String storedSalt) throws NoSuchAlgorithmException {
        // Hashowanie hasła wprowadzonego przez użytkownika z użyciem przechowywanej soli i pieprzu
        String hashedInputPassword = hashPassword(rawPassword, storedSalt);

        // Porównanie z przechowywanym hashem
        return hashedInputPassword.equals(storedHashedPassword);
    }

    private boolean isUserLoggedIn(HttpSession session) {
        return session.getAttribute("loggedIn") != null;
    }
}
