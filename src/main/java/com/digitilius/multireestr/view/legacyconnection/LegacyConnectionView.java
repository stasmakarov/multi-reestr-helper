package com.digitilius.multireestr.view.legacyconnection;

import com.digitilius.multireestr.entity.LegacyConnectionSettings;
import com.digitilius.multireestr.legacy.LegacyConnectionService;
import com.digitilius.multireestr.legacy.LegacyDbInfo;
import com.digitilius.multireestr.legacy.LegacyDbStatus;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import com.vaadin.flow.component.textfield.PasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
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
    private InstanceContainer<LegacyConnectionSettings> settingsDc;
    @ViewComponent
    private Span sourceLabel;
    @ViewComponent
    private Span secretKeyHint;
    @ViewComponent
    private Span passwordSavedHint;
    @ViewComponent
    private PasswordField passwordField;
    @ViewComponent
    private JmixButton saveApplyButton;
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
    private LegacyConnectionService legacyConnectionService;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        loadModel();
    }

    @Subscribe("checkButton")
    public void onCheckButtonClick(final ClickEvent<JmixButton> event) {
        if (!validateParameters()) {
            return;
        }
        LegacyConnectionSettings settings = settingsDc.getItem();
        LegacyDbStatus status = legacyConnectionService.testFromForm(formValues(settings));
        showStatus(status);
        notifyResult(status);
    }

    @Subscribe("saveApplyButton")
    public void onSaveApplyButtonClick(final ClickEvent<JmixButton> event) {
        if (!legacyConnectionService.secretKeyConfigured()) {
            notifications.create(messageBundle.getMessage("secretKeyMissing"))
                    .withThemeVariant(NotificationVariant.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
            return;
        }
        if (!validateParameters()) {
            return;
        }
        LegacyConnectionSettings settings = settingsDc.getItem();
        try {
            LegacyDbStatus status = legacyConnectionService.saveAndApply(settings, passwordField.getValue());
            settingsDc.setItem(legacyConnectionService.loadForScreen().settings());
            passwordField.clear();
            updateHints();
            showStatus(status);
            if (status.ok()) {
                notifications.create(messageBundle.getMessage("savedNotification"))
                        .withThemeVariant(NotificationVariant.SUCCESS)
                        .withPosition(Notification.Position.TOP_END)
                        .show();
            } else {
                notifications.create(messageBundle.getMessage("savedButFailedNotification")
                                + ": " + briefError(status.error()))
                        .withThemeVariant(NotificationVariant.ERROR)
                        .withPosition(Notification.Position.TOP_END)
                        .show();
            }
            if (status.privilegeWarning()) {
                notifications.create(messageBundle.getMessage("privilegeWarning"))
                        .withThemeVariant(NotificationVariant.WARNING)
                        .withPosition(Notification.Position.TOP_END)
                        .show();
            }
        } catch (RuntimeException e) {
            notifications.create(briefError(e.getMessage()))
                    .withThemeVariant(NotificationVariant.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    @Subscribe("resetButton")
    public void onResetButtonClick(final ClickEvent<JmixButton> event) {
        LegacyDbStatus status = legacyConnectionService.resetToProperties();
        loadModel();
        showStatus(status);
        notifications.create(messageBundle.getMessage("resetNotification"))
                .withPosition(Notification.Position.TOP_END)
                .show();
    }

    private void loadModel() {
        LegacyConnectionService.ScreenModel model = legacyConnectionService.loadForScreen();
        settingsDc.setItem(model.settings());
        sourceLabel.setText(messageBundle.getMessage(switch (model.source()) {
            case DATABASE -> "source.database";
            case PROPERTIES -> "source.properties";
            case NONE -> "source.none";
        }));
        passwordField.clear();
        updateHints();
        showStatus(legacyDbInfo.check());
    }

    private void updateHints() {
        boolean keyOk = legacyConnectionService.secretKeyConfigured();
        secretKeyHint.setVisible(!keyOk);
        secretKeyHint.setText(messageBundle.getMessage("secretKeyMissing"));
        saveApplyButton.setEnabled(keyOk);
        LegacyConnectionSettings settings = settingsDc.getItemOrNull();
        passwordSavedHint.setVisible(settings != null
                && settings.getPasswordEncrypted() != null
                && !settings.getPasswordEncrypted().isBlank());
    }

    private boolean validateParameters() {
        LegacyConnectionSettings settings = settingsDc.getItemOrNull();
        if (settings == null) {
            return false;
        }
        if (isBlank(settings.getHost()) || isBlank(settings.getDatabaseName()) || isBlank(settings.getUsername())) {
            notifications.create(messageBundle.getMessage("validation.required"))
                    .withThemeVariant(NotificationVariant.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
            return false;
        }
        Integer port = settings.getPort();
        if (port == null || port < 1 || port > 65535) {
            notifications.create(messageBundle.getMessage("validation.port"))
                    .withThemeVariant(NotificationVariant.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
            return false;
        }
        return true;
    }

    private LegacyConnectionService.FormValues formValues(LegacyConnectionSettings settings) {
        return new LegacyConnectionService.FormValues(
                Boolean.TRUE.equals(settings.getEnabled()),
                settings.getHost(),
                settings.getPort(),
                settings.getDatabaseName(),
                settings.getUsername(),
                passwordField.getValue()
        );
    }

    private void showStatus(LegacyDbStatus status) {
        statusField.setValue(messageBundle.getMessage(status.ok() ? "status.ok" : "status.fail"));
        serverVersionField.setValue(nullToEmpty(status.serverVersion()));
        currentUserField.setValue(nullToEmpty(status.currentUser()));
        databaseField.setValue(nullToEmpty(status.database()));
        hostPortField.setValue(formatHostPort(status.host(), status.port()));
        readOnlyField.setValue(messageBundle.getMessage(status.readOnly() ? "readOnly.yes" : "readOnly.no"));
        responseMsField.setValue(String.valueOf(status.responseMs()));
        errorField.setValue(nullToEmpty(status.error()));
    }

    private void notifyResult(LegacyDbStatus status) {
        if (status.privilegeWarning()) {
            notifications.create(messageBundle.getMessage("privilegeWarning"))
                    .withThemeVariant(NotificationVariant.WARNING)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
