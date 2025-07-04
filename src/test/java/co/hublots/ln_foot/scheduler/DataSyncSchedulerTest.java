package co.hublots.ln_foot.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import co.hublots.ln_foot.dto.SyncStatusDto;
import co.hublots.ln_foot.services.DataSyncService;

@ExtendWith(MockitoExtension.class)
class DataSyncSchedulerTest {

    @Mock
    private DataSyncService dataSyncServiceMock;

    @InjectMocks
    private DataSyncScheduler dataSyncScheduler;

    @Test
    void scheduleDailyFullSync_callsSyncMainFixturesWithDateParam() {
        // Arrange
        ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.captor();
        when(dataSyncServiceMock.syncMainFixtures(paramsCaptor.capture()))
            .thenReturn(any(SyncStatusDto.class)); // Mocking the return value to avoid any issues

        // Act
        dataSyncScheduler.scheduleDailyFullSync();

        // Assert
        verify(dataSyncServiceMock, times(1)).syncMainFixtures(anyMap());
        Map<String, String> capturedParams = paramsCaptor.getValue();
        assertEquals(LocalDate.now(ZoneOffset.UTC).toString(), capturedParams.get("date"));
    }

    @Test
    void scheduleHourlyRecentFixturesSync_callsSyncMainFixturesWithLiveParam() {
        // Arrange
        ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.captor();
        when(dataSyncServiceMock.syncMainFixtures(paramsCaptor.capture()))
            .thenReturn(any(SyncStatusDto.class)); // Mocking the return value to avoid any issues

        // Act
        dataSyncScheduler.scheduleHourlyRecentFixturesSync();

        // Assert
        verify(dataSyncServiceMock, times(1)).syncMainFixtures(anyMap());
        Map<String, String> capturedParams = paramsCaptor.getValue();
        assertEquals("all", capturedParams.get("live"));
    }

    @Test
    void scheduleDailyFullSync_whenServiceThrowsException_logsError() {
        // Arrange
        doThrow(new RuntimeException("Test Sync Error")).when(dataSyncServiceMock).syncMainFixtures(anyMap());

        // Capture logs
        Logger schedulerLogger = (Logger) LoggerFactory.getLogger(DataSyncScheduler.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        schedulerLogger.addAppender(listAppender);

        // Act
        dataSyncScheduler.scheduleDailyFullSync();

        // Assert
        // Check that the method completed (didn't rethrow the exception from scheduler)
        // and that an error was logged.
        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream()
            .anyMatch(event -> event.getLevel().toString().equals("ERROR") &&
                               event.getFormattedMessage().contains("Error during scheduled daily full fixture sync:")),
            "Expected error log message not found.");

        schedulerLogger.detachAppender(listAppender); // Clean up
    }

    @Test
    void scheduleHourlyRecentFixturesSync_whenServiceThrowsException_logsError() {
        // Arrange
        doThrow(new RuntimeException("Test Hourly Sync Error")).when(dataSyncServiceMock).syncMainFixtures(anyMap());

        Logger schedulerLogger = (Logger) LoggerFactory.getLogger(DataSyncScheduler.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        schedulerLogger.addAppender(listAppender);

        // Act
        dataSyncScheduler.scheduleHourlyRecentFixturesSync();

        // Assert
        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream()
            .anyMatch(event -> event.getLevel().toString().equals("ERROR") &&
                               event.getFormattedMessage().contains("Error during scheduled hourly recent fixtures sync:")),
            "Expected hourly error log message not found.");

        schedulerLogger.detachAppender(listAppender);
    }
}
