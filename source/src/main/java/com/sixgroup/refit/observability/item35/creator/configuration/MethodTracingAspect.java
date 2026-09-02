package com.sixgroup.refit.observability.item35.creator.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.concurrent.TimeUnit;

/**
 * Logs the route followed by an item through the public methods of the
 * application's Spring beans. Arguments and return values are intentionally
 * omitted to avoid leaking Kafka payloads, headers or configuration secrets.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "observability.method-tracing",
        name = "enabled",
        havingValue = "true"
)
public class MethodTracingAspect {

    @Around("execution(public * com.sixgroup.refit.observability.item35.creator.application..*(..))"
            + " || execution(public * com.sixgroup.refit.observability.item35.creator.infrastructure.consumer..*(..))"
            + " || execution(public * com.sixgroup.refit.observability.item35.creator.infrastructure.producer..*(..))"
            + " || execution(public * com.sixgroup.refit.observability.item35.creator.infrastructure.file..*(..))"
            + " || execution(public * com.sixgroup.refit.observability.item35.creator.infrastructure.repository..*(..))")
    public Object trace(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String method = methodName(joinPoint);
        final long start = System.nanoTime();

        log.info("METHOD_TRACE ENTER {}", method);
        try {
            final Object result = joinPoint.proceed();
            log.info("METHOD_TRACE EXIT {} durationMs={}", method, elapsedMillis(start));
            return result;
        } catch (Throwable throwable) {
            log.error("METHOD_TRACE ERROR {} durationMs={} exception={}",
                    method, elapsedMillis(start), throwable.getClass().getSimpleName());
            throw throwable;
        }
    }

    private String methodName(final ProceedingJoinPoint joinPoint) {
        final Class<?> targetClass = joinPoint.getTarget() == null
                ? ((MethodSignature) joinPoint.getSignature()).getDeclaringType()
                : ClassUtils.getUserClass(joinPoint.getTarget());
        return targetClass.getSimpleName() + "." + joinPoint.getSignature().getName();
    }

    private long elapsedMillis(final long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
