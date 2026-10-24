package com.digitilius.multireestr.view.about;

import com.digitilius.multireestr.service.AppVersionService;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@DialogMode(width = "22rem")
@ViewController(id = "mtr_AboutView")
@ViewDescriptor(path = "about-view.xml")
public class AboutView extends StandardView {

    @Autowired
    private AppVersionService appVersionService;

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private Span versionLabel;
    @ViewComponent
    private Span buildLabel;
    @ViewComponent
    private Span branchLabel;
    @ViewComponent
    private Span dateLabel;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        AppVersionService.Info info = appVersionService.current();
        versionLabel.setText(line("version.line", info.version()));
        buildLabel.setText(line("build.line", info.build()));
        branchLabel.setText(line("branch.line", info.branch()));
        dateLabel.setText(line("date.line", formatDate(info)));
    }

    private String line(String key, String value) {
        return String.format(messageBundle.getMessage(key), display(value));
    }

    private String display(String value) {
        return value != null ? value : messageBundle.getMessage("unknown");
    }

    private String formatDate(AppVersionService.Info info) {
        if (info.date() == null) {
            return null;
        }
        Locale locale = VaadinSession.getCurrent() != null
                ? VaadinSession.getCurrent().getLocale()
                : Locale.getDefault();
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault())
                .format(info.date());
    }
}
