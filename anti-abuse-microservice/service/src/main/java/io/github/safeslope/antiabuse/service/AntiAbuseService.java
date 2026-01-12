package io.github.safeslope.antiabuse.service;

import io.github.safeslope.antiabuse.model.*;
import io.github.safeslope.antiabuse.repository.AbuseEventRepository;
import io.github.safeslope.antiabuse.repository.BlockedCardRepository;
import io.github.safeslope.entities.AbuseEvent;
import io.github.safeslope.entities.BlockedCard;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AntiAbuseService {

    private static final int MAX_USES_PER_MINUTE = 6;
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

        AbuseEvent event = new AbuseEvent();
        event.setSkiTicketId(cmd.skiTicketId());
        event.setLockId(cmd.lockId());
        event.setLockerId(cmd.lockerId());
        event.setResortId(cmd.resortId());
        event.setAction(cmd.action().name()); // LOCK / UNLOCK
        event.setTimestamp(Instant.ofEpochMilli(cmd.timestamp()));
        abuseEventRepository.save(event);


        if (cmd.action() == Action.LOCK) {

            List<AbuseEvent> events =
                    abuseEventRepository.findBySkiTicketIdAndTimestampAfter(
                            cmd.skiTicketId(),
                            Instant.EPOCH // vzamemo vse, brez repo sprememb
                    );

            Map<Integer, Action> lastActionByLock = new HashMap<>();
            events.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

            for (AbuseEvent e : events) {
                lastActionByLock.put(
                        e.getLockId(),
                        Action.valueOf(e.getAction())
                );
            }

            boolean lockedOnAnotherLock = lastActionByLock.entrySet().stream()
                    .anyMatch(en ->
                            en.getValue() == Action.LOCK &&
                            !en.getKey().equals(cmd.lockId())
                    );

            if (lockedOnAnotherLock) {
                return block(cmd.skiTicketId(), now,
                        "Ticket already locked on another lock");
            }
        }

        // 4) suspicious use -> block 
        Instant oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
        long recentUses =
                abuseEventRepository.countBySkiTicketIdAndTimestampAfter(
                        cmd.skiTicketId(), oneMinuteAgo);

        if (recentUses >= MAX_USES_PER_MINUTE) {
            return block(cmd.skiTicketId(), now,
                    "Too many requests in 1 minute");
        }

        return new DecisionResult(
                Decision.ALLOW,
                CardState.ACTIVE,
                "OK",
                null
        );
    }

    private DecisionResult block(Integer ticketId, Instant now, String reason) {
        Instant blockUntil = now.plus(BLOCK_MINUTES, ChronoUnit.MINUTES);

        BlockedCard blocked = blockedCardRepository.findById(ticketId)
                .orElseGet(() -> {
                    BlockedCard b = new BlockedCard();
                    b.setSkiTicketId(ticketId);
                    return b;
                });

        blocked.setBlockedUntil(blockUntil);
        blocked.setReason(reason);
        blockedCardRepository.save(blocked);

        return new DecisionResult(
                Decision.DENY,
                CardState.BLOCKED,
                reason,
                blockUntil
        );
    }
}
