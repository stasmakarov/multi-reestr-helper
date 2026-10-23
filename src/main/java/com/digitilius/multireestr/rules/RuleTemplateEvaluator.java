package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;

import java.util.List;

public interface RuleTemplateEvaluator {

    RuleTemplateType templateType();

    List<CheckResultDto> evaluate(NoticeDto notice, Rule rule, RuleVersion version, RuleEvalParams params);
}
