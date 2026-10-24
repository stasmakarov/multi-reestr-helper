package com.digitilius.multireestr.view.referencelist;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.rules.ReferenceListExcelPreview;
import com.digitilius.multireestr.rules.ReferenceListExcelService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.textarea.JmixTextArea;
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

@ViewController(id = "mtr_ReferenceListExcelImportDialog")
@ViewDescriptor(path = "reference-list-excel-import-dialog.xml")
@DialogMode(width = "40em")
public class ReferenceListExcelImportDialog extends StandardView {

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private VerticalLayout uploadBox;
    @ViewComponent
    private JmixTextArea summaryField;
    @ViewComponent
    private JmixButton importButton;

    @Autowired
    private ReferenceListExcelService excelService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;

    private byte[] uploadedExcel;
    private ReferenceList list;
    private ReferenceListExcelPreview preview;

    public void setList(ReferenceList list) {
        this.list = list;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        importButton.setEnabled(false);
        Upload upload = uiComponents.create(Upload.class);
        upload.setAcceptedFileTypes(
                ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        upload.setMaxFiles(1);
        upload.setUploadHandler(UploadHandler.inMemory((metadata, data) -> {
            uploadedExcel = data;
            preview = null;
            importButton.setEnabled(false);
        }));
        upload.setWidthFull();
        Div uploadWrapper = uiComponents.create(Div.class);
        uploadWrapper.setWidthFull();
        uploadWrapper.add(upload);
        uploadBox.add(uploadWrapper);
    }

    @Subscribe("previewButton")
    public void onPreviewButtonClick(final ClickEvent<JmixButton> event) {
        try {
            if (uploadedExcel == null || uploadedExcel.length == 0) {
                throw new IllegalArgumentException(messageBundle.getMessage("importExcelDialog.fileRequired"));
            }
            preview = excelService.preview(list, new ByteArrayInputStream(uploadedExcel));
            summaryField.setValue(messageBundle.getMessage("importExcelDialog.summary").formatted(
                    preview.itemCount(),
                    preview.existingCount()
            ));
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
            showError(messageBundle.getMessage("importExcelDialog.needPreview"));
            return;
        }
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("importExcelDialog.confirmTitle"))
                .withText(summaryField.getValue())
                .withActions(
                        new DialogAction(DialogAction.Type.YES).withHandler(e -> replace()),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    private void replace() {
        try {
            excelService.replaceItems(list, preview);
            notifications.create(messageBundle.getMessage("importExcelDialog.success"))
                    .withThemeVariant(NotificationVariant.SUCCESS)
                    .show();
            close(StandardOutcome.SAVE);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        notifications.create(message == null ? "" : message)
                .withThemeVariant(NotificationVariant.ERROR)
                .show();
    }
}
