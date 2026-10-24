package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public final class ViolationMerge {

    static final int OBJECT_CAPTION_MAX = 255;

    private ViolationMerge() {
    }

    public static String objectCaption(NoticeDto notice) {
        String number = notice.getNumber() == null ? "" : notice.getNumber();
        String subject = notice.getSubject();
        if (subject == null || subject.isBlank()) {
            return truncate(number);
        }
        return truncate(number + " — " + subject);
    }

    public static void copyHitFields(
            Violation target,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            NoticeDto notice
    ) {
        copyHitFields(target, hit, rule, version, notice.getId(), objectCaption(notice));
    }

    public static void copyHitFields(
            Violation target,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            Integer legacyId,
            String caption
    ) {
        target.setRule(rule);
        target.setRuleVersion(version);
        target.setObjectType(hit.getObjectType());
        target.setObjectKey(hit.getObjectKey());
        target.setLegacyId(legacyId);
        target.setObjectCaption(caption == null ? null : truncate(caption));
        target.setDetail(hit.getDetail());
        target.setFingerprint(hit.getFingerprint());
        target.setSeverity(hit.getSeverity());
        target.setMessage(hit.getMessage());
        target.setLegalRef(hit.getLegalRef());
    }

    public static void applyExistingHit(
            Violation existing,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            NoticeDto notice,
            CheckRun run,
            OffsetDateTime now
    ) {
        applyExistingHit(existing, hit, rule, version, notice.getId(), objectCaption(notice), run, now);
    }

    public static void applyExistingHit(
            Violation existing,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            Integer legacyId,
            String caption,
            CheckRun run,
            OffsetDateTime now
    ) {
        copyHitFields(existing, hit, rule, version, legacyId, caption);
        existing.setLastDetectedAt(now);
        existing.setLastCheckRun(run);
        if (existing.getStatus() == ViolationStatus.FIXED) {
            existing.setStatus(ViolationStatus.NEW);
            existing.setResolvedAt(null);
            existing.setAcknowledgedBy(null);
            existing.setAcknowledgedAt(null);
        }
    }

    public static void initNew(
            Violation created,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            NoticeDto notice,
            CheckRun run,
            OffsetDateTime now
    ) {
        initNew(created, hit, rule, version, notice.getId(), objectCaption(notice), run, now);
    }

    public static void initNew(
            Violation created,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            Integer legacyId,
            String caption,
            CheckRun run,
            OffsetDateTime now
    ) {
        copyHitFields(created, hit, rule, version, legacyId, caption);
        created.setStatus(ViolationStatus.NEW);
        created.setFirstDetectedAt(now);
        created.setLastDetectedAt(now);
        created.setLastCheckRun(run);
    }

    public static void applyDeadline(
            Violation target,
            java.time.LocalDate dueDate,
            java.time.LocalDate factDate,
            Integer deviationDays
    ) {
        target.setDueDate(dueDate);
        target.setFactDate(factDate);
        target.setDeviationDays(deviationDays);
    }

    public static boolean shouldMarkFixed(
            Violation existing,
            Set<UUID> evaluatedRuleIds,
            Set<String> currentFingerprints
    ) {
        if (existing.getStatus() == ViolationStatus.FIXED
                || existing.getStatus() == ViolationStatus.FALSE_POSITIVE) {
            return false;
        }
        if (existing.getFingerprint() != null && currentFingerprints.contains(existing.getFingerprint())) {
            return false;
        }
        UUID ruleId = existing.getRule() == null ? null : existing.getRule().getId();
        return ruleId != null && evaluatedRuleIds.contains(ruleId);
    }

    public static void markFixed(Violation existing, OffsetDateTime now) {
        existing.setStatus(ViolationStatus.FIXED);
        existing.setResolvedAt(now);
    }

    private static String truncate(String value) {
        if (value.length() <= OBJECT_CAPTION_MAX) {
            return value;
        }
        return value.substring(0, OBJECT_CAPTION_MAX);
    }
}
