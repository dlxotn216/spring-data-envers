Application을 구현하면서 대부분 요구사항에 명시되는 기능이 변경사항 추적이다.
이때 변경 사항을 추적하기 위해 필요한 데이터는 누가 언제 생성하였으며, 누가 언제 변경하였는지
그리고 해당 데이터의 시점에 따른 변경 이력이다.

전통적인 Mybatis를 이용하는 Spring MVC와 같은 프레임워크에선 AOP를 이용한다던지
또는 Mapper에서 Insert, Update, Delete 쿼리를 실행할 때마다 History table에 Insert하는 쿼리를 같이 실행 하도록 처리하는 등의 방법이 있다.
이러한 방법을 사용할 때 생각보다 개발 일정을 지연시키는 요소가 많다.

예를들어 History 테이블의 PK를 잘못 지정하여 발생하는 예외나 DBMS마다 상이한 다량의 쿼리문 실행 문법 등이다.
전자는 개발 단계보다 UAT 기간 등에서 발견하기 쉬울 뿐더러 모든 대상 테이블을 돌면서 Schema를 변경해야 하는 번거로움이 있다.

하지만 Spring data jpa를 사용한다면 이러한 수고를 덜어낼 수 있다.

## (1) Spring data jpa의 Auditing

Spring data common 프로젝트에는 CreateBy, CreatedDate, LastModifiedBy, LastModifiedDate 애노테이션이 존재한다.
네이밍그대로 누가 언제 생성하였고, 누가 언제 변경하였는지를 의미한다

아래 코드와 같이 각 Property에 지정하여 사용할 수 있으며, Audit이 필요한 Entity는 BaseEntity를 상속받으면 될 것이다.
<pre><code>
@EntityListeners(value = {AuditingEntityListener.class})
@MappedSupperClass
public abstract class BaseEntity {
     @CreatedBy
     @Column(name = "CREATED_BY", nullable = false, updatable = false)    
     private Long createdBy;
 
     @LastModifiedBy
     @Column(name = "UPDATED_BY", nullable = false)
     private Long updatedBy;
 
     @CreatedDate
     @Column(nullable = false)
     private LocalDateTime createdAt;               
 
     @LastModifiedDate                                   
     @Column(nullable = false)
     private LocalDateTime updatedAt;
 }
 ...
 @Configurable
 public class AuditingEntityListener {
 
 	private @Nullable ObjectFactory<AuditingHandler> handler;
 	
 	public void setAuditingHandler(ObjectFactory<AuditingHandler> auditingHandler) {
 
 		Assert.notNull(auditingHandler, "AuditingHandler must not be null!");
 		this.handler = auditingHandler;
 	}
 	
 	@PrePersist
 	public void touchForCreate(Object target) {
 
 		Assert.notNull(target, "Entity must not be null!");
 
 		if (handler != null) {
 
 			AuditingHandler object = handler.getObject();
 			if (object != null) {
 				object.markCreated(target);
 			}
 		}
 	}
 
 	@PreUpdate
 	public void touchForUpdate(Object target) {
 
 		Assert.notNull(target, "Entity must not be null!");
 
 		if (handler != null) {
 
 			AuditingHandler object = handler.getObject();
 			if (object != null) {
 				object.markModified(target);
 			}
 		}
 	}
 }
</code></pre>

AuditEntityListener는 Spring data jpa에서 구현한 EntityListener이다.
@PrePersist, @PreUpdate 설정을 통해 엔티티가 영속화 되기이전에 AuditingHandler를 통해 생성일, 생성자, 수정일, 수정자를 자동으로 찾아 설정한다.

생성일, 수정일은 아래와 같은 코드에서 입력된다
<code><pre>
private Optional<TemporalAccessor> touchDate(AuditableBeanWrapper wrapper, boolean isNew) {
    Assert.notNull(wrapper, "AuditableBeanWrapper must not be null!");

    Optional<TemporalAccessor> now = dateTimeProvider.getNow();
    Assert.notNull(now, () -> String.format("Now must not be null! Returned by: %s!", dateTimeProvider.getClass()));

    now.filter(__ -> isNew).ifPresent(it -> wrapper.setCreatedDate(it));
    now.filter(__ -> !isNew || modifyOnCreation).ifPresent(it -> wrapper.setLastModifiedDate(it));

    return now;
}
</pre></code>

