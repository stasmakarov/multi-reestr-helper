package com.digitilius.multireestr.view.calendarday;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
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

    @Autowired
    private DialogWindows dialogWindows;

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
