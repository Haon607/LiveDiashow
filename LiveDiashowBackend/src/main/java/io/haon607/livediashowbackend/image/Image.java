package io.haon607.livediashowbackend.image;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String data;
    private LocalDateTime creationDate;

    public Image(String data) {
        this.data = data;
        this.creationDate = LocalDateTime.now(ZoneOffset.of("Z+1"));
        this.id = UUID.randomUUID();
    }
}