생성자, 수정자는 아래와 같은 코드에서 입력된다
```java
private Optional<Object> touchAuditor(AuditableBeanWrapper wrapper, boolean isNew) {
    Assert.notNull(wrapper, "AuditableBeanWrapper must not be null!");
    
    return auditorAware.map(it -> {
        Optional<?> auditor = it.getCurrentAuditor();
        Assert.notNull(auditor,
                () -> String.format("Auditor must not be null! Returned by: %s!", AopUtils.getTargetClass(it)));

        auditor.filter(__ -> isNew).ifPresent(foo -> wrapper.setCreatedBy(foo));
        auditor.filter(__ -> !isNew || modifyOnCreation).ifPresent(foo -> wrapper.setLastModifiedBy(foo));

        return auditor;
    });
}
```


각각 DateTimeProvider, AuditorAware Bean으로부터 추출된다. 따라서 개발하려는 Application의 특성에 맞게
DateTime을 제공하고, Auditor를 제공하는 구현이 필요할 것이다. 그 예는 아래와 같다.
(AuditorAware는 필수적으로 등록해주어야 하며 DateTimeProvider는 Default 구현체가 있다)

```java
/*
    Spring security를 사용한다면 SecurityContextHolder에서 Authentication 객체로부터 User 정보를 가져올 수 있을 것이고
    Token 기반의 인증을 사용한다면 일반적으로 Token을 파싱한 결과를 Thread local에 담아둘 것이므로
    Thread local로부터 사용자 정보를 가져오는 구현을 아래에서 구현하면 될 것이다.
*/
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfiguration {
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.of(new Random().nextLong());
    }
}
```

※ EnableJpaAuditing 애노테이션에 지정할 수 있는 프로퍼티는 아래와 같다
* String auditorAwareRef() default "";      //AuditorAware 빈 이름
* boolean setDates() default true;          //Audit date를 설정 할 지 여부
* boolean modifyOnCreate() default true;    //생성 시 Modify로 취급 할 지 (false일 경우 생성 시엔 ModifiedBy, ModifiedAt 정보는 저장되지 않는다)
* String dateTimeProviderRef() default "";  //DateTimeProvider 빈 이름


DateTimeProvider가 없다면 아래와 같이 CurrentDateTimeProvider가 기본 구현체로 등록 된다
<img src="https://raw.githubusercontent.com/dlxotn216/image/master/Audit-CurrentDateTimeProvider.png"/>

CurrentDateTimeProvider의 구현은 아래와 같다.
```java
public enum CurrentDateTimeProvider implements DateTimeProvider {
	INSTANCE;
	
	@Override
	public Optional<TemporalAccessor> getNow() {
		return Optional.of(LocalDateTime.now());
	}
}
```

여기서 한 가지 문제점이 CreatedDate, LastModifiedDate을 ZonedDateTime으로 사용할 경우 에러가 발생한다는 것이다.
다국어 프로젝트인 경우, Entity의 변경 이력을 추적해야 하는경우 민감한 것이 Timezone 정보이다. 

보통 UTC로 DB에 저장하고 사용자의 Timezone 정보에 따라 값을 변환하여 응답하는 것이 일반적일텐데 ZonedDateTime에서 UTC 타임을 구하여
LocalDateTime으로 변환하는 작업을 일일히 하자니 번거롭다.

그에 대한 해법으로 AuditingEntityListener를 사용하지 않고 자체의 Listener를 사용하는 것이다

아래 예제에선 Auditor를 구하는 것은 AuditingEntityListener를 이용하였고
나머지 AuditDateTime을 구하는 것은 BaseEntity 내에 선언 된 EntityListener 기능을 이용했다.
(AuditingEntityListener를 아예 쓰지 않을수도 있다.)

```java
@EntityListeners(value = {AuditingEntityListener.class})
public abstract class BaseEntity {
    
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = ZonedDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
```


