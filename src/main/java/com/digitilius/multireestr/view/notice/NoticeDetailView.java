package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ContractItemDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.LotDto;
import com.digitilius.multireestr.entity.legacy.NoticeDocumentDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.NoticeFinanceDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeExcelExporter;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.rules.OnDemandCheckService;
import com.digitilius.multireestr.view.checkresult.CheckResultDialog;
import com.digitilius.multireestr.view.contractterm.ContractTermListView;
import com.digitilius.multireestr.view.main.MainView;
import com.digitilius.multireestr.view.violation.ViolationListView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Route(value = "notices/:number", layout = MainView.class)
@ViewController(id = "mtr_NoticeDetailView")
@ViewDescriptor(path = "notice-detail-view.xml")
public class NoticeDetailView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(NoticeDetailView.class);

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private InstanceContainer<NoticeDto> noticeDc;
    @ViewComponent
    private InstanceContainer<OrganizationDto> customerDc;
    @ViewComponent
    private CollectionContainer<LotDto> lotsDc;
    @ViewComponent
    private CollectionContainer<NoticeFinanceDto> financesDc;
    @ViewComponent
    private CollectionContainer<NoticeDocumentDto> documentsDc;
    @ViewComponent
    private CollectionContainer<SpecPositionDto> itemsDc;
    @ViewComponent
    private CollectionContainer<KtruCharacteristicDto> characteristicsDc;
    @ViewComponent
    private CollectionContainer<ProtocolDto> protocolsDc;
    @ViewComponent
    private InstanceContainer<ContractDto> contractDc;
    @ViewComponent
    private InstanceContainer<OrganizationDto> supplierDc;
    @ViewComponent
    private CollectionContainer<ContractItemDto> contractItemsDc;
    @ViewComponent
    private CollectionContainer<ExecutionDto> executionsDc;
    @ViewComponent
    private DataGrid<SpecPositionDto> itemsGrid;
    @ViewComponent
    private VerticalLayout warningsBox;
    @ViewComponent
    private Span notFoundSpan;
    @ViewComponent
    private VerticalLayout notFoundBox;
    @ViewComponent
    private VerticalLayout cardBox;
    @ViewComponent
    private VerticalLayout noContractBox;
    @ViewComponent
    private VerticalLayout contractBox;
    @ViewComponent
    private Anchor eisUrlAnchor;
    @ViewComponent
    private Span paidTotalValue;
    @ViewComponent
    private JmixButton excelBtn;
    @ViewComponent
    private JmixButton checkBtn;
    @ViewComponent
    private JmixButton violationsBtn;
    @ViewComponent
    private JmixButton contractTermsBtn;

    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Notifications notifications;
    @Autowired
    private OnDemandCheckService onDemandCheckService;
    @Autowired
    private Downloader downloader;
    @Autowired
    private NoticeExcelExporter noticeExcelExporter;
    @Autowired
    private NoticeCardService noticeCardService;

    private String number = "";

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        number = event.getRouteParameters().get("number").orElse("");
        super.beforeEnter(event);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        setPageTitle(messageBundle.formatMessage("noticeDetailView.title", number));
        checkBtn.setVisible(onDemandCheckService.canCheckOnDemand());
        loadCard();
    }

    @Subscribe("backBtn")
    public void onBackBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, NoticeSearchView.class).navigate();
    }

    @Subscribe("checkBtn")
    public void onCheckBtnClick(final ClickEvent<JmixButton> event) {
        NoticeDto notice = noticeDc.getItemOrNull();
        if (notice == null) {
            return;
        }
        try {
            OnDemandCheckService.OnDemandCheckResult result = onDemandCheckService.check(notice);
            var window = dialogWindows.view(this, CheckResultDialog.class).build();
            window.getView().setCheck(notice, result);
            window.open();
        } catch (RuntimeException e) {
            notifications.create(e.getMessage() == null ? "" : e.getMessage()).show();
        }
    }

    @Subscribe("violationsBtn")
    public void onViolationsBtnClick(final ClickEvent<JmixButton> event) {
        NoticeDto notice = noticeDc.getItemOrNull();
        if (notice == null || notice.getNumber() == null) {
            return;
        }
        viewNavigators.view(this, ViolationListView.class)
                .withQueryParameters(QueryParameters.of("objectKey", notice.getNumber()))
                .navigate();
    }

    @Subscribe("contractTermsBtn")
    public void onContractTermsBtnClick(final ClickEvent<JmixButton> event) {
        ContractDto contract = contractDc.getItemOrNull();
        if (contract == null || contract.getId() == null) {
            return;
        }
        viewNavigators.view(this, ContractTermListView.class)
                .withQueryParameters(QueryParameters.of("contractLegacyId", String.valueOf(contract.getId())))
                .navigate();
    }

    @Subscribe("excelBtn")
    public void onExcelBtnClick(final ClickEvent<JmixButton> event) {
        NoticeDto notice = noticeDc.getItemOrNull();
        if (notice == null) {
            return;
        }
        String fileName = "извещение-" + notice.getNumber() + ".xlsx";
        downloader.download(noticeExcelExporter.writeBytes(List.of(notice)), fileName, DownloadFormat.XLSX);
    }

    @Supply(to = "documentsGrid.url", subject = "renderer")
    private Renderer<NoticeDocumentDto> documentUrlRenderer() {
        return new ComponentRenderer<>(document -> {
            String url = document == null ? null : document.getUrl();
            if (url == null || url.isBlank()) {
                return new Span();
            }
            Anchor anchor = new Anchor(url, url);
            anchor.setTarget(AnchorTarget.BLANK);
            return anchor;
        });
    }

    private void loadCard() {
        Optional<NoticeNumber> parsed = NoticeNumber.tryParse(number);
        if (parsed.isEmpty()) {
            showNotFound(messageBundle.getMessage("noticeNumberInvalid"));
            return;
        }
        try {
            Optional<NoticeDto> found = noticeCardService.loadOriginal(parsed.get());
            if (found.isEmpty()) {
                showNotFound(messageBundle.getMessage("noticeNotFound"));
                return;
            }
            showNotice(found.get());
        } catch (DataAccessException e) {
            log.error("Legacy MultiReestr is not available", e);
            showNotFound(messageBundle.getMessage("legacyConnectionError"));
        }
    }

    private void showNotFound(String message) {
        notFoundSpan.setText(message);
        notFoundBox.setVisible(true);
        cardBox.setVisible(false);
        excelBtn.setEnabled(false);
        checkBtn.setEnabled(false);
        violationsBtn.setEnabled(false);
        contractTermsBtn.setEnabled(false);
    }

    private void showNotice(NoticeDto notice) {
        notFoundBox.setVisible(false);
        cardBox.setVisible(true);
        excelBtn.setEnabled(true);
        checkBtn.setEnabled(checkBtn.isVisible());
        violationsBtn.setEnabled(true);
        noticeDc.setItem(notice);
        customerDc.setItem(notice.getCustomer());
        lotsDc.setItems(orEmpty(notice.getLots()));
        financesDc.setItems(orEmpty(notice.getFinances()));
        documentsDc.setItems(orEmpty(notice.getDocuments()));
        itemsDc.setItems(orEmpty(notice.getItems()));
        protocolsDc.setItems(orEmpty(notice.getProtocols()));
        showWarnings(notice.getWarnings());
        showEisUrl(notice.getEisUrl());

        ContractDto contract = notice.getContract();
        contractTermsBtn.setEnabled(contract != null && contract.getId() != null);
        if (contract == null) {
            contractBox.setVisible(false);
            noContractBox.setVisible(true);
            contractDc.setItem(null);
            supplierDc.setItem(null);
            contractItemsDc.setItems(List.of());
            executionsDc.setItems(List.of());
            paidTotalValue.setText("");
            selectFirstItem();
            return;
        }
        noContractBox.setVisible(false);
        contractBox.setVisible(true);
        contractDc.setItem(contract);
        supplierDc.setItem(contract.getSupplier());
        contractItemsDc.setItems(orEmpty(contract.getItems()));
        executionsDc.setItems(orEmpty(contract.getExecutions()));
        paidTotalValue.setText(formatAmount(contract.getPaidTotal()));
        selectFirstItem();
    }

    private void selectFirstItem() {
        List<SpecPositionDto> items = List.copyOf(itemsDc.getItems());
        if (items.isEmpty()) {
            characteristicsDc.setItems(List.of());
            return;
        }
        SpecPositionDto first = items.get(0);
        itemsGrid.select(first);
        if (characteristicsDc.getItems().isEmpty()) {
            characteristicsDc.setItems(orEmpty(first.getCharacteristics()));
        }
    }

    private void showWarnings(List<String> warnings) {
        warningsBox.removeAll();
        if (warnings == null || warnings.isEmpty()) {
            warningsBox.setVisible(false);
            return;
        }
        for (String warning : warnings) {
            warningsBox.add(new Span(warning));
        }
        warningsBox.setVisible(true);
    }

    private void showEisUrl(String url) {
        if (url == null || url.isBlank()) {
            eisUrlAnchor.setText("");
            eisUrlAnchor.setHref("");
            eisUrlAnchor.setVisible(false);
            return;
        }
        eisUrlAnchor.setHref(url);
        eisUrlAnchor.setText(url);
        eisUrlAnchor.setTarget(AnchorTarget.BLANK);
        eisUrlAnchor.setVisible(true);
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        return amount.stripTrailingZeros().toPlainString();
    }

    private static <T> List<T> orEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }
}
