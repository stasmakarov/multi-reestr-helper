package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.MultiReestrApplication;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleEnabled;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {MultiReestrApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
class RuleEnabledViewTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void newRuleDefaultsToYesAndNoCanBeSaved() {
        viewNavigators.view(UiTestUtils.getCurrentView(), RuleListView.class).navigate();

        viewNavigators.detailView(UiTestUtils.getCurrentView(), Rule.class)
                .newEntity()
                .navigate();
        RuleDetailView createView = UiTestUtils.getCurrentView();
        JmixComboBox<RuleEnabled> createField = UiTestUtils.getComponent(createView, "enabledField");
        assertThat(createField.getValue()).isEqualTo(RuleEnabled.YES);
        assertThat(labels(createField)).containsExactlyInAnyOrder("Да", "Нет", "Не задано");
        createView.closeWithDiscard();

        Rule rule = dataManager.load(Rule.class)
                .query("select e from mtr_Rule e where e.code = :code")
                .parameter("code", "R03_PP1875")
                .one();
        RuleEnabled original = rule.getEnabled();
        try {
            viewNavigators.detailView(UiTestUtils.getCurrentView(), Rule.class)
                    .editEntity(rule)
                    .navigate();
            RuleDetailView detail = UiTestUtils.getCurrentView();
            JmixComboBox<RuleEnabled> enabledField = UiTestUtils.getComponent(detail, "enabledField");
            enabledField.setValue(RuleEnabled.NO);
            JmixButton save = UiTestUtils.getComponent(detail, "saveAndCloseButton");
            save.click();

            Rule reloaded = dataManager.load(Rule.class).id(rule.getId()).one();
            assertThat(reloaded.getEnabled()).isEqualTo(RuleEnabled.NO);
        } finally {
            Rule restore = dataManager.load(Rule.class).id(rule.getId()).one();
            restore.setEnabled(original == null ? RuleEnabled.YES : original);
            dataManager.save(restore);
        }
    }

    private static List<String> labels(JmixComboBox<RuleEnabled> field) {
        return field.getGenericDataView().getItems()
                .map(item -> field.getItemLabelGenerator().apply(item))
                .toList();
    }
}
