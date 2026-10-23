package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Route(value = "notices/search", layout = MainView.class)
@ViewController(id = "mtr_NoticeSearchView")
@ViewDescriptor(path = "notice-search-view.xml")
public class NoticeSearchView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(NoticeSearchView.class);

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private CollectionContainer<NoticeCandidateDto> candidatesDc;
    @ViewComponent
    private TypedTextField<String> numberField;
    @ViewComponent
    private TypedDatePicker<LocalDate> dateField;
    @ViewComponent
    private TypedTextField<String> subjectField;
    @ViewComponent
    private DataGrid<NoticeCandidateDto> candidatesGrid;
    @ViewComponent
    private JmixButton openBtn;

    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private Notifications notifications;
    @Autowired
    private NoticeLookupService noticeLookupService;

    @Subscribe("searchBtn")
    public void onSearchBtnClick(final ClickEvent<JmixButton> event) {
        search();
    }

    @Subscribe("clearBtn")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        numberField.clear();
        dateField.clear();
        subjectField.clear();
        clearNumberError();
        candidatesDc.setItems(List.of());
        openBtn.setEnabled(false);
    }

    @Subscribe("openBtn")
    public void onOpenBtnClick(final ClickEvent<JmixButton> event) {
        openDetail(candidatesGrid.getSingleSelectedItem());
    }

    @Subscribe("candidatesGrid")
    public void onCandidatesGridSelection(
            final SelectionEvent<DataGrid<NoticeCandidateDto>, NoticeCandidateDto> event
    ) {
        openBtn.setEnabled(candidatesGrid.getSingleSelectedItem() != null);
    }

    @Subscribe("candidatesGrid")
    public void onCandidatesGridItemDoubleClick(final ItemDoubleClickEvent<NoticeCandidateDto> event) {
        openDetail(event.getItem());
    }

    @Install(to = "candidatesGrid", subject = "partNameGenerator")
    private String candidatesGridPartNameGenerator(final NoticeCandidateDto candidate) {
        return candidate != null && Boolean.TRUE.equals(candidate.getChosen()) ? "chosen-row" : null;
    }

    @Install(to = "candidatesGrid.subject", subject = "partNameGenerator")
    private String subjectPartNameGenerator(final NoticeCandidateDto candidate) {
        return "subject-cell";
    }

    @Supply(to = "candidatesGrid.deleted", subject = "renderer")
    private Renderer<NoticeCandidateDto> deletedRenderer() {
        return booleanCheckRenderer(NoticeCandidateDto::getDeleted);
    }

    @Supply(to = "candidatesGrid.canceled", subject = "renderer")
    private Renderer<NoticeCandidateDto> canceledRenderer() {
        return booleanCheckRenderer(NoticeCandidateDto::getCanceled);
    }

    @Supply(to = "candidatesGrid.localCopy", subject = "renderer")
    private Renderer<NoticeCandidateDto> localCopyRenderer() {
        return booleanCheckRenderer(NoticeCandidateDto::getLocalCopy);
    }

    @Supply(to = "candidatesGrid.chosen", subject = "renderer")
    private Renderer<NoticeCandidateDto> chosenRenderer() {
        return booleanCheckRenderer(NoticeCandidateDto::getChosen);
    }

    private void search() {
        clearNumberError();
        String rawNumber = numberField.getValue();
        if (!isBlank(rawNumber)) {
            var parsed = NoticeNumber.tryParse(rawNumber);
            if (parsed.isEmpty()) {
                numberField.setInvalid(true);
                numberField.setErrorMessage(messageBundle.getMessage("numberField.invalid"));
                return;
            }
            showCandidates(lookupByNumber(parsed.get()));
            return;
        }
        LocalDate date = dateField.getValue();
        String subject = subjectField.getValue();
        if (date == null && isBlank(subject)) {
            notifications.create(messageBundle.getMessage("searchCriteriaRequired"))
                    .withPosition(Notification.Position.TOP_END)
                    .show();
            return;
        }
        showCandidates(lookupByDateAndSubject(date, subject));
    }

    private List<NoticeCandidateDto> lookupByNumber(NoticeNumber number) {
        try {
            return noticeLookupService.findByNumber(number);
        } catch (DataAccessException e) {
            showLegacyConnectionError(e);
            return null;
        }
    }

    private List<NoticeCandidateDto> lookupByDateAndSubject(LocalDate date, String subject) {
        try {
            return noticeLookupService.findByDateAndSubject(date, subject);
        } catch (DataAccessException e) {
            showLegacyConnectionError(e);
            return null;
        }
    }

    private void showCandidates(List<NoticeCandidateDto> found) {
        if (found == null) {
            return;
        }
        candidatesDc.setItems(found);
        openBtn.setEnabled(false);
        if (found.isEmpty()) {
            notifications.create(messageBundle.getMessage("nothingFound"))
                    .withPosition(Notification.Position.TOP_END)
                    .show();
            return;
        }
        if (found.size() == 1 && Boolean.TRUE.equals(found.get(0).getChosen())) {
            openDetail(found.get(0));
        }
    }

    private void openDetail(NoticeCandidateDto candidate) {
        if (candidate == null || isBlank(candidate.getNumber())) {
            return;
        }
        viewNavigators.view(this, NoticeDetailView.class)
                .withRouteParameters(new RouteParameters("number", candidate.getNumber()))
                .navigate();
    }

    private void showLegacyConnectionError(DataAccessException e) {
        if (e != null) {
            log.error("Legacy MultiReestr is not available", e);
        } else {
            log.error("Legacy MultiReestr is not configured");
        }
        notifications.create(messageBundle.getMessage("legacyConnectionError"))
                .withThemeVariant(NotificationVariant.ERROR)
                .withPosition(Notification.Position.TOP_END)
                .show();
    }

    private void clearNumberError() {
        numberField.setInvalid(false);
        numberField.setErrorMessage(null);
    }

    private static Renderer<NoticeCandidateDto> booleanCheckRenderer(
            Function<NoticeCandidateDto, Boolean> getter
    ) {
        return new ComponentRenderer<>(item -> {
            if (Boolean.TRUE.equals(getter.apply(item))) {
                return new Icon(VaadinIcon.CHECK);
            }
            return new Span();
        });
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
