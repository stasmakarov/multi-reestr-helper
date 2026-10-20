package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "rule-parameters/:id", layout = MainView.class)
@ViewController(id = "mtr_RuleParameter.detail")
@ViewDescriptor(path = "rule-parameter-detail-view.xml")
@EditedEntityContainer("ruleParameterDc")
@DialogMode(width = "40em")
public class RuleParameterDetailView extends StandardDetailView<RuleParameter> {
}
