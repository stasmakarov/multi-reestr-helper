package com.digitilius.multireestr.security;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Специалист", code = SpecialistRole.CODE)
public interface SpecialistRole {

    String CODE = "specialist";

    @ViewPolicy(viewIds = {
            "mtr_Rule.list", "mtr_Rule.detail",
            "mtr_RuleVersion.detail", "mtr_RuleParameter.detail",
            "mtr_ReferenceList.list", "mtr_ReferenceList.detail", "mtr_ReferenceListItem.detail",
            "mtr_Violation.list", "mtr_Violation.detail",
            "mtr_CheckRun.list", "mtr_CheckRun.detail",
            "mtr_CheckResultDialog",
            "mtr_CalendarDay.list", "mtr_CalendarDay.detail",
            "mtr_ContractTerm.list", "mtr_ContractTerm.detail",
            "mtr_PlanScheduleEvent.list", "mtr_PlanScheduleEvent.detail",
            "mtr_ContractLookupDialog"
    })
    @MenuPolicy(menuIds = {
            "rules", "control", "catalogs",
            "mtr_Rule.list", "mtr_ReferenceList.list",
            "mtr_Violation.list", "mtr_CheckRun.list",
            "mtr_CalendarDay.list", "mtr_ContractTerm.list", "mtr_PlanScheduleEvent.list"
    })
    void screens();

    @EntityPolicy(entityClass = Rule.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = Rule.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = RuleVersion.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = RuleVersion.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = RuleParameter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = RuleParameter.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ReferenceList.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ReferenceList.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ReferenceListItem.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ReferenceListItem.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Violation.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    @EntityAttributePolicy(entityClass = Violation.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = CheckRun.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    @EntityAttributePolicy(entityClass = CheckRun.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = CheckResultDto.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = CheckResultDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @SpecificPolicy(resources = MultiReestrSpecifics.CHECK_ON_DEMAND)
    @EntityPolicy(entityClass = CalendarDay.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = CalendarDay.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ContractTerm.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ContractTerm.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PlanScheduleEvent.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = PlanScheduleEvent.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PlanScheduleDto.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = PlanScheduleDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void entities();
}
