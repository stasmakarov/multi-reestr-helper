package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.MultiReestrApplication;
import com.digitilius.multireestr.entity.legacy.ContractItemDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.LotDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.data.grid.DataGridItems;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {MultiReestrApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
@RequireDocker
class NoticeDetailViewTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void opensGoldenNoticeWithFilledTabs() {
        NoticeDetailView view = openDetail("0173100017026000015");

        TypedTextField<String> numberField = UiTestUtils.getComponent(view, "numberField");
        assertThat(numberField.getValue()).isEqualTo("0173100017026000015");

        TypedTextField<String> customerInnField = UiTestUtils.getComponent(view, "customerInnField");
        assertThat(customerInnField.getValue()).isEqualTo("7714841940");

        VerticalLayout cardBox = UiTestUtils.getComponent(view, "cardBox");
        VerticalLayout notFoundBox = UiTestUtils.getComponent(view, "notFoundBox");
        assertThat(cardBox.isVisible()).isTrue();
        assertThat(notFoundBox.isVisible()).isFalse();

        DataGrid<LotDto> lotsGrid = UiTestUtils.getComponent(view, "lotsGrid");
        assertThat(gridItems(lotsGrid)).isNotEmpty();

        DataGrid<SpecPositionDto> itemsGrid = UiTestUtils.getComponent(view, "itemsGrid");
        assertThat(gridItems(itemsGrid)).isNotEmpty();

        DataGrid<ContractItemDto> contractItemsGrid = UiTestUtils.getComponent(view, "contractItemsGrid");
        assertThat(gridItems(contractItemsGrid)).isNotEmpty();

        DataGrid<ProtocolDto> protocolsGrid = UiTestUtils.getComponent(view, "protocolsGrid");
        assertThat(gridItems(protocolsGrid)).hasSize(2);

        DataGrid<ExecutionDto> executionsGrid = UiTestUtils.getComponent(view, "executionsGrid");
        assertThat(gridItems(executionsGrid)).hasSize(2);

        Span paidTotalValue = UiTestUtils.getComponent(view, "paidTotalValue");
        assertThat(paidTotalValue.getText()).isEqualTo("16000");

        VerticalLayout contractBox = UiTestUtils.getComponent(view, "contractBox");
        VerticalLayout noContractBox = UiTestUtils.getComponent(view, "noContractBox");
        assertThat(contractBox.isVisible()).isTrue();
        assertThat(noContractBox.isVisible()).isFalse();

        JmixButton checkBtn = UiTestUtils.getComponent(view, "checkBtn");
        JmixButton violationsBtn = UiTestUtils.getComponent(view, "violationsBtn");
        assertThat(checkBtn.isEnabled()).isTrue();
        assertThat(violationsBtn.isEnabled()).isTrue();
    }

    @Test
    void opensNoticeWithoutContract() {
        NoticeDetailView view = openDetail("0173100017024000010");

        TypedTextField<String> numberField = UiTestUtils.getComponent(view, "numberField");
        assertThat(numberField.getValue()).isEqualTo("0173100017024000010");

        VerticalLayout cardBox = UiTestUtils.getComponent(view, "cardBox");
        VerticalLayout contractBox = UiTestUtils.getComponent(view, "contractBox");
        VerticalLayout noContractBox = UiTestUtils.getComponent(view, "noContractBox");
        assertThat(cardBox.isVisible()).isTrue();
        assertThat(contractBox.isVisible()).isFalse();
        assertThat(noContractBox.isVisible()).isTrue();

        DataGrid<ProtocolDto> protocolsGrid = UiTestUtils.getComponent(view, "protocolsGrid");
        assertThat(gridItems(protocolsGrid)).isEmpty();
    }

    @Test
    void showsMessageForUnknownNumber() {
        NoticeDetailView view = openDetail("0000000000000000001");

        VerticalLayout notFoundBox = UiTestUtils.getComponent(view, "notFoundBox");
        VerticalLayout cardBox = UiTestUtils.getComponent(view, "cardBox");
        Span notFoundSpan = UiTestUtils.getComponent(view, "notFoundSpan");

        assertThat(notFoundBox.isVisible()).isTrue();
        assertThat(cardBox.isVisible()).isFalse();
        assertThat(notFoundSpan.getText()).isNotBlank();
    }

    @Test
    void showsMessageForInvalidNumber() {
        NoticeDetailView view = openDetail("123");

        VerticalLayout notFoundBox = UiTestUtils.getComponent(view, "notFoundBox");
        VerticalLayout cardBox = UiTestUtils.getComponent(view, "cardBox");
        Span notFoundSpan = UiTestUtils.getComponent(view, "notFoundSpan");

        assertThat(notFoundBox.isVisible()).isTrue();
        assertThat(cardBox.isVisible()).isFalse();
        assertThat(notFoundSpan.getText()).isNotBlank();
    }

    private NoticeDetailView openDetail(String number) {
        viewNavigators.view(UiTestUtils.getCurrentView(), NoticeDetailView.class)
                .withRouteParameters(new RouteParameters("number", number))
                .navigate();
        return UiTestUtils.getCurrentView();
    }

    private static <T> List<T> gridItems(DataGrid<T> grid) {
        DataGridItems<T> items = grid.getItems();
        assertThat(items).isNotNull();
        return List.copyOf(items.getItems());
    }
}
