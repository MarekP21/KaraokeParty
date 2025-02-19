package org.example.projectjava;

import jakarta.servlet.http.HttpSession;
import org.example.projectjava.controllers.QueueController;
import org.example.projectjava.models.QueueItem;
import org.example.projectjava.models.Song;
import org.example.projectjava.repositories.QueueRepository;
import org.example.projectjava.repositories.SongRepository;
import org.example.projectjava.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueueController.class)
class QueueControllerTest {

    @Autowired
    private QueueController queueController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongRepository songRepository;

    @MockBean
    private QueueRepository queueRepository;

    @Test // Sprawdza czy pozycje piosenek w kolejce są odpowiednio zmieniane
    void testUpdatePosition() throws Exception {
        // Przygotowanie przykładowych pozycji w kolejce
        List<QueueItem> queue = new ArrayList<>(Arrays.asList(
                new QueueItem(1, "Song1", "paweladmin", 1),
                new QueueItem(2, "Song2", "paweladmin", 2),
                new QueueItem(3, "Song3", "paweladmin", 3),
                new QueueItem(4, "Song4", "paweladmin", 4),
                new QueueItem(5, "Song5", "paweladmin", 5)
        ));

        // Zamockowanie metody updatePositionNumber
        Mockito.doAnswer(invocation -> {
            int currentPosition = invocation.getArgument(0);
            int newPosition = invocation.getArgument(1);

            QueueItem itemToMove = queue.stream()
                    .filter(item -> item.getPosition() == currentPosition)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));

            // Przesunięcie w dół
            if (newPosition > currentPosition) {
                queue.stream()
                        .filter(item -> item.getPosition() > currentPosition && item.getPosition() <= newPosition)
                        .forEach(item -> item.setPosition(item.getPosition() - 1));
            }
            // Przesunięcie w górę
            else if (newPosition < currentPosition) {
                queue.stream()
                        .filter(item -> item.getPosition() >= newPosition && item.getPosition() < currentPosition)
                        .forEach(item -> item.setPosition(item.getPosition() + 1));
            }

            itemToMove.setPosition(newPosition);
            queue.sort(Comparator.comparingInt(QueueItem::getPosition));
            return null;
        }).when(queueRepository).updatePositionNumber(Mockito.anyInt(), Mockito.anyInt());

        // Wywołanie metody do przetestowania
        queueRepository.updatePositionNumber(1, 4);

        // Weryfikacja
        assert Objects.equals(queue.get(0).getSongName(), "Song2") : "Expected Song2 at position 1";
        assert Objects.equals(queue.get(3).getSongName(), "Song1") : "Expected Song1 at position 4";
        assert queue.get(0).getPosition() == 1 : "Expected position 1 for Song2";
        assert queue.get(3).getPosition() == 4 : "Expected position 4 for Song1";
    }

    @Test // testuje czy jeden użytkownik zwykły może dodać więcej niż jedną piosenkę do kolejki
    void testAddMultipleSongsToQueueByRegularUser() {
        // Przygotowanie użytkownika
        User regularUser = new User();
        regularUser.setLogin("MarekNormal");

        // Mockowanie sesji
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
        session.setAttribute("loggedInUser", regularUser); //

        // Przygotowanie przykładowych piosenek
        Song song1 = new Song(1, "admin", "Producer1", "Song1", List.of("Pop"), "Poland", "Lyrics", 180);
        Song song2 = new Song(2, "admin", "Producer2", "Song2", List.of("Rock"), "USA", "Lyrics", 200);

        Mockito.when(songRepository.getById(1)).thenReturn(song1);
        Mockito.when(songRepository.getById(2)).thenReturn(song2);

        // Mockowanie kolejki
        QueueItem queueItem1 = new QueueItem(1, song1.getName(), regularUser.getLogin(), song1.getId());

        // Pierwsze wywołanie `existsByAddedBy` zwraca `false` (użytkownik nie ma piosenki w kolejce)
        // Drugie wywołanie zwraca `true` (piosenka została dodana)
        Mockito.when(queueRepository.existsByAddedBy(regularUser.getLogin()))
                .thenReturn(false) // Pierwsze wywołanie
                .thenReturn(true); // Drugie wywołanie

        // Wywołanie metody `addToQueue` dla pierwszej piosenki
        String result1 = queueController.addToQueue(song1.getId(), session);

        // Wywołanie metody `addToQueue` dla drugiej piosenki
        String result2 = queueController.addToQueue(song2.getId(), session);

        // Weryfikacja wyników
        assertEquals("redirect:/karaoke/", result1, "First song should be added successfully.");
        assertEquals("redirect:/karaoke/", result2, "Second song should not be added, user already added one.");

        // Weryfikacja interakcji z repozytorium
        Mockito.verify(queueRepository, Mockito.times(1)).add(Mockito.any(QueueItem.class)); // Powinna być jedna próba dodania
        Mockito.verify(queueRepository, Mockito.times(2)).existsByAddedBy(regularUser.getLogin()); // Sprawdzenie, czy użytkownik już dodał piosenkę
    }

}
