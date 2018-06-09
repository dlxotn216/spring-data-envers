package me.strong.report.domain;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by taesu on 2018-06-08.
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
public class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportHistoryRepository reportHistoryRepository;

    @Test
    public void a1_생성자_생성일_테스트() {
        //Given
        Report report = new Report("Report1");
        reportRepository.save(report);

        //When
        Report saved = reportRepository.findById(report.getReportKey()).orElseThrow(IllegalArgumentException::new);

        //Then
        assertThat(saved.getCreatedBy()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    public void a2_수정자_수정일_테스트() {
        //Given
        Report saved = reportRepository.findById(2L).orElseThrow(IllegalArgumentException::new);

        //When
        saved.setTitle("Changed Report");
        Report changed = reportRepository.save(saved);

        //Then
        assertThat(changed.getUpdatedAt()).isNotNull();
        assertThat(changed.getUpdatedBy()).isNotNull();

        assertThat(changed.getCreatedAt()).isNotEqualTo(changed.getUpdatedAt());
    }

    @Test
    public void a3_변경이력_테스트() {
        //Then
        Revisions<Integer, Report> revisions = reportRepository.findRevisions(2L);

        assertThat(revisions).isNotEmpty();

        revisions.forEach(System.out::println);

        revisions.getContent().stream().map(Revision::getEntity).forEach(System.out::println);

        /*
        me.strong.report.domain.Report@3ecc9baa
        me.strong.report.domain.Report@4f2a9903
         */
        reportRepository.findCustomRepositoryRevisionsByReportKey(2L).forEach(System.out::println);

        /*
        me.strong.report.domain.Report@73a88711
        DefaultRevisionEntity(id = 1, revisionDate = 2018. 6. 8 오후 11:28:36)
        ADD
        me.strong.report.domain.Report@5978ba12
        DefaultRevisionEntity(id = 2, revisionDate = 2018. 6. 8 오후 11:28:36)
        MOD
         */
        reportRepository.findCustomRepositoryRevisionsByReportKeyWithRevisionMetadata(2L)
                .forEach(objects -> {
                    Arrays.asList(objects).forEach(System.out::println);
                });
    }

    @Test
    public void a4_직접구현체로_직접리비전조회_테스트() {
        reportHistoryRepository.findAllHistoriesByReportKey(2L)
                .forEach(System.out::println);
    }

    @Test
    public void a5_직접구현체2로_테스트() {
        reportRepository.findCustomRepositoryRevisionsByReportKeyWithModifiedFlag(2L)
                .forEach(objects -> {
                    Collections.singletonList(objects).forEach(System.out::println);
                });
    }

    @Test
    public void a6_직접_JPQL작성_테스트() {
        reportRepository.findCustomReportHistoriesByReportKey(2L).
                forEach(System.out::println);
    }

    @Test
    public void a7_직접_NativeQuery_작성_테스트(){
        reportRepository.findCustomReportHistoriesByReportKeyAsNativeQuery(2L)
                .forEach(System.out::println);
    }
}