package io.github.safeslope.antiabuse.service;

import io.github.safeslope.antiabuse.model.DecisionResult;
import io.github.safeslope.antiabuse.model.EvaluateCommand;
import io.github.safeslope.antiabuse.repository.AbuseEventRepository;
import io.github.safeslope.antiabuse.repository.BlockedCardRepository;
import io.github.safeslope.antiabuse.model.CardState;
import io.github.safeslope.antiabuse.model.Decision;
import io.github.safeslope.entities.AbuseEvent;
import io.github.safeslope.entities.BlockedCard;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class AntiAbuseService {

    private static final int MAX_USES_PER_MINUTE = 3;
    private static final int BLOCK_MINUTES = 10;

    private final AbuseEventRepository abuseEventRepository;
    private final BlockedCardRepository blockedCardRepository;

    public AntiAbuseService(AbuseEventRepository abuseEventRepository,
                            BlockedCardRepository blockedCardRepository) {
        this.abuseEventRepository = abuseEventRepository;
        this.blockedCardRepository = blockedCardRepository;
    }

    public DecisionResult evaluate(EvaluateCommand cmd) {
        Instant now = Instant.now();

        // blocked ticket? deny
        var blockedOpt = blockedCardRepository.findById(cmd.skiTicketId());
        if (blockedOpt.isPresent()) {
            BlockedCard blocked = blockedOpt.get();
            Instant until = blocked.getBlockedUntil();

            if (until != null && until.isAfter(now)) {
                return new DecisionResult(
                        Decision.DENY,
                        CardState.BLOCKED,
                        blocked.getReason() != null ? blocked.getReason() : "Ticket is blocked",
                        until
                );
            }
        }

        // mark event
        AbuseEvent event = new AbuseEvent();
        event.setSkiTicketId(cmd.skiTicketId());
        event.setLockId(cmd.lockId());
        event.setLockerId(cmd.lockerId());
        event.setResortId(cmd.resortId());
        event.setAction(cmd.action().name()); // enum -> String
        event.setTimestamp(Instant.ofEpochMilli(cmd.timestamp()));
        abuseEventRepository.save(event);

        // suspicious use -> block
        Instant oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
        long recentUses = abuseEventRepository.countBySkiTicketIdAndTimestampAfter(cmd.skiTicketId(), oneMinuteAgo);

        if (recentUses >= MAX_USES_PER_MINUTE) {
            Instant blockUntil = now.plus(BLOCK_MINUTES, ChronoUnit.MINUTES);

            BlockedCard blocked = new BlockedCard();
            blocked.setSkiTicketId(cmd.skiTicketId());
            blocked.setBlockedUntil(blockUntil);
            blocked.setReason("Too many requests in 1 minute");
            blockedCardRepository.save(blocked);

            return new DecisionResult(
                    Decision.DENY,
                    CardState.BLOCKED,
                    "Ticket blocked due to abuse",
                    blockUntil
            );
        }

        return new DecisionResult(
                Decision.ALLOW,
                CardState.ACTIVE,
                "OK",
                null
        );
    }
}
