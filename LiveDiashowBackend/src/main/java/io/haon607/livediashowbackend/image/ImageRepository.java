package io.haon607.livediashowbackend.image;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ImageRepository extends ListCrudRepository<Image, UUID> {
    Optional<Image> findFirstByExcludedFalseAndAlreadyShownFalseOrderByCreationDateAsc();
    List<Image> findByExcludedFalseAndAlreadyShownTrueAndShownInCycleFalse();
    List<Image> findByExcludedFalseAndAlreadyShownTrueOrderByCreationDateAsc();
    List<Image> findAllByOrderByCreationDateDesc();
    List<Image> findAllByExcludedFalse();
    List<Image> findAllByOrderByCreationDateAsc();
    List<Image> findAllByOrderByCreationDateDescIdDesc();
    @Query("select image from Image image where image.creationDate > (select cursor.creationDate from Image cursor where cursor.id = :cursorId) or (image.creationDate = (select cursor.creationDate from Image cursor where cursor.id = :cursorId) and image.id > :cursorId) order by image.creationDate asc, image.id asc")
    List<Image> findCreatedAfterCursor(@Param("cursorId") UUID cursorId);
}
