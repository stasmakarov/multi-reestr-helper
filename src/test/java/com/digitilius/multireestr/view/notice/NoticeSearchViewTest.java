package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.MultiReestrApplication;
import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.Test;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {MultiReestrApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
@RequireDocker
class NoticeSearchViewTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void opensSearchView() {
        NoticeSearchView view = openSearchView();
        JmixButton searchBtn = UiTestUtils.getComponent(view, "searchBtn");
        JmixButton showAllBtn = UiTestUtils.getComponent(view, "showAllBtn");
        assertThat(view).isNotNull();
        assertThat(searchBtn).isInstanceOf(JmixButton.class);
        assertThat(showAllBtn).isInstanceOf(JmixButton.class);
    }

    @Test
    void searchByCanonicalNumberReturnsOriginal() {
        assertSearchResult("0173100017026000015", 3);
    }

    @Test
    void searchBySpacedNumberReturnsSameResult() {
        assertSearchResult("0173 1000 1702 6000 015", 3);
    }

    @Test
    void searchByFragmentDoesNotInvalidateNumberField() {
        NoticeSearchView view = openSearchView();
        TypedTextField<String> numberField = UiTestUtils.getComponent(view, "numberField");
        numberField.setValue("123");
        JmixButton searchBtn = UiTestUtils.getComponent(view, "searchBtn");
        searchBtn.click();

        assertThat(numberField.isInvalid()).isFalse();
        DataGrid<NoticeCandidateDto> grid = UiTestUtils.getComponent(view, "candidatesGrid");
        assertThat(gridItems(grid)).isEmpty();
    }

    @Test
    void showAllFillsGridWithOriginalsAndStaysOnSearch() {
        NoticeSearchView view = openSearchView();
        JmixButton showAllBtn = UiTestUtils.getComponent(view, "showAllBtn");
        showAllBtn.click();

        assertThat(UiTestUtils.getCurrentView() instanceof NoticeSearchView).isTrue();
        DataGrid<NoticeCandidateDto> grid = UiTestUtils.getComponent(view, "candidatesGrid");
        List<NoticeCandidateDto> items = gridItems(grid);
        assertThat(items).isNotEmpty();
        assertThat(items).allMatch(c -> Boolean.TRUE.equals(c.getChosen()));
        assertThat(items.size()).isLessThanOrEqualTo(20);
    }

    @Test
    void invertedDateRangeDoesNotLoad() {
        NoticeSearchView view = openSearchView();
        TypedDatePicker<LocalDate> from = UiTestUtils.getComponent(view, "dateFromField");
        TypedDatePicker<LocalDate> to = UiTestUtils.getComponent(view, "dateToField");
        from.setValue(LocalDate.of(2026, 4, 7));
        to.setValue(LocalDate.of(2026, 4, 3));
        JmixButton searchBtn = UiTestUtils.getComponent(view, "searchBtn");
        searchBtn.click();

        assertThat(to.isInvalid()).isTrue();
        DataGrid<NoticeCandidateDto> grid = UiTestUtils.getComponent(view, "candidatesGrid");
        assertThat(gridItems(grid)).isEmpty();
    }

    private void assertSearchResult(String number, int originalId) {
        NoticeSearchView view = openSearchView();
        TypedTextField<String> numberField = UiTestUtils.getComponent(view, "numberField");
        numberField.setValue(number);

        JmixButton searchBtn = UiTestUtils.getComponent(view, "searchBtn");
        searchBtn.click();

        if (UiTestUtils.getCurrentView() instanceof NoticeDetailView) {
            return;
        }
        DataGrid<NoticeCandidateDto> grid = UiTestUtils.getComponent(view, "candidatesGrid");
        List<NoticeCandidateDto> items = gridItems(grid);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getChosen()).isTrue();
        assertThat(items.get(0).getId()).isEqualTo(originalId);
    }

    private NoticeSearchView openSearchView() {
        viewNavigators.view(UiTestUtils.getCurrentView(), NoticeSearchView.class).navigate();
        return UiTestUtils.getCurrentView();
    }

    private static List<NoticeCandidateDto> gridItems(DataGrid<NoticeCandidateDto> grid) {
        DataGridItems<NoticeCandidateDto> items = grid.getItems();
        assertThat(items).isNotNull();
        return List.copyOf(items.getItems());
    }
}
