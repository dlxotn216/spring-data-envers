package me.strong.common.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;

/**
 * Created by itaesu on 2021/03/05.
 *
 * @author Lee Tae Su
 * @version TBD
 * @since TBD
 */
@Setter
@Getter
@Embeddable
public class Audit {
    @Column(name = "created_by")
    @CreatedBy
    private Long createdBy;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "modified_by")
    @LastModifiedBy
    private Long modifiedBy;

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
