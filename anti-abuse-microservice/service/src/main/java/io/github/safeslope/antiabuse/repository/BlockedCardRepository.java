package io.github.safeslope.antiabuse.repository;

import io.github.safeslope.entities.BlockedCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedCardRepository extends JpaRepository<BlockedCard, Integer> {
}
