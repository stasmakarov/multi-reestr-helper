package com.digitilius.multireestr.legacy.test_support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class DockerRequiredCondition implements ExecutionCondition {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(DockerRequiredCondition.class)
    public @interface RequireDocker {
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (DockerAvailability.dockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker is available");
        }
        if (DockerAvailability.allowNoDocker()) {
            return ConditionEvaluationResult.disabled(
                    "Docker is not available; skipped because allowNoDocker=true (-PallowNoDocker)"
            );
        }
        throw new IllegalStateException(
                "Docker is required for this test. Re-run with ./gradlew test -PallowNoDocker to skip tests that need Docker."
        );
    }
}
