package org.example.projectjava.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.projectjava.models.Song;
import org.example.projectjava.repositories.SongRepository;
import org.example.projectjava.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/song")
@Controller
public class SongController {

    @Autowired
    private SongRepository songRepository;

    // Zwraca wszystkie piosenki
    @GetMapping("/")
    public String getAll(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        boolean isModerator = user != null && "moderator".equalsIgnoreCase(user.getRole());
        model.addAttribute("isModerator", isModerator);
        model.addAttribute("songs", songRepository.getAll());
        return "song/list";  // Nazwa widoku: song/list.html
    }

    // Zwraca piosenkę o danym id
    @GetMapping("/{id}")
    public String getSongDetails(@PathVariable("id") int id, Model model) {
        Song song = songRepository.getById(id);
        if (song == null) {
            // Obsługuje przypadek, gdy piosenka nie istnieje
            return "error";  // Widok błędu
        }
        model.addAttribute("song", song);
        // Połącz gatunki w jeden ciąg znaków
        String genres = String.join(", ", song.getGenre());
        model.addAttribute("genres", genres);
        return "song/details"; // Wskazuje na widok song/details.html
    }

    // Dodaje piosenki do listy
    @PostMapping("")
    public int add(@RequestBody List<Song> songs, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return songRepository.save(songs, user.getLogin());
    }

    // Aktualizuje wszystkie dane piosenki o danym id
    @PutMapping("/{id}")
    public int update(@PathVariable("id") int id, @RequestBody Song updatedSong) {
        Song song = songRepository.getById(id);
        if (song != null) {
            song.setUserLogin(updatedSong.getUserLogin());
            song.setProduced_by(updatedSong.getProduced_by());
            song.setName(updatedSong.getName());
            song.setGenre(updatedSong.getGenre());
            song.setOrigin(updatedSong.getOrigin());
            song.setLyrics(updatedSong.getLyrics());
            song.setLength(updatedSong.getLength());
            songRepository.update(song);
            return 1;
        } else {
            return -1;
        }
    }

    // Aktualizuje wybrane pola piosenki o danym id
    @PatchMapping("/{id}")
    public int partiallyUpdate(@PathVariable("id") int id, @RequestBody Song updatedSong) {
        Song song = songRepository.getById(id);
        if (song != null) {
            if (updatedSong.getUserLogin() != null) song.setUserLogin(updatedSong.getUserLogin());
            if (updatedSong.getProduced_by() != null) song.setProduced_by(updatedSong.getProduced_by());
            if (updatedSong.getName() != null) song.setName(updatedSong.getName());
            if (updatedSong.getGenre() != null) song.setGenre(updatedSong.getGenre());
            if (updatedSong.getOrigin() != null) song.setOrigin(updatedSong.getOrigin());
            if (updatedSong.getLyrics() != null) song.setLyrics(updatedSong.getLyrics());
            if (updatedSong.getLength() > 0) song.setLength(updatedSong.getLength());
            songRepository.update(song);
            return 1;
        } else {
            return -1;
        }
    }
    // Usuwa piosenkę o danym id
    @DeleteMapping("/{id}")
    public int delete(@PathVariable("id") int id) {
        return songRepository.delete(id);
    }

