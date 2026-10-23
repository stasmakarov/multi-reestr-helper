package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KtruExtraCharacteristicsEvaluator implements RuleTemplateEvaluator {

    @Override
    public RuleTemplateType templateType() {
        return RuleTemplateType.KTRU_EXTRA_CHARACTERISTICS;
    }

    @Override
    public List<CheckResultDto> evaluate(NoticeDto notice, Rule rule, RuleVersion version, RuleEvalParams params) {
        List<CheckResultDto> hits = new ArrayList<>();
        if (notice.getItems() == null) {
            return hits;
        }
        for (SpecPositionDto item : notice.getItems()) {
            if (item == null || item.getKtru() == null || item.getKtru().isBlank()) {
                continue;
            }
            if (item.getCharacteristics() == null) {
                continue;
            }
            for (KtruCharacteristicDto characteristic : item.getCharacteristics()) {
                if (characteristic == null || Boolean.TRUE.equals(characteristic.getFromEis())) {
                    continue;
                }
                String detail = Okpd2ForeignBanEvaluator.detail(item) + ";char=" + characteristic.getId();
                hits.add(CheckResults.hit(rule, notice, detail, Map.of(
                        "pos", Okpd2ForeignBanEvaluator.pos(item),
                        "name", nullToEmpty(item.getName()),
                        "okpd2", nullToEmpty(item.getOkpd2()),
                        "ktru", nullToEmpty(item.getKtru())
                )));
            }
        }
        return hits;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
