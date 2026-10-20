package com.digitilius.multireestr.view.legacyconnection;

import com.digitilius.multireestr.MultiReestrApplication;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {MultiReestrApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
class LegacyConnectionViewUiTest {

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void opensAndCheckDoesNotCrashWithoutLegacyDatabase() {
        viewNavigators.view(UiTestUtils.getCurrentView(), LegacyConnectionView.class).navigate();

        LegacyConnectionView view = UiTestUtils.getCurrentView();
        JmixButton checkButton = UiTestUtils.getComponent(view, "checkButton");
        checkButton.click();

        TypedTextField<String> statusField = UiTestUtils.getComponent(view, "statusField");
        TypedTextField<String> errorField = UiTestUtils.getComponent(view, "errorField");
        assertThat(statusField.getValue()).isNotBlank();
        assertThat(errorField.getValue()).isNotBlank();
    }
}
