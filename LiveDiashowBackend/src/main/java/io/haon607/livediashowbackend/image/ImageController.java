package io.haon607.livediashowbackend.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageRepository imageRepository;

    @PostMapping
    public ResponseEntity<Image> uploadImage(@RequestBody String data) {
        return ResponseEntity.ok(imageRepository.save(new Image(data)));
    }
}
