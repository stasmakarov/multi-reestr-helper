package com.digitilius.multireestr.view.planscheduleevent;

import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "plan-schedule-events/:id", layout = MainView.class)
@ViewController(id = "mtr_PlanScheduleEvent.detail")
@ViewDescriptor(path = "plan-schedule-event-detail-view.xml")
@EditedEntityContainer("planScheduleEventDc")
public class PlanScheduleEventDetailView extends StandardDetailView<PlanScheduleEvent> {
}
