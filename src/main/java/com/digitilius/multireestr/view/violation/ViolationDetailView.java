package com.digitilius.multireestr.view.violation;

import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "violations/:id", layout = MainView.class)
@ViewController(id = "mtr_Violation.detail")
@ViewDescriptor(path = "violation-detail-view.xml")
@EditedEntityContainer("violationDc")
public class ViolationDetailView extends StandardDetailView<Violation> {
}
