package me.strong.report.domain;

import java.util.List;

/**
 * Created by taesu on 2018-06-08.
 */
public interface CustomRevisionRepository {
    List<Report> findCustomRepositoryRevisionsByReportKey(Long reportKey);

    List<Object[]> findCustomRepositoryRevisionsByReportKeyWithRevisionMetadata(Long reportKey);

    List<Object[]> findCustomRepositoryRevisionsByReportKeyWithModifiedFlag(Long reportKey);

    List<ReportHistory> findCustomReportHistoriesByReportKey(Long reprtKey);

    List<ReportHistory> findCustomReportHistoriesByReportKeyAsNativeQuery(Long reprtKey);
}
