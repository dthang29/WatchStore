package com.pet_project.watchstore.aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingAspectTest {

    @Test
    void logsControllerAndServiceWithoutExposingArgumentsResultsOrExceptionMessages() {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(LoggingAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            String secret = "sensitive-value-123";
            assertEquals(secret, proxy(new SampleService()).echo(secret));
            assertThrows(IllegalArgumentException.class, () -> proxy(new SampleController()).fail(secret));

            String messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (left, right) -> left + "\n" + right);
            assertTrue(messages.contains("SampleService.echo"));
            assertTrue(messages.contains("SampleController.fail"));
            assertTrue(messages.contains("IllegalArgumentException"));
            assertFalse(messages.contains(secret));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private static <T> T proxy(T target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new LoggingAspect());
        return factory.getProxy();
    }

    @Service
    public static class SampleService {
        public String echo(String value) {
            return value;
        }
    }

    @RestController
    public static class SampleController {
        public void fail(String value) {
            throw new IllegalArgumentException(value);
        }
    }
}
