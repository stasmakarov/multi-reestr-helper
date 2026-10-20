package com.digitilius.multireestr.view.legacyconnection;

import com.digitilius.multireestr.legacy.LegacyDbInfo;
import com.digitilius.multireestr.legacy.LegacyDbStatus;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "legacy-connection", layout = MainView.class)
@ViewController(id = "mtr_LegacyConnectionView")
@ViewDescriptor(path = "legacy-connection-view.xml")
public class LegacyConnectionView extends StandardView {

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private TypedTextField<String> statusField;
    @ViewComponent
    private TypedTextField<String> serverVersionField;
    @ViewComponent
    private TypedTextField<String> currentUserField;
    @ViewComponent
    private TypedTextField<String> databaseField;
    @ViewComponent
    private TypedTextField<String> hostPortField;
    @ViewComponent
    private TypedTextField<String> readOnlyField;
    @ViewComponent
    private TypedTextField<String> responseMsField;
    @ViewComponent
    private TypedTextField<String> errorField;

    @Autowired
    private LegacyDbInfo legacyDbInfo;
    @Autowired
    private Notifications notifications;

    @Subscribe("checkButton")
    public void onCheckButtonClick(final ClickEvent<JmixButton> event) {
        LegacyDbStatus status = legacyDbInfo.check();
        statusField.setValue(messageBundle.getMessage(status.ok() ? "status.ok" : "status.fail"));
        serverVersionField.setValue(nullToEmpty(status.serverVersion()));
        currentUserField.setValue(nullToEmpty(status.currentUser()));
        databaseField.setValue(nullToEmpty(status.database()));
        hostPortField.setValue(formatHostPort(status.host(), status.port()));
        readOnlyField.setValue(messageBundle.getMessage(status.readOnly() ? "readOnly.yes" : "readOnly.no"));
        responseMsField.setValue(String.valueOf(status.responseMs()));
        errorField.setValue(nullToEmpty(status.error()));

        if (status.ok()) {
            notifications.create(messageBundle.getMessage("connectedNotification"))
                    .withThemeVariant(NotificationVariant.SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        } else {
            notifications.create(briefError(status.error()))
                    .withThemeVariant(NotificationVariant.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    private String briefError(String error) {
        String prefix = messageBundle.getMessage("failedNotification");
        if (error == null || error.isBlank()) {
            return prefix;
        }
        String brief = error.length() > 180 ? error.substring(0, 180) + "…" : error;
        return prefix + ": " + brief;
    }

    private static String formatHostPort(String host, Integer port) {
        if (host == null || host.isBlank()) {
            return "";
        }
        return port == null ? host : host + ":" + port;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
