package io.github.safeslope.antiabuse.service;

import io.github.safeslope.antiabuse.model.*;
import io.github.safeslope.antiabuse.repository.AbuseEventRepository;
import io.github.safeslope.antiabuse.repository.BlockedCardRepository;
import io.github.safeslope.entities.AbuseEvent;
import io.github.safeslope.entities.BlockedCard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AntiAbuseServiceTest {

    @Mock private AbuseEventRepository abuseEventRepository;
    @Mock private BlockedCardRepository blockedCardRepository;

    @InjectMocks private AntiAbuseService antiAbuseService;

    private EvaluateCommand cmd(Action action) {
        return new EvaluateCommand(
                10,        
                20,       
                30,         
                40,         
                action,
                123456789L   
        );
    }

    @Test
    void evaluate_deniesRequest_whenSkiTicketIsCurrentlyBlocked() {
        BlockedCard blocked = new BlockedCard();
        blocked.setSkiTicketId(10);
        blocked.setBlockedUntil(Instant.now().plusSeconds(600));
        blocked.setReason("Too many requests");

        when(blockedCardRepository.findById(10)).thenReturn(Optional.of(blocked));

        DecisionResult result = antiAbuseService.evaluate(cmd(Action.UNLOCK));

        assertThat(result.decision()).isEqualTo(Decision.DENY);
        assertThat(result.cardState()).isEqualTo(CardState.BLOCKED);
        assertThat(result.reason()).isEqualTo("Too many requests");
        assertThat(result.blockUntil()).isNotNull();

        verify(blockedCardRepository).findById(10);
        verifyNoInteractions(abuseEventRepository);
        verifyNoMoreInteractions(blockedCardRepository);
    }

    @Test
    void evaluate_blocksTicket_whenLockActionAndTicketLockedOnDifferentLock() {
        when(blockedCardRepository.findById(10)).thenReturn(Optional.empty());

        AbuseEvent otherLockLocked = new AbuseEvent();
        otherLockLocked.setLockId(999);
        otherLockLocked.setAction("LOCK");
        otherLockLocked.setTimestamp(Instant.parse("2026-01-20T10:00:00Z"));

        when(abuseEventRepository.findBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class)))
                .thenReturn(new ArrayList<>(List.of(otherLockLocked)));

        DecisionResult result = antiAbuseService.evaluate(cmd(Action.LOCK));

        assertThat(result.decision()).isEqualTo(Decision.DENY);
        assertThat(result.cardState()).isEqualTo(CardState.BLOCKED);
        assertThat(result.reason()).contains("already locked on another lock");
        assertThat(result.blockUntil()).isNotNull();

        ArgumentCaptor<BlockedCard> blockedCaptor = ArgumentCaptor.forClass(BlockedCard.class);
        verify(blockedCardRepository, times(2)).findById(10);
        verify(blockedCardRepository).save(blockedCaptor.capture());

        BlockedCard saved = blockedCaptor.getValue();
        assertThat(saved.getSkiTicketId()).isEqualTo(10);
        assertThat(saved.getReason()).contains("already locked on another lock");
        assertThat(saved.getBlockedUntil()).isNotNull();

        verify(abuseEventRepository).save(any(AbuseEvent.class));
        verify(abuseEventRepository).findBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class));
        verify(abuseEventRepository, never()).countBySkiTicketIdAndTimestampAfter(anyInt(), any());
    }

    @Test
    void evaluate_blocks_whenTooManyRequestsInOneMinute() {
        when(blockedCardRepository.findById(10)).thenReturn(Optional.empty());
        when(abuseEventRepository.countBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class)))
                .thenReturn(6L);

        DecisionResult result = antiAbuseService.evaluate(cmd(Action.UNLOCK));

        assertThat(result.decision()).isEqualTo(Decision.DENY);
        assertThat(result.cardState()).isEqualTo(CardState.BLOCKED);
        assertThat(result.reason()).contains("Too many requests");
        assertThat(result.blockUntil()).isNotNull();

        verify(abuseEventRepository).save(any(AbuseEvent.class));

        ArgumentCaptor<BlockedCard> captor = ArgumentCaptor.forClass(BlockedCard.class);
        verify(blockedCardRepository, times(2)).findById(10);
        verify(blockedCardRepository).save(captor.capture());

        BlockedCard saved = captor.getValue();
        assertThat(saved.getSkiTicketId()).isEqualTo(10);
        assertThat(saved.getReason()).contains("Too many requests");
        assertThat(saved.getBlockedUntil()).isNotNull();

        verify(abuseEventRepository).countBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class));
        verifyNoMoreInteractions(blockedCardRepository, abuseEventRepository);
    }

    @Test
    void evaluate_returnsALLOW_andSavesEvent_whenNormalUse() {
        when(blockedCardRepository.findById(10)).thenReturn(Optional.empty());

        when(abuseEventRepository.findBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class)))
                .thenReturn(new ArrayList<>());

        when(abuseEventRepository.countBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class)))
                .thenReturn(0L);

        DecisionResult result = antiAbuseService.evaluate(cmd(Action.LOCK));

        assertThat(result.decision()).isEqualTo(Decision.ALLOW);
        assertThat(result.cardState()).isEqualTo(CardState.ACTIVE);
        assertThat(result.reason()).isEqualTo("OK");
        assertThat(result.blockUntil()).isNull();

        ArgumentCaptor<AbuseEvent> captor = ArgumentCaptor.forClass(AbuseEvent.class);
        verify(abuseEventRepository).save(captor.capture());

        AbuseEvent saved = captor.getValue();
        assertThat(saved.getSkiTicketId()).isEqualTo(10);
        assertThat(saved.getLockId()).isEqualTo(20);
        assertThat(saved.getLockerId()).isEqualTo(30);
        assertThat(saved.getResortId()).isEqualTo(40);
        assertThat(saved.getAction()).isEqualTo("LOCK");
        assertThat(saved.getTimestamp()).isEqualTo(Instant.ofEpochMilli(123456789L));

        verify(blockedCardRepository).findById(10);
        verify(abuseEventRepository).findBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class));
        verify(abuseEventRepository).countBySkiTicketIdAndTimestampAfter(eq(10), any(Instant.class));
        verifyNoMoreInteractions(blockedCardRepository, abuseEventRepository);
    }
}
