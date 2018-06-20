package me.strong.report.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by taesu on 2018-06-20.
 */
@Getter
@Setter
public class ReportHistoryDto {
    private BigInteger reportKey;
    private String title;
    private Boolean titleChanged;
    private ZonedDateTime signedAt;
    private Boolean signedAtChanged;
    private BigInteger createdBy;
    private ZonedDateTime createdAt;
    private BigInteger updatedBy;
    private Boolean updatedByChanged;
    private ZonedDateTime updatedAt;
    private Boolean updatedAtChanged;

    //QLRM에선 Date 관련 Timestamp로 처리하는 생성자를 요구한다
    public ReportHistoryDto(BigInteger reportKey, String title, Boolean titleChanged, Timestamp signedAt, Boolean signedAtChanged, BigInteger createdBy, Timestamp createdAt, BigInteger updatedBy, Boolean updatedByChanged, Timestamp updatedAt, Boolean updatedAtChanged) {
        this.reportKey = reportKey;
        this.title = title;
        this.titleChanged = titleChanged;
        this.signedAt = this.convertTimestampToZonedDateTime(signedAt);
        this.signedAtChanged = signedAtChanged;
        this.createdBy = createdBy;
        this.createdAt = this.convertTimestampToZonedDateTime(createdAt);
        this.updatedBy = updatedBy;
        this.updatedByChanged = updatedByChanged;
        this.updatedAt = this.convertTimestampToZonedDateTime(updatedAt);
        this.updatedAtChanged = updatedAtChanged;
    }

    private ZonedDateTime convertTimestampToZonedDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
//        return ZonedDateTime.of(timestamp.toLocalDateTime(), ZoneId.of("UTC"));   -> normalize() 호출을 통해 변환 하는데 여기서 defaultTimezone(Asia/Seoul)으로 바뀜
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC"));     //절대적인 시간으로 바꾸므로 서버에 시간이 정확히 UTC라면 변환이 제대로 동작 함
    }

    @Override
    public String toString() {
        return "ReportHistoryDto{" +
                "reportKey=" + reportKey +
                ", title='" + title + '\'' +
                ", titleChanged=" + titleChanged +
                ", signedAt=" + signedAt +
                ", signedAtChanged=" + signedAtChanged +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedBy=" + updatedBy +
                ", updatedByChanged=" + updatedByChanged +
                ", updatedAt=" + updatedAt +
                ", updatedAtChanged=" + updatedAtChanged +
                '}';
    }
}
