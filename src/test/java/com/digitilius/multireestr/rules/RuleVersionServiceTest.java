package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class RuleVersionServiceTest {

    @Autowired
    RuleVersionService ruleVersionService;
    @Autowired
    DataManager dataManager;

    @Test
    void copyToDraftAndRejectOverlappingActivate() {
        RuleVersion source = dataManager.load(RuleVersion.class)
                .query("select e from mtr_RuleVersion e where e.rule.code = :code")
                .parameter("code", "R07_NO_DESCRIPTION")
                .one();
        RuleVersion draft = ruleVersionService.copyToDraft(source);
        try {
            assertThat(draft.getStatus()).isEqualTo(RuleVersionStatus.DRAFT);
            assertThat(draft.getVersionNumber()).isEqualTo(source.getVersionNumber() + 1);
            assertThatThrownBy(() -> ruleVersionService.activate(draft))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("пересекается");
        } finally {
            List<RuleParameter> parameters = dataManager.load(RuleParameter.class)
                    .query("select e from mtr_RuleParameter e where e.ruleVersion.id = :id")
                    .parameter("id", draft.getId())
                    .list();
            SaveContext saveContext = new SaveContext();
            for (RuleParameter parameter : parameters) {
                saveContext.removing(parameter);
            }
            dataManager.load(RuleVersion.class).id(draft.getId()).optional()
                    .ifPresent(saveContext::removing);
            dataManager.save(saveContext);
        }
    }
}
