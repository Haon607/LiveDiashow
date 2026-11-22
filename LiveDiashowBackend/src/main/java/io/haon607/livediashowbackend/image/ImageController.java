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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        Set<String> entryNames = new HashSet<>();
        List<Image> images = new ArrayList<>(imageRepository.findAllByExcludedFalse());
        images.sort(Comparator.comparing(Image::getCreationDate).thenComparing(image -> image.getId().toString()));

        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (Image image : images) {
                ImageFile imageFile = decodeImage(image);
                String timestamp = image.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String entryName = uniqueEntryName(timestamp, imageFile.extension(), entryNames);
                zip.putNextEntry(new ZipEntry(entryName));
                zip.write(imageFile.bytes());
                zip.closeEntry();
            }
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/zip"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"livediashow-images.zip\"")
            .body(output.toByteArray());
    }

    private String uniqueEntryName(String timestamp, String extension, Set<String> existingNames) {
        String baseName = timestamp + "." + extension;
        if (existingNames.add(baseName)) {
            return baseName;
        }

        int suffix = 2;
        String candidate;
        do {
            candidate = timestamp + " (" + suffix++ + ")." + extension;
        } while (!existingNames.add(candidate));
        return candidate;
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

    @GetMapping("/admin")
    public List<Image> getImagesForAdmin() {
        return imageRepository.findAllByOrderByCreationDateDescIdDesc();
    }

    @GetMapping("/admin/new")
    public List<Image> getNewAdminImages(@RequestParam java.util.UUID afterId) {
        return imageRepository.findCreatedAfterCursor(afterId);
    }

    @PutMapping("/{id}/exclude")
    public ResponseEntity<Void> excludeImage(@PathVariable java.util.UUID id) {
        return setExcluded(id, true);
    }

    @PutMapping("/{id}/include")
    public ResponseEntity<Void> includeImage(@PathVariable java.util.UUID id) {
        return setExcluded(id, false);
    }

    private ResponseEntity<Void> setExcluded(java.util.UUID id, boolean excluded) {
        return imageRepository.findById(id).map(image -> {
            if (excluded) image.exclude();
            else image.include();
            imageRepository.save(image);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Transactional
    public ResponseEntity<Image> getNextImage() {
        Image image = imageRepository.findFirstByExcludedFalseAndAlreadyShownFalseOrderByCreationDateAsc().orElse(null);
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
        List<Image> unseenInCycle = imageRepository.findByExcludedFalseAndAlreadyShownTrueAndShownInCycleFalse();
        if (unseenInCycle.isEmpty()) {
            List<Image> shownImages = imageRepository.findByExcludedFalseAndAlreadyShownTrueOrderByCreationDateAsc();
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
