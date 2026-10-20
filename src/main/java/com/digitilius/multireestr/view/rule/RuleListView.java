package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "rules", layout = MainView.class)
@ViewController(id = "mtr_Rule.list")
@ViewDescriptor(path = "rule-list-view.xml")
@LookupComponent("rulesDataGrid")
@DialogMode(width = "64em")
public class RuleListView extends StandardListView<Rule> {
}
