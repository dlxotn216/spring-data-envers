package me.strong.common.domain;

import lombok.Getter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Created by itaesu on 2021/03/05.
 *
 * @author Lee Tae Su
 * @version TBD
 * @since TBD
 */
@Getter
@Entity
@RevisionEntity
@Table(name = "REVINFO")
public class Revision implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REV_SEQ")
    @SequenceGenerator(name = "REV_SEQ", sequenceName = "REV_SEQ")
    @RevisionNumber
    @Column(name = "REV")
    private Long id;

    @RevisionTimestamp
    @Column(name = "REVTSTMP")
    private Long timestamp;


    @Transient
    public LocalDateTime getRevisionDate() {
        return LocalDateTime.from(Instant.ofEpochMilli(timestamp));
    }
}
