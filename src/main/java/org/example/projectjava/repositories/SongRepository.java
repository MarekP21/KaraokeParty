package org.example.projectjava.repositories;

import org.example.projectjava.models.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SongRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Zwraca listę wszystkich piosenek z tabeli song
    public List<Song> getAll(){
        return jdbcTemplate.query(
                "SELECT id, userLogin, produced_by, name, genre, origin, lyrics, length FROM song",
                BeanPropertyRowMapper.newInstance(Song.class)
        );
    }

    // Wyszukuje piosenkę na podstawie id
    public Song getById(int id){
        return jdbcTemplate.queryForObject(
                "SELECT id, userLogin, produced_by, name, genre, origin, lyrics, length FROM song WHERE id = ?",
                BeanPropertyRowMapper.newInstance(Song.class),
                id
        );
    }

    // Dodaje nowe piosenki do tabeli song z automatycznym przypisaniem kolejnego id.
    public int save(List<Song> songs, String currentUserLogin) {
        Integer maxId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM song", Integer.class);
        int currentId = (maxId != null ? maxId : 0) + 1;
        for (Song song : songs) {
            song.setId(currentId++);
            song.setUserLogin(currentUserLogin); // Ustawia login aktualnego użytkownika
            // Konwertujemy listę genre na ciąg tekstowy, oddzielając elementy przecinkiem
            String genreString = String.join(",", song.getGenre());
            jdbcTemplate.update(
                    "INSERT INTO song(id, userLogin, produced_by, name, genre, origin, lyrics, length) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
                    song.getId(), song.getUserLogin(), song.getProduced_by(), song.getName(),
                    genreString, song.getOrigin(), song.getLyrics(), song.getLength()
            );
        }
        return songs.size();
    }
    // Aktualizuje istniejącą piosenkę na podstawie id
    public int update(Song song){
        // Konwertujemy listę genre na ciąg tekstowy, oddzielając elementy przecinkiem
        String genreString = String.join(",", song.getGenre());
        return jdbcTemplate.update(
                "UPDATE song SET userLogin=?, produced_by=?, name=?, genre=?, origin=?, lyrics=?, length=? WHERE id=?",
                song.getUserLogin(), song.getProduced_by(), song.getName(),
                genreString, song.getOrigin(), song.getLyrics(), song.getLength(),
                song.getId()
        );
    }

    // Usuwa piosenkę na podstawie id.
    public int delete(int id){
        return jdbcTemplate.update("DELETE FROM song WHERE id=?", id);
    }
}


