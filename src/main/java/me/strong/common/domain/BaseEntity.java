package me.strong.common.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Created by taesu on 2018-06-08.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
@Audited(withModifiedFlag = true)      //설정 해주어야 Audit 추적 됨
public abstract class BaseEntity {

    @CreatedBy
    //ZonedDateTime을 못쓴다면 CreatedBy, UpdatedBy도 EntityListener로 처리하는 것이 깔끔한듯
    @Column(name = "CREATED_BY", nullable = false, updatable = false)        //nullable 설정이 있어야 한다
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", nullable = false)
    private Long updatedBy;

//    @CreatedDate
//    @Column(nullable = false)
//    private LocalDateTime createdAt;                    //LocalDateTime은 가능하나 ZonedDateTime은 Audit 기능으로 사용이 안된다
//
//    @LastModifiedDate                                     //EntityListenr를 통해 ZonedDateTime 적용 가능
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        //createdBy = ThreadLocal.get
        //updateBy = ThreadLocal.get
        this.createdAt = ZonedDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onPreUpdate() {
        //updateBy = ThreadLocal.get
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
