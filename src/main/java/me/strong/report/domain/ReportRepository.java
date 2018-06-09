package me.strong.report.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

/**
 * Created by taesu on 2018-06-08.
 */
public interface ReportRepository extends JpaRepository<Report, Long>, RevisionRepository<Report, Long, Integer>, CustomRevisionRepository {
}
