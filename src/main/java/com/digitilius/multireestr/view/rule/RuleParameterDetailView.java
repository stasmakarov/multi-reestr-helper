package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.ParameterType;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.time.LocalDate;

@Route(value = "rule-parameters/:id", layout = MainView.class)
@ViewController(id = "mtr_RuleParameter.detail")
@ViewDescriptor(path = "rule-parameter-detail-view.xml")
@EditedEntityContainer("ruleParameterDc")
@DialogMode(width = "40em")
public class RuleParameterDetailView extends StandardDetailView<RuleParameter> {

    @ViewComponent
    private JmixComboBox<ParameterType> paramTypeField;
    @ViewComponent
    private TypedTextField<String> valueField;
    @ViewComponent
    private JmixCheckbox booleanValueField;
    @ViewComponent
    private TypedDatePicker<LocalDate> dateValueField;
    @ViewComponent
    private EntityComboBox<?> referenceListField;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        applyEditors(getEditedEntity().getParamType());
        if (getEditedEntity().getParamType() == ParameterType.BOOLEAN) {
            booleanValueField.setValue(Boolean.parseBoolean(getEditedEntity().getValue()));
        }
        if (getEditedEntity().getParamType() == ParameterType.DATE && getEditedEntity().getValue() != null
                && !getEditedEntity().getValue().isBlank()) {
            dateValueField.setTypedValue(LocalDate.parse(getEditedEntity().getValue()));
        }
    }

    @Subscribe("paramTypeField")
    public void onParamTypeFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<JmixComboBox<ParameterType>, ParameterType> event
    ) {
        applyEditors(event.getValue());
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        ParameterType type = getEditedEntity().getParamType();
        if (type == ParameterType.BOOLEAN) {
            getEditedEntity().setValue(Boolean.TRUE.equals(booleanValueField.getValue()) ? "true" : "false");
        } else if (type == ParameterType.DATE) {
            LocalDate date = dateValueField.getTypedValue();
            getEditedEntity().setValue(date == null ? null : date.toString());
        }
    }

    private void applyEditors(ParameterType type) {
        boolean bool = type == ParameterType.BOOLEAN;
        boolean date = type == ParameterType.DATE;
        boolean list = type == ParameterType.LIST;
        valueField.setVisible(!bool && !date && !list);
        booleanValueField.setVisible(bool);
        dateValueField.setVisible(date);
        referenceListField.setVisible(list);
        if (paramTypeField != null && type == null) {
            valueField.setVisible(true);
        }
    }
}
