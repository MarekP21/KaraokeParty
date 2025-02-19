package org.example.projectjava.registration;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.xml.bind.Unmarshaller;
import org.example.projectjava.user.User;
import org.example.projectjava.user.UserList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import static org.example.projectjava.user.PasswordHasher.generateSalt;
import static org.example.projectjava.user.PasswordHasher.hashPassword;

@Controller
@RequestMapping
public class RegistrationController {

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        if (isUserLoggedIn(session)) {
            return "redirect:/welcome";  // Przekierowanie dla zalogowanego użytkownika
        }
        model.addAttribute("user", new User());
        return "registration";
    }

    @GetMapping("/welcome")
    public String showWelcomePage(Model model, HttpSession session) {
        // Załóżmy, że użytkownik został poprawnie zalogowany
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("message", "Welcome back, " + loggedInUser.getLogin() + "!");
        } else {
            model.addAttribute("message", "You are not logged in.");
        }
        return "welcome";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, BindingResult result, Model model, HttpSession session) {
        if (isUserLoggedIn(session)) {
            return "redirect:/welcome";  // Przekierowanie dla zalogowanego użytkownika
        }
        if (result.hasErrors()) {
            handleFieldErrors(result, model, "login");
            handleFieldErrors(result, model, "password");
            handleFieldErrors(result, model, "phoneNumber");
            handleFieldErrors(result, model, "email");
            return "registration";
        }

        try {
            // Generowanie soli
            String salt = generateSalt();

            // Hashowanie hasła przed zapisaniem
            String hashedPassword = hashPassword(user.getPassword(), salt);
            user.setPassword(hashedPassword);
            user.setSalt(salt);

            File file = new File("users.xml");
            UserList userList = loadUserList(file);

            // Dodanie nowego użytkownika
            userList.addUser(user);

            // Zapisanie zaktualizowanej listy użytkowników do pliku
            saveUserList(userList, file);

        } catch (JAXBException | NoSuchAlgorithmException e) {
            model.addAttribute("message", "Error saving user data in XML format. Please try again.");
            return "registration";
        }
        session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
        session.setAttribute("loggedInUser", user);
        session.removeAttribute("lockoutEndTime");
        return "redirect:/welcome";
    }

    private void handleFieldErrors(BindingResult result, Model model, String fieldName) {
        List<FieldError> errors = result.getFieldErrors(fieldName).stream()
            .sorted((e1, e2) -> {
                if (e1.getDefaultMessage().contains("is required")) return -1;
                if (e2.getDefaultMessage().contains("is required")) return 1;
                return e1.getDefaultMessage().contains("must be") ? -1 : 1;
            })
            .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            model.addAttribute(fieldName + "Error", errors.get(0));
        }
    }

    private UserList loadUserList(File file) throws JAXBException {
        if (file.exists()) {
            JAXBContext context = JAXBContext.newInstance(UserList.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (UserList) unmarshaller.unmarshal(file);
        }
        return new UserList();
    }

    private void saveUserList(UserList userList, File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(UserList.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(userList, file);
    }

    private boolean isUserLoggedIn(HttpSession session) {
        return session.getAttribute("loggedIn") != null;
    }
}
