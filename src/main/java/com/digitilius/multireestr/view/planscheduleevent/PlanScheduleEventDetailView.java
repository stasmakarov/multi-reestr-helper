package com.digitilius.multireestr.view.planscheduleevent;

import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.planschedule.PlanScheduleLookupService;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

@Route(value = "plan-schedule-events/:id", layout = MainView.class)
@ViewController(id = "mtr_PlanScheduleEvent.detail")
@ViewDescriptor(path = "plan-schedule-event-detail-view.xml")
@EditedEntityContainer("planScheduleEventDc")
public class PlanScheduleEventDetailView extends StandardDetailView<PlanScheduleEvent> {

    private static final DateTimeFormatter PUBLISH = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @ViewComponent
    private CollectionContainer<PlanScheduleDto> planHintsDc;
    @ViewComponent
    private JmixComboBox<PlanScheduleDto> planHintComboBox;

    @Autowired
    private PlanScheduleLookupService planScheduleLookupService;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        planHintsDc.setItems(planScheduleLookupService.listCurrent());
        planHintComboBox.setItemLabelGenerator(this::hintLabel);
        planHintComboBox.addValueChangeListener(change -> {
            if (change.isFromClient()) {
                applyHint(change.getValue());
            }
        });
    }

    private void applyHint(PlanScheduleDto dto) {
        if (dto == null) {
            return;
        }
        PlanScheduleEvent entity = getEditedEntity();
        entity.setLegacyPlanId(dto.getId());
        entity.setFinYear(dto.getFinYear());
        entity.setPlanVersion(dto.getVersion());
    }

    private String hintLabel(PlanScheduleDto dto) {
        if (dto == null) {
            return "";
        }
        String published = dto.getPublishDate() == null ? "—" : PUBLISH.format(dto.getPublishDate());
        return dto.getFinYear() + " / v" + dto.getVersion() + " / " + published;
    }
}
