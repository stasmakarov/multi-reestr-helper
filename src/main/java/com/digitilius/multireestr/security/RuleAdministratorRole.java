package com.digitilius.multireestr.security;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Администратор правил", code = RuleAdministratorRole.CODE)
public interface RuleAdministratorRole {

    String CODE = "rule-administrator";

    @ViewPolicy(viewIds = {
            "mtr_CalendarYearLoadDialog",
            "mtr_ReferenceListExcelImportDialog",
            "mtr_CheckResultDialog"
    })
    void screens();

    @EntityPolicy(entityClass = Rule.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = Rule.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RuleVersion.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RuleVersion.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RuleParameter.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = RuleParameter.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ReferenceList.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ReferenceList.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ReferenceListItem.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ReferenceListItem.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = CalendarDay.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = CalendarDay.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @SpecificPolicy(resources = {
            MultiReestrSpecifics.RULE_ACTIVATE,
            MultiReestrSpecifics.REFERENCE_LIST_IMPORT,
            MultiReestrSpecifics.CHECK_ON_DEMAND
    })
    void entities();
}
