package me.strong.member.domain;

import me.strong.common.domain.Address;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZoneOffset;

import static org.junit.Assert.*;

/**
 * Created by taesu on 2018-06-09.
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void a1_TestForHistory(){
        //Given
        Member member = new Member("a@a.com", ZoneOffset.UTC, new Address("1", "2", "123-415"));

        //When
        Member saved = memberRepository.save(member);

        //Then
        memberRepository.findRevisions(saved.getMemberKey()).forEach(System.out::println);
    }

    @Test
    public void a2_TestForEmbeddedValueChangeHistory(){
        //Given
        Member member = memberRepository.findById(3L).orElseThrow(IllegalArgumentException::new);

        //When
        member.changeAddress(new Address("changed1", "changed2", "zpawef-123"));
        memberRepository.save(member);

        //Then
        memberRepository.findRevisions(member.getMemberKey()).forEach(System.out::println);
    }
}