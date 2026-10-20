package com.digitilius.multireestr.view.referencelist;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "reference-lists/:id", layout = MainView.class)
@ViewController(id = "mtr_ReferenceList.detail")
@ViewDescriptor(path = "reference-list-detail-view.xml")
@EditedEntityContainer("referenceListDc")
public class ReferenceListDetailView extends StandardDetailView<ReferenceList> {
}
