package com.digitilius.multireestr.view.contractlookup;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.contract.ContractLookupService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.data.selection.SelectionEvent;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.function.Consumer;

@ViewController(id = "mtr_ContractLookupDialog")
@ViewDescriptor(path = "contract-lookup-dialog.xml")
@DialogMode(width = "64em")
public class ContractLookupDialog extends StandardView {

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private CollectionContainer<ContractDto> contractsDc;
    @ViewComponent
    private TypedTextField<String> queryField;
    @ViewComponent
    private DataGrid<ContractDto> contractsGrid;
    @ViewComponent
    private JmixButton selectButton;

    @Autowired
    private ContractLookupService contractLookupService;
    @Autowired
    private Notifications notifications;

    private ContractDto selected;
    private Consumer<ContractDto> onSelect;

    public void setOnSelect(Consumer<ContractDto> onSelect) {
        this.onSelect = onSelect;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        selectButton.setEnabled(false);
    }

    @Subscribe("searchButton")
    public void onSearchButtonClick(final ClickEvent<JmixButton> event) {
        try {
            List<ContractDto> found = contractLookupService.search(queryField.getTypedValue());
            contractsDc.setItems(found);
            selected = null;
            selectButton.setEnabled(false);
        } catch (DataAccessException e) {
            notifications.create(messageBundle.getMessage("legacyConnectionError")).show();
        }
    }

    @Subscribe("contractsGrid")
    public void onContractsGridSelection(final SelectionEvent<DataGrid<ContractDto>, ContractDto> event) {
        selected = event.getFirstSelectedItem().orElse(null);
        selectButton.setEnabled(selected != null);
    }

    @Subscribe("selectButton")
    public void onSelectButtonClick(final ClickEvent<JmixButton> event) {
        if (selected == null) {
            return;
        }
        if (onSelect != null) {
            onSelect.accept(selected);
        }
        closeWithDefaultAction();
    }
}
