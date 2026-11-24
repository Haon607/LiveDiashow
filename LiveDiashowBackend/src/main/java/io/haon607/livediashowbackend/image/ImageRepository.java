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
    @Query("select image.id as id, image.creationDate as creationDate, image.excluded as excluded from Image image order by image.creationDate desc, image.id desc")
    List<ImageAdminSummary> findAllAdminSummaries();
    @Query("select image.id as id, image.creationDate as creationDate, image.excluded as excluded from Image image where image.creationDate > (select cursor.creationDate from Image cursor where cursor.id = :cursorId) or (image.creationDate = (select cursor.creationDate from Image cursor where cursor.id = :cursorId) and image.id > :cursorId) order by image.creationDate asc, image.id asc")
    List<ImageAdminSummary> findAdminSummariesCreatedAfterCursor(@Param("cursorId") UUID cursorId);

    interface ImageAdminSummary {
        UUID getId();
        java.time.LocalDateTime getCreationDate();
        Boolean getExcluded();
    }
}
