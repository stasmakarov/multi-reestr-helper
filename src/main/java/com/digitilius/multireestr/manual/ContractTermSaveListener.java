package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.entity.ContractTerm;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ContractTermSaveListener {

    @EventListener
    public void onSaving(EntitySavingEvent<?> event) {
        if (event.getEntity() instanceof ContractTerm term) {
            ContractTermRules.validateXor(term);
        }
    }
}
