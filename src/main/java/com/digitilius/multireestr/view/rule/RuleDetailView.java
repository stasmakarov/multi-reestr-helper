package com.digitilius.multireestr.view.rule;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "rules/:id", layout = MainView.class)
@ViewController(id = "mtr_Rule.detail")
@ViewDescriptor(path = "rule-detail-view.xml")
@EditedEntityContainer("ruleDc")
public class RuleDetailView extends StandardDetailView<Rule> {

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Rule> event) {
        event.getEntity().setEnabled(true);
    }
}
