package com.digitilius.multireestr.view.violation;

import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import com.digitilius.multireestr.rules.OnDemandCheckService;
import com.digitilius.multireestr.view.main.MainView;
import com.digitilius.multireestr.view.notice.NoticeDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "violations/:id", layout = MainView.class)
@ViewController(id = "mtr_Violation.detail")
@ViewDescriptor(path = "violation-detail-view.xml")
@EditedEntityContainer("violationDc")
public class ViolationDetailView extends StandardDetailView<Violation> {

    @ViewComponent
    private JmixButton acknowledgeButton;
    @ViewComponent
    private JmixButton falsePositiveButton;
    @ViewComponent
    private JmixButton openObjectButton;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private OnDemandCheckService onDemandCheckService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private ViewNavigators viewNavigators;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        refreshActions();
    }

    @Subscribe("acknowledgeButton")
    public void onAcknowledgeButtonClick(final ClickEvent<JmixButton> event) {
        try {
            Violation saved = onDemandCheckService.acknowledge(getEditedEntity());
            getEditedEntityContainer().setItem(saved);
            notifications.create(messageBundle.getMessage("acknowledgeDone")).show();
            refreshActions();
        } catch (RuntimeException e) {
            notifications.create(e.getMessage() == null ? "" : e.getMessage()).show();
        }
    }

    @Subscribe("falsePositiveButton")
    public void onFalsePositiveButtonClick(final ClickEvent<JmixButton> event) {
        try {
            Violation saved = onDemandCheckService.markFalsePositive(getEditedEntity());
            getEditedEntityContainer().setItem(saved);
            notifications.create(messageBundle.getMessage("falsePositiveDone")).show();
            refreshActions();
        } catch (RuntimeException e) {
            notifications.create(e.getMessage() == null ? "" : e.getMessage()).show();
        }
    }

    @Subscribe("openObjectButton")
    public void onOpenObjectButtonClick(final ClickEvent<JmixButton> event) {
        Violation violation = getEditedEntity();
        if (violation.getObjectType() != RuleObjectType.NOTICE || violation.getObjectKey() == null) {
            notifications.create(messageBundle.getMessage("openObjectUnavailable")).show();
            return;
        }
        viewNavigators.view(this, NoticeDetailView.class)
                .withRouteParameters(new RouteParameters("number", violation.getObjectKey()))
                .navigate();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    private void refreshActions() {
        ViolationStatus status = getEditedEntity().getStatus();
        acknowledgeButton.setEnabled(status == ViolationStatus.NEW);
        falsePositiveButton.setEnabled(status != ViolationStatus.FIXED);
        openObjectButton.setEnabled(getEditedEntity().getObjectType() == RuleObjectType.NOTICE);
    }
}
