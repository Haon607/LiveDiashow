package io.haon607.livediashowbackend.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column()
    private String data;
    private LocalDateTime creationDate;
    private Boolean alreadyShown;
    private Boolean shownInCycle;
    private Boolean excluded;

    public Image(String data) {
        this.data = data;
        this.creationDate = LocalDateTime.now(ZoneId.of("Europe/Vienna"));
        this.alreadyShown = false;
        this.shownInCycle = false;
        this.excluded = false;
    }

    @PostLoad
    private void initializeCycleFlag() {
        if (shownInCycle == null) {
            shownInCycle = false;
        }
    }

    public void markShown() {
        this.alreadyShown = true;
        this.shownInCycle = true;
    }

    public void resetCycle() {
        this.shownInCycle = false;
    }

    public void exclude() {
        this.excluded = true;
    }

    public void include() {
        this.excluded = false;
    }
}
