package me.strong.report.domain;

import lombok.Getter;
import me.strong.common.domain.BaseEntity;
import me.strong.member.domain.Member;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.AuditOverrides;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;

/**
 * Created by taesu on 2018-06-08.
 */
@Entity(name = "Report")
@Getter
@Table
@Audited(withModifiedFlag = true)       //컬럼이 변경 되었는지 컬럼마다 추적
@EntityListeners(value = {AuditingEntityListener.class})
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ReportSequenceGenerator")
    @SequenceGenerator(name = "ReportSequenceGenerator", sequenceName = "REPORT_SEQ")
    private Long reportKey;

    private String title;

    private ZonedDateTime signedAt;

    @ManyToOne
    @JoinColumn(name = "SIGNER")
    private Member signer;

    protected Report() {

    }

    public Report(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void sign(Member signer) {
        if (this.signer != null) {
            throw new IllegalStateException("이미 서명한 Report는 재 서명이 불가능합니다.");
        }
        this.signer = signer;
        this.signedAt = ZonedDateTime.now(signer.getZoneId());
        this.signer.addSignedReports(this);
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportKey=" + reportKey +
                ", title='" + title + '\'' +
                ", signedAt=" + signedAt +
                ", signer=" + signer +
                "} " + super.toString();
    }
}