테스트 코드는 아래와 같다
한 가지 주의할 점은 @SpringBootTest 대신 @DataJpaTest를 사용하지 말아야 한다는 것이다.
이럴경우 AuditorAware 빈이 정상적으로 등록되지 않아 Auditing 기능이 제대로 동작하지 않는 것처럼 보일수 있다. (삽질 2시간...)

<code><pre>
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
    ...
}
</pre></code>

## (2) Spring data envers

앞서 Audit 기능은 누가 언제 생성 했고, 누가 언제 변경 했는지에 대한 것이 중점인 Audit 기능이었다.
한 가지 더 필요한 것이 Entity가 변경 된 전체 이력을 관리하는 History 기능이다.

Hibernate의 envers라는 프로젝트가 이 기능을 완벽히 지원하며 Spring data envers 프로젝트는
 Spring data 프로젝트에 걸맞게 더욱 편리하게 기능을 사용할 수 있도록 한다.
 
 
 설정은 매우 간단하다
 아래와 같이 Maven dependency를 추가한 후 @SpringBootApplication 애노테이션 위에
 @EnableJpaRepositories 애노테이션을 붙이면 된다.
 
 그 후 변경 이력을 추적 할 Entity에 @Audited 애노테이션을 붙이면 Entity가 영속화 될 때마다
 History 테이블이 자동으로 관리되며 각 Entity에 매칭되는 Table 이름에 _AUD가 붙은 이력 관리용 테이블이 추가생성된다.
 
 ```xml
 <dependency>
     <groupId>org.springframework.data</groupId>
     <artifactId>spring-data-envers</artifactId>
 </dependency>
 ```
<code>
@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@SpringBootApplication
public class AuditApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuditApplication.class, args);
	}
}

...

@Audited      
public abstract class BaseEntity {}
</code>

History 테이블의 Postfix를 바꾸고 싶은 경우엔 아래 옵션을 설정하면 된다.
 spring.jpa.properties.org.hibernate.envers.audit_table_suffix=_HISTORY

