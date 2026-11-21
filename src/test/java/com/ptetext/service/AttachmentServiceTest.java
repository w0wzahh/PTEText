package com.ptetext.service;

import com.ptetext.dto.Dtos;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.FileFailedToUploadException;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
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
import static org.mockito.Mockito.never;
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
        when(repo.findFileByName("report.pdf")).thenReturn(Optional.of(a));
        assertEquals(a, service.findFileByName("report.pdf"));
    }

    @Test
    void findByNameThrowsOnMiss() {
        when(repo.findFileByName("nope.txt")).thenReturn(Optional.empty());
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
    void findByMessageIdReturnsResult() {
        AttachmentEntity a = attachment("a.zip");
        when(repo.findFileByMessageId(5L)).thenReturn(Optional.of(a));
        assertEquals(a, service.findFileByMessageId(5L));
    }

    @Test
    void findByMessageIdThrowsOnMiss() {
        when(repo.findFileByMessageId(42L)).thenReturn(Optional.empty());
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

    @Test
    void sendAttachmentDeletesStoredFileWhenInsertFails() throws IOException {
        MultipartFile mpf = mock(MultipartFile.class);
        when(mpf.getOriginalFilename()).thenReturn("doc.pdf");
        org.mockito.Mockito.doAnswer(inv -> {
            Path target = inv.getArgument(0);
            java.nio.file.Files.createDirectories(target.getParent());
            java.nio.file.Files.write(target, new byte[]{1});
            return null;
        }).when(mpf).transferTo(any(Path.class));
        doThrow(new RuntimeException("proc blew up")).when(repo)
                .fileUpload(any(), anyInt(), anyString(), anyString(), any(), anyString());

        assertThrows(RuntimeException.class,
                () -> service.sendAttachment(5L, 1, "doc.pdf", "application/pdf", 1L, mpf));

        try (var stream = java.nio.file.Files.list(Path.of("target/test-uploads"))) {
            assertEquals(0, stream.count(), "orphan file left on disk");
        }
    }

    @Test
    void downloadFileReturnsResource() throws IOException {
        Path dir = Path.of("target/test-uploads").toAbsolutePath();
        Files.createDirectories(dir);
        Files.write(dir.resolve("stored-abc.txt"), "hello".getBytes());
        AttachmentEntity a = attachment("note.txt");
        a.setStoredPath("stored-abc.txt");
        when(repo.findFileByAttachmentId(7L)).thenReturn(Optional.of(a));

        Dtos.DownloadResult r = service.downloadFile("attachmentId", "7");

        assertTrue(r.resource().exists());
        assertEquals("note.txt", r.originalName());
        assertEquals("image/png", r.mimeType());
        assertEquals(1024L, r.sizeBytes());
        Files.delete(dir.resolve("stored-abc.txt"));
    }

    @Test
    void downloadFileRejectsPathTraversal() {
        AttachmentEntity a = attachment("evil.txt");
        a.setStoredPath("../outside.txt");
        when(repo.findFileByAttachmentId(8L)).thenReturn(Optional.of(a));

        assertThrows(NoFileFoundException.class,
                () -> service.downloadFile("attachmentId", "8"));
    }

    @Test
    void downloadFileRejectsNonNumericId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.downloadFile("attachmentId", "abc"));
    }

    @Test
    void fileDeleteRemovesFileAndRow() throws IOException {
        Path dir = Path.of("target/test-uploads").toAbsolutePath();
        Files.createDirectories(dir);
        Path stored = dir.resolve("del-me.txt");
        Files.write(stored, "bye".getBytes());
        AttachmentEntity a = attachment("del.txt");
        a.setStoredPath("del-me.txt");
        when(repo.findFileByAttachmentId(9L)).thenReturn(Optional.of(a));

        service.fileDelete(9L, 1);

        assertTrue(Files.notExists(stored));
        verify(repo).fileDeletion(9L);
        verify(audit).log(eq(1), eq("ATTACHMENT_DELETED"), anyString());
    }

    @Test
    void fileDeleteStillRemovesRowWhenFileMissing() {
        AttachmentEntity a = attachment("ghost.txt");
        a.setStoredPath("never-existed.bin");
        when(repo.findFileByAttachmentId(10L)).thenReturn(Optional.of(a));

        service.fileDelete(10L, 1);

        verify(repo).fileDeletion(10L);
    }

    @Test
    void fileDeleteRejectsNonOwner() {
        AttachmentEntity a = attachment("not-yours.txt");
        a.setStoredPath("x.txt");
        when(repo.findFileByAttachmentId(11L)).thenReturn(Optional.of(a));

        assertThrows(IllegalArgumentException.class,
                () -> service.fileDelete(11L, 2));
        verify(repo, never()).fileDeletion(any());
    }
}
