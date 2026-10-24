package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.entity.User;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class RulesLiquibaseCoverageTest {

    static final int REFERENCE_LIST_COUNT = 2;
    static final int REFERENCE_LIST_ITEM_COUNT = 15;
    static final int RULE_COUNT = 5;
    static final int ACTIVE_VERSION_COUNT = 5;
    static final int PARAMETER_COUNT = 2;

    @Autowired
    DataManager dataManager;

    @Autowired
    NoticeRuleEvaluator noticeRuleEvaluator;

    @Test
    void seedCounts() {
        assertThat(dataManager.load(ReferenceList.class).all().list()).hasSize(REFERENCE_LIST_COUNT);
        assertThat(dataManager.load(ReferenceListItem.class).all().list()).hasSize(REFERENCE_LIST_ITEM_COUNT);
        List<Rule> rules = dataManager.load(Rule.class).all().list();
        assertThat(rules).extracting(Rule::getCode)
                .containsExactlyInAnyOrder(
                        "R03_PP1875", "R05_KTRU_CHARS", "R07_NO_DESCRIPTION",
                        "R09_DELIVERY", "R10_ACCEPTANCE");
        assertThat(rules).hasSize(RULE_COUNT);
        List<RuleVersion> versions = dataManager.load(RuleVersion.class).all().list();
        assertThat(versions).hasSize(ACTIVE_VERSION_COUNT);
        assertThat(versions).allMatch(version -> version.getStatus() == RuleVersionStatus.ACTIVE);
        assertThat(dataManager.load(RuleParameter.class).all().list()).hasSize(PARAMETER_COUNT);
    }

    @Test
    void demoUsersLoadedWhenContextUnfiltered() {
        assertThat(dataManager.load(User.class)
                .query("select e from mtr_User e where e.username in :names")
                .parameter("names", List.of("specialist", "rule-admin"))
                .list())
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("specialist", "rule-admin");
    }

    @Test
    void demoUsersChangesetHasDemoContext() throws Exception {
        String xml = new String(getClass().getResourceAsStream(
                "/com/digitilius/multireestr/liquibase/changelog/052-users-roles.xml").readAllBytes());
        assertThat(xml).contains("context=\"demo\"");
        assertThat(xml).doesNotContain("<changeSet id=\"1\" author=\"multi-reestr\">");
    }

    @Test
    void noticeEvaluatorUsesSeededOkpdList() {
        com.digitilius.multireestr.entity.legacy.NoticeDto notice =
                new com.digitilius.multireestr.entity.legacy.NoticeDto();
        notice.setNumber("0173100017026000008");
        notice.setPublishedAt(java.time.LocalDateTime.of(2026, 2, 1, 10, 0));
        com.digitilius.multireestr.entity.legacy.SpecPositionDto item =
                new com.digitilius.multireestr.entity.legacy.SpecPositionDto();
        item.setId(21);
        item.setPos(1);
        item.setName("Батареи");
        item.setOkpd2("27.20.23.130");
        notice.setItems(List.of(item));

        var hits = noticeRuleEvaluator.evaluate(notice);
        assertThat(hits).extracting(com.digitilius.multireestr.entity.legacy.CheckResultDto::getRuleCode)
                .contains("R03_PP1875", "R07_NO_DESCRIPTION")
                .doesNotContain("R05_KTRU_CHARS");
    }
}
