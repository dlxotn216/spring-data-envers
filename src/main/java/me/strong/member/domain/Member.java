package me.strong.member.domain;

import lombok.Getter;
import me.strong.common.domain.Address;
import me.strong.common.domain.BaseEntity;
import me.strong.report.domain.Report;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by taesu on 2018-06-08.
 */
@Entity
@Getter
@Table
@Audited(withModifiedFlag = true)
@EntityListeners(value = {AuditingEntityListener.class})
public class Member extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MemberSequenceGenerator")
    @SequenceGenerator(name = "MemberSequenceGenerator", sequenceName = "MEMBER_SEQ")
    private Long memberKey;

    private String memberId;

    private ZoneId zoneId;

    @OneToMany(mappedBy = "signer", cascade = CascadeType.ALL)
    private List<Report> signedReports = new ArrayList<>();

    @Embedded
    private Address address;

    private Member(){

    }

    public Member(String memberId, ZoneId zoneId, Address address) {
        this.memberId = memberId;
        this.zoneId = zoneId;
        this.address = address;
    }

    public void addSignedReports(Report report){
        this.signedReports.add(report);
    }

    public void changeAddress(Address newAddres){
        this.address = newAddres;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberKey=" + memberKey +
                ", memberId='" + memberId + '\'' +
                ", zoneId=" + zoneId +
                ", address=" + address +
                "} " + super.toString();
    }
}
