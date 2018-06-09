package me.strong.report.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by taesu on 2018-06-08.
 */
@Entity(name = "Report_History")
@Getter
@Setter
@Table(name = "REPORT_HISTORY")
public class ReportHistory extends Report {

    protected ReportHistory() {

    }

    @Column(name = "REV", updatable = false)
    private int rev;

    @Column(name = "TITLE_CHANGED", updatable = false)
    private Boolean titleChanged;

    @Column(name = "SIGNED_AT_CHANGED", updatable = false)
    private Boolean signedAtChanged;

    @Column(name = "SIGNER_CHANGED", updatable = false)     //SIGNER_KEY_CHANGED가 아님을 주의
    private Boolean signerChanged;

    @Column(name = "CREATED_BY_CHANGED", updatable = false)
    private Boolean createdByChanged;

    @Column(name = "UPDATED_BY_CHANGED", updatable = false)
    private Boolean updatedByChanged;

    @Column(name = "CREATED_AT_CHANGED", updatable = false)
    private Boolean createdAtChanged;

    @Column(name = "UPDATED_AT_CHANGED", updatable = false)
    private Boolean updatedAtChanged;

    @Override
    public String toString() {
        return "ReportHistory{" +
                "titleChanged=" + titleChanged +
                ", signedAtChanged=" + signedAtChanged +
                ", signerChanged=" + signerChanged +
                ", createdByChanged=" + createdByChanged +
                ", updatedByChanged=" + updatedByChanged +
                ", createdAtChanged=" + createdAtChanged +
                ", updatedAtChanged=" + updatedAtChanged +
                "} " + super.toString();
    }
}
