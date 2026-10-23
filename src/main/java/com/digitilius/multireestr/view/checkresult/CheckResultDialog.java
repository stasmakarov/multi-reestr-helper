package com.digitilius.multireestr.view.checkresult;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.rules.NoticeCheckOutcome;
import com.digitilius.multireestr.rules.OnDemandCheckService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController(id = "mtr_CheckResultDialog")
@ViewDescriptor(path = "check-result-dialog.xml")
@DialogMode(width = "72em", height = "auto")
public class CheckResultDialog extends StandardView {

    @ViewComponent
    private CollectionContainer<CheckResultDto> resultsDc;
    @ViewComponent
    private Span emptyHint;
    @ViewComponent
    private JmixButton saveButton;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private OnDemandCheckService onDemandCheckService;
    @Autowired
    private Notifications notifications;

    private NoticeDto notice;
    private CheckRun checkRun;
    private NoticeCheckOutcome outcome;

    public void setCheck(NoticeDto notice, OnDemandCheckService.OnDemandCheckResult result) {
        this.notice = notice;
        this.checkRun = result.checkRun();
        this.outcome = result.outcome();
        resultsDc.setItems(outcome.hits());
        emptyHint.setVisible(outcome.hits().isEmpty());
    }

    @Subscribe("saveButton")
    public void onSaveButtonClick(final ClickEvent<JmixButton> event) {
        saveButton.setEnabled(false);
        try {
            onDemandCheckService.saveToRegistry(checkRun, notice, outcome);
            notifications.create(messageBundle.getMessage("checkResultDialog.saved")).show();
            close(StandardOutcome.SAVE);
        } catch (RuntimeException e) {
            saveButton.setEnabled(true);
            notifications.create(e.getMessage() == null ? "" : e.getMessage()).show();
        }
    }
}
