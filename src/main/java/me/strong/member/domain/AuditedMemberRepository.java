package me.strong.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

/**
 * Created by taesu on 2018-06-08.
 */
public interface AuditedMemberRepository
        extends JpaRepository<AuditedMember, Long>, RevisionRepository<AuditedMember, Long, Long> {
}
