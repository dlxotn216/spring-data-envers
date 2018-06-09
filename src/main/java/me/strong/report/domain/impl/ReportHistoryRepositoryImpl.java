package me.strong.report.domain.impl;

import me.strong.report.domain.QReportHistory;
import me.strong.report.domain.Report;
import me.strong.report.domain.ReportHistory;
import me.strong.report.domain.ReportHistoryRepository;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by taesu on 2018-06-08.
 */
@Repository
public class ReportHistoryRepositoryImpl extends QuerydslRepositorySupport implements ReportHistoryRepository {

    public ReportHistoryRepositoryImpl() {
        super(Report.class);
    }

    @Override
    public List<ReportHistory> findAllHistoriesByReportKey(Long reportKey) {
        QReportHistory qReportHistory = new QReportHistory("q1");

        /*
            dtype 조건 뺀 경우 아래 쿼리
            select reporthist0_.report_key as report_k2_2_, reporthist0_.created_at as created_3_2_, reporthist0_.created_by as created_4_2_, reporthist0_.updated_at as updated_5_2_, reporthist0_.updated_by as updated_6_2_, reporthist0_.signed_at as signed_a7_2_, reporthist0_.member_key as member_11_2_, reporthist0_.title as title8_2_, reporthist0_.signed_at_changed as signed_a9_2_, reporthist0_.title_changed as title_c10_2_ from report reporthist0_ where reporthist0_.dtype='ReportHistory' and reporthist0_.report_key=?

            dtype 조건 주면
            select reporthist0_.report_key as report_k2_2_, reporthist0_.created_at as created_3_2_, reporthist0_.created_by as created_4_2_, reporthist0_.updated_at as updated_5_2_, reporthist0_.updated_by as updated_6_2_, reporthist0_.signed_at as signed_a7_2_, reporthist0_.member_key as member_11_2_, reporthist0_.title as title8_2_, reporthist0_.dtype as dtype1_2_, reporthist0_.signed_at_changed as signed_a9_2_, reporthist0_.title_changed as title_c10_2_ from report reporthist0_ where reporthist0_.dtype='ReportHistory' and reporthist0_.report_key=? and reporthist0_.dtype=?
         */
        return from(qReportHistory)
                .where(qReportHistory.reportKey.eq(reportKey)).fetch();
    }
}
