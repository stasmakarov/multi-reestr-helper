package com.digitilius.multireestr.view.contractterm;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "contract-terms", layout = MainView.class)
@ViewController(id = "mtr_ContractTerm.list")
@ViewDescriptor(path = "contract-term-list-view.xml")
@LookupComponent("contractTermsDataGrid")
@DialogMode(width = "64em")
public class ContractTermListView extends StandardListView<ContractTerm> {
}
