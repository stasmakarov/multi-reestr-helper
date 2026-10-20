package com.digitilius.multireestr.view.notice;

import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "notices/:number", layout = MainView.class)
@ViewController(id = "mtr_NoticeDetailView")
@ViewDescriptor(path = "notice-detail-view.xml")
public class NoticeDetailView extends StandardView {

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private Span numberSpan;

    @Autowired
    private ViewNavigators viewNavigators;

    private String number = "";

    @Override
    protected void processBeforeEnterInternal(BeforeEnterEvent event) {
        super.processBeforeEnterInternal(event);
        number = event.getRouteParameters().get("number").orElse("");
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        setPageTitle(messageBundle.formatMessage("noticeDetailView.title", number));
        numberSpan.setText(number);
    }

    @Subscribe("backBtn")
    public void onBackBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, NoticeSearchView.class).navigate();
    }
}
