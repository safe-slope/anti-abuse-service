package io.github.safeslope.antiabuse.repository;

import io.github.safeslope.entities.AbuseEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AbuseEventRepository extends JpaRepository<AbuseEvent, Long> {

    List<AbuseEvent> findByCardUidAndTimestampAfter(
            String cardUid,
            Instant since
    );
}
