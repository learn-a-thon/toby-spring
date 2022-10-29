# AOP
AOP는 IoC/DI, 서비스 추상화와 더불어 스프링의 3대 기반 기술 중 하나다. 

## 6.1 트랜잭션 코드의 분리
지금까지 서비스 추상화 기법을 적용해 트랜잭션 기술에 독립적으로 구현했지만 트랜잭션 경계설정을 위해 넣은 코드 때문에 찜찜한 구석이 남아있다. 

## 6.1.1 메소드 분리 
upgradeLevels() 메소드를 살펴보면 비즈니스 로직 코드를 사이에 두고 트랜잭션 시작과 종료를 담당하는 코드가 앞뒤에 위치하고 있다. 이는 트랜잭션 경계설정의 코드와 비즈니스 로직 코드 간에 서로 주고받는 정보가 없고, 서로 완벽하게 독립적인 코드이다. 이 성격이 다른 코드를 두 개의 메소드로 분리해보자.

```java
public void upgradeLevels() Exception {
    TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

    try {
        upgradeLevelsInternal();
        this.transactionManage.commit(status);
    } catch (Exception e) {
        this.transactionManager.rollback(status);
        throw e;
    }
}

private void upgradeLevelsInternal() {
    List<User> users = userDao.getAll();
    for (User user : users) {
        if (canUpgradeLevel(user)) {
            upgradeLevel(user);
        }
    }
}
```
순수하게 사용자 레벨 업그레이드를 담당하는 비즈니스 로직 코드만 독립적인 메소드에 담겨 있으니 이해하기 편해졌다.

## 6.1.2 DI를 이용한 클래스의 분리
비즈니스 로직을 담당하는 코드는 분리했지만 여전히 트랜잭션을 담당하는 기술적인 코드가 UserService 안에 자리잡고 있다. 이 문제는 간단하게 트랜잭션 코드를 클래스 밖으로 뽑아내는 것으로 해결할 수 있다. 

