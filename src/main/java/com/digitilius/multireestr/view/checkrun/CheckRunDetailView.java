package com.digitilius.multireestr.view.checkrun;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "check-runs/:id", layout = MainView.class)
@ViewController(id = "mtr_CheckRun.detail")
@ViewDescriptor(path = "check-run-detail-view.xml")
@EditedEntityContainer("checkRunDc")
public class CheckRunDetailView extends StandardDetailView<CheckRun> {
}
