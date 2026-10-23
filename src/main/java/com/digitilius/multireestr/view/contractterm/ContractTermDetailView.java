package com.digitilius.multireestr.view.contractterm;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.contract.ContractLookupService;
import com.digitilius.multireestr.manual.ContractTermDueDateService;
import com.digitilius.multireestr.view.contractlookup.ContractLookupDialog;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
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
    private TypedDatePicker<LocalDate> dueDateField;
    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private InstanceContainer<ContractTerm> contractTermDc;

    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private ContractLookupService contractLookupService;
    @Autowired
    private ContractTermDueDateService dueDateService;

    private Integer initialContractLegacyId;

    public void setInitialContractLegacyId(Integer initialContractLegacyId) {
        this.initialContractLegacyId = initialContractLegacyId;
    }

    @Subscribe
    public void onInitEntity(final InitEntityEvent<ContractTerm> event) {
        if (initialContractLegacyId != null) {
            event.getEntity().setContractLegacyId(initialContractLegacyId);
            applyContract(contractLookupService.findById(initialContractLegacyId).orElse(null), event.getEntity());
        }
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        refreshDueDate();
    }

    @Subscribe(id = "contractTermDc", target = Target.DATA_CONTAINER)
    public void onContractTermDcItemPropertyChange(
            final InstanceContainer.ItemPropertyChangeEvent<ContractTerm> event
    ) {
        if ("contractLegacyId".equals(event.getProperty())) {
            Integer id = getEditedEntity().getContractLegacyId();
            contractLookupService.findById(id).ifPresent(contract -> applyContract(contract, getEditedEntity()));
        }
        refreshDueDate();
    }

    @Subscribe("findContractButton")
    public void onFindContractButtonClick(final ClickEvent<JmixButton> event) {
        var window = dialogWindows.view(this, ContractLookupDialog.class).build();
        window.getView().setSelectedHandler(contract -> applyContract(contract, getEditedEntity()));
        window.open();
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        ContractTerm term = getEditedEntity();
        boolean fixed = term.getFixedDate() != null;
        boolean formula = ContractTermDueDateService.formulaReady(term.getTerm());
        boolean partial = formulaPartial(term.getTerm());
        if (fixed && formula) {
            event.getErrors().add(messageBundle.getMessage("validation.xor"));
        } else if (!fixed && !formula) {
            event.getErrors().add(messageBundle.getMessage(
                    partial ? "validation.incompleteFormula" : "validation.requiredTerm"));
        }
    }

    private void applyContract(ContractDto contract, ContractTerm term) {
        if (contract == null || term == null) {
            return;
        }
        term.setContractLegacyId(contract.getId());
        term.setContractNumber(contract.getNumber());
        term.setPurchaseNumber(contract.getPurchaseNumber());
    }

    private void refreshDueDate() {
        dueDateField.setValue(dueDateService.calculate(getEditedEntity()).orElse(null));
    }

    private static boolean formulaPartial(DeadlineSpec spec) {
        if (spec == null) {
            return false;
        }
        return spec.getAnchor() != null
                || spec.getOffsetValue() != null
                || spec.getUnit() != null
                || spec.getShiftRule() != null
                || spec.getTarget() != null
                || spec.getCompareOperator() != null
                || spec.getWarnBeforeDays() != null;
    }
}