### DI 적용을 이용한 트랜잭션 분리
![](https://velog.velcdn.com/images/nunddu/post/6865a3b2-16b8-4be8-988d-f0ee5c0dbb8d/image.png)  
인터페이스를 이용해 구현 클래스를 클라이언트에 노출하지 않고 런타임 시에 DI를 통해 적용하는 방법을 쓰는 이유는, 일반적으로 구현 클래스를 바꿔가면서 사용하기 위해서이다. 

UserService에 순수하게 비즈니스 로직을 담고 있는 코드만 두고 트랜잭션 경계설정을 담당하는 코드를 외부로 빼내려는 것이다. 하지만 클라이언트가 UserService의 기능을 제대로 이용하려면 트랜잭션이 적용돼야 한다. 아래와 같은 구조를 생각해볼 수 있다. 

![](https://velog.velcdn.com/images/nunddu/post/823e4007-167f-49c8-8ce9-4452706680e6/image.png)  

### UserService 인터페이스 도입
UserService를 인터페이스로 만들고 기존 서비스 역할을 하던 클래스를 UserServiceImpl로 사용한다. 

```java
public interface UserService {
    void add(User user);
    void upgradeLevels();
}
```

```java
public class UserServiceImpl implements UserService {

    UserDao userDao;
    MailSender mailSender;

    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for (User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }
    }
}
```
upgradeLevels()에 남겨뒀던 트랜잭션 관련 코드를 모두 제거한다. 

### 분리된 트랜잭션 기능
비즈니스 트랜잭션 처리를 담은 UserServiceTx를 만들어보자. 같은 인터페이스를 구현한 다른 오브젝트에게 고스란히 작업을 위임하게 만들면 된다. 적어도 비즈니스 로직에 대해서는 UserServiceTx가 아무런 관여도 하지 않는다.

```java
public class UserServiceTx implements UserService {
    // 위임 기능을 가진 UserServiceTx
    UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void add(User user) {
        userService.add(user);
    }

    public void upgradeLevels() {
        userService.upgradeLevels();
    }
}
```
UserServiceTx는 사용자 관리라는 비즈니스 로직을 전혀 갖지 않고 고스란히 다른 UserService 구현 오브젝트에 기능을 위임한다. 이를 위해 UserService 오브젝트를 DI 받을 수 있도록 만든다.

```java
public class UserServiceTx implements UserService { 
    // 트랜잭션이 적용된 UserServiceTx
	UserService userService;
	PlatformTransactionManager transactionManager;

	public void setTransactionManager( 
    		PlatformTransactionManager transactionManager) { 
		this.transactionManager = transactionManager;
	}

	public void setUserService(UserService userService) { 
		this.userService = userService;
	}

	public void add(User user) { 
    	this.userService.add(user);
	}

	public void upgradeLevels() {
		TransactionStatus status = this.transactionManager
        	.getTransaction(new DefaultTransactionDefinition());
		try {
			userService.upgradeLevels();
			this.transactionManager.commit(status);
		} catch (RuntimeException e) { 
        	this.transactionManager.rollback(status);
			throw e;
		} 
    } 
}
```

### 트랜잭션 적용을 위한 DI 설정 
클라이언트가 UserService라는 인터페이스를 통해 사용자 관리 로직을 이용하려고 할 때 먼저 트랜잭션을 담당하는 오브젝트가 사용돼서 트랜잭션에 관련된 작업을 진행하고, 실제 사용자 관리 로직을 담은 오브젝트가 이후에 호출돼서 비즈니스 로직을 수행한다.  
스프링 DI 설정에 의해 결국 만들어질 빈 오브젝트와 그 의존 관계는 다음과 같다. 

![](https://velog.velcdn.com/images/nunddu/post/24600555-4a6a-4f37-aa5f-730f7eb696b0/image.png)  
클라이언트는 UserServiceTx로 주입된 UserService 인터페이스를 사용하고 UserServiceTx 내의 UserService는 UserServiceImpl 빈을 DI하도록 구성한다. 

### 트랜잭션 경계설정 코드 분리의 장점
 - 비즈니스 로직을 담당하고 있는 UserServiceImpl의 코드를 작성할 때는 트랜잭션과 같은 기술적인 내용에는 전혀 신경쓰지 않아도 된다.
 - 비즈니스 로직에 대한 테스트를 손쉽게 만들어낼 수 있다. 

## 6.2 고립된 단위 테스트 
좋은 테스트 방법은 가능한 한 작은 단위로 쪼개서 테스트하는 것이다. 작은 단위의 테스트는 테스트가 실패했을 때 그 원인을 찾기 쉽다. 

## 6.2.1 복잡한 의존관계 속의 테스트
UserService의 테스트 단위는 복잡한 의존관계의 상관없이 테스트의 대상인 UserService여야 한다. 하지만 UserService는 UserDao, TransactionManager, MailSender라는 세 가지의 의존관계를 갖고 있다. 더 큰 문제는 세 가지 의존 오브젝트로 자신의 코드만 실행하고 마는 것이 아니라는 것이 아니라는 것이다.  
따라서 UserService를 테스트하는 것처럼 보이지만 사실은 그 뒤에 존재하는 훨씬 더 많은 오브젝트와 환경, 서비스, 서버, 심지어 네트워크까지 함께 테스트하는 셈이 된다. 

## 6.2.2 테스트 대상 오브젝트 고립시키기
그래서 테스트의 대상이 환경이나 외부서버, 다른 클래스의 코드에 종속되고 영향을 받지 않도록 고립시킬 필요가 있다. 테스트를 의존 대상으로부터 분리해서 고립시키는 방법은 **테스트를 위한 대역**을 사용하는 것이다. MailSender에는 이미 DummyMailSender라는 테스트 스텁을 적용했다. 특별히 만든 MockMailSender라는 목 오브젝트로 사용해봤다.

### 테스트를 위한 UserServiceImpl 고립
UserServiceImpl은 PlatformTransactionManager에 더 이상 의존하지 않는다. 고립된 테스트가 가능하도록 UserServiceImple를 재구성해보면 다음과 같은 구조가 된다. 

![](https://velog.velcdn.com/images/nunddu/post/1f51a169-ec28-4a88-810b-4836a6d58e51/image.png)  

UserDao는 테스트 대상의 코드가 정상적으로 수행되도록 도와주기만 하는 스텁이 아니라, 부가적인 검증 기능까지 가진 목 오브젝트로 만들었다. 그 이유는 고립된 환경에서 동작하는 upgradeLevels()의 테스트 결과를 검증할 방법이 필요하기 때문이다. 

기존에는 UserService의 메소드를 실행시킨 후에 UserDao를 이용해 DB에 들어간 결과를 가져와 검증하는 방법을 사용했다. 그런데 의존 오브젝트나 외부서비스에 의존하지 않는 고립된 테스트 방식으로 만든 UserServiceImpl은 아무리 그 기능이 수행돼도 그 결과가 DB 등을 통해서 데이터가 남지 않으니, 기존의 방법으로는 작업 결과를 검증하기 힘들다. 그래서 이럴 땐 **테스트 대상인 UserServiceImpl과 그 협력 오브젝트인 UserDao에게 어떤 요청을 했는지를 확인하는 작업이 필요하다. 테스트 중에 DB에 결과가 반영되지는 않았지만, UserDao 의 update() 메소드를 호출하는 것을 확인할 수 있다면, 결국 DB에 그 결과가 반영될 것이라고 결론을 내릴 수 있기 때문이다.**  

### 고립된 단위 테스트 활용
고립된 단위 테스트 방법을 적용해보자.

```java
@Test
public void upgradeLevels() throws Expcetion {
    userDao.deleteAll();
    users.forEach(user -> userDao.add(user));

    MockMailSender mockMailSender = new MockMailSender();
    userServiceImpl.setMailSender(mockMailSender); // 의존성 주입

    userService.upgradeLevels();

    checkLevelUpgraded(user.get(0), false);
    checkLevelUpgraded(user.get(1), true);
    checkLevelUpgraded(user.get(2), false);
    checkLevelUpgraded(user.get(3), true);
    checkLevelUpgraded(user.get(4), false); // DB에 저장된 결과 확인

    List<String> request = mockMailSender.getRequests();
    assertThat(request.size(), is(2));
    assertThat(request.get(0), is(user.get(1).getEmail()));
    assertThat(request.get(1), is(user.get(3).getEmail())); // 결과 확인
}
```
이 테스트는 아래와 같은 작업으로 구성된다.
 - UserDao를 통해 가져올 테스트용 정보를 DB에 넣는다. 
 - 메일 발송 여부를 확인하기 위해 MailSender 목 오브젝트를 DI
 - 실제 테스트 대상인 userService의 메소드를 실행한다.
 - 결과가 DB에 반영돼는지 확인하기 위해서 UserDao를 이용해 DB에서 데이터를 가져와 결과를 확인한다.
 - 목 오브젝트를 통해 UserService에 의한 메일 발송이 있었는지를 확인하면 된다. 

### UserDao 목 오브젝트
DB까지 직접 의존하고 있는 테스트 방식도 목 오브젝트를 만들어서 적용해보자.
```java
 // 사용자 레벨 업그레이드 작업 중에 UserDao를 사용하는 코드
public void upgardeLevels() {
    userDao.getAll()
        .filter(user -> canUpgradeLevel(user))
        .forEach(user -> upgradeLevel(user));
}

protected void upgradeLevel(User user) {
    user.upgradeLevel();
    userDao.update(user);
    sendUpgradeEmail();
}
```

```java
// UserDao 오브젝트
static class MockUserDao implements UserDao {
    private final List<User> users; // 레벨 업그레이드 후보 User 오브젝트 목록
    private List<User> updated = new ArrayList<>(); // 업그레이드 대상 오브젝트를 저장해둘 목록

    public List<User> getUpdated() {
        return this.updated;
    }

    public List<User> getAll() { // 스텁 기능 제공
        return this.users;
    }

    public void update(User user) { // 목 오브젝트 기능 제공
        updated.add(user;)
    }

    // 테스트에 사용되지 않는 메소드 구현
    public void add(User user) { throw new UnsupportedOperationException(); }
    public void deleteAll(User user) { throw new UnsupportedOperationException(); }
    public User get(String id) { throw new UnsupportedOperationException(); }
    public int getCount() { throw new UnsupportedOperationException(); }
}
```
인터페이스를 구현하면서 어쩔 수 없이 사용하지 않는 메소드도 구현을 해줘야한다. 실수로 사용될 위험을 줄이기 위해 적당한 예외를 던져준다. MockUserDao를 사용하여 고립된 테스트를 작성해보자.

```java
@Test
public void upgradeLevels() throws Exception {
    UserServiceImpl userServiceImpl = new UserServiceImpl(); // 테스트 대상 오브젝트 직접 생성

    MockUserDao mockUserDao = new MockUserDao(this.users);
    userServiceImpl.setUserDao(mockUserDao); // 테스트용 obj 직접 DI

    MockMailSender mockMailSender = new MockMailSend();
    userServiceImpl.setMailSender(mockMailSender);

    List<User> updated = mockUserDao.getUpdated(); // 목 오브젝트 리턴
    assertThat(updated.size(), is(2));
    checkUserAndLevel(update.get(0), "zin0", Level.SILVER);
    checkUserAndLevel(updated.get(1), "jinyoung", Level.GOLD);
}

private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
    assertThat(updated.getId(), is(expectedId));
    assertThat(updated.getLevel(), is(expectedLevel));
}
```
테스트 대역 오브젝트를 이용해 완전히 고립된 테스트로 만들기 전의 테스트의 대상은 스프링 컨테이너에서 @Autowired를 통해 가져온 UserService 타입의 빈이었다. 컨테이너에서 가져온 오브젝트는 DI를 통해서 많은 의존 오브젝트와 서비스, 외부 환경에 의존하고 있었다. 이제는 완전히 고립돼서 테스트만을 위해 독립적으로 동작하는 테스트 대상을 사용할 것이기 때문에 스프링 컨테이너에서 빈을 가져올 필요가 없어졌다.  
스프링 테스트 컨텍스트를 이용하기 위해 도입한 @RunWith 등은 제거할 수 있다. 

### 테스트 수행 성능의 향상
스프링 테스트 컨테이너를 사용하고 DB에 직접 접근하는 의존적인 DAO를 사용하는 테스트일 경우 시간이 많이 걸리지만 고립된 테스트 방식은 테스트를 위한 mock 오브젝트에만 의존하기 때문에 테스트 수행 성능이 훨씬 좋다. 이러한 성능 차이는 복잡한 테스트일수록 극명하게 드러난다.

## 6.2.3 단위 테스트와 통합 테스트
`단위 테스트` - 의존 오브젝트나 외부의 리소스를 사용하지 않도록 고립시켜서 테스트하는 것  
`통합 테스트` - 두 개 이상의, 성격이나 계층이 다른 오브젝트가 연동하도록 만들어 테스트하거나, 또는 외부의 DB나 파일, 서비스 등의 리소스가 참여하는 테스트

## 6.2.4 목 프레임워크
### Mockito 프레임워크
UserDao 인터페이스를 구현한 테스트용 목 오브젝트는 다음과 같인 Mockito의 스태틱 메소드를 호출해주면 만들 수 있다.
```java
UserDao mockUserDao = mock(UserDao.class);
```
이렇게 만들어진 목 오브젝트는 아직 아무런 기능이 없다. 사용자 목록을 리턴하는 스텁을 추가해준다.
```java
when(mockUserDao.getAll().thenReturn(this.users));
```
Mockito를 통해 만들어진 목 오브젝트는 메소드의 호출과 관련된 모든 내용을 자동으로 저장해두고, 이를 간단하게 메소드로 검증할 수 있게 해준다. 테스트를 진행하는 동안 mockUserDao의 update() 메소드가 두 번 호출됐는지 확인하고 싶다면, 다음과 같이 검증 코드를 작성할 수 있다.
```java
verify(mockUserDao, times(2)).update(any(User.class));
```
User 타입의 오브젝트를 파라미터로 받으며 update() 메소드가 두 번 호출됐는지 확인하는 것이다. 

목 오브젝트는 다음과 네 단계를 거쳐서 사용하면 된다.
 - 인터페이스를 이용해 목 오브젝트를 만든다.
 - 목 오브젝트가 리턴할 값이 있으면 이를 지정해준다. 메소드가 호출되면 에외를 강제로 던지게 만들 수도 있다.
 - 테스트 대상 오브젝트에 DI해서 목 오브젝트가 테스트 중에 사용되도록 만든다.
 - 테스트 대상 오브젝트를 사용한 후에 목 오브젝트의 특정 메소드가 호출됐는지, 어떤 값을 가지고 몇 번 호출됐는지를 검증한다. 

 ```java
 // Mockito를 적용한 테스트 코드
@Test
public void upgradeLevels() throws Exception {
	UserServiceImpl userServiceImpl = new UserServiceImpl();
	
	UserDao mockUserDao = mock(UserDao.class);
	when(mockUserDao.getAll()).thenReturn(this.users);
	userServiceImpl.setUserDao(mockUserDao);
	
	MailSender mockMailSender = mock(MailSender.class);
	userServiceImpl.setMailSender(mockMailSender);
	
	userServiceImpl.upgradeLevels();
	
	//레벨 검증 
	verify(mockUserDao, times(2)).update(any(User.class));
	verify(mockUserDao).update(users.get(1));
	assertEquals(users.get(1).getLevel(), Level.SILVER);
	verify(mockUserDao).update(users.get(3));
	assertEquals(users.get(3).getLevel(), Level.GOLD);
	
	ArgumentCaptor<SimpleMailMessage> mailMessageArg = 
			ArgumentCaptor.forClass(SimpleMailMessage.class);
	verify(mockMailSender, times(2)).send(mailMessageArg.capture());
	List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
	assertEquals(mailMessages.get(0).getTo()[0], users.get(1).getEmail());
	assertEquals(mailMessages.get(1).getTo()[0], users.get(3).getEmail());
}
```



