package com.digitilius.multireestr.view.checkresult;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.rules.CheckHitFingerprints;
import com.digitilius.multireestr.rules.NoticeCheckOutcome;
import com.digitilius.multireestr.rules.OnDemandCheckService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ViewController(id = "mtr_CheckResultDialog")
@ViewDescriptor(path = "check-result-dialog.xml")
@DialogMode(width = "72em", height = "auto", maxHeight = "90%", resizable = true)
public class CheckResultDialog extends StandardView {

    @ViewComponent
    private CollectionContainer<CheckResultDto> resultsDc;
    @ViewComponent
    private DataGrid<CheckResultDto> resultsDataGrid;
    @ViewComponent
    private Span emptyHint;
    @ViewComponent
    private Span selectHint;
    @ViewComponent
    private FormLayout hitDetails;
    @ViewComponent
    private TypedTextField<String> ruleField;
    @ViewComponent
    private TypedTextField<String> severityField;
    @ViewComponent
    private JmixTextArea messageField;
    @ViewComponent
    private JmixTextArea detailField;
    @ViewComponent
    private JmixTextArea legalRefField;
    @ViewComponent
    private JmixButton saveButton;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private OnDemandCheckService onDemandCheckService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;

    private NoticeDto notice;
    private CheckRun checkRun;
    private NoticeCheckOutcome outcome;
    private List<CheckResultDto> shownHits = List.of();

    public void setCheck(NoticeDto notice, OnDemandCheckService.OnDemandCheckResult result) {
        this.notice = notice;
        this.checkRun = result.checkRun();
        this.outcome = result.outcome();
        this.shownHits = CheckHitFingerprints.uniqueHits(outcome.hits());
        resultsDc.setItems(shownHits);
        emptyHint.setVisible(shownHits.isEmpty());
        selectInitialHit();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        selectInitialHit();
    }

    @Supply(to = "resultsDataGrid.detail", subject = "renderer")
    private Renderer<CheckResultDto> detailRenderer() {
        return new TextRenderer<>(hit -> describe(hit == null ? null : hit.getDetail()));
    }

    @Subscribe("resultsDataGrid")
    public void onResultsDataGridSelection(
            final SelectionEvent<DataGrid<CheckResultDto>, CheckResultDto> event
    ) {
        showHit(event.getFirstSelectedItem().orElse(null));
    }

    @Subscribe("resultsDataGrid")
    public void onResultsDataGridItemDoubleClick(final ItemDoubleClickEvent<CheckResultDto> event) {
        CheckResultDto item = event.getItem();
        if (item == null) {
            return;
        }
        resultsDataGrid.select(item);
        showHit(item);
        focusDetails();
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

    private void selectInitialHit() {
        if (shownHits.isEmpty()) {
            showHit(null);
            return;
        }
        CheckResultDto selected = resultsDataGrid.getSingleSelectedItem();
        if (selected == null || !shownHits.contains(selected)) {
            selected = shownHits.get(0);
            resultsDataGrid.select(selected);
        }
        showHit(selected);
    }

    private void showHit(CheckResultDto hit) {
        boolean present = hit != null;
        hitDetails.setVisible(present);
        selectHint.setVisible(!present && !shownHits.isEmpty());
        if (!present) {
            ruleField.setTypedValue("");
            severityField.setTypedValue("");
            messageField.setValue("");
            detailField.setValue("");
            legalRefField.setValue("");
            return;
        }
        ruleField.setTypedValue(ruleLabel(hit));
        severityField.setTypedValue(hit.getSeverity() == null ? "" : messages.getMessage(hit.getSeverity()));
        messageField.setValue(hit.getMessage() == null ? "" : hit.getMessage());
        detailField.setValue(describe(hit.getDetail()));
        legalRefField.setValue(hit.getLegalRef() == null ? "" : hit.getLegalRef());
    }

    private void focusDetails() {
        hitDetails.getElement().executeJs("this.scrollIntoView({block:'nearest'})");
        messageField.focus();
    }

    private String describe(String detail) {
        return CheckHitDetailText.describe(detail, notice, detailPhrases());
    }

    private CheckHitDetailText.Phrases detailPhrases() {
        return new CheckHitDetailText.Phrases(
                messageBundle.getMessage("checkResultDialog.detail.positionNamed"),
                messageBundle.getMessage("checkResultDialog.detail.position"),
                messageBundle.getMessage("checkResultDialog.detail.charValue"),
                messageBundle.getMessage("checkResultDialog.detail.charName"),
                messageBundle.getMessage("checkResultDialog.detail.charNumber"),
                messageBundle.getMessage("checkResultDialog.detail.join")
        );
    }

    private static String ruleLabel(CheckResultDto hit) {
        String code = hit.getRuleCode() == null ? "" : hit.getRuleCode().trim();
        String name = hit.getRuleName() == null ? "" : hit.getRuleName().trim();
        if (code.isEmpty()) {
            return name;
        }
        if (name.isEmpty()) {
            return code;
        }
        return code + " — " + name;
    }
}
