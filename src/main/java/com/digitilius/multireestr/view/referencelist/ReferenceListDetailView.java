package com.digitilius.multireestr.view.referencelist;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.rules.ReferenceListExcelService;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Route(value = "reference-lists/:id", layout = MainView.class)
@ViewController(id = "mtr_ReferenceList.detail")
@ViewDescriptor(path = "reference-list-detail-view.xml")
@EditedEntityContainer("referenceListDc")
public class ReferenceListDetailView extends StandardDetailView<ReferenceList> {

    @ViewComponent
    private InstanceLoader<ReferenceList> referenceListDl;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Downloader downloader;
    @Autowired
    private ReferenceListExcelService excelService;

    @Subscribe("importExcelButton")
    public void onImportExcelButtonClick(final ClickEvent<JmixButton> event) {
        if (entityStates.isNew(getEditedEntity())) {
            notifications.create(messageBundle.getMessage("saveListFirst")).show();
            return;
        }
        var window = dialogWindows.view(this, ReferenceListExcelImportDialog.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        referenceListDl.load();
                    }
                })
                .build();
        window.getView().setList(getEditedEntity());
        window.open();
    }

    @Subscribe("exportExcelButton")
    public void onExportExcelButtonClick(final ClickEvent<JmixButton> event) {
        ReferenceList list = getEditedEntity();
        if (list.getItems() == null && list.getId() != null) {
            list = referenceListDl.getContainer().getItem();
        }
        byte[] bytes = excelService.export(list);
        String fileName = list.getCode() + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        downloader.download(bytes, fileName, DownloadFormat.XLSX);
    }
}
