package com.ptetext.service;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttachmentServiceTest {

    private AttachmentRepository repo;
    private AttachmentService service;

    @BeforeEach
    void setUp() {
        repo = mock(AttachmentRepository.class);
        service = new AttachmentService(repo);
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
        assertEquals(Optional.of(a), service.findFileByAttachmentId(3L));
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
}
