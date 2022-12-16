package org.apereo.cas.util.logging;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DisabledLogMessageSummarizerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Utility")
@Slf4j
public class DisabledLogMessageSummarizerTests {
    @Test
    public void verifyOperation() throws Exception {
        val summarizer = new DisabledLogMessageSummarizer();
        assertFalse(summarizer.shouldSummarize(LOGGER));
        assertTrue(summarizer.summarizeStackTrace("Message", new IllegalArgumentException("Error")).isEmpty());
    }
}
