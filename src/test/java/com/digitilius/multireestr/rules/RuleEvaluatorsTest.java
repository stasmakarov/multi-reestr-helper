package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.entity.Severity;
import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEvaluatorsTest {

    @Test
    void okpd2RequiresBanOrReason() {
        Rule rule = rule("R03_PP1875", RuleTemplateType.OKPD2_FOREIGN_BAN);
        ReferenceList list = new ReferenceList();
        ReferenceListItem item = new ReferenceListItem();
        item.setValue("27.20");
        item.setPrefixMatch(true);
        list.setItems(List.of(item));
        RuleEvalParams params = params(listParam(list));

        SpecPositionDto hit = position(1, 10, "Батарея", "27.20.23.130", null);
        SpecPositionDto okBan = position(2, 11, "Батарея", "27.20.23.130", null);
        okBan.setForeignBan(true);
        SpecPositionDto okReason = position(3, 12, "Батарея", "27.20.23.130", null);
        okReason.setBanImpossible(true);
        okReason.setBanImpossibleReason("нет аналогов");
        SpecPositionDto other = position(4, 13, "Бумага", "17.12.14.110", null);

        List<com.digitilius.multireestr.entity.legacy.CheckResultDto> results =
                new Okpd2ForeignBanEvaluator().evaluate(notice(hit, okBan, okReason, other), rule, new RuleVersion(), params);

        assertThat(results).extracting(com.digitilius.multireestr.entity.legacy.CheckResultDto::getDetail)
                .containsExactly("pos=1;id=10");
        assertThat(results.get(0).getFingerprint()).hasSize(64);
        assertThat(results.get(0).getMessage()).contains("27.20.23.130");
    }

    @Test
    void extraCharacteristicsOnlyWhenKtruPresent() {
        Rule rule = rule("R05_KTRU_CHARS", RuleTemplateType.KTRU_EXTRA_CHARACTERISTICS);
        SpecPositionDto withKtru = position(1, 20, "Бумага", "17.12.14.110", "17.12.14.110-00000003");
        KtruCharacteristicDto fromEis = characteristic(1, true);
        KtruCharacteristicDto extra = characteristic(2, false);
        withKtru.setCharacteristics(List.of(fromEis, extra));
        SpecPositionDto noKtru = position(2, 21, "Услуга", "61.10.30.190", null);
        noKtru.setCharacteristics(List.of(characteristic(3, false)));

        var results = new KtruExtraCharacteristicsEvaluator()
                .evaluate(notice(withKtru, noKtru), rule, new RuleVersion(), new RuleEvalParams(List.of()));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDetail()).isEqualTo("pos=1;id=20;char=2");
    }

    @Test
    void noDescriptionWhenNoKtruAndNoCharacteristics() {
        Rule rule = rule("R07_NO_DESCRIPTION", RuleTemplateType.NON_KTRU_NO_DESCRIPTION);
        SpecPositionDto empty = position(1, 30, "Журнал", "18.12.14.000", null);
        empty.setCharacteristics(List.of());
        SpecPositionDto described = position(2, 31, "Лента", "22.29.21.000", null);
        described.setCharacteristics(List.of(characteristic(9, false)));
        SpecPositionDto ktru = position(3, 32, "Бумага", "17.12.14.110", "17.12.14.110-00000003");

        var results = new NonKtruNoDescriptionEvaluator()
                .evaluate(notice(empty, described, ktru), rule, new RuleVersion(), new RuleEvalParams(List.of()));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDetail()).isEqualTo("pos=1;id=30");
    }

    @Test
    void activeVersionOnPicksHighestVersionNumber() {
        Rule rule = rule("R03_PP1875", RuleTemplateType.OKPD2_FOREIGN_BAN);
        RuleVersion first = activeVersion(1, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31));
        RuleVersion second = activeVersion(3, LocalDate.of(2025, 1, 1), null);
        RuleVersion third = activeVersion(2, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        rule.setVersions(new ArrayList<>(List.of(first, second, third)));

        RuleVersion chosen = NoticeRuleEvaluator.activeVersionOn(rule, LocalDate.of(2025, 6, 1));
        assertThat(chosen).isSameAs(second);
        assertThat(chosen.getVersionNumber()).isEqualTo(3);
    }

    private static RuleVersion activeVersion(int number, LocalDate from, LocalDate to) {
        RuleVersion version = new RuleVersion();
        version.setVersionNumber(number);
        version.setStatus(RuleVersionStatus.ACTIVE);
        version.setValidFrom(from);
        version.setValidTo(to);
        return version;
    }

    private static NoticeDto notice(SpecPositionDto... items) {
        NoticeDto notice = new NoticeDto();
        notice.setNumber("0173100017025000020");
        notice.setPublishedAt(LocalDateTime.of(2025, 3, 1, 12, 0));
        notice.setItems(List.of(items));
        return notice;
    }

    private static SpecPositionDto position(int pos, int id, String name, String okpd2, String ktru) {
        SpecPositionDto item = new SpecPositionDto();
        item.setId(id);
        item.setPos(pos);
        item.setName(name);
        item.setOkpd2(okpd2);
        item.setKtru(ktru);
        return item;
    }

    private static KtruCharacteristicDto characteristic(int id, boolean fromEis) {
        KtruCharacteristicDto characteristic = new KtruCharacteristicDto();
        characteristic.setId(id);
        characteristic.setName("Характеристика " + id);
        characteristic.setFromEis(fromEis);
        return characteristic;
    }

    private static Rule rule(String code, RuleTemplateType type) {
        Rule rule = new Rule();
        rule.setCode(code);
        rule.setName(code);
        rule.setTemplateType(type);
        rule.setObjectType(RuleObjectType.NOTICE);
        rule.setSeverity(Severity.ERROR);
        rule.setExplanationTemplate("Позиция {pos} ({name}): ОКПД2 {okpd2} КТРУ {ktru}");
        return rule;
    }

    private static RuleEvalParams params(RuleParameter parameter) {
        return new RuleEvalParams(List.of(parameter));
    }

    private static RuleParameter listParam(ReferenceList list) {
        RuleParameter parameter = new RuleParameter();
        parameter.setCode("okpdList");
        parameter.setReferenceList(list);
        return parameter;
    }
}
