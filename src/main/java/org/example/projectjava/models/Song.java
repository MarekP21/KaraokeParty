package org.example.projectjava.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Song {
    private int id;
    private String userLogin;

    @NotBlank(message = "Produced by cannot be empty")
    private String produced_by;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Genre cannot be empty")
    private List<String> genre;

    @NotBlank(message = "Origin cannot be empty")
    @Pattern(regexp = "^[a-zA-ZąęćńśółżźźĄĘĆŃŚÓŁŻŹ]+$", message = "Origin can only contain letters, including Polish characters")
    private String origin;

    @Size(max = 2000, message = "Lyrics cannot exceed 2000 characters")
    private String lyrics;

    @Positive(message = "Length must be a positive integer")
    private int length;
}