    @GetMapping("/add")
    public String showAddSongForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"moderator".equalsIgnoreCase(user.getRole())) {
            return "redirect:/song/"; // Brak uprawnień
        }
        // Lista dostępnych gatunków
        List<String> availableGenres = Arrays.asList("Pop", "Rock", "Rap", "Jazz", "Electronic", "Country", "Blues", "Reggae", "Grunge", "Metal", "Folk", "Punk", "Disco", "Techno", "Other");
        model.addAttribute("availableGenres", availableGenres);
        model.addAttribute("song", new Song());
        return "song/add";
    }

    @PostMapping("/add")
    public String addSong(@ModelAttribute @Valid Song song, BindingResult result, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"moderator".equalsIgnoreCase(user.getRole())) {
            return "redirect:/song/"; // Brak uprawnień
        }
        if (result.hasErrors()) {
            handleFieldErrors(result, model, "name");
            handleFieldErrors(result, model, "produced_by");
            handleFieldErrors(result, model, "genre");
            List<String> availableGenres = Arrays.asList("Pop", "Rock", "Rap", "Jazz", "Electronic", "Country", "Blues", "Reggae", "Grunge", "Metal", "Folk", "Punk", "Disco", "Techno", "Other");
            model.addAttribute("availableGenres", availableGenres);
            handleFieldErrors(result, model, "origin");
            handleFieldErrors(result, model, "lyrics");
            handleFieldErrors(result, model, "length");
            return "song/add";
        }
        songRepository.save(List.of(song), user.getLogin());
        return "redirect:/song/"; // Przekierowanie po zapisaniu
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"moderator".equalsIgnoreCase(user.getRole())) {
            return "redirect:/song"; // Przekierowanie, jeśli użytkownik nie ma uprawnień
        }
        Song song = songRepository.getById(id);
        if (song != null) {
            model.addAttribute("song", song);
            // Lista dostępnych gatunków
            List<String> availableGenres = Arrays.asList("Pop", "Rock", "Rap", "Jazz", "Electronic", "Country", "Blues", "Reggae", "Grunge", "Metal", "Folk", "Punk", "Disco", "Techno", "Other");
            model.addAttribute("availableGenres", availableGenres);
            return "song/edit"; // Widok: song/edit.html
        } else {
            return "redirect:/song/"; // Jeśli piosenka nie istnieje, przekierowanie na listę
        }
    }

    // Zapisuje edytowaną piosenkę
    @PostMapping("/edit/{id}")
    public String updateSong(@PathVariable("id") int id, @ModelAttribute @Valid Song song, BindingResult result, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"moderator".equalsIgnoreCase(user.getRole())) {
            return "redirect:/song/"; // Przekierowanie, jeśli użytkownik nie ma uprawnień
        }
        if (result.hasErrors()) {
            handleFieldErrors(result, model, "name");
            handleFieldErrors(result, model, "produced_by");
            handleFieldErrors(result, model, "genre");
            List<String> availableGenres = Arrays.asList("Pop", "Rock", "Rap", "Jazz", "Electronic", "Country", "Blues", "Reggae", "Grunge", "Metal", "Folk", "Punk", "Disco", "Techno", "Other");
            model.addAttribute("availableGenres", availableGenres);
            handleFieldErrors(result, model, "origin");
            handleFieldErrors(result, model, "lyrics");
            handleFieldErrors(result, model, "length");
            return "song/edit";
        }
        Song existingSong = songRepository.getById(id);
        if (existingSong != null) {
            existingSong.setName(song.getName());
            existingSong.setProduced_by(song.getProduced_by());
            existingSong.setGenre(song.getGenre());
            existingSong.setOrigin(song.getOrigin());
            existingSong.setLyrics(song.getLyrics());
            existingSong.setLength(song.getLength());
            songRepository.update(existingSong);
        }
        return "redirect:/song/"; // Po zapisaniu przekierowanie na listę piosenek
    }

    // Usuwa piosenkę
    @GetMapping("/delete/{id}")
    public String deleteSong(@PathVariable("id") int id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"moderator".equalsIgnoreCase(user.getRole())) {
            return "redirect:/song/"; // Przekierowanie, jeśli użytkownik nie ma uprawnień
        }

        songRepository.delete(id);
        return "redirect:/song/"; // Po usunięciu przekierowanie na listę piosenek
    }

    private void handleFieldErrors(BindingResult result, Model model, String fieldName) {
        List<FieldError> errors = result.getFieldErrors(fieldName).stream()
                .sorted((e1, e2) -> Boolean.compare(
                        !e1.getDefaultMessage().contains("be empty"),
                        !e2.getDefaultMessage().contains("be empty")))
                .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            model.addAttribute(fieldName + "Error", errors.get(0));
        }
    }

}
