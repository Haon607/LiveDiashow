package io.haon607.livediashowbackend.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageRepository imageRepository;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportImages() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (Image image : imageRepository.findAll()) {
                ImageFile imageFile = decodeImage(image);
                zip.putNextEntry(new ZipEntry(image.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "." + imageFile.extension()));
                zip.write(imageFile.bytes());
                zip.closeEntry();
            }
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/zip"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"livediashow-images.zip\"")
            .body(output.toByteArray());
    }

    private ImageFile decodeImage(Image image) {
        String data = image.getData();
        String extension = "jpg";
        int comma = data.indexOf(',');
        if (data.startsWith("data:") && comma >= 0) {
            String mimeType = data.substring(5, data.indexOf(';') > 0 ? data.indexOf(';') : comma);
            extension = switch (mimeType.toLowerCase()) {
                case "image/png" -> "png";
                case "image/gif" -> "gif";
                case "image/webp" -> "webp";
                case "image/jpeg" -> "jpg";
                default -> "bin";
            };
            data = data.substring(comma + 1);
        }
        return new ImageFile(java.util.Base64.getDecoder().decode(data), extension);
    }

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

    private record ImageFile(byte[] bytes, String extension) {
    }

    public record ImageUploadRequest(String data) {
    }
}
