package com.ankispirelink.anki;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongSupplier;

public final class ExternalAnkiReviewSession {
    public static final long DEFAULT_POLL_INTERVAL_MILLIS = 500L;
    public static final long DEFAULT_CARD_WAIT_TIMEOUT_MILLIS = 30000L;

    private final AnkiConnectClient client;
    private final ExternalApplicationController applications;
    private final long pollIntervalMillis;
    private final long cardWaitTimeoutMillis;
    private final LongSupplier clock;

    private volatile boolean done;
    private volatile boolean successful;
    private volatile boolean cancelled;
    private volatile AnkiEase ease = AnkiEase.GOOD;
    private volatile Status status = Status.STARTING;
    private volatile String statusDetail = "";
    private volatile FailureReason failureReason = FailureReason.NONE;
    private volatile String failureMessage = "";
    private Thread worker;

    public ExternalAnkiReviewSession(AnkiConnectClient client, ExternalApplicationController applications) {
        this(client, applications, DEFAULT_POLL_INTERVAL_MILLIS, DEFAULT_CARD_WAIT_TIMEOUT_MILLIS);
    }

    public ExternalAnkiReviewSession(AnkiConnectClient client, ExternalApplicationController applications,
                                     long pollIntervalMillis, long cardWaitTimeoutMillis) {
        this(client, applications, pollIntervalMillis, cardWaitTimeoutMillis, System::currentTimeMillis);
    }

    public ExternalAnkiReviewSession(AnkiConnectClient client, ExternalApplicationController applications,
                                     long pollIntervalMillis, long cardWaitTimeoutMillis, LongSupplier clock) {
        this.client = client;
        this.applications = applications;
        this.pollIntervalMillis = Math.max(1L, pollIntervalMillis);
        this.cardWaitTimeoutMillis = Math.max(1L, cardWaitTimeoutMillis);
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    public synchronized void start() {
        if (worker != null) {
            return;
        }
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                runSession();
            }
        }, "AnkiSpireLink-external-review");
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void cancel() {
        if (successful) {
            return;
        }
        cancelled = true;
        done = true;
        if (worker != null) {
            worker.interrupt();
        }
    }

    public boolean isDone() {
        return done;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public AnkiEase getEase() {
        return ease;
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    private void runSession() {
        try {
            long sessionStartedAt = Math.max(0L, clock.getAsLong());
            boolean switchFocus = applications.switchesFocus();
            setStatus(switchFocus ? Status.OPENING_ANKI : Status.CONNECTING_TO_ANKI, "");
            if (switchFocus) {
                applications.focusAnki();
            }
            if (cancelled) {
                return;
            }

            String deckName = client.getDeckName();
            if (deckName != null && !deckName.trim().isEmpty()) {
                setStatus(Status.STARTING_DECK_REVIEW, deckName);
                client.startDeckReview(deckName);
                if (cancelled) {
                    return;
                }
            } else {
                setStatus(Status.WAITING_FOR_MANUAL_REVIEW, "");
            }

            Optional<AnkiEase> reviewedEase = waitForReviewedCard(sessionStartedAt);
            if (cancelled) {
                return;
            }
            if (!reviewedEase.isPresent()) {
                if (failureMessage == null || failureMessage.isEmpty()) {
                    fail(FailureReason.NO_ACTIVE_CARD, "");
                }
                return;
            }
            complete(reviewedEase.get());
        } catch (RuntimeException e) {
            fail(FailureReason.EXCEPTION, e.getMessage());
        }
    }

    private Optional<AnkiEase> waitForReviewedCard(long sessionStartedAt) {
        long deadline = clock.getAsLong() + cardWaitTimeoutMillis;
        Set<Long> candidateCardIds = new LinkedHashSet<>();
        boolean sawReviewCard = false;
        while (!cancelled && (sawReviewCard || clock.getAsLong() <= deadline)) {
            Optional<AnkiGuiCard> card = client.currentGuiCard();
            if (cancelled) {
                return Optional.empty();
            }
            if (card.isPresent()) {
                long cardId = card.get().getCardId();
                if (cardId > 0L) {
                    sawReviewCard = true;
                    candidateCardIds.add(cardId);
                    setStatus(Status.ANSWER_CURRENT_CARD, card.get().getDeckName());
                }
            }
            Optional<AnkiEase> reviewedEase = latestReviewedEase(candidateCardIds, sessionStartedAt);
            if (cancelled) {
                return Optional.empty();
            }
            if (reviewedEase.isPresent()) {
                return reviewedEase;
            }
            sleep();
        }
        return Optional.empty();
    }

    private Optional<AnkiEase> latestReviewedEase(Set<Long> candidateCardIds, long sessionStartedAt) {
        return client.latestReviewEaseAtOrAfter(candidateCardIds, sessionStartedAt);
    }

    private void complete(AnkiEase reviewedEase) {
        this.ease = reviewedEase == null ? AnkiEase.GOOD : reviewedEase;
        this.successful = true;
        setStatus(Status.COMPLETE, "");
        this.done = true;
        if (applications.switchesFocus()) {
            applications.focusGame();
        }
    }

    private void fail(FailureReason reason, String message) {
        this.failureReason = reason == null ? FailureReason.EXCEPTION : reason;
        this.failureMessage = message == null ? "" : message.trim();
        setStatus(Status.FAILED, "");
        this.successful = false;
        this.done = true;
    }

    private void setStatus(Status status, String detail) {
        this.status = status == null ? Status.STARTING : status;
        this.statusDetail = detail == null ? "" : detail;
    }

    private void sleep() {
        try {
            Thread.sleep(pollIntervalMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelled = true;
        }
    }

    public enum Status {
        STARTING,
        CONNECTING_TO_ANKI,
        OPENING_ANKI,
        STARTING_DECK_REVIEW,
        WAITING_FOR_MANUAL_REVIEW,
        ANSWER_CURRENT_CARD,
        COMPLETE,
        FAILED
    }

    public enum FailureReason {
        NONE,
        NO_ACTIVE_CARD,
        EXCEPTION
    }
}
