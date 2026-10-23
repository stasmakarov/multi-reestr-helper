package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.rules.RuleVersionService;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "rule-versions/:id", layout = MainView.class)
@ViewController(id = "mtr_RuleVersion.detail")
@ViewDescriptor(path = "rule-version-detail-view.xml")
@EditedEntityContainer("ruleVersionDc")
@DialogMode(width = "64em", height = "auto")
public class RuleVersionDetailView extends StandardDetailView<RuleVersion> {

    @ViewComponent
    private FormLayout deadlineBox;
    @ViewComponent
    private JmixButton activateButton;
    @ViewComponent
    private JmixButton archiveButton;
    @ViewComponent
    private DataGrid<?> parametersDataGrid;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private RuleVersionService ruleVersionService;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<RuleVersion> event) {
        RuleVersion version = event.getEntity();
        version.setStatus(RuleVersionStatus.DRAFT);
        if (version.getValidFrom() == null) {
            version.setValidFrom(LocalDate.now());
        }
        Rule rule = version.getRule();
        int next = 1;
        if (rule != null && rule.getVersions() != null) {
            for (RuleVersion existing : rule.getVersions()) {
                if (existing.getVersionNumber() != null) {
                    next = Math.max(next, existing.getVersionNumber() + 1);
                }
            }
        }
        version.setVersionNumber(next);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        RuleVersion version = getEditedEntity();
        RuleTemplateType templateType = version.getRule() == null ? null : version.getRule().getTemplateType();
        boolean deadline = templateType == RuleTemplateType.DEADLINE
                || templateType == RuleTemplateType.LATE_DELIVERY_PENALTY;
        deadlineBox.setVisible(deadline);
        boolean locked = version.getStatus() == RuleVersionStatus.ACTIVE
                || version.getStatus() == RuleVersionStatus.ARCHIVED;
        if (locked) {
            setReadOnly(true);
        }
        boolean persisted = !entityStates.isNew(version);
        activateButton.setVisible(persisted && version.getStatus() == RuleVersionStatus.DRAFT);
        archiveButton.setVisible(persisted && version.getStatus() == RuleVersionStatus.ACTIVE);
        parametersDataGrid.setEnabled(!locked);
    }

    @Subscribe("activateButton")
    public void onActivateButtonClick(final ClickEvent<JmixButton> event) {
        try {
            ruleVersionService.activate(getEditedEntity());
            notifications.create(messageBundle.getMessage("versionActivated")).show();
            closeWithDiscard();
        } catch (RuntimeException e) {
            notifications.create(e.getMessage()).show();
        }
    }

    @Subscribe("archiveButton")
    public void onArchiveButtonClick(final ClickEvent<JmixButton> event) {
        try {
            ruleVersionService.archive(getEditedEntity());
            notifications.create(messageBundle.getMessage("versionArchived")).show();
            closeWithDiscard();
        } catch (RuntimeException e) {
            notifications.create(e.getMessage()).show();
        }
    }
}
