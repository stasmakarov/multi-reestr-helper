package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.security.MultiReestrSpecifics;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.SaveContext;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RuleVersionService {

    private final DataManager dataManager;
    private final FetchPlans fetchPlans;
    private final Metadata metadata;
    private final MetadataTools metadataTools;
    private final AccessManager accessManager;
    private final CurrentAuthentication currentAuthentication;

    public RuleVersionService(
            DataManager dataManager,
            FetchPlans fetchPlans,
            Metadata metadata,
            MetadataTools metadataTools,
            AccessManager accessManager,
            CurrentAuthentication currentAuthentication
    ) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.accessManager = accessManager;
        this.currentAuthentication = currentAuthentication;
    }

    @Transactional
    public RuleVersion copyToDraft(RuleVersion source) {
        RuleVersion loaded = reload(source.getId());
        Rule rule = loaded.getRule();
        int nextNumber = 1;
        List<RuleVersion> versions = rule.getVersions() == null ? List.of() : rule.getVersions();
        for (RuleVersion version : versions) {
            if (version.getVersionNumber() != null) {
                nextNumber = Math.max(nextNumber, version.getVersionNumber() + 1);
            }
        }
        RuleVersion draft = metadata.create(RuleVersion.class);
        draft.setRule(rule);
        draft.setVersionNumber(nextNumber);
        draft.setStatus(RuleVersionStatus.DRAFT);
        draft.setValidFrom(loaded.getValidFrom() == null ? LocalDate.now() : loaded.getValidFrom());
        draft.setValidTo(loaded.getValidTo());
        draft.setChangeNote(null);
        if (loaded.getDeadline() != null) {
            DeadlineSpec copy = metadata.create(DeadlineSpec.class);
            metadataTools.copy(loaded.getDeadline(), copy);
            draft.setDeadline(copy);
        }
        List<RuleParameter> copied = new ArrayList<>();
        if (loaded.getParameters() != null) {
            for (RuleParameter parameter : loaded.getParameters()) {
                RuleParameter copy = metadata.create(RuleParameter.class);
                copy.setRuleVersion(draft);
                copy.setCode(parameter.getCode());
                copy.setParamType(parameter.getParamType());
                copy.setValue(parameter.getValue());
                copy.setReferenceList(parameter.getReferenceList());
                copied.add(copy);
            }
        }
        draft.setParameters(copied);
        SaveContext saveContext = new SaveContext().saving(draft);
        for (RuleParameter parameter : copied) {
            saveContext.saving(parameter);
        }
        dataManager.save(saveContext);
        return reload(draft.getId());
    }

    @Transactional
    public RuleVersion activate(RuleVersion version) {
        requireActivatePermission();
        RuleVersion loaded = reload(version.getId());
        if (loaded.getStatus() != RuleVersionStatus.DRAFT) {
            throw new IllegalStateException("Активировать можно только черновик");
        }
        if (loaded.getValidFrom() == null) {
            throw new IllegalStateException("Укажите дату начала действия");
        }
        List<RuleVersion> versions = loaded.getRule().getVersions() == null
                ? List.of()
                : loaded.getRule().getVersions();
        for (RuleVersion other : versions) {
            if (other.getId().equals(loaded.getId()) || other.getStatus() != RuleVersionStatus.ACTIVE) {
                continue;
            }
            if (overlaps(loaded.getValidFrom(), loaded.getValidTo(), other.getValidFrom(), other.getValidTo())) {
                throw new IllegalStateException("Период пересекается с действующей версией " + other.getVersionNumber());
            }
        }
        loaded.setStatus(RuleVersionStatus.ACTIVE);
        loaded.setApprovedBy(currentAuthentication.getUser().getUsername());
        loaded.setApprovedAt(OffsetDateTime.now());
        return dataManager.save(loaded);
    }

    @Transactional
    public RuleVersion archive(RuleVersion version) {
        requireActivatePermission();
        RuleVersion loaded = reload(version.getId());
        if (loaded.getStatus() != RuleVersionStatus.ACTIVE) {
            throw new IllegalStateException("В архив можно перевести только действующую версию");
        }
        loaded.setStatus(RuleVersionStatus.ARCHIVED);
        return dataManager.save(loaded);
    }

    static boolean overlaps(LocalDate from1, LocalDate to1, LocalDate from2, LocalDate to2) {
        LocalDate end1 = to1 == null ? LocalDate.MAX : to1;
        LocalDate end2 = to2 == null ? LocalDate.MAX : to2;
        return !from1.isAfter(end2) && !from2.isAfter(end1);
    }

    private void requireActivatePermission() {
        SpecificOperationAccessContext context =
                new SpecificOperationAccessContext(MultiReestrSpecifics.RULE_ACTIVATE);
        accessManager.applyRegisteredConstraints(context);
        if (!context.isPermitted()) {
            throw new IllegalStateException("Нет права активировать версию правила");
        }
    }

    private RuleVersion reload(UUID id) {
        FetchPlan fetchPlan = fetchPlans.builder(RuleVersion.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("rule", rule -> rule.addFetchPlan(FetchPlan.BASE).add("versions", FetchPlan.BASE))
                .add("parameters", parameters -> parameters.addFetchPlan(FetchPlan.BASE).add("referenceList", FetchPlan.INSTANCE_NAME))
                .build();
        return dataManager.load(RuleVersion.class)
                .id(id)
                .fetchPlan(fetchPlan)
                .one();
    }
}