저장 된 변경 이력을 조회하고 싶은 경우엔 아래와 같이 특정 Entity의 Repository에 RevisionRepository를 상속받으면 된다.
 <code><pre>
 public interface MemberRepository extends JpaRepository<Member, Long>, RevisionRepository<Member, Long, Integer> {
 }
 
 ...
 
 @NoRepositoryBean
 public interface RevisionRepository<T, ID, N extends Number & Comparable<N>> extends Repository<T, ID> {
 	Optional<Revision<N, T>> findLastChangeRevision(ID id);
 	
 	Revisions<N, T> findRevisions(ID id);
 	
 	Page<Revision<N, T>> findRevisions(ID id, Pageable pageable);
 	
 	Optional<Revision<N, T>> findRevision(ID id, N revisionNumber);
 }
 </pre></code>
 
 Pageable 객체를통해 옵션을 통해 정렬, 페이징을 손쉽게 구현할 수 있어 웬만한 변경 이력 추적 요구사항은 대응 가능하다.
 
 여기서 한 가지 더 발전적인 요구사항을 보면
 "변경 이력을 List로 조회하되 각 Row에 이전 Row와 비교하여 실제 변경 된 컬럼은 별도 표시해주세요" 라는 것이 있다.
 (View 단의 Logic을 통해 이전 Row를 기억하여 해결은 가능하지만...)
 
 이런 것을 대비하여 @Audited 애노테이션엔 withModifiedFlag 옵션이 있다.
 true로 줄경우에 각 Column에 매칭되는 Flag 컬럼이 추가적으로 생성되며 값의 변경 여부를 저장한다.
 <img src="https://raw.githubusercontent.com/dlxotn216/image/master/spring-data-envers%20with%20modified%20flag%20option.png" />
 
 Flag 컬럼의 기본 이름은 _MOD Postfix가 붙은 이름이며 아래와 같이 변경 가능하다.
 spring.jpa.properties.org.hibernate.envers.modified_flag_suffix=_CHANGED
 
 * * * 
 ※현재 Spring data jpa, Spring data envers 2.0.2버전에서 사용하는 hibernate 버전은 5.2.17.final 버전이며,
 해당 버전에서 withModifiedFlag 옵션을 true로 주어도 Revision Entity내엔 관련 flag가 없어 조회가 불가능합니다.
 
 이것을 해결하기 위해 몇몇 시도를 해보았습니다.
 
 (1) History를 위한 Entity를 만들어서 조회하기 (대상 Entity는 Report이다)
 https://github.com/dlxotn216/spring-data-envers/blob/master/src/main/java/me/strong/report/domain/impl/ReportHistoryRepositoryImpl.java
 
 이 경우엔 Query DSL을 사용하였고 생성 된 JPQL을 보면 아래와 같다.
 <code><pre>
        select 
            reporthist0_.report_key as report_k2_2_, 
            reporthist0_.created_at as created_3_2_, 
            reporthist0_.created_by as created_4_2_, 
            reporthist0_.updated_at as updated_5_2_, 
            reporthist0_.updated_by as updated_6_2_, 
            reporthist0_.signed_at as signed_a7_2_, 
            reporthist0_.member_key as member_11_2_, 
            reporthist0_.title as title8_2_, 
            reporthist0_.signed_at_changed as signed_a9_2_, 
            reporthist0_.title_changed as title_c10_2_ 
        from 
            report reporthist0_ 
        where 
            reporthist0_.dtype='ReportHistory' and reporthist0_.report_key=?
    </pre></code>
 
 dType이 ReportHistory로 고정되어있어 아무런 이력이 조회되지 않는다. 아마 ReportHistory Entity로부터 JPQL이 생성 되었기 때문인 듯하다.
  
 dType을 Report로 준 경우엔 아래와 같다
 <code><pre>
    select 
        reporthist0_.report_key as report_k2_2_, 
        reporthist0_.created_at as created_3_2_, 
        reporthist0_.created_by as created_4_2_, 
        reporthist0_.updated_at as updated_5_2_, 
        reporthist0_.updated_by as updated_6_2_, 
        reporthist0_.signed_at as signed_a7_2_, 
        reporthist0_.member_key as member_11_2_, 
        reporthist0_.title as title8_2_, 
        reporthist0_.dtype as dtype1_2_, 
        reporthist0_.signed_at_changed as signed_a9_2_, 
        reporthist0_.title_changed as title_c10_2_ 
    from 
        report reporthist0_ 
    where 
        reporthist0_.dtype='ReportHistory' and reporthist0_.report_key=? and reporthist0_.dtype=?
 </pre></code>
 
 따라서 이 방법은 마땅한 방법이 없었다.
 
 (2) Native Query 사용
 https://github.com/dlxotn216/spring-data-envers/blob/master/src/main/java/me/strong/report/domain/impl/CustomRevisionRepositoryImpl.java
 
 REOIRT_HISTORY 테이블을 직접 조회하는 쿼리를 날려보았다.
 조회는 되지만 결과 row의 수가 이상하다.
 바로 ReportHistory가 Report entity를 상속하고있고 reportKey가 Id이기 때문 인것으로 파악했고
 DTO로 매핑이 필요하였지만 차라리 이렇게 하느니 Mybatis를 같이 사용하는 것이 나아 보였다.
 
 (3) Envers의 AuditQuery 사용
  https://github.com/dlxotn216/spring-data-envers/blob/master/src/main/java/me/strong/report/domain/impl/CustomRevisionRepositoryImpl.java
  
  아래와 같이 Envers의 AuditQuery를 사용하는 것인데 어느정도 정상적으로 조회는 된다.
  하지만 Relation을 가지는 Entity의 경우 modified flag는 조회 되지만 실제 column의 정보는 조회가 안된다.
  아마 signer를 memberKey로 대체해야 할 듯 하다.
  
  ```java
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
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("title"))))     //NPE 발생, alis로 뭘 넘기는지? -> 그냥 null을 넣으면 된다
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("signedAt"))))
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("signer"))))
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("createdBy"))))
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("updatedBy"))))
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("createdAt"))))
          .addProjection(new AuditProperty(null, new ModifiedFlagPropertyName(new EntityPropertyName("updatedAt"))))
          .add(AuditEntity.id().eq(reportKey))
          .getResultList();
  ```
 
   그나마 3번 방식이 가장 나은 듯 하나 우아하진 않은 것 같다.
   좀 더 자료를 검색 해보니 아래 논의를 확인했고
   https://discourse.hibernate.org/t/how-to-retrieve-the-modification-flags-with-hibernate-envers/259
     
   아래 메소드를 통해 modified flag를 조회할 수 있는 API를 확인했고 hibernate 5.0에 배포 예정인 것을
   hibernate 5.3에 추가하여 배포하였다는 것을 확인했다.
   
   
   As you’ll notice this was planned to be included as part of 6; 
   however I can go ahead and move this up to be 
   included in the upcoming 5.3 release if that would help you.
   
   ```java
    @Incubating(since = "6.0")
    public AuditQuery forRevisionsOfEntityWithChanges(Class<?> clazz, boolean selectDeletedEntities)
   ```
   
   추가된 사항은 아래에서 확인할 수 있다.
   http://docs.jboss.org/hibernate/orm/5.3/javadocs/org/hibernate/envers/query/AuditQueryCreator.html#forRevisionsOfEntityWithChanges-java.lang.Class-boolean-
    
    
   아래 spring boot의 github에서 hibernate5.3.0.final이 릴리즈 되었고 
   그에 따라서 spring data jpa의 호환 여부를 처리하려는 움직임이 있는 것 같다
   https://github.com/spring-projects/spring-boot/issues/11725
    
    
    
   - 2019-08-15 
   아래와 같이 traverseRelation을 이용하여 조인할 수 있다.  
    
   ```java
       AuditQuery auditQuery = reader.createQuery()
                                      .forRevisionsOfEntity(TemplateVersion.class, false, true)
                                      .traverseRelation("templateFile", JoinType.INNER, "awf")
                                      ;
   ```  
    
   아래의 주의와 같이 many-to-one 매핑에만 LEFT, INNER를 통해서만 된다고 한다  
   Relation joins can be applied to many-to-one and many-to-one mappings only when using JoinType.LEFT or JoinType.INNER.  
   
   왜만들어 놓은지 모르겠으나 그 위에 보면 아직 실험적이란다.  
   Relation join queries are considered experimental and may change in future releases.  
    
   대충 실행 아래와 같은 jpql이 생성되는 듯 하다  
   io.crscube.safetyapp.template.domain.model.TemplateVersion_HISTORY Entity를 통해서 직접 조인하려고 했으나  
   생각해보니 연관관계가 없어서 그마저도 불가능한 듯.  
   ```sql
   	 select e__.originalId.key,
	      e__.originalId.REV.id,
	      e__.versionName,
	      e__.templateType,
	      e__.templateFile.originName,
	      e__.handlingInfo.updatedBy,
	      e__.handlingInfo.updatedAt
	  from io.crscube.safetyapp.template.domain.model.TemplateVersion_HISTORY e__,
	  org.hibernate.envers.DefaultRevisionEntity r
	  where e__.originalId.REV.id = r.id
	  order by e__.originalId.REV.id desc
   ```  
    
    
   - 2019-08-15 (2)
   jpql로 연관관계가 없어도 쿼리작성이 가능하단다. 그래서 아래와 같이 쿼리 짜보았다  
    
   ```sql
       select t from io.crscube.safetyapp.template.domain.model.TemplateVersion_HISTORY t join fetch SystemFile f on t.fileKey = f.key
   ```
    
   근데 계속 fileKey가 없다고 에러가 난다.  
   혹시나 해서 file_key로 조회 해보아도 안된다  
   
   왜그럴까 하고 디버깅을 해서 따라 들어가보았고 아래 이미지에서처럼 프로퍼티 매핑에서 단서를 찾을 수 있었다.  
   enver가 생성하는 history entity의 프로퍼티 규칙은 entityName_entityKeyName 인 듯 하다.
   
   즉 templateFile_key로 생각하면 될 것 같다     
   <img src="https://github.com/dlxotn216/image/blob/master/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202019-08-15%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%205.01.19.png?raw=true" />

   JPQL로 조인하는 방법이 제일 깔끔한 것 같기도 하다. 정렬이나 페이징도 손쉽게 쓸 수 있으니...
