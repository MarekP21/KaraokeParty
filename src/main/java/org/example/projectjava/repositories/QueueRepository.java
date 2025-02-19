package org.example.projectjava.repositories;

import org.example.projectjava.models.QueueItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class QueueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Zwraca wszystkie pozycje z tabeli kolejki
    public List<QueueItem> getAll() {
        return jdbcTemplate.query(
            "SELECT position, song_name, added_by, song_id FROM queue_item ORDER BY position ASC",
            BeanPropertyRowMapper.newInstance(QueueItem.class)
        );
    }

    public void updatePosition(int currentPosition, int newPosition) {
        // Tymczasowe ustawienie na wartość buforową (-1) dla rekordu na docelowej pozycji
        jdbcTemplate.update("UPDATE queue_item SET position = -1 WHERE position = ?", newPosition);

        // Ustawienie rekordu na nową pozycję
        jdbcTemplate.update("UPDATE queue_item SET position = ? WHERE position = ?", newPosition, currentPosition);

        // Przywrócenie wartości dla rekordu tymczasowego (-1)
        jdbcTemplate.update("UPDATE queue_item SET position = ? WHERE position = -1", currentPosition);

    }

    public void updatePositionNumber(int currentPosition, int newPosition) {

        // ustwienie pozycji -1 dla wybranej piosenki
        jdbcTemplate.update("UPDATE queue_item SET position = -1 WHERE position = ?", currentPosition);

        // Sprawdzenie, czy przesunięcie jest w górę czy w dół
        if (newPosition > currentPosition) {
            // Jeśli newPosition jest większe, to wszystkie elementy pomiędzy currentPosition a newPosition (wyłącznie)
            // przesunięcie o 1 pozycję w górę
            jdbcTemplate.update("UPDATE queue_item SET position = position - 1 WHERE position > ? AND position <= ?", currentPosition, newPosition);
        } else if (newPosition < currentPosition) {
            // Jeśli newPosition jest mniejsze, to wszystkie elementy pomiędzy currentPosition a newPosition (wyłącznie)
            // przesunięcie o 1 pozycję w dół
            jdbcTemplate.update("UPDATE queue_item SET position = position + 1 WHERE position < ? AND position >= ?", currentPosition, newPosition);
        }

        // Teraz ustawienie piosenki na docelową pozycję
        jdbcTemplate.update("UPDATE queue_item SET position = ? WHERE position = -1", newPosition);
        getAll();
    }


    public void updateSong(int position, String newSongName, int newSongId) {
        jdbcTemplate.update(
            "UPDATE queue_item SET song_name = ?, song_id = ? WHERE position = ?",
            newSongName, newSongId, position
        );
    }

    // Dodaje nową pozycję do kolejki
    public int add(QueueItem queueItem) {
        Integer maxPosition = jdbcTemplate.queryForObject("SELECT MAX(position) FROM queue_item", Integer.class);
        if (maxPosition == null) {
            maxPosition = 0;
        }
        return jdbcTemplate.update(
            "INSERT INTO queue_item (position, song_name, added_by, song_id) VALUES (?, ?, ?, ?)",
                maxPosition + 1, queueItem.getSongName(), queueItem.getAddedBy(), queueItem.getSongId()
        );
    }

    // Usuwa pozycję z kolejki na podstawie numeru porządkowego
    public void deleteByPosition(int position) {
        jdbcTemplate.update(
            "DELETE FROM queue_item WHERE position = ?",
            position
        );
    }

    // Wyszukuje pozycję w kolejce na podstawie numeru porządkowego
    public QueueItem getByPosition(int position) {
        return jdbcTemplate.queryForObject(
            "SELECT position, song_name, added_by, song_id FROM queue_item WHERE position = ?",
            BeanPropertyRowMapper.newInstance(QueueItem.class),
            position
        );
    }

    // Sprawdza, czy użytkownik już dodał piosenkę do kolejki
    public boolean existsByAddedBy(String addedBy) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM queue_item WHERE added_by = ?",
            Integer.class,
            addedBy
        );
        return count != null && count > 0;
    }

    // Resetuje kolejkę (usuwa wszystkie pozycje)
    public int clearQueue() {
        return jdbcTemplate.update("DELETE FROM queue_item");
    }

}
