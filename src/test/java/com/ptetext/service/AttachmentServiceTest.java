package com.ptetext.service;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.FileFailedToUploadException;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentServiceTest {

    private AttachmentRepository repo;
    private AuditLogger audit;
    private AttachmentService service;

    @BeforeEach
    void setUp() {
        repo = mock(AttachmentRepository.class);
        audit = mock(AuditLogger.class);
        service = new AttachmentService(repo, audit, "target/test-uploads");
    }

    private AttachmentEntity attachment(String name) {
        return new AttachmentEntity(5L, 1, name, "image/png", 1024);
    }

    @Test
    void findByNameReturnsResults() {
        AttachmentEntity a = attachment("report.pdf");
        when(repo.findFileByName("report.pdf")).thenReturn(List.of(a));
        assertEquals(List.of(a), service.findFileByName("report.pdf"));
    }

    @Test
    void findByNameThrowsOnMiss() {
        when(repo.findFileByName("nope.txt")).thenReturn(List.of());
        NoFileFoundException ex = assertThrows(NoFileFoundException.class,
                () -> service.findFileByName("nope.txt"));
        assertTrue(ex.getMessage().contains("nope.txt"));
    }

    @Test
    void findByIdReturnsEntity() {
        AttachmentEntity a = attachment("photo.png");
        when(repo.findFileByAttachmentId(3L)).thenReturn(Optional.of(a));
        assertEquals(a, service.findFileByAttachmentId(3L));
    }

    @Test
    void findByIdThrowsOnMiss() {
        when(repo.findFileByAttachmentId(99L)).thenReturn(Optional.empty());
        NoFileFoundException ex = assertThrows(NoFileFoundException.class,
                () -> service.findFileByAttachmentId(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void findByMessageIdReturnsResults() {
        AttachmentEntity a = attachment("a.zip");
        AttachmentEntity b = attachment("b.zip");
        when(repo.findFileByMessageId(5L)).thenReturn(List.of(a, b));
        assertEquals(List.of(a, b), service.findFileByMessageId(5L));
    }

    @Test
    void findByMessageIdThrowsOnMiss() {
        when(repo.findFileByMessageId(42L)).thenReturn(List.of());
        assertThrows(NoFileFoundException.class,
                () -> service.findFileByMessageId(42L));
    }

    @Test
    void findByUploaderIdReturnsResults() {
        AttachmentEntity a = attachment("mine.docx");
        when(repo.findFileByUploaderId(7)).thenReturn(List.of(a));
        assertEquals(List.of(a), service.findFileByUploaderId(7));
    }

    @Test
    void findByUploaderIdThrowsOnMiss() {
        when(repo.findFileByUploaderId(123)).thenReturn(List.of());
        assertThrows(NoFileFoundException.class,
                () -> service.findFileByUploaderId(123));
    }

    @Test
    void sendAttachmentStoresFileAndAudits() throws IOException {
        MultipartFile mpf = mock(MultipartFile.class);
        when(mpf.getOriginalFilename()).thenReturn("notes.txt");

        service.sendAttachment(5L, 1, "notes.txt", "text/plain", 100L, mpf);

        verify(mpf).transferTo(any(Path.class));
        verify(repo).fileUpload(any(Long.class), anyInt(),
                eq("notes.txt"), eq("text/plain"), eq(100L), anyString());
        verify(audit).log(eq(1), eq("ATTACHMENT_ADDED"), anyString());
    }

    @Test
    void sendAttachmentThrowsWhenWriteFails() throws IOException {
        MultipartFile mpf = mock(MultipartFile.class);
        when(mpf.getOriginalFilename()).thenReturn("x.png");
        doThrow(new IOException("disk full")).when(mpf).transferTo(any(Path.class));

        assertThrows(FileFailedToUploadException.class,
                () -> service.sendAttachment(5L, 1, "x.png", "image/png", 5L, mpf));
    }
}
