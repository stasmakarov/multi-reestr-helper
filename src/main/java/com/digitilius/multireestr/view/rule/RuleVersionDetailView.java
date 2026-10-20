package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "rule-versions/:id", layout = MainView.class)
@ViewController(id = "mtr_RuleVersion.detail")
@ViewDescriptor(path = "rule-version-detail-view.xml")
@EditedEntityContainer("ruleVersionDc")
@DialogMode(width = "64em", height = "auto")
public class RuleVersionDetailView extends StandardDetailView<RuleVersion> {
}
