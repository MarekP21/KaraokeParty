package org.example.projectjava.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class QueueItem {
    @NotBlank(message = "Position cannot be empty")
    private int position;

    @NotBlank(message = "Song name cannot be empty")
    private String songName;

    @NotBlank(message = "Added by cannot be empty")
    private String addedBy;

    @NotBlank(message = "Song id cannot be empty")
    private int songId;
}


