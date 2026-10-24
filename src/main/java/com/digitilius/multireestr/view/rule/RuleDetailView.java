package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.rules.DryRunService;
import com.digitilius.multireestr.rules.RuleVersionService;
import com.digitilius.multireestr.view.dryrun.DryRunView;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

@Route(value = "rules/:id", layout = MainView.class)
@ViewController(id = "mtr_Rule.detail")
@ViewDescriptor(path = "rule-detail-view.xml")
@EditedEntityContainer("ruleDc")
public class RuleDetailView extends StandardDetailView<Rule> {

    @ViewComponent
    private JmixComboBox<?> templateTypeField;
    @ViewComponent
    private DataGrid<RuleVersion> versionsDataGrid;
    @ViewComponent
    private JmixButton dryRunButton;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private RuleVersionService ruleVersionService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private DryRunService dryRunService;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Rule> event) {
        event.getEntity().setEnabled(true);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        templateTypeField.setReadOnly(!entityStates.isNew(getEditedEntity()));
        dryRunButton.setVisible(dryRunService.canDryRun());
    }

    @Subscribe("dryRunButton")
    public void onDryRunButtonClick(final ClickEvent<JmixButton> event) {
        RuleVersion source = versionsDataGrid.getSingleSelectedItem();
        if (source == null || source.getId() == null || source.getStatus() != RuleVersionStatus.DRAFT) {
            notifications.create(messageBundle.getMessage("dryRunNeedsDraft")).show();
            return;
        }
        viewNavigators.view(this, DryRunView.class)
                .withQueryParameters(QueryParameters.of("versionId", source.getId().toString()))
                .navigate();
    }

    @Subscribe("copyVersionButton")
    public void onCopyVersionButtonClick(final ClickEvent<JmixButton> event) {
        if (entityStates.isNew(getEditedEntity())) {
            notifications.create(messageBundle.getMessage("saveRuleFirst")).show();
            return;
        }
        RuleVersion source = versionsDataGrid.getSingleSelectedItem();
        if (source == null) {
            List<RuleVersion> versions = getEditedEntity().getVersions();
            if (versions == null || versions.isEmpty()) {
                notifications.create(messageBundle.getMessage("noVersionToCopy")).show();
                return;
            }
            source = versions.stream()
                    .max(Comparator.comparing(version -> version.getVersionNumber() == null ? 0 : version.getVersionNumber()))
                    .orElse(null);
        }
        if (source == null || source.getId() == null) {
            notifications.create(messageBundle.getMessage("saveRuleFirst")).show();
            return;
        }
        try {
            ruleVersionService.copyToDraft(source);
            closeWithDiscard();
            notifications.create(messageBundle.getMessage("versionCopied")).show();
        } catch (RuntimeException e) {
            notifications.create(e.getMessage()).show();
        }
    }
}
