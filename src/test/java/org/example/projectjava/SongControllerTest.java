package org.example.projectjava;

import jakarta.servlet.http.HttpSession;
import org.example.projectjava.controllers.SongController;
import org.example.projectjava.models.Song;
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

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SongController.class)
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongRepository songRepository;

    @Test // Sprawdzenie czy odpowiednio wyświetla się lista piosenek
    void testGetAllSongs() throws Exception {
        User loggedInUser = new User();
        loggedInUser.setLogin("MarekAdmin");
        loggedInUser.setRole("moderator");
        // Tworzymy sesję mockowaną z użytkownikiem
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
        session.setAttribute("loggedInUser", loggedInUser); // Zapisanie użytkownika w sesji
        // Tworzenie przykładowych piosenek
        List<Song> songs = Arrays.asList(
                new Song(1, "admin", "Producer1", "Song1", List.of("Pop"), "Poland", "Lyrics", 180),
                new Song(2, "admin", "Producer2", "Song2", List.of("Rock"), "USA", "Lyrics", 200)
        );
        // Mockowanie repozytorium piosenek
        Mockito.when(songRepository.getAll()).thenReturn(songs);
        // Wykonanie żądania z zamockowaną sesją
        mockMvc.perform(get("/song/")
                        .session(session)) // Dodajemy sesję z użytkownikiem
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("songs"))
                .andExpect(view().name("song/list"));
    }

    @Test // Testowanie dodania nowej piosenki przez moderatora
    void testAddSong() throws Exception {
        // Tworzymy użytkownika, który jest już zalogowany
        User loggedInUser = new User();
        loggedInUser.setLogin("MarekAdmin");
        loggedInUser.setRole("moderator");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
        session.setAttribute("loggedInUser", loggedInUser); // Zapisanie użytkownika w sesji
        // Przykładowa piosenka do dodania
        Song songToAdd = new Song(1, "admin", "Producer1", "New Song", List.of("Pop"), "Poland", "New lyrics", 200);

        // Mockowanie metody save w repozytorium
        Mockito.when(songRepository.save(Mockito.anyList(), Mockito.anyString())).thenReturn(1);

        // Wykonanie żądania POST z dodaniem piosenki
        mockMvc.perform(post("/song/add")
                        .session(session) // Dodajemy sesję z użytkownikiem
                        .param("name", songToAdd.getName())
                        .param("produced_by", songToAdd.getProduced_by())
                        .param("genre", String.join(",", songToAdd.getGenre())) // List<String> na String
                        .param("origin", songToAdd.getOrigin())
                        .param("lyrics", songToAdd.getLyrics())
                        .param("length", String.valueOf(songToAdd.getLength())))
                .andExpect(status().is3xxRedirection()) // Oczekiwane przekierowanie po dodaniu
                .andExpect(redirectedUrl("/song/")); // Oczekiwane przekierowanie na listę piosenek
    }

    @Test // Testowanie wyświetlenia szczegółów piosenki
    void testGetSongDetails() throws Exception {
        User loggedInUser = new User();
        loggedInUser.setLogin("MarekAdmin");
        loggedInUser.setRole("moderator");

        // Tworzymy sesję mockowaną z użytkownikiem
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
        session.setAttribute("loggedInUser", loggedInUser); // Zapisanie użytkownika w sesji

        Song song = new Song(1, "admin", "Producer1", "Song1", List.of("Pop"), "Poland", "Lyrics", 180);
        Mockito.when(songRepository.getById(1)).thenReturn(song);

        mockMvc.perform(get("/song/1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("song"))
                .andExpect(model().attribute("genres", "Pop"))
                .andExpect(view().name("song/details"));
    }

    // Testowanie, czy wyświetla się odpowiedni błąd przy próbie wejścia w szczegóły
    @Test      // piosenki, która nie istnieje
    void testGetSongDetails_NotFound() throws Exception {
        User loggedInUser = new User();
        loggedInUser.setLogin("MarekAdmin");
        loggedInUser.setRole("moderator");

        // Tworzymy sesję mockowaną z użytkownikiem
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedIn", true); // Ustawienie atrybutu sesji po zalogowaniu
        session.setAttribute("loggedInUser", loggedInUser); // Zapisanie użytkownika w sesji

        Mockito.when(songRepository.getById(99)).thenReturn(null);

        mockMvc.perform(get("/song/99")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }
}
