package com.digitilius.multireestr.view.contractterm;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.manual.ContractTermDueDateService;
import com.digitilius.multireestr.manual.ContractTermRules;
import com.digitilius.multireestr.view.contractlookup.ContractLookupDialog;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "contract-terms/:id", layout = MainView.class)
@ViewController(id = "mtr_ContractTerm.detail")
@ViewDescriptor(path = "contract-term-detail-view.xml")
@EditedEntityContainer("contractTermDc")
public class ContractTermDetailView extends StandardDetailView<ContractTerm> {

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private TypedDatePicker<LocalDate> dueDateField;

    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Notifications notifications;
    @Autowired
    private ContractTermDueDateService contractTermDueDateService;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        refreshDueDate();
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        try {
            ContractTermRules.validateXor(getEditedEntity());
        } catch (IllegalArgumentException e) {
            event.getErrors().add(e.getMessage());
        }
    }

    @Subscribe("findContractBtn")
    public void onFindContractBtnClick(final ClickEvent<JmixButton> event) {
        var window = dialogWindows.view(this, ContractLookupDialog.class).build();
        window.getView().setOnSelect(contract -> {
            ContractTerm term = getEditedEntity();
            term.setContractLegacyId(contract.getId());
            term.setContractNumber(contract.getNumber());
            term.setPurchaseNumber(contract.getPurchaseNumber());
            refreshDueDate();
        });
        window.open();
    }

    @Subscribe("fixedDateField")
    public void onFixedDateFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<?, ?> event
    ) {
        refreshDueDate();
    }

    @Subscribe("termAnchorField")
    public void onTermAnchorFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<?, ?> event
    ) {
        refreshDueDate();
    }

    @Subscribe("termOffsetValueField")
    public void onTermOffsetValueFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<?, ?> event
    ) {
        refreshDueDate();
    }

    @Subscribe("termUnitField")
    public void onTermUnitFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<?, ?> event
    ) {
        refreshDueDate();
    }

    @Subscribe("purchaseNumberField")
    public void onPurchaseNumberFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<?, ?> event
    ) {
        refreshDueDate();
    }

    @Subscribe("contractLegacyIdField")
    public void onContractLegacyIdFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<?, ?> event
    ) {
        refreshDueDate();
    }

    private void refreshDueDate() {
        try {
            dueDateField.setTypedValue(contractTermDueDateService.dueDate(getEditedEntity()).orElse(null));
        } catch (RuntimeException e) {
            dueDateField.setTypedValue(null);
            String message = e.getMessage() == null ? messageBundle.getMessage("dueDateError") : e.getMessage();
            notifications.create(message).show();
        }
    }
}
