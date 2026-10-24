package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService.NumberCount;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService.SearchCriteria;
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
import io.jmix.core.LoadContext;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View.InitEvent;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private CollectionLoader<NoticeCandidateDto> candidatesDl;
    @ViewComponent
    private TypedTextField<String> numberField;
    @ViewComponent
    private TypedDatePicker<LocalDate> dateFromField;
    @ViewComponent
    private TypedDatePicker<LocalDate> dateToField;
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

    private SearchCriteria activeCriteria;
    private NumberCount lastCount = NumberCount.of(0);
    private boolean autoOpenPending;
    private boolean notifyIfEmpty;

    @Subscribe
    public void onInit(final InitEvent event) {
        candidatesDl.setMaxResults(NoticeLookupService.SEARCH_PAGE_SIZE);
        candidatesDl.setFirstResult(0);
    }

    @Subscribe("searchBtn")
    public void onSearchBtnClick(final ClickEvent<JmixButton> event) {
        search(false);
    }

    @Subscribe("showAllBtn")
    public void onShowAllBtnClick(final ClickEvent<JmixButton> event) {
        clearFields();
        search(true);
    }

    @Subscribe("clearBtn")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        clearFields();
        activeCriteria = null;
        lastCount = NumberCount.of(0);
        autoOpenPending = false;
        notifyIfEmpty = false;
        candidatesDl.setFirstResult(0);
        candidatesDl.load();
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

    @Install(to = "candidatesDl", target = Target.DATA_LOADER)
    private List<NoticeCandidateDto> candidatesDlLoadDelegate(final LoadContext<NoticeCandidateDto> loadContext) {
        if (activeCriteria == null) {
            lastCount = NumberCount.of(0);
            return List.of();
        }
        try {
            int first = 0;
            int max = NoticeLookupService.SEARCH_PAGE_SIZE;
            if (loadContext.getQuery() != null) {
                first = loadContext.getQuery().getFirstResult();
                int requested = loadContext.getQuery().getMaxResults();
                if (requested > 0) {
                    max = requested;
                }
            }
            var page = noticeLookupService.findOriginals(activeCriteria, first, max);
            lastCount = page.count();
            return page.originals();
        } catch (DataAccessException e) {
            showLegacyConnectionError(e);
            lastCount = NumberCount.of(0);
            return List.of();
        }
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    private Integer paginationTotalCountDelegate() {
        return lastCount.capped();
    }

    @Subscribe(id = "candidatesDl", target = Target.DATA_LOADER)
    public void onCandidatesDlPostLoad(final CollectionLoader.PostLoadEvent<NoticeCandidateDto> event) {
        List<NoticeCandidateDto> found = List.copyOf(candidatesDc.getItems());
        openBtn.setEnabled(false);
        if (notifyIfEmpty && found.isEmpty() && !lastCount.truncated()) {
            notifications.create(messageBundle.getMessage("nothingFound"))
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
        notifyIfEmpty = false;
        if (autoOpenPending && found.size() == 1 && Boolean.TRUE.equals(found.get(0).getChosen())) {
            autoOpenPending = false;
            openDetail(found.get(0));
        }
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

    private void search(boolean showAll) {
        clearFieldErrors();
        SearchCriteria criteria;
        try {
            criteria = showAll
                    ? SearchCriteria.empty()
                    : SearchCriteria.of(
                    numberField.getValue(),
                    dateFromField.getValue(),
                    dateToField.getValue(),
                    subjectField.getValue()
            );
        } catch (IllegalArgumentException e) {
            dateToField.setInvalid(true);
            dateToField.setErrorMessage(messageBundle.getMessage("dateRange.invalid"));
            return;
        }
        activeCriteria = criteria;
        autoOpenPending = !showAll && criteria.exactNumber();
        notifyIfEmpty = true;
        candidatesDl.setFirstResult(0);
        candidatesDl.setMaxResults(NoticeLookupService.SEARCH_PAGE_SIZE);
        candidatesDl.load();
        if (lastCount.truncated()) {
            notifications.create(messageBundle.getMessage("searchTruncated"))
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    private void clearFields() {
        numberField.clear();
        dateFromField.clear();
        dateToField.clear();
        subjectField.clear();
        clearFieldErrors();
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

    private void clearFieldErrors() {
        numberField.setInvalid(false);
        numberField.setErrorMessage(null);
        dateFromField.setInvalid(false);
        dateFromField.setErrorMessage(null);
        dateToField.setInvalid(false);
        dateToField.setErrorMessage(null);
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
