package org.example.projectjava.controllers;

import jakarta.servlet.http.HttpSession;
import org.example.projectjava.models.QueueItem;
import org.example.projectjava.models.Song;
import org.example.projectjava.repositories.QueueRepository;
import org.example.projectjava.repositories.SongRepository;
import org.example.projectjava.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/karaoke")
public class QueueController {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private QueueRepository queueRepository;

    // Wyświetlenie kolejki i obsługa dodawania
    @GetMapping("/")
    public String showQueue(Model model, HttpSession session) {
        // Pobranie obiektu użytkownika z sesji
        model.addAttribute("songs", songRepository.getAll());
        User currentUser = (User) session.getAttribute("loggedInUser");
        boolean isModerator = currentUser != null && "moderator".equalsIgnoreCase(currentUser.getRole());
        model.addAttribute("isModerator", isModerator);
        if (currentUser == null) {
            return "redirect:/login";
        }
        // Dodanie danych do widoku
        model.addAttribute("queue", queueRepository.getAll());
        model.addAttribute("songs", songRepository.getAll()); // Lista dostępnych piosenek

        // Sprawdzenie, czy użytkownik już dodał piosenkę
        boolean alreadyInQueue = !"moderator".equalsIgnoreCase(currentUser.getRole()) && queueRepository.existsByAddedBy(currentUser.getLogin());

        model.addAttribute("canAdd", !alreadyInQueue); // Flaga dla widoku
        return "karaoke/list";
    }

    @PostMapping("/")
    public String addToQueue(@RequestParam("songId") int songId, HttpSession session) {
        // Pobranie obiektu użytkownika z sesji
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login"; // Przekierowanie dla niezalogowanych użytkowników
        }

        // Weryfikacja: użytkownik może dodać tylko jedną piosenkę
        if (!"moderator".equalsIgnoreCase(currentUser.getRole()) && queueRepository.existsByAddedBy(currentUser.getLogin())) {
            return "redirect:/karaoke/";
        }

        // Pobieramy wybraną piosenkę
        Song song = songRepository.getById(songId);
        if (song != null) {
            QueueItem item = new QueueItem(0, song.getName(), currentUser.getLogin(), song.getId());
            queueRepository.add(item); // Dodajemy piosenkę do kolejki
            queueRepository.getAll();
        }

        return "redirect:/karaoke/";
    }

    // Usunięcie pozycji z kolejki
    @PostMapping("/delete/{position}")
    public String deleteFromQueue(@PathVariable("position") int position, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null || !"moderator".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/karaoke/";
        }

        queueRepository.deleteByPosition(position);
        reorderQueue(); // Aktualizujemy kolejkę po usunięciu
        return "redirect:/karaoke/";
    }

    // Aktualizacja pozycji w kolejce
    @PostMapping("/update/position")
    public String updatePosition(@RequestParam("currentPosition") int currentPosition,
                                 @RequestParam("newPosition") int newPosition, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null || !"moderator".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/karaoke/";
        }

        List<QueueItem> queue = queueRepository.getAll();
        if (newPosition < 1 || newPosition > queue.size()) {
            return "redirect:/karaoke/"; // Walidacja pozycji
        }
        queueRepository.updatePositionNumber(currentPosition,  newPosition);
        return "redirect:/karaoke/";
    }

    // Aktualizacja piosenki w danej pozycji
    @PostMapping("/update/song")
    public String updateSong(@RequestParam("position") int position, @RequestParam("songId") int songId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null || !"moderator".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/karaoke/";
        }

        Song newSong = songRepository.getById(songId);
        if (newSong != null) {
            queueRepository.updateSong(position, newSong.getName(), newSong.getId());
        }
        return "redirect:/karaoke/";
    }

    // Reorganizacja kolejki po zmianach
    private void reorderQueue() {
        List<QueueItem> queue = queueRepository.getAll();
        for (int i = 0; i < queue.size(); i++) {
            queueRepository.updatePosition(queue.get(i).getPosition(), i + 1);
        }
    }
}
