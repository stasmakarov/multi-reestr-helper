package com.digitilius.multireestr.view.calendarday;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "calendar-days", layout = MainView.class)
@ViewController(id = "mtr_CalendarDay.list")
@ViewDescriptor(path = "calendar-day-list-view.xml")
@LookupComponent("calendarDaysDataGrid")
@DialogMode(width = "64em")
public class CalendarDayListView extends StandardListView<CalendarDay> {

    @ViewComponent
    private CollectionLoader<CalendarDay> calendarDaysDl;
    @ViewComponent
    private JmixButton loadYearButton;

    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private Metadata metadata;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        CrudEntityContext context = new CrudEntityContext(metadata.getClass(CalendarDay.class));
        accessManager.applyRegisteredConstraints(context);
        loadYearButton.setVisible(context.isUpdatePermitted());
    }

    @Subscribe("loadYearButton")
    public void onLoadYearButtonClick(final ClickEvent<JmixButton> event) {
        dialogWindows.view(this, CalendarYearLoadDialog.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        calendarDaysDl.load();
                    }
                })
                .open();
    }
}
