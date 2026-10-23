package com.digitilius.multireestr.view.contractlookup;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.contract.ContractLookupService;
import com.vaadin.flow.component.ClickEvent;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
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

import java.util.function.Consumer;

@ViewController(id = "mtr_ContractLookupDialog")
@ViewDescriptor(path = "contract-lookup-dialog.xml")
@DialogMode(width = "56em", height = "auto")
public class ContractLookupDialog extends StandardView {

    @ViewComponent
    private CollectionContainer<ContractDto> contractsDc;
    @ViewComponent
    private DataGrid<ContractDto> contractsDataGrid;
    @ViewComponent
    private TypedTextField<String> queryField;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private ContractLookupService contractLookupService;
    @Autowired
    private Notifications notifications;

    private Consumer<ContractDto> selectedHandler;

    public void setSelectedHandler(Consumer<ContractDto> selectedHandler) {
        this.selectedHandler = selectedHandler;
    }

    @Subscribe("searchButton")
    public void onSearchButtonClick(final ClickEvent<JmixButton> event) {
        contractsDc.setItems(contractLookupService.search(queryField.getValue()));
    }

    @Subscribe("selectButton")
    public void onSelectButtonClick(final ClickEvent<JmixButton> event) {
        ContractDto selected = contractsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create(messageBundle.getMessage("contractLookupDialog.nothingSelected")).show();
            return;
        }
        if (selectedHandler != null) {
            selectedHandler.accept(selected);
        }
        close(StandardOutcome.SAVE);
    }
}
