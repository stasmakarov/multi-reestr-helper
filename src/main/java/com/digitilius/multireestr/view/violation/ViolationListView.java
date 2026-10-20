package com.digitilius.multireestr.view.violation;

import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "violations", layout = MainView.class)
@ViewController(id = "mtr_Violation.list")
@ViewDescriptor(path = "violation-list-view.xml")
@LookupComponent("violationsDataGrid")
@DialogMode(width = "64em")
public class ViolationListView extends StandardListView<Violation> {
}
