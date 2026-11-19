package com.ptetext.controller;

import com.ptetext.dto.Dtos;
import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.dto.Dtos.MessageIdResponse;
import com.ptetext.exception.FileFailedToUploadException;
import com.ptetext.service.AttachmentService;
import com.ptetext.service.AuthService;
import com.ptetext.service.ChatService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentControllerTest {

    private AttachmentService attachment;
    private AuthService auth;
    private ChatService chat;
    private AttachmentController controller;

    @BeforeEach
    void setUp() {
        attachment = mock(AttachmentService.class);
        auth = mock(AuthService.class);
        chat = mock(ChatService.class);
        controller = new AttachmentController(attachment, auth, chat);
    }

    private MultipartFile file(String name, String mime) {
        MultipartFile f = mock(MultipartFile.class);
        when(f.getOriginalFilename()).thenReturn(name);
        when(f.getContentType()).thenReturn(mime);
        when(f.getSize()).thenReturn(100L);
        when(f.isEmpty()).thenReturn(false);
        return f;
    }

    private Dtos.SendAttachmentRequest req(String session, MultipartFile file) {
        return new Dtos.SendAttachmentRequest(session, file);
    }

    @Test
    void uploadSucceedsWithValidSessionAndFile() {
        when(auth.validateSession("tok")).thenReturn(Optional.of(1));
        when(chat.sendAttachment(2, 1, "notes.txt")).thenReturn(30L);

        ResponseEntity<?> res = controller.sendAttachment(2,
                req("tok", file("notes.txt", "text/plain")));

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals(30L, ((MessageIdResponse) res.getBody()).messageId());
        verify(attachment).sendAttachment(eq(30L), eq(1), eq("notes.txt"),
                eq("text/plain"), eq(100L), any(MultipartFile.class));
    }

    @Test
    void uploadRejectsInvalidSession() {
        when(auth.validateSession("bad")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.sendAttachment(2,
                req("bad", file("a.txt", "text/plain")));

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertInstanceOf(ErrorResponse.class, res.getBody());
        verify(chat, never()).sendAttachment(anyInt(), anyInt(), anyString());
    }

    @Test
    void uploadRejectsMissingFile() {
        when(auth.validateSession("tok")).thenReturn(Optional.of(1));

        assertThrows(IllegalArgumentException.class,
                () -> controller.sendAttachment(2, req("tok", null)));
        verify(chat, never()).sendAttachment(anyInt(), anyInt(), anyString());
    }

    @Test
    void uploadRejectsDisallowedExtension() {
        when(auth.validateSession("tok")).thenReturn(Optional.of(1));

        assertThrows(IllegalArgumentException.class,
                () -> controller.sendAttachment(2, req("tok", file("virus.exe", null))));
        verify(chat, never()).sendAttachment(anyInt(), anyInt(), anyString());
    }

    @Test
    void uploadFailureSoftDeletesTheMessage() {
        when(auth.validateSession("tok")).thenReturn(Optional.of(1));
        when(chat.sendAttachment(2, 1, "notes.txt")).thenReturn(30L);
        org.mockito.Mockito.doThrow(new FileFailedToUploadException("disk"))
                .when(attachment).sendAttachment(anyLong(), anyInt(), anyString(),
                        anyString(), any(Long.class), any(MultipartFile.class));

        assertThrows(FileFailedToUploadException.class,
                () -> controller.sendAttachment(2, req("tok", file("notes.txt", "text/plain"))));
        verify(chat).hideMessage(2, 30L, 1);
    }
}
