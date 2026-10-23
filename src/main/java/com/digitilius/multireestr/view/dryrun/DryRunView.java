package com.digitilius.multireestr.view.dryrun;

import com.digitilius.multireestr.entity.DryRunDiff;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.rules.DryRunExcelExporter;
import com.digitilius.multireestr.rules.DryRunService;
import com.digitilius.multireestr.scan.ScanProperties;
import com.digitilius.multireestr.view.main.MainView;
import com.digitilius.multireestr.view.rule.RuleVersionDetailView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Route(value = "dry-run", layout = MainView.class)
@ViewController(id = "mtr_DryRunView")
@ViewDescriptor(path = "dry-run-view.xml")
public class DryRunView extends StandardView {

    @ViewComponent
    private EntityComboBox<RuleVersion> versionField;
    @ViewComponent
    private TypedTextField<String> objectTypeField;
    @ViewComponent
    private TypedDatePicker<LocalDate> periodFromField;
    @ViewComponent
    private TypedDatePicker<LocalDate> periodToField;
    @ViewComponent
    private JmixButton exportButton;
    @ViewComponent
    private JmixButton activateButton;
    @ViewComponent
    private CollectionContainer<CheckResultDto> resultsDc;
    @ViewComponent
    private CollectionContainer<RuleVersion> versionsDc;
    @ViewComponent
    private CollectionLoader<RuleVersion> versionsDl;
    @ViewComponent
    private Span summaryLabel;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private DryRunService dryRunService;
    @Autowired
    private ScanProperties scanProperties;
    @Autowired
    private Clock clock;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Downloader downloader;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Messages messages;

    private String pendingVersionId;
    private DryRunService.DryRunResult lastResult;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        pendingVersionId = event.getLocation().getQueryParameters()
                .getSingleParameter("versionId")
                .orElse(null);
        super.beforeEnter(event);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        versionField.setItemLabelGenerator(this::versionLabel);
        LocalDate today = LocalDate.now(clock);
        int lookback = Math.max(0, scanProperties.getNoticeLookbackDays());
        periodToField.setTypedValue(today);
        periodFromField.setTypedValue(today.minusDays(lookback));
        if (pendingVersionId != null && !pendingVersionId.isBlank()) {
            try {
                UUID id = UUID.fromString(pendingVersionId);
                versionsDl.load();
                RuleVersion version = versionsDc.getItems().stream()
                        .filter(item -> id.equals(item.getId()))
                        .findFirst()
                        .orElse(null);
                if (version == null) {
                    version = dataManager.load(RuleVersion.class).id(id).optional().orElse(null);
                }
                if (version != null) {
                    versionField.setValue(version);
                    fillObjectType(version);
                }
            } catch (IllegalArgumentException ignored) {
                // query is informational
            }
        }
        exportButton.setEnabled(false);
        activateButton.setEnabled(versionField.getValue() != null);
    }

    @Subscribe("versionField")
    public void onVersionFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<EntityComboBox<RuleVersion>, RuleVersion> event
    ) {
        fillObjectType(event.getValue());
        activateButton.setEnabled(event.getValue() != null);
    }

    @Subscribe("runButton")
    public void onRunButtonClick(final ClickEvent<JmixButton> event) {
        lastResult = null;
        resultsDc.setItems(List.of());
        try {
            DryRunService.DryRunResult result = dryRunService.run(
                    versionField.getValue(),
                    periodFromField.getTypedValue(),
                    periodToField.getTypedValue()
            );
            lastResult = result;
            resultsDc.setItems(result.rows());
            summaryLabel.setText(messageBundle.formatMessage(
                    "summary",
                    result.checkRun().getObjectsChecked() == null ? 0 : result.checkRun().getObjectsChecked(),
                    result.count(DryRunDiff.NEW),
                    result.count(DryRunDiff.UNCHANGED),
                    result.count(DryRunDiff.DISAPPEARED)
            ));
            exportButton.setEnabled(!result.rows().isEmpty());
            activateButton.setEnabled(versionField.getValue() != null);
            if (result.truncated()) {
                notifications.create(messageBundle.formatMessage("truncated", scanProperties.getMaxNotices())).show();
            }
            if (result.checkRun().getErrorMessage() != null) {
                notifications.create(result.checkRun().getErrorMessage()).show();
            }
        } catch (RuntimeException e) {
            notifications.create(e.getMessage() == null ? "" : e.getMessage()).show();
        }
    }

    @Subscribe("exportButton")
    public void onExportButtonClick(final ClickEvent<JmixButton> event) {
        if (lastResult == null) {
            return;
        }
        downloader.download(
                DryRunExcelExporter.writeBytes(lastResult.rows()),
                "dry-run.xlsx",
                DownloadFormat.XLSX
        );
    }

    @Subscribe("activateButton")
    public void onActivateButtonClick(final ClickEvent<JmixButton> event) {
        RuleVersion version = versionField.getValue();
        if (version == null || version.getId() == null) {
            return;
        }
        viewNavigators.view(this, RuleVersionDetailView.class)
                .withRouteParameters(new RouteParameters("id", version.getId().toString()))
                .navigate();
    }

    private void fillObjectType(RuleVersion version) {
        if (version == null || version.getRule() == null || version.getRule().getObjectType() == null) {
            objectTypeField.setTypedValue("");
            return;
        }
        objectTypeField.setTypedValue(messages.getMessage(version.getRule().getObjectType()));
    }

    private String versionLabel(RuleVersion version) {
        if (version == null) {
            return "";
        }
        String code = version.getRule() == null ? "" : version.getRule().getCode();
        return code + " v" + version.getVersionNumber();
    }
}
