package com.digitilius.multireestr.view.planscheduleevent;

import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.planschedule.PlanScheduleLookupService;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "plan-schedule-events/:id", layout = MainView.class)
@ViewController(id = "mtr_PlanScheduleEvent.detail")
@ViewDescriptor(path = "plan-schedule-event-detail-view.xml")
@EditedEntityContainer("planScheduleEventDc")
public class PlanScheduleEventDetailView extends StandardDetailView<PlanScheduleEvent> {

    private static final DateTimeFormatter PUBLISH = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @ViewComponent
    private JmixComboBox<PlanScheduleDto> planScheduleHintField;

    @Autowired
    private PlanScheduleLookupService planScheduleLookupService;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        List<PlanScheduleDto> items = planScheduleLookupService.listAll();
        planScheduleHintField.setItems(items);
        planScheduleHintField.setItemLabelGenerator(this::label);
        Integer legacyPlanId = getEditedEntity().getLegacyPlanId();
        if (legacyPlanId != null) {
            items.stream()
                    .filter(dto -> legacyPlanId.equals(dto.getId()))
                    .findFirst()
                    .ifPresent(planScheduleHintField::setValue);
        }
    }

    @Subscribe("planScheduleHintField")
    public void onPlanScheduleHintFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<JmixComboBox<PlanScheduleDto>, PlanScheduleDto> event
    ) {
        if (!event.isFromClient()) {
            return;
        }
        PlanScheduleDto dto = event.getValue();
        if (dto == null) {
            return;
        }
        PlanScheduleEvent entity = getEditedEntity();
        entity.setLegacyPlanId(dto.getId());
        entity.setPlanVersion(dto.getVersion());
        entity.setFinYear(dto.getFinYear());
    }

    private String label(PlanScheduleDto dto) {
        String year = dto.getFinYear() == null ? "—" : dto.getFinYear().toString();
        String version = dto.getVersion() == null ? "—" : dto.getVersion().toString();
        String published = dto.getPublishDate() == null
                ? "—"
                : PUBLISH.format(dto.getPublishDate());
        return year + " / " + version + " / " + published;
    }
}
