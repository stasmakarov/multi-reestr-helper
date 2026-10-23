package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.Severity;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationMergeTest {

    @Test
    void newHitStartsAsNew() {
        Violation created = new Violation();
        OffsetDateTime now = OffsetDateTime.parse("2026-02-01T10:00:00Z");
        ViolationMerge.initNew(created, hit(), rule(), version(), notice(), run(), now);

        assertThat(created.getStatus()).isEqualTo(ViolationStatus.NEW);
        assertThat(created.getFirstDetectedAt()).isEqualTo(now);
        assertThat(created.getLastDetectedAt()).isEqualTo(now);
        assertThat(created.getFingerprint()).isEqualTo("fp-1");
        assertThat(created.getLegalRef()).isEqualTo("ПП 1875");
        assertThat(created.getObjectCaption()).isEqualTo("0173100017026000008 — Батареи");
    }

    @Test
    void sameFingerprintUpdatesLastDetectedAndKeepsAcknowledged() {
        Violation existing = acknowledged();
        OffsetDateTime later = OffsetDateTime.parse("2026-02-02T10:00:00Z");
        ViolationMerge.applyExistingHit(existing, hit(), rule(), version(), notice(), run(), later);

        assertThat(existing.getStatus()).isEqualTo(ViolationStatus.ACKNOWLEDGED);
        assertThat(existing.getLastDetectedAt()).isEqualTo(later);
        assertThat(existing.getAcknowledgedBy()).isEqualTo("specialist");
    }

    @Test
    void falsePositiveSurvivesRehit() {
        Violation existing = acknowledged();
        existing.setStatus(ViolationStatus.FALSE_POSITIVE);
        existing.setComment("не применяется");
        ViolationMerge.applyExistingHit(existing, hit(), rule(), version(), notice(), run(), OffsetDateTime.now());

        assertThat(existing.getStatus()).isEqualTo(ViolationStatus.FALSE_POSITIVE);
        assertThat(existing.getComment()).isEqualTo("не применяется");
    }

    @Test
    void fixedRehitReopensAsNew() {
        Violation existing = acknowledged();
        existing.setStatus(ViolationStatus.FIXED);
        existing.setResolvedAt(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
        ViolationMerge.applyExistingHit(existing, hit(), rule(), version(), notice(), run(), OffsetDateTime.now());

        assertThat(existing.getStatus()).isEqualTo(ViolationStatus.NEW);
        assertThat(existing.getResolvedAt()).isNull();
        assertThat(existing.getAcknowledgedBy()).isNull();
        assertThat(existing.getAcknowledgedAt()).isNull();
    }

    @Test
    void missingFingerprintOfEvaluatedRuleIsFixed() {
        Violation existing = acknowledged();
        UUID evaluated = existing.getRule().getId();

        assertThat(ViolationMerge.shouldMarkFixed(existing, Set.of(evaluated), Set.of())).isTrue();
    }

    @Test
    void doesNotFixWhenRuleWasNotEvaluated() {
        Violation existing = acknowledged();
        UUID otherRule = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

        assertThat(ViolationMerge.shouldMarkFixed(existing, Set.of(otherRule), Set.of())).isFalse();
        assertThat(ViolationMerge.shouldMarkFixed(existing, Set.of(), Set.of())).isFalse();
    }

    @Test
    void doesNotFixWhenFingerprintStillPresent() {
        Violation existing = acknowledged();

        assertThat(ViolationMerge.shouldMarkFixed(
                existing, Set.of(existing.getRule().getId()), Set.of("fp-1"))).isFalse();
    }

    @Test
    void alreadyFixedIsNotMarkedAgain() {
        Violation existing = acknowledged();
        existing.setStatus(ViolationStatus.FIXED);

        assertThat(ViolationMerge.shouldMarkFixed(
                existing, Set.of(existing.getRule().getId()), Set.of())).isFalse();
    }

    private static Violation acknowledged() {
        Violation existing = new Violation();
        ViolationMerge.initNew(existing, hit(), rule(), version(), notice(), run(),
                OffsetDateTime.parse("2026-02-01T10:00:00Z"));
        existing.setStatus(ViolationStatus.ACKNOWLEDGED);
        existing.setAcknowledgedBy("specialist");
        existing.setAcknowledgedAt(OffsetDateTime.parse("2026-02-01T12:00:00Z"));
        return existing;
    }

    private static CheckResultDto hit() {
        CheckResultDto hit = new CheckResultDto();
        hit.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        hit.setRuleCode("R03_PP1875");
        hit.setObjectType(RuleObjectType.NOTICE);
        hit.setObjectKey("0173100017026000008");
        hit.setDetail("pos=1;id=21");
        hit.setMessage("нужен запрет");
        hit.setLegalRef("ПП 1875");
        hit.setSeverity(Severity.ERROR);
        hit.setFingerprint("fp-1");
        return hit;
    }

    private static Rule rule() {
        Rule rule = new Rule();
        rule.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        rule.setCode("R03_PP1875");
        return rule;
    }

    private static RuleVersion version() {
        RuleVersion version = new RuleVersion();
        version.setId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        return version;
    }

    private static NoticeDto notice() {
        NoticeDto notice = new NoticeDto();
        notice.setId(10);
        notice.setNumber("0173100017026000008");
        notice.setSubject("Батареи");
        return notice;
    }

    private static CheckRun run() {
        CheckRun run = new CheckRun();
        run.setId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        return run;
    }
}
