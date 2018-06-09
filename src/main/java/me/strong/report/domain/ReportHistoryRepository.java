package me.strong.report.domain;

import java.util.List;

/**
 * Created by taesu on 2018-06-08.
 */
public interface ReportHistoryRepository  {
    List<ReportHistory> findAllHistoriesByReportKey(Long reportKey);
}
