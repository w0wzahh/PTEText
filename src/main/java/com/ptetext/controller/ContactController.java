package com.ptetext.controller;

import com.ptetext.aspect.RequiresSession;
import com.ptetext.aspect.SessionContext;
import com.ptetext.dto.Dtos.AddContactRequest;
import com.ptetext.dto.Dtos.ContactDto;
import com.ptetext.service.ContactService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts")
public class ContactController {

    private final ContactService contacts;

    public ContactController(ContactService contacts) {
        this.contacts = contacts;
    }

    @GetMapping
    @RequiresSession
    @Operation(summary = "List my contacts")
    public ResponseEntity<List<ContactDto>> list(@RequestParam String sessionId) {
        return ResponseEntity.ok(contacts.listContacts(SessionContext.userId()));
    }

    @PostMapping
    @RequiresSession
    @Operation(summary = "Add a contact by username (or update its nickname)")
    public ResponseEntity<ContactDto> add(@RequestBody AddContactRequest req) {
        ContactDto dto = contacts.addContact(SessionContext.userId(),
                req.username(), req.nickname());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{contactUserId}")
    @RequiresSession
    @Operation(summary = "Remove a contact")
    public ResponseEntity<Void> remove(@PathVariable int contactUserId,
                                       @RequestParam String sessionId) {
        contacts.removeContact(SessionContext.userId(), contactUserId);
        return ResponseEntity.noContent().build();
    }
}
