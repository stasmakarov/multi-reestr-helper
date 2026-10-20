package com.digitilius.multireestr.view.referencelist;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "reference-lists", layout = MainView.class)
@ViewController(id = "mtr_ReferenceList.list")
@ViewDescriptor(path = "reference-list-list-view.xml")
@LookupComponent("referenceListsDataGrid")
@DialogMode(width = "64em")
public class ReferenceListListView extends StandardListView<ReferenceList> {
}
