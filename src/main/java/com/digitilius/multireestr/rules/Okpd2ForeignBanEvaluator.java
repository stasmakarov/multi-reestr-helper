package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class Okpd2ForeignBanEvaluator implements RuleTemplateEvaluator {

    @Override
    public RuleTemplateType templateType() {
        return RuleTemplateType.OKPD2_FOREIGN_BAN;
    }

    @Override
    public List<CheckResultDto> evaluate(NoticeDto notice, Rule rule, RuleVersion version, RuleEvalParams params) {
        List<CheckResultDto> hits = new ArrayList<>();
        if (notice.getItems() == null) {
            return hits;
        }
        LocalDate date = notice.getPublishedAt() == null ? null : notice.getPublishedAt().toLocalDate();
        for (SpecPositionDto item : notice.getItems()) {
            if (item == null) {
                continue;
            }
            if (!ReferenceListMatcher.matches(params.list("okpdList"), item.getOkpd2(), date)) {
                continue;
            }
            if (hasBanOrJustifiedImpossibility(item)) {
                continue;
            }
            String detail = detail(item);
            hits.add(CheckResults.hit(rule, notice, detail, Map.of(
                    "pos", pos(item),
                    "name", nullToEmpty(item.getName()),
                    "okpd2", nullToEmpty(item.getOkpd2()),
                    "ktru", nullToEmpty(item.getKtru())
            )));
        }
        return hits;
    }

    static boolean hasBanOrJustifiedImpossibility(SpecPositionDto item) {
        if (Boolean.TRUE.equals(item.getForeignBan())) {
            return true;
        }
        return Boolean.TRUE.equals(item.getBanImpossible())
                && item.getBanImpossibleReason() != null
                && !item.getBanImpossibleReason().isBlank();
    }

    static String detail(SpecPositionDto item) {
        return "pos=" + pos(item) + ";id=" + item.getId();
    }

    static String pos(SpecPositionDto item) {
        return item.getPos() == null ? "" : item.getPos().toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
