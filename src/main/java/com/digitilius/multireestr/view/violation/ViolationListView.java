package com.digitilius.multireestr.view.violation;

import com.digitilius.multireestr.entity.Violation;
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

@Route(value = "violations", layout = MainView.class)
@ViewController(id = "mtr_Violation.list")
@ViewDescriptor(path = "violation-list-view.xml")
@LookupComponent("violationsDataGrid")
@DialogMode(width = "64em")
public class ViolationListView extends StandardListView<Violation> {

    @ViewComponent
    private CollectionLoader<Violation> violationsDl;

    private String objectKeyFilter;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        objectKeyFilter = event.getLocation().getQueryParameters()
                .getSingleParameter("objectKey")
                .orElse(null);
        super.beforeEnter(event);
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        if (objectKeyFilter != null && !objectKeyFilter.isBlank()) {
            violationsDl.setQuery(
                    "select e from mtr_Violation e where e.objectKey = :objectKey order by e.firstDetectedAt desc");
            violationsDl.setParameter("objectKey", objectKeyFilter);
        }
    }
}
