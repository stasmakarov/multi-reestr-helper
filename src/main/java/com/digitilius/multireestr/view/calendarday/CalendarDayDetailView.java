package com.digitilius.multireestr.view.calendarday;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "calendar-days/:id", layout = MainView.class)
@ViewController(id = "mtr_CalendarDay.detail")
@ViewDescriptor(path = "calendar-day-detail-view.xml")
@EditedEntityContainer("calendarDayDc")
public class CalendarDayDetailView extends StandardDetailView<CalendarDay> {
}
