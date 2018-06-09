package me.strong.runner;

import me.strong.common.domain.Address;
import me.strong.member.domain.Member;
import me.strong.member.domain.MemberRepository;
import me.strong.report.domain.Report;
import me.strong.report.domain.ReportHistoryRepository;
import me.strong.report.domain.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

/**
 * Created by taesu on 2018-06-08.
 */
@Component
public class AuditAppRunner implements ApplicationRunner {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReportHistoryRepository reportHistoryRepository;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        //Initial Input
        Member member = new Member("test@test.com", ZoneOffset.UTC, new Address("1", "2", "123-415"));
        memberRepository.save(member);

        //Initial input
        Report report = new Report("Report1");
        reportRepository.save(report);

        //Change title
        Report saved = reportRepository.findById(1L).orElseThrow(IllegalArgumentException::new);
        saved.setTitle("Changed Report");
        reportRepository.save(saved);

        //Sign
        Report secondSaved = reportRepository.findById(1L).orElseThrow(IllegalArgumentException::new);
        secondSaved.sign(member);
        reportRepository.save(secondSaved);

        List<Object[]> reportHistories = reportRepository.findCustomRepositoryRevisionsByReportKeyWithModifiedFlag(1L);
        reportHistories
                .forEach(objects -> {
                    System.out.println("=================");
                    System.out.println("ID -> " + objects[0]);
                    System.out.println("Number -> " + objects[1]);
                    System.out.println("Title -> " + objects[2]);
                    System.out.println("SignedAt -> " + objects[3]);
                    System.out.println("CreatedBy -> " + objects[4]);
                    System.out.println("UpdatedBy -> " + objects[5]);
                    System.out.println("CreatedAt -> " + objects[6]);
                    System.out.println("UpdateAt -> " + objects[7]);
                    System.out.println("title Changed -> " + objects[8]);
                    System.out.println("signedAt Changed -> " + objects[9]);
                    System.out.println("signer Changed -> " + objects[10]);
                    System.out.println("createdBy Changed -> " + objects[11]);
                    System.out.println("updatedBy Changed -> " + objects[12]);
                    System.out.println("createdAt Changed -> " + objects[13]);
                    System.out.println("updatedAt Changed -> " + objects[14]);
                });

        //Member Address Change
        //Given
        Member member1 = new Member("a@a.com", ZoneOffset.UTC, new Address("1", "2", "123-415"));

        //When
        Member saved1 = memberRepository.save(member1);

        //Then
        memberRepository.findRevisions(saved1.getMemberKey()).forEach(System.out::println);


        //Given
        Member member2 = memberRepository.findById(2L).orElseThrow(IllegalArgumentException::new);

        //When
        member2.changeAddress(new Address("changed1", "changed2", "zpawef-123"));
        memberRepository.save(member2);

        //Then
        memberRepository.findRevisions(member2.getMemberKey()).forEach(System.out::println);
    }
}
