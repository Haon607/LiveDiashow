package io.haon607.livediashowbackend.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageRepository imageRepository;

    @PostMapping
    public ResponseEntity<Image> uploadImage(@RequestBody ImageUploadRequest request) {
        return ResponseEntity.ok(imageRepository.save(new Image(request.data())));
    }

    @GetMapping
    @Transactional
    public ResponseEntity<Image> getNextImage() {
        Image image = imageRepository.findFirstByAlreadyShownFalseOrderByCreationDateAsc().orElse(null);
        if (image == null) {
            image = nextFromCycle();
        }
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        image.markShown();
        return ResponseEntity.ok(imageRepository.save(image));
    }

    private Image nextFromCycle() {
        List<Image> unseenInCycle = imageRepository.findByAlreadyShownTrueAndShownInCycleFalse();
        if (unseenInCycle.isEmpty()) {
            List<Image> shownImages = imageRepository.findByAlreadyShownTrueOrderByCreationDateAsc();
            if (shownImages.isEmpty()) {
                return null;
            }
            shownImages.forEach(Image::resetCycle);
            imageRepository.saveAll(shownImages);
            unseenInCycle = shownImages;
        }

        Collections.shuffle(unseenInCycle);
        return unseenInCycle.getFirst();
    }

    public record ImageUploadRequest(String data) {
    }
}
