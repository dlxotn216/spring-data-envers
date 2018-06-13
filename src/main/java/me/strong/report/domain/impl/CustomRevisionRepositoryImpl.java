package me.strong.report.domain.impl;

import me.strong.report.domain.CustomRevisionRepository;
import me.strong.report.domain.Report;
import me.strong.report.domain.ReportHistory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditProperty;
import org.hibernate.envers.query.internal.property.EntityPropertyName;
import org.hibernate.envers.query.internal.property.ModifiedFlagPropertyName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by taesu on 2018-06-08.
 * <p>
 * 동일 패키지 하위에 존재해야 한다
 */
public class CustomRevisionRepositoryImpl implements CustomRevisionRepository {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public List<Report> findCustomRepositoryRevisionsByReportKey(Long reportKey) {
        AuditReader reader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());

        List<Report> revisions = reader.createQuery().forRevisionsOfEntity(Report.class, true, true)
                .add(AuditEntity.id().eq(reportKey))
//                .add(AuditEntity.property("firstName").hasChanged())
                .getResultList();
        return revisions;
    }

    @Override
    public List<Object[]> findCustomRepositoryRevisionsByReportKeyWithRevisionMetadata(Long reportKey) {
        AuditReader reader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());

        List<Object[]> revisions = reader.createQuery().forRevisionsOfEntity(Report.class, false, true)
                .add(AuditEntity.id().eq(reportKey))
//                .add(AuditEntity.property("firstName").hasChanged())
                .getResultList();
        return revisions;
    }

    @Override
    public List<Object[]> findCustomRepositoryRevisionsByReportKeyWithModifiedFlag(Long reportKey) {
        AuditReader reader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());
//
//        //hibernate 5.3에 추가 되어있는 기능으로 spring-data-envers 2.0.7에선 사용 불가
        //https://discourse.hibernate.org/t/how-to-retrieve-the-modification-flags-with-hibernate-envers/259/2
//        List<Object[]> revisions = reader.createQuery().forRevisionsOfEntityWithChanges(Report.class, false)
//                .add(AuditEntity.id().eq(reportKey))
////                .add(AuditEntity.property("firstName").hasChanged())
//                .getResultList();
//        return revisions;


        //현재 버전에서 가장 사용할 만한 방식이지만 우아하지 않음
        AuditQuery auditQuery = reader.createQuery()
                .forRevisionsOfEntity(Report.class, false, true)
                .addProjection(AuditEntity.id())
                .addProjection(AuditEntity.revisionNumber())
                .addProjection(AuditEntity.property("title"))
                .addProjection(AuditEntity.property("signedAt"))
//                .addProjection(AuditEntity.property("signer.memberKey"))          //현재로서 could not resolve property 발생
                .addProjection(AuditEntity.property("createdBy"))
                .addProjection(AuditEntity.property("updatedBy"))
                .addProjection(AuditEntity.property("createdAt"))
                .addProjection(AuditEntity.property("updatedAt"));

        // for the modification properties
        List histories =
                auditQuery
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("title"))))     //NPE 발생, alis로 뭘 넘기는지... 그냥 null을 넣으면 된다 1시간 먹힘
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("signedAt"))))
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("signer"))))
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("createdBy"))))
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("updatedBy"))))
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("createdAt"))))
                .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("updatedAt"))))
                .add(AuditEntity.id().eq(reportKey))
                .getResultList();
        return histories;
    }

    @Override
    public List<ReportHistory> findCustomReportHistoriesByReportKey(Long reprtKey) {
        TypedQuery<ReportHistory> query = entityManagerFactory.createEntityManager().createQuery("SELECT h FROM ReportHistory h WHERE h.reportKey = :reportKey", ReportHistory.class);
        query.setParameter("reportKey", reprtKey);

        /*
            select reporthist0_.report_key as report_k2_2_, reporthist0_.created_at as created_3_2_, reporthist0_.created_by as created_4_2_, reporthist0_.updated_at as updated_5_2_, reporthist0_.updated_by as updated_6_2_, reporthist0_.signed_at as signed_a7_2_, reporthist0_.member_key as member_11_2_, reporthist0_.title as title8_2_, reporthist0_.signed_at_changed as signed_a9_2_, reporthist0_.title_changed as title_c10_2_ from report reporthist0_
            where reporthist0_.dtype='ReportHistory' and reporthist0_.report_key=?
         */
        List<ReportHistory> histories = query.getResultList();
        return histories;
    }

    @Override
    public List<ReportHistory> findCustomReportHistoriesByReportKeyAsNativeQuery(Long reprtKey) {
        Query nativeQuery = entityManagerFactory.createEntityManager().createNativeQuery("SELECT * FROM REPORT_HISTORY H WHERE H.REPORT_KEY = :reportKey", ReportHistory.class);
        nativeQuery.setParameter("reportKey", reprtKey);

        /*
            https://discourse.hibernate.org/t/how-to-retrieve-the-modification-flags-with-hibernate-envers/259/2
            위에 Hibernate 5.3에서 Revision Meta와 modified flag 관련 정보도 조회할 수 있는 기능이
            출시 예정중인 기능이 완료되기전까진 native query를 통해 history를 조회하는 것이 가장 나은 듯 하다.

            현재 spring.data.jpa 2.1.0에선 hibernate core 5.2.17 final 사용 중

            EmbededId를 사용하지 않는 경우
            RepositoryHistory의 경우 ID가 report key로 잡혀 있으므로 조회시 reportKey에 대한 것들이 최종 row만 중복 되어 출력 된다
             별도의 DTO로 ResultMapping 필요 -> 그리 우아하지 않은 것 같다

             findCustomRepositoryRevisionsByReportKeyWithModifiedFlag 구현처럼 조회하는 것이 가장 나은 듯...

         */
        List<ReportHistory> histories = nativeQuery.getResultList();
        return histories;
    }
}
