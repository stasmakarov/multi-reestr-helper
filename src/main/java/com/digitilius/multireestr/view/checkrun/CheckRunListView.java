package com.digitilius.multireestr.view.checkrun;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "check-runs", layout = MainView.class)
@ViewController(id = "mtr_CheckRun.list")
@ViewDescriptor(path = "check-run-list-view.xml")
@LookupComponent("checkRunsDataGrid")
@DialogMode(width = "64em")
public class CheckRunListView extends StandardListView<CheckRun> {
}
