package com.ptetext.entity.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "contacts", catalog = "ptetext_users")
@IdClass(ContactEntity.ContactId.class)
public class ContactEntity {

    public static class ContactId implements Serializable {
        private Integer ownerId;
        private Integer contactId;

        public ContactId() {
        }

        public ContactId(Integer ownerId, Integer contactId) {
            this.ownerId = ownerId;
            this.contactId = contactId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContactId other)) return false;
            return Objects.equals(ownerId, other.ownerId)
                    && Objects.equals(contactId, other.contactId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ownerId, contactId);
        }
    }

    @Id
    @Column(name = "owner_id")
    private Integer ownerId;

    @Id
    @Column(name = "contact_id")
    private Integer contactId;

    @Column(length = 100)
    private String nickname;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    @Column(name = "added_at", insertable = false, updatable = false)
    private LocalDateTime addedAt;

    protected ContactEntity() {
    }

    public ContactEntity(Integer ownerId, Integer contactId, String nickname) {
        this.ownerId = ownerId;
        this.contactId = contactId;
        this.nickname = nickname;
    }

    public Integer getOwnerId()        { return ownerId; }
    public Integer getContactId()      { return contactId; }
    public String getNickname()        { return nickname; }
    public boolean isFavorite()        { return favorite; }
    public LocalDateTime getAddedAt()  { return addedAt; }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
