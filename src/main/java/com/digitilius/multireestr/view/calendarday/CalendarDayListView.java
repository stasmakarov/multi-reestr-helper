package com.digitilius.multireestr.view.calendarday;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "calendar-days", layout = MainView.class)
@ViewController(id = "mtr_CalendarDay.list")
@ViewDescriptor(path = "calendar-day-list-view.xml")
@LookupComponent("calendarDaysDataGrid")
@DialogMode(width = "64em")
public class CalendarDayListView extends StandardListView<CalendarDay> {
}
