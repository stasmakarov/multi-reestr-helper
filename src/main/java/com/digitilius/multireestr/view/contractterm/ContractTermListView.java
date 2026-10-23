package com.digitilius.multireestr.view.contractterm;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "contract-terms", layout = MainView.class)
@ViewController(id = "mtr_ContractTerm.list")
@ViewDescriptor(path = "contract-term-list-view.xml")
@LookupComponent("contractTermsDataGrid")
@DialogMode(width = "64em")
public class ContractTermListView extends StandardListView<ContractTerm> {

    @ViewComponent
    private CollectionLoader<ContractTerm> contractTermsDl;

    private Integer contractLegacyIdFilter;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        contractLegacyIdFilter = event.getLocation().getQueryParameters()
                .getSingleParameter("contractLegacyId")
                .map(ContractTermListView::parseId)
                .orElse(null);
        super.beforeEnter(event);
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        if (contractLegacyIdFilter != null) {
            contractTermsDl.setQuery(
                    "select e from mtr_ContractTerm e where e.contractLegacyId = :contractLegacyId "
                            + "order by e.contractNumber, e.termType");
            contractTermsDl.setParameter("contractLegacyId", contractLegacyIdFilter);
        }
    }

    private static Integer parseId(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
