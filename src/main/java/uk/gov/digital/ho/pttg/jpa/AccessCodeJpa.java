package uk.gov.digital.ho.pttg.jpa;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access")
@Access(AccessType.FIELD)
@NoArgsConstructor
@Getter
public class AccessCodeJpa {

    @Id
    @Column(name = "id")
    private long id = AccessRepository.ACCESS_ID;

    private LocalDateTime expiry;

    private LocalDateTime updatedDate = LocalDateTime.now();

    private String code;

    public AccessCodeJpa(LocalDateTime expiry, String code) {
        this.expiry = expiry;
        this.code = code;
    }

    @PrePersist
    @PreUpdate
    public void onSaveOrUpdate() {
        updatedDate = LocalDateTime.now();
    }
}