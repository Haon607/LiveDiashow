package io.haon607.livediashowbackend.image;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface ImageRepository extends ListCrudRepository<Image, UUID> {
    Optional<Image> findFirstByAlreadyShownFalseOrderByCreationDateAsc();
    List<Image> findByAlreadyShownTrueAndShownInCycleFalse();
    List<Image> findByAlreadyShownTrueOrderByCreationDateAsc();
}
