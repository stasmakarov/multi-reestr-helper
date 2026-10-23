package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.Severity;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

public final class CheckResults {

    private CheckResults() {
    }

    public static CheckResultDto hit(Rule rule, NoticeDto notice, String detail, Map<String, String> vars) {
        return hit(
                rule,
                notice.getNumber(),
                detail,
                rule.getSeverity(),
                interpolate(rule.getExplanationTemplate(), rule, notice, detail, vars)
        );
    }

    public static CheckResultDto hit(
            Rule rule,
            String objectKey,
            String detail,
            Severity severity,
            Map<String, String> vars
    ) {
        return hit(
                rule,
                objectKey,
                detail,
                severity,
                interpolate(rule.getExplanationTemplate(), rule, objectKey, detail, vars)
        );
    }

    private static CheckResultDto hit(
            Rule rule,
            String objectKey,
            String detail,
            Severity severity,
            String message
    ) {
        CheckResultDto result = new CheckResultDto();
        result.setId(UUID.randomUUID());
        result.setRuleCode(rule.getCode());
        result.setRuleName(rule.getName());
        result.setSeverity(severity);
        result.setObjectType(rule.getObjectType());
        result.setObjectKey(objectKey);
        result.setDetail(detail);
        result.setLegalRef(rule.getLegalRef());
        result.setMessage(message);
        result.setFingerprint(fingerprint(rule.getCode(), objectKey, detail));
        return result;
    }

    public static String fingerprint(String ruleCode, String objectKey, String detail) {
        String payload = nullToEmpty(ruleCode) + "|" + nullToEmpty(objectKey) + "|" + nullToEmpty(detail);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String interpolate(String template, Rule rule, NoticeDto notice, String detail, Map<String, String> vars) {
        return interpolate(template, rule, notice == null ? null : notice.getNumber(), detail, vars);
    }

    static String interpolate(String template, Rule rule, String number, String detail, Map<String, String> vars) {
        String message = template;
        if (message == null || message.isBlank()) {
            message = rule.getName() == null ? rule.getCode() : rule.getName();
            if (detail != null && !detail.isBlank()) {
                message = message + ": " + detail;
            }
            return message;
        }
        if (vars != null) {
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", nullToEmpty(entry.getValue()));
            }
        }
        message = message.replace("{detail}", nullToEmpty(detail));
        message = message.replace("{number}", nullToEmpty(number));
        return message;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
