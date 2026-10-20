package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.MultiReestrApplication;
import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {MultiReestrApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
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
        assertThat(view).isNotNull();
        assertThat(searchBtn).isInstanceOf(JmixButton.class);
    }

    @Test
    void searchByCanonicalNumberReturnsTwoRowsWithChosenOriginal() {
        assertSearchResult("0173100017026000015");
    }

    @Test
    void searchBySpacedNumberReturnsSameResult() {
        assertSearchResult("0173 1000 1702 6000 015");
    }

    @Test
    void invalidNumberShowsFieldErrorAndDoesNotChangeGrid() {
        NoticeSearchView view = openSearchView();
        DataGrid<NoticeCandidateDto> grid = UiTestUtils.getComponent(view, "candidatesGrid");
        assertThat(gridItems(grid)).isEmpty();

        TypedTextField<String> numberField = UiTestUtils.getComponent(view, "numberField");
        numberField.setValue("123");
        JmixButton searchBtn = UiTestUtils.getComponent(view, "searchBtn");
        searchBtn.click();

        assertThat(numberField.isInvalid()).isTrue();
        assertThat(numberField.getErrorMessage()).isNotBlank();
        assertThat(gridItems(grid)).isEmpty();
    }

    private void assertSearchResult(String number) {
        NoticeSearchView view = openSearchView();
        TypedTextField<String> numberField = UiTestUtils.getComponent(view, "numberField");
        numberField.setValue(number);

        JmixButton searchBtn = UiTestUtils.getComponent(view, "searchBtn");
        searchBtn.click();

        DataGrid<NoticeCandidateDto> grid = UiTestUtils.getComponent(view, "candidatesGrid");
        List<NoticeCandidateDto> items = gridItems(grid);
        assertThat(items).hasSize(2);

        NoticeCandidateDto first = items.get(0);
        assertThat(first.getChosen()).isTrue();
        assertThat(first.getId()).isEqualTo(3);
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
