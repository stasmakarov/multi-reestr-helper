package com.digitilius.multireestr.view.planscheduleevent;

import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "plan-schedule-events", layout = MainView.class)
@ViewController(id = "mtr_PlanScheduleEvent.list")
@ViewDescriptor(path = "plan-schedule-event-list-view.xml")
@LookupComponent("planScheduleEventsDataGrid")
@DialogMode(width = "64em")
public class PlanScheduleEventListView extends StandardListView<PlanScheduleEvent> {
}
