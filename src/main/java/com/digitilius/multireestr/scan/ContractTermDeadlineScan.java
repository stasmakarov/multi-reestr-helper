package com.digitilius.multireestr.scan;

import com.digitilius.multireestr.deadline.DeadlineAnchorResolver;
import com.digitilius.multireestr.deadline.DeadlineCalculator;
import com.digitilius.multireestr.entity.CompareOperator;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.Severity;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.manual.ContractTermDueDateService;
import com.digitilius.multireestr.rules.CheckResults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Component
public class ContractTermDeadlineScan {

    public static final String R09_DELIVERY = "R09_DELIVERY";
    public static final String R10_ACCEPTANCE = "R10_ACCEPTANCE";
    public static final String DETAIL_DELIVERY = "term=DELIVERY";
    public static final String DETAIL_ACCEPTANCE = "term=ACCEPTANCE";

    private final ContractTermDueDateService dueDateService;
    private final DeadlineAnchorResolver anchorResolver;
    private final DeadlineCalculator deadlineCalculator;

    public ContractTermDeadlineScan(
            ContractTermDueDateService dueDateService,
            DeadlineAnchorResolver anchorResolver,
            DeadlineCalculator deadlineCalculator
    ) {
        this.dueDateService = dueDateService;
        this.anchorResolver = anchorResolver;
        this.deadlineCalculator = deadlineCalculator;
    }

    public Assessment assess(
            ContractTerm term,
            NoticeDto notice,
            ContractDto contract,
            PlanScheduleEvent event,
            Rule rule,
            RuleVersion version,
            LocalDate today
    ) {
        Optional<LocalDate> due = dueDateService.dueDate(term, notice, contract, event);
        if (due.isEmpty()) {
            return Assessment.skipped();
        }
        DeadlineSpec spec = term.getTerm();
        CompareOperator operator = spec == null || spec.getCompareOperator() == null
                ? CompareOperator.NOT_LATER
                : spec.getCompareOperator();
        DeadlineAnchor target = spec == null || spec.getTarget() == null
                ? defaultTarget(term.getTermType())
                : spec.getTarget();
        LocalDate fact = anchorResolver.resolve(target, notice, contract, event).orElse(null);
        LocalDate compareDate = fact != null ? fact : today;
        boolean ok = deadlineCalculator.compare(compareDate, due.get(), operator);
        int deviation = (int) ChronoUnit.DAYS.between(due.get(), compareDate);
        String objectKey = String.valueOf(term.getContractLegacyId());
        String detail = detail(term.getTermType());
        Map<String, String> vars = Map.of(
                "due", due.get().toString(),
                "fact", fact == null ? "нет" : fact.toString(),
                "days", String.valueOf(Math.abs(deviation))
        );
        if (!ok) {
            CheckResultDto hit = CheckResults.hit(rule, objectKey, detail, rule.getSeverity(), vars);
            return Assessment.hit(hit, rule, version, due.get(), fact, deviation);
        }
        Integer warnBefore = spec == null ? null : spec.getWarnBeforeDays();
        if (warnBefore != null && warnBefore >= 0 && !due.get().isBefore(today)) {
            long daysLeft = ChronoUnit.DAYS.between(today, due.get());
            if (daysLeft <= warnBefore) {
                CheckResultDto hit = CheckResults.hit(rule, objectKey, detail, Severity.WARNING, vars);
                return Assessment.hit(hit, rule, version, due.get(), fact, (int) daysLeft);
            }
        }
        return Assessment.clean(rule, version, due.get(), fact, deviation);
    }

    static String ruleCode(ContractTermType type) {
        if (type == ContractTermType.DELIVERY) {
            return R09_DELIVERY;
        }
        if (type == ContractTermType.ACCEPTANCE) {
            return R10_ACCEPTANCE;
        }
        return null;
    }

    static String detail(ContractTermType type) {
        if (type == ContractTermType.ACCEPTANCE) {
            return DETAIL_ACCEPTANCE;
        }
        return DETAIL_DELIVERY;
    }

    private static DeadlineAnchor defaultTarget(ContractTermType type) {
        if (type == ContractTermType.ACCEPTANCE) {
            return DeadlineAnchor.ACCEPTANCE;
        }
        return DeadlineAnchor.DELIVERY_DOCUMENT;
    }

    public record Assessment(
            boolean evaluated,
            CheckResultDto hit,
            Rule rule,
            RuleVersion version,
            LocalDate dueDate,
            LocalDate factDate,
            Integer deviationDays
    ) {
        static Assessment skipped() {
            return new Assessment(false, null, null, null, null, null, null);
        }

        static Assessment clean(Rule rule, RuleVersion version, LocalDate due, LocalDate fact, Integer deviation) {
            return new Assessment(true, null, rule, version, due, fact, deviation);
        }

        static Assessment hit(
                CheckResultDto hit,
                Rule rule,
                RuleVersion version,
                LocalDate due,
                LocalDate fact,
                Integer deviation
        ) {
            return new Assessment(true, hit, rule, version, due, fact, deviation);
        }
    }
}
