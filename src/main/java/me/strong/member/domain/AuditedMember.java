package me.strong.member.domain;

import lombok.Getter;
import me.strong.common.domain.Audit;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * Created by itaesu on 2021/03/05.
 *
 * @author Lee Tae Su
 * @version TBD
 * @since TBD
 */
@Entity
@Getter
@Table(name = "AUD_MEMBER")
@Audited(withModifiedFlag = true)
@EntityListeners(value = {AuditingEntityListener.class})
public class AuditedMember {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUD_MEMBER_SEQ")
    @SequenceGenerator(name = "AUD_MEMBER_SEQ", sequenceName = "AUD_MEMBER_SEQ")
    private Long key;

    private String id;

    private String name;

    @Embedded
    private Audit audit;

    public Long getCreatedBy() {
        return audit.getCreatedBy();
    }
}
