package com.digitilius.multireestr.security;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ContractItemDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.LotDto;
import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.entity.legacy.NoticeDocumentDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Чтение поиска и карточки извещения. Назначается вместе с {@link UiMinimalRole}.
 */
@ResourceRole(name = "Notice viewer", code = NoticeViewerRole.CODE)
public interface NoticeViewerRole {

    String CODE = "notice-viewer";

    @ViewPolicy(viewIds = {"mtr_NoticeSearchView", "mtr_NoticeDetailView"})
    @MenuPolicy(menuIds = {"mtr_NoticeSearchView"})
    void screens();

    @EntityPolicy(entityClass = NoticeCandidateDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = NoticeCandidateDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = NoticeDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = NoticeDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = OrganizationDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = OrganizationDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = LotDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = LotDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = SpecPositionDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = SpecPositionDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = KtruCharacteristicDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = KtruCharacteristicDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = NoticeDocumentDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = NoticeDocumentDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ProtocolDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = ProtocolDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ContractDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = ContractDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ContractItemDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = ContractItemDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ExecutionDto.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = ExecutionDto.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void entities();
}
