package com.digitilius.multireestr.view.calendarday;

import com.digitilius.multireestr.calendar.CalendarYearImportService;
import com.digitilius.multireestr.calendar.CalendarYearPreview;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Year;

@ViewController(id = "mtr_CalendarYearLoadDialog")
@ViewDescriptor(path = "calendar-year-load-dialog.xml")
@DialogMode(width = "40em")
public class CalendarYearLoadDialog extends StandardView {

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private JmixIntegerField yearField;
    @ViewComponent
    private JmixCheckbox fromFileField;
    @ViewComponent
    private VerticalLayout uploadBox;
    @ViewComponent
    private JmixTextArea summaryField;
    @ViewComponent
    private JmixButton importButton;

    @Autowired
    private CalendarYearImportService importService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;

    private final MemoryBuffer memoryBuffer = new MemoryBuffer();
    private byte[] uploadedXml;
    private CalendarYearPreview preview;

    @Subscribe
    public void onInit(final InitEvent event) {
        yearField.setValue(Year.now().getValue());
        importButton.setEnabled(false);
        Upload upload = uiComponents.create(Upload.class);
        upload.setAcceptedFileTypes(".xml", "text/xml", "application/xml");
        upload.setMaxFiles(1);
        upload.setReceiver(memoryBuffer);
        upload.setWidthFull();
        upload.addSucceededListener(succeededEvent -> {
            try {
                uploadedXml = memoryBuffer.getInputStream().readAllBytes();
                preview = null;
                importButton.setEnabled(false);
            } catch (IOException e) {
                showError(e.getMessage());
            }
        });
        Div uploadWrapper = uiComponents.create(Div.class);
        uploadWrapper.setWidthFull();
        uploadWrapper.add(upload);
        uploadBox.add(uploadWrapper);
    }

    @Subscribe("fromFileField")
    public void onFromFileFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event
    ) {
        boolean fromFile = Boolean.TRUE.equals(event.getValue());
        uploadBox.setVisible(fromFile);
        preview = null;
        importButton.setEnabled(false);
    }

    @Subscribe("previewButton")
    public void onPreviewButtonClick(final ClickEvent<JmixButton> event) {
        try {
            preview = buildPreview();
            summaryField.setValue(formatSummary(preview));
            importButton.setEnabled(true);
        } catch (RuntimeException e) {
            preview = null;
            importButton.setEnabled(false);
            showError(e.getMessage());
        }
    }

    @Subscribe("importButton")
    public void onImportButtonClick(final ClickEvent<JmixButton> event) {
        if (preview == null) {
            showError(messageBundle.getMessage("loadYearDialog.needPreview"));
            return;
        }
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("loadYearDialog.confirmTitle"))
                .withText(formatSummary(preview))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> replaceYear()),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    private void replaceYear() {
        try {
            importService.replaceYear(preview);
            notifications.create(messageBundle.getMessage("loadYearDialog.success"))
                    .withThemeVariant(NotificationVariant.SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
            close(StandardOutcome.SAVE);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private CalendarYearPreview buildPreview() {
        Integer year = yearField.getValue();
        if (year == null) {
            throw new IllegalArgumentException(messageBundle.getMessage("loadYearDialog.yearRequired"));
        }
        if (Boolean.TRUE.equals(fromFileField.getValue())) {
            if (uploadedXml == null || uploadedXml.length == 0) {
                throw new IllegalArgumentException(messageBundle.getMessage("loadYearDialog.fileRequired"));
            }
            return importService.preview(year, new ByteArrayInputStream(uploadedXml));
        }
        return importService.previewFromUrl(year);
    }

    private String formatSummary(CalendarYearPreview preview) {
        return messageBundle.getMessage("loadYearDialog.summary").formatted(
                preview.year(),
                preview.holidays(),
                preview.weekends(),
                preview.shortened(),
                preview.workingTransfers(),
                preview.existingCount(),
                preview.existingManualCount()
        );
    }

    private void showError(String message) {
        notifications.create(message == null ? "" : message)
                .withThemeVariant(NotificationVariant.ERROR)
                .withPosition(Notification.Position.TOP_END)
                .show();
    }
}
