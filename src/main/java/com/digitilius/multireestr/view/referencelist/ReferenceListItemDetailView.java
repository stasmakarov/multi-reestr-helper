package com.digitilius.multireestr.view.referencelist;

import com.digitilius.multireestr.entity.ReferenceListItem;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "reference-list-items/:id", layout = MainView.class)
@ViewController(id = "mtr_ReferenceListItem.detail")
@ViewDescriptor(path = "reference-list-item-detail-view.xml")
@EditedEntityContainer("referenceListItemDc")
@DialogMode(width = "40em")
public class ReferenceListItemDetailView extends StandardDetailView<ReferenceListItem> {
}
