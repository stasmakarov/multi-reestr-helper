package com.digitilius.multireestr.view.contractterm;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "contract-terms/:id", layout = MainView.class)
@ViewController(id = "mtr_ContractTerm.detail")
@ViewDescriptor(path = "contract-term-detail-view.xml")
@EditedEntityContainer("contractTermDc")
public class ContractTermDetailView extends StandardDetailView<ContractTerm> {
}
