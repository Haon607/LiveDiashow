package io.haon607.livediashowbackend.image;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

@Repository
public interface ImageRepository extends ListCrudRepository<Image, UUID> {
}
