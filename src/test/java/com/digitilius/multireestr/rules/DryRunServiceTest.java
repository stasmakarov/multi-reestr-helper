package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.entity.User;
import com.digitilius.multireestr.security.SpecialistRole;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class DryRunServiceTest {

    @Autowired
    DryRunService dryRunService;
    @Autowired
    RuleVersionService ruleVersionService;
    @Autowired
    DataManager dataManager;
    @Autowired
    SystemAuthenticator systemAuthenticator;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void rejectsNonDraftAndTooLongPeriod() {
        RuleVersion active = loadActive("R03_PP1875");
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 2);
        assertThatThrownBy(() -> dryRunService.run(active, from, LocalDate.of(2024, 6, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("черновика");
        RuleVersion draft = ruleVersionService.copyToDraft(active);
        try {
            assertThatThrownBy(() -> dryRunService.run(draft, from, to))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("365");
        } finally {
            removeDraft(draft);
        }
    }

    @Test
    void missingLegacyWritesFailedJournal() {
        RuleVersion draft = ruleVersionService.copyToDraft(loadActive("R03_PP1875"));
        CheckRun run = null;
        try {
            DryRunService.DryRunResult result = dryRunService.run(
                    draft, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            run = result.checkRun();
            assertThat(run.getMode()).isEqualTo(CheckRunMode.DRY_RUN);
            assertThat(run.getStatus()).isEqualTo(CheckRunStatus.FAILED);
            assertThat(run.getErrorMessage()).contains("БД MultiReestr недоступна");
            assertThat(run.getRuleVersion().getId()).isEqualTo(draft.getId());
        } finally {
            removeDraft(draft);
        }
    }

    @Test
    void specialistCannotDryRun() {
        RuleVersion draft = ruleVersionService.copyToDraft(loadActive("R03_PP1875"));
        User specialist = null;
        RoleAssignmentEntity assignment = null;
        try {
            specialist = createUser("dry-spec-" + UUID.randomUUID().toString().substring(0, 8));
            assignment = assignResourceRole(specialist.getUsername(), SpecialistRole.CODE);
            systemAuthenticator.begin(specialist.getUsername());
            try {
                assertThatThrownBy(() -> dryRunService.run(
                        draft, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1)))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Нет права");
            } finally {
                systemAuthenticator.end();
            }
        } finally {
            removeDraft(draft);
            if (assignment != null) {
                dataManager.remove(assignment);
            }
            if (specialist != null) {
                dataManager.remove(specialist);
            }
        }
    }

    private void removeDraft(RuleVersion draft) {
        if (draft == null || draft.getId() == null) {
            return;
        }
        SaveContext saveContext = new SaveContext();
        List<CheckRun> runs = dataManager.load(CheckRun.class)
                .query("select e from mtr_CheckRun e where e.ruleVersion = :version")
                .parameter("version", draft)
                .list();
        for (CheckRun run : runs) {
            saveContext.removing(run);
        }
        List<RuleParameter> parameters = dataManager.load(RuleParameter.class)
                .query("select e from mtr_RuleParameter e where e.ruleVersion = :version")
                .parameter("version", draft)
                .list();
        for (RuleParameter parameter : parameters) {
            saveContext.removing(parameter);
        }
        dataManager.save(saveContext);
        dataManager.remove(dataManager.load(RuleVersion.class).id(draft.getId()).one());
    }

    private RuleVersion loadActive(String code) {
        Rule rule = dataManager.load(Rule.class)
                .query("select e from mtr_Rule e where e.code = :code")
                .parameter("code", code)
                .one();
        return dataManager.load(RuleVersion.class)
                .query("select e from mtr_RuleVersion e where e.rule = :rule and e.status = :status")
                .parameter("rule", rule)
                .parameter("status", RuleVersionStatus.ACTIVE)
                .one();
    }

    private User createUser(String username) {
        User user = dataManager.create(User.class);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("test-passwd"));
        user.setActive(true);
        return dataManager.save(user);
    }

    private RoleAssignmentEntity assignResourceRole(String username, String roleCode) {
        RoleAssignmentEntity assignment = dataManager.create(RoleAssignmentEntity.class);
        assignment.setUsername(username);
        assignment.setRoleCode(roleCode);
        assignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
        return dataManager.save(assignment);
    }
}
