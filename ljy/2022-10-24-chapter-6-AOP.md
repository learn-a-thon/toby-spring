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

## 6.3 다이내믹 프록시와 팩토리 빈
## 데코레이터 패턴
데코레이터 패턴은 타깃에 부가적인 기능을 런타임 시 다이내믹하게 부여해주기 위해 프록시를 사용하는 패턴을 말한다. 한 개 이상의 프록시를 통해 순서를 정해 단계적으로 위임하는 구조로 기능을 더해갈 수 있다.  
프록시로서 동작하는 각 데코레이터는 위임하는 대상에도 <U>인터페이스로 접근하기 때문에</U> 자신이 최종 타깃으로 위임하는지, 아니면 다음 단계의 데코레이터 프록시로 위임하는지 알지 못한다. 
> 자바 IO 패키지의 InputStream과 OutputStream 구현 클래스는 데코레이터 패턴이 사용된 대표적인 예다.  
데코레이터 패턴은 타깃의 코드를 손대지 않고, 클라이언트가 호출하는 방법도 변경하지 않은 채로 새로운 기능을 추가할 때 유용한 방법이다.

## 프록시 패턴
디자인패턴에서 말하는 프록시는 타깃에 대한 접근 방법을 제어하려는 목적을 가진 경우를 말한다.  
클라이언트에게 타깃에 대한 레퍼런스를 넘겨야하는데, 실제 타깃 오브젝트를 만드는 대신 프록시를 넘겨주는 방법을 활용할 수 있다. 

## 6.3.2 다이내믹 프록시
프록시는 기존 코드에 영향을 주지 않으면서 타깃의 기능을 확장하거나 접근 방법을 제어할 수 있는 유용한 방법이다. reflect 패키지 안에 프록시를 손쉽게 만들 수 있도록 지원해주는 클래스들이 있다.

## 리플렉션
다이내믹 프록시는 리플렉션 기능을 이용해서 프록시를 만들어준다. 리플렉션은 자바의 코드 자체를 추상화해서 접근하도록 만든 것이다. 자바의 모든 클래스는 `.class`나 `getClass()` 메소드를 호출해 메타 정보를 가져오거나 오브젝트를 조작할 수 있다. 

```java
// 리플렉션을 이용해 메소드를 호출하는 학습테스트
public class ReflectionTest {
  @Test
  public void invokeMethod() throws Exception {
      String name = "Spring";
        
      // length()
      assertThat(name.length(), is(6));
        
      Method lengthMethod = String.class.getMethod("length");
      assertThat((Integer)lengthMethod.invoke(name), is(6));
        
      // charAt
      assertThat(name.charAt(0), is('S'));
        
      Method charAtMethod = String.class.getMethod("charAt", int.class);
      assertThat((Character)charAtMethod.invoke(name, 0), is('S'));
  }
}
```

## 프록시 클래스
다이내믹 프록시를 이용한 프록시를 만들어보자.

```java
interface Hello {
    String sayHello(String name);
    String sayHi(String name);
    String sayThankYou(String name);
}

public class HelloTest implements Hello {
    String sayHello(String name) {
        return "Hello " + name;
    }

    String sayHi(String name) {
        return "Hi " + name;
    }

    String sayThankYou(String name) {
        return "Thank You " + name;
    }
}
```

```java
// 클라이언트 역할의 테스트
@Test
public void simpleProxy() {
    Hello hello = new HelloTarget();
    assertThat(hello.sayHello("Toby"), is("Hello Toby"));
    assertThat(hello.sayHi("Toby"), is("Hi Toby"));
    assertThat(hello.sayThankYou("Toby"), is("Thank You Toby"));
}
```

Hello 인터페이스를 구현한 프록시를 만들어보자. 프록시에는 데코레이터 패턴을 적용해서 타깃인 HelloTarget에 부가기능을 추가한다.

```java
public class HelloUppercase implements Hello {
  Hello hello;

  public HelloUppercase(Hello hello) {
      this.hello = hello;
  }

  @Override
  public String sayHello(String name) {
      // 위임과 부가기능 적용
      return hello.sayHello(name).toUpperCase();
  }

  @Override
  public String sayHi(String name) {
      return hello.sayHi(name).toUpperCase();
  }

  @Override
  public String sayThankYou(String name) {
      return hello.sayThankYou(name).toUpperCase();
  }
}
```

```java
// HelloUppercase 프록시 테스트
Hello proxiedHello = new HelloUppercase(new HelloTarget());
assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
```

## 다이내믹 프록시 적용
클래스로 만든 프록시인 HelloUpperdcase를 다이내믹 프록시를 이용해 만들어보자. 동작방식은 다음과 같다. 
![](https://velog.velcdn.com/images/nunddu/post/a3f0818c-84d0-4820-8682-865d19614ea2/image.png)  

프록시 팩토리에게 인터페이스 정보만 제공해주면 해당 인터페이스를 구현한 클래스의 오브젝트를 자동으로 만들어준다. Hello 인터페이스를 제공하면서 프록시 팩토리에게 다이내믹 프록시를 만들어달라고 요청하면 Hello 인터페이스의 모든 메소드를 구현한 오브젝트를 생성해준다. InvocationHandler 인터페이스를 구현한 오브젝트를 제공해주면 다이내믹 프록시가 받는 모든 요청을 InvocationHandler의 invoke() 메소드로 보내준다. Hello 인터페이스의 메소드가 아무리 많더라도 invoke() 메소드 하나로 처리할 수 있다.  

![](https://velog.velcdn.com/images/nunddu/post/2c6d5cf4-66ae-4669-a555-b9fef953bcb8/image.png)  


```java
// invocationHandler 구현
public class UppercaseHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String rest = (String) method.invoke(target, args);
        return ret.toUpperCase();
    }
}
```

InvoactionHandler를 사용하고 Hello 인터페이스를 구현하는 프록시를 만들면 다음과 같다.
```java
Hello proxiedHello = (Hello)Proxy.newProxyInstance(
            // 동적으로 생성되는 다이내믹 프록시 클래스 로딩에 사용할 클래스로더
            getClass().getClassLoader(),
            // 구현할 인터페이스
            new Class[] { Hello.class },
            // 부가기능과 위임 코드를 담은 InvocationHandler
            new UppercaseHandler(new HelloTarget()));
```

### 다이내믹 프록시의 확장
현재는 모든 메소드의 리턴 타입이 스트링이라고 가정하지만 예외의 경우 런타임 시 캐스팅 오류가 발생할 수 있다.

```java
// 확장된 UppercaseHandler
public class UppercaseHandler implements InvocationHandler {

  // 어떤 종류의 인터페이스를 구현한 타깃에도 적용 가능하도록 Object 타입으로 수정
  Object target;

  public UppercaseHandler(Object target) {
      this.target = target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Object ret = method.invoke(target, args);

      // String인 경우에만 대문자 변경 기능을 적용
      if (ret instanceof String) {
          return ((String)ret).toUpperCase();
      } else {
          return ret;
      }
  }
}
```

## 다이내믹 프록시를 이용한 트랜잭션 부가기능
UserServiceTx를 다이내믹 프록시 방식으로 변경해보자. 트랜잭션 처리가 필요한 경우마다 UserServiceTx처럼 일일히 구현하는 것은 큰 부담이다. 

### 트랜잭션 InvocationHandler
트랜잭션 부가기능을 가진 핸들러의 코드
```java
public class TransactionHandler implements InvocationHandler {
    // 부가기능을 제공할 타깃 오브젝트
    private Object target;
    // 트랜잭션 기능을 제공하는데 필요한 트랜잭션 매니저
    private PlatformTransactionManager transactionManager;
    // 트랜잭션을 적용할 메소드 이름 패턴
    private String pattern;

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 트랜잭션 적용 대상 메소드를 선별해서 트랜잭션 경계설정 기능을 부여해준다.
        if (method.getName().startsWith(pattern)) {
            return invokeInTransaction(method, args);
        } else {
            return method.invoke(target, args);
        }
    }

    private Object invokeInTransaction(Method method, Object[] args) throws Throwable {
        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 트랜잭션을 시작하고 타깃 오브젝트의 메소드를 호출
            Object ret = method.invoke(target, args);
            // 정상적으로 처리되면 커밋
            this.transactionManager.commit(status);
            return ret;
        } catch (InvocationTargetException e) {
            // 예외 발생 시 롤백
            this.transactionManager.rollback(status);
            throw e.getTargetException();
        }
    }
}
```
UserServiceTx 보다 코드는 복잡하지 않으면서 UserService뿐만 아니라 모든 트랜잭션이 필요한 오브젝트에 적용 가능한 트랜잭션 프록시 핸들러가 만들어졌다.

## 6.3.4 다이내믹 프록시를 위한 팩토리 빈
이제 TransactionHandler와 다이내믹 프록시를 스프링의 DI를 통해 사용할 수 있도록 만들어야 한다. 

### 팩토리 빈
팩토리 빈이란 스프링을 대신해서 오브젝트의 생성로직을 담당하도록 만들어진 특별한 빈을 말한다. 

```java
public interface FactoryBean<T> {
  // 빈 오브젝트를 생성해서 돌려줌
  T getObject() throws Exception;
  // 셍성되는 오브젝트의 타입을 알려줌
  Class<? extends T> getObjectType();
  // getObject()가 돌려줒는 오브젝트가 항상 싱글톤 오브젝트인지 알려줌
  boolean isSigleton();
}
```

```java
// 생성자를 제공하지 않는 클래스
public class Message {
  String text;

  // 생성자가 private으로 선언되어 외부에서 생성자를 통해 오브젝트를 만들 수 없다.
  private Message(String text) {
      this.text = text;
  }

  public String getText() {
      return text;
  }

  // 생성자 대신 사용할 수 있는 스태틱 팩토리 메소드 제공
  public static Message newMessage(String text) {
      return new Message(text);
  }
}
```

스프링은 private 생성자를 가진 클래스도 빈으로 등록해주면 리플렉션을 이용해 오브젝트를 만들어준다. 리플렉션은 private으로 선언된 접근 규약을 위반할 수 있는 강력한 기능이 있다. 일반적으로 private 생성자를 가진 클래스를 빈으로 등록하는 일은 권장되지 않으며, 등록하더라고 빈 오브젝트가 바르게 동작하지 않을 가능성이 있으니 주의해야한다.

Message 클래스의 오브젝트를 생성해주는 팩토리 빈 클래스를 만들어보자.
```java
public class MessageFactoryBean implements FactoryBean<Message> {
  String text;

  /*
   * 오브젝트를 생성할 때 필요한 정보를 팩토리 빈의 프로퍼티로 설정하여 대신 DI
   * 주입된 정보는 오브젝트 생성 중 사용됨
  */
  public void setText(String text) {
      this.text = text;
  }

  /*
   * 실제 빈으로 사용될 오브젝트를 직접 생성
   * 코드를 이용하므로 복잡한 방식의 오브젝트 생성과 초기화 작업도 가능
  */
  @Override
  public Message getObject() throws Exception {
      return Message.newMessage(this.text);
  }

  @Override
  public Class<?> getObjectType() {
      return Message.class;
  }

  /*
   * getObject()가 돌려주는 오브젝트가 싱글톤인지 알려준다.
   * 이 팩토리 빈은 요청할 때마다 새로운 오브젝트를 만들어주므로 false
   * 이것은 팩토리 빈의 동작방식에 관한 설정이고, 
   * 만들어진 빈 오브젝트는 싱글톤으로 스프링이 관리해줄 수 있다.
  */
  @Override
  public boolean isSingleton() {
      return false;
  }
}
```
팩토리 빈은 전형적인 팩토리 메소드를 가진 오브젝트다. 스프링은 FactoryBean인터페이스를 구현한 클래스가 빈의 클래스로 지정되면, 팩토리 빈 클래스의 오브젝트의 getObject() 메소드를 이용해 오브젝트를 가져오고, 이를 빈으로 사용한다. 

### 프록시 팩토리 빈 방식의 장점
 - 프록시를 적용할 대상이 구현하고 있는 인터페이스를 구현하는 프록시 클래스를 일일이 만들어야하는 번거로움
 - 부가적인 기능이 여러 메소드에 반복적으로 나타나게 돼서 코드의 중복
### 프록시 팩토리 빈의 한계
 - 비슷한 팩토리 빈의 설정 중복 문제
 - 핸들러가 오브젝트 프록시 팩토리 빈 개수만큼 만들어진다. (부가기능의 개수만큼 증가)

 ## 6.4.1 스프링의 프록시 팩토리 빈
 스프링은 일관된 방법으로 프록시를 만들 수 있게 도와주는 추상 레이어를 제공한다. 스프링의 **ProxyFactoryBean**은 프록시를 생성해서 빈 오브젝트로 등록하게 해주는 팩토리 빈이다. ProxyFactoryBeanㅇ은 순수하게 프록시를 생성하는 작업만을 담당하고 프록시를 통해 제공해줄 부각기능은 별도의 빈에 둘 수 있다. 부가기능은 **MethodInterceptor**를 구현해서 만들 수 있다. 

```java
...
@Test
public void simpleProxy(){
    // JDK 다이내믹 프록시 생성
    Hello proxiedHello = (Hello)Proxy.newProxyInstance(
      getClass().getClassLoader(),
      new Class[]{Hello.class},
      new UppercaseHandler(new HelloTarget()));
}

@Test
public void proxyFactoryBean(){
  ProxyFactoryBean pfBean = new ProxyFactoryBean();
  pfBean.setTarget(new HelloTarget()); // 타깃 설정
  pfBean.addAdvice(new UppercaseAdvice()); // 부가 기능을 담은 어드바이스를 추가

  Hello proxiedHello = (Hello) pfBean.getObject(); // FactoryBean이므로 getObject()로 생성된 프록시를 가져온다.

  ...
}

static class UpperAdvice implements MethodInterceptor{
  public Object invoke(MethodInvocation invocation) throws Throwable{
    // 메서드 실행 시 target obj를 저달할 필요 없음
    // MethodInvocation에 target obj 정보가 들어 있기 때문이다.
    // proceed()메서드를 실행하면 target obj의 메서드를 내부적으로 실행시켜 준다.
    String ret = (String)invocation.proceed(); 
    return ret.toUpperCase(); // 부가기능적용
  }
}
```

### 어드바이스: 타깃이 필요없는 순수한 부가기능
인터페이스 타입을 제공하지 않아도 ProxyFactoryBean이 인터페이스 자동검출 기능을 사용해 target이 구현하고 있는 인터페이스 정보를 알아낸다.

ProxyFactoryBean에는 여러 개의 MethodsInterceprot를 추가할 수 있다. ProxyFactoryBean 하나만으로 여러 개의 부가 기능을 제공해주는 프록시를 만들 수 있다. 새로운 부가기능을 추가할 때마다 프록시와 프록시 팩토리 빈도 추가해줘야 한다는 문제를 해결할 수 있다. 

MethodInterceptor처럼 타깃 오브젝트에 적용하는 부가기능을 담은 오브젝트를 스프링에서는 어드바이스라고 부른다.

어드바이스 : 타깃 오브젝트에 종속되지 않는 순수한 부가기능을 담은 오브젝트

### 포인트컷: 부가기능 적용 대상 메소드 선정 방법
![](https://velog.velcdn.com/images/nunddu/post/88647b10-c7be-461e-8be9-dd37d6c89519/image.png)  
스프링은 부가기능을 제공하는 오브젝트를 어드바이스라고 부르고, 메소드 선정 알고리즘을 담은 오브젝트를 포인트컷이라고 부른다. 어드바이스와 포인트컷은 모두 프록시에 DI로 주입돼서 사용된다. 두 가지 모두 여러 프록시에서 공유가 가능하도록 만들어지기 때문에 싱글톤 빈으로 등록이 가능하다.
 1) 프록시는 클라이언트로부터 요청을 받으면 먼저 포인트컷에게 부가기능을 부여할 메소드인지 확인한다.
 2) 프록시는 포인트컷으로부터 부가기능을 적용할 대상 메소드인지 확인받으면, MethodInterceptor 타입의 어드바이스를 호출한다.

### 어드바이스와 포인트컷의 재사용
ProxyFactoryBean은 스프링의 DI와 템플릿/콜백 패턴, 서비스 추상화 등의 기법이 모두 적용된 것이다. 부가기능을 구현하는 어드바이스를 그대로 적용할 수 있고, 이름 패턴을 지정해서 메소드 선정 포인트 컷을 적용할 수도 있다. 

## 6.5.2 DafaultAdvisorAutoProxyCreate의 적용
### 클래스 필터를 적용한 포인트컷 작성
NameMathchMethodPointCut을 상속해서 프로퍼티로 주어진 이름 패턴을 가지고 클래스 이름을 비교하는 ClassFilter를 추가하도록 만든다. 

```java
public class NameMatchClassMethodPointcut extends NameMatchMethodPonitcut {
    public void setMappedClassNAme(String mappedClassName) {
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }

    static class SimpleClassFilter implements ClassFilter {
        String mappedName;

        private SimpleClassFiilter(String mappedName) {
            this.mappedName = mappedName;
        }

        public boolean matches(Class<?> clazz) {
            return PatternMatchUtils.simpleMatch(mappedName, clazz.getSimpleName()); // 와일드카드가 들어간 문자열 비교를 지원하는 스프링의 유틸성 메소드 *name, name*, *name* 등
        }
    }
}

```

### 어드바이저를 이용하는 자동 프록시 생성기 등록
적용할 자동 프록시 생성기인 DefaultAdvisorAutoProxyCreator는 등록된 빈 중에서 Advisor 인터페이스를 구현한 것을 모두 찾는다. 생성되는 모든 빈에 대해 어드바이저의 포인트컷을 적용해보면서 프록시 적용 대상을 선정한다. 

### 6.5.3 포인트컷 표현식을 이용한 포인트컷
기존의 단순한 필터 방식 대신에 더 복잡하고 세밀한 기준을 이용해 클래스나 메소드를 선정하게 하려면 어떻게 해야할까? 스프링은 아주 간단하고 효과적인 방법으로 포인트컷의 클래스와 메소드를 선정하는 알고리즘을 작성할 수 있는 방법을 제공한다. => **포인트컷 표현식**

### 포인트컷 표현식
포인트컷 표현식을 지원하는 포인트컷을 적용하려면 AspectJExpressionPointcut 클래스를 사용하면 된다. 포인트컷 표현식은 자바의 RegEx 클래스가 지원하는 정규식처럼 간단한 문자열로 복잡한 선정조건을 쉽게 만들어낼 수 있는 강력한 표현식을 지원한다.

### 포인트컷 표현식 문법
AspectJ 포인트컷 표현식은 포인트컷 지시자를 이용해 작성한다. 대표적으로 사용되는 것은 execution()이다.

`execution([접근제한자 패턴] 타입패턴 [타입패턴.]이름패턴 (타입패턴 | "..", ...) [throws 예외 패턴])
메소드의 풀 시그니처를 문자열로 비교하는 개념이다. 

다음 문장을 실행하면 리플렉션의 Method 오브젝트가 제공하는 Target.minus()메소드의 풀 시그니처를 볼 수 있다.

```java
public class Target implements TargetInterface {
    public int minus(int a, int b) throws RuntimeException { return 0; }
}
```
`System.out.println(Target.class.getMethod("minus, int.class, int.class))`;

`public int springbook.learningtest.spring.pointcut.Target.minus(int, int) throws java.lang.RuntimeException`

 - `public`   
 접근 제한자로 포인트컷 표현식에서 생략할 수 있다.
 - `int`  
 리턴 값의 타입을 나타내는 패턴이다. 포인트컷의 표현식에서 리턴 값의 타입 패턴은 필수. *로 모든 타입 지정 가능
 - `springbook.learningtest.spring.pointcut.Target`  
 패키지 경로로 생략 가능. 
 - `minus`  
 메소드 이름 패턴이다. 필수항목 *로 모든 메소드 지정 가능
 - `(int, int)`  
 메소드 파라미터의 타입 패턴으로 ','로 구분하면서 순서대로 적는다. 
 - `throws java.lang.RuntimeException`
 예외 이름에 대한 타입 패턴이다. 생략 가능

## 6.5.4 AOP란 무엇인가?
### 트랜잭션 서비스 추상화
트랜잭션 적용이라는 추상적인 작업 내용은 유지한채로 구체적인 구현 방법을 자유롭게 바꿀 수 있도록 서비스 추상화 기법을 적용했다. 구체적인 구현 내용을 담은 의존 오브젝트는 런타임 시에 다이내믹하게 연결해준다는 DI를 활용한 전형적인 접근 방법. 트랜잭션 추상화란 결국 인터페이스와 DI를 통해 무엇을 하는지는 남기고, 그것을 어떻게 하는지를 분리한 것이다. 어떻게 할지는 더 이상 비즈니스 로직 코드에는 영향을 주지 않고 독립적으로 개발할 수 있게 댔다.

### 프록시와 데코레이터 패턴
트랜잭션의 경계 설정을 담당하는 코드의 특성 때문에 단순한 추상화와 메소드 추출 방법으로는 제거할 할 방법이 없었지만 DI를 이용해 데코레이터 패턴을 적용하는 방법을 사용했다. 클라이언트가 인터페이스와 DI를 통해 접근하도록 설계하고 데코레이터 패턴을 적용해서 비즈니스 로직을 담은 클래스의 코드에는 전형 영향을 주지 않으면서 트랜잭션이라는 부가기능을 부여할 수 있는 구조를 만들었다.

### 다이내믹 프록시와 프록시 팩토리 빈
프록시 클래스 없이도 프록시 오브젝트를 런타임 시에 만들어주는 JDK 다이내믹 프록시 기술을 적용했다. 덕분에 프록시 클래스 코드 작성의 부담도 덜고, 부가 기능 부여 코드 여기저기 중복돼서 나타나는 문제도 일부 해결할 수 있었다. 단, 동일한 기능의 프록시를 여러 오브젝트에 적용할 경우 오브젝트 단위로는 중복이 일어나는 문제는 해결하지 못함.

### 자동 프록시 생성 방법과 포인트컷
트랜잭션 적용 대상이 되는 빈마다 일일이 프록시 팩토리 빈을 설정해줘야 한다는부담이 남아있었다. 이를 위해 컨테이너 초기화 시점에서 자동으로 프록시를 만들어주는 방법을 도입했다. 프록시를 적용할 대상을 일일이 지정하지 않고 패턴을 이용해 자동으로 선정할 수 있또록, 클래스를 선정하는 기능을 담은 확장된 포인트컷을 사용했다. 덕분에 트랜잭션 부가기능을 어디에 적용 하는지에 대한 정보를 포인트컷이라는 독립적인 정보로 완전히 분리할 수 있었다. 최종적으로는 포인트컷 표현식이라는 깔끔한 방법을 선택할 수 있게 됐다.

### 부가기능의 모듈화
관심사가 같은 코드를 분리해 한데 모으는 것은 소프트웨어 개발의 가장 기본이 되는 원칙.  
부가기능은 다른 핵심기능과 같은 레벨에서는 독립적으로 존재하기 어렵다. 지금까지 봐온 DI, 데코레이터 패턴, 다이내믹 프록시, 오브젝트 생성 후 처리, 자동 프록시 생성, 포인트컷과 같은 기법은 이런 문제를 해결하기 위해 적용한 대표적인 방법이다. 그렇게 트랜잭션 경계설정 기능은 TransactionAdvice라는 이름으로 모듈화될 수 있었다.

### AOP: 관점 지향 프로그래밍

## 6.5.5 AOP 적용기술
### 프록시를 이용한 AOP
스프링은 IoC/DI 컨테이너와 다이내믹 프록시, 데코레이터 패턴, 프록시 패턴, 자동 프록시 생성 기법, 빈 오브젝트의 후처리 조작 기법 등의 다양한 기술을 조합해 AOP를 지원하고 있다. 

독립적으로 개발한 부가기능 모듈을 다양한 타깃 오브젝트의 메소드에 다이내믹하게 적용해주기 위해 가장 중요한 역할을 맡고 있는게 바로 프록시이다. 그래서 스프링 AOP는 프록시 방식의 AOP라고 할 수 있다. 

## 6.6.1 트랜잭션 정의
### 트랜잭션 전파
트랜잭션 전파란 트랜잭션의 경계에서 이밎 진행중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다. 
 - PROPAGATION_REQUIRED  
    진행중인 트랜잭션이 없으면 새로 시작하고, 이미 시작된 트랜잭션이 있으면 참여한다.
 - PROPAGATION_REQUIRES_NEW  
    항상 새로운 트랜잭션을 시작한다. 독자적으로 동작한다.
 - PROPAGATION_NOT_SUPPORTED  
    트랜잭션 없이 동작하도록 만들 수 있다. 진행 중인 트랜잭션이 있어도 무시한다. 
 
### 격리수준
모든 DB 트랜잭션은 격리수준을 갖고 있어야한다. 가능하면 모든 트랜잭션이 순차적으로 진행돼서 다른 트랜잭션의 작업에 독립적인 것이 좋지만, 그렇게되면 성능이 크게 떨어질 수 밖에 없다. 격리수준은 기본적으로 DB에 설정되어 있지만 JDBC 드라이버나 DataSource 등에서 재설정할 수 있다.

### 제한시간
트랜잭션을 수행하는 제한시간을 설정할 수 있다.

### 읽기전용
읽기전용으로 설정해두면 트랜잭션 내에서 데이터를 조작하는 시도를 막아줄 수 있다. 또한 데이터 엑세스 기술에 따라서 성능이 향상될 수 있다. 

## 6.6.2 트랜잭션 인터셉터와 트랜잭션 속성
메소드별로 다른 트랜잭션 정의를 적용하려면 어드바이스의 기능을 확장해야 한다. 마치 초기에 TransactionHandler에서 메소드 이름을 이용해 트랜잭션 적용 여부를 판단 했던 것과 비슷한 방식을 사용할 수 있다.

### TransactionInterceptor
스프링의 TransactionInterceptor를 이용해보자.

```java
public Object invoke(MethodInvocation invocation) throws Throwable {
    TransactionStatus status = 
        this.transactionManager.getTransaction(new DefaultTransactionDefinition()); // 트랜잭션 정의를 통한 네 가지 조건
        try {
            Objedt ret = invocation.proceed();
            this.transactionManager.commit(status);
            return ret;
        } catch (RuntimeException e) { 
            this.transactionManger.rollback(status);
            throw e;
        }
}

```
체크 예외를 던지는 타깃에 사용한다면 문제가 될 수 있다. 런타임 예외가 아닌 체크 예외를 던지는 경우에는 이것을 예외상황으로 해석하지 않고 일종의 비즈니스 로직에 따른, 의미가 있는 리턴 방식의한 가지로 인식해서 트랜잭션을 커밋해버린다. TransactionInterceptor는 이런 TransactionAttribute를 Properties라는 일종의 맵 타입 오브젝트로 전달받는다. 컬렉션을 사용하는 이유는 메소드 패턴에 따라서 각기 다른 트랜잭션 속성을 부여할 수 있게 하기 위해서다. 

## 6.6.3 포인트컷과 트랜잭션 속성의 적용 전략
트랜잭션을 적용할 후보 메소드를 선정하는 작업은 포인트컷에 의해 진행된다. 

### 트랜잭션 포인트컷 표현식은 타입 패턴이나 빈 이름을 이용한다.
일반적으로 트랜잭션을 적용할 타깃 클래스의 메소드는 모두 트랜잭션 적용 후보가 되는 것이 바람직하다. 쓰기 작업이 없는 단순한 조회 작업만 하는 메소드에도 모두 트랜잭션을 적용하는게 좋다. 조회의 경우에는 읽기 전용으로 트랜잭션 속성을 설정해두면 그만큼 성능의 향상을 가져올 수 있다. 

### 공통된 메소드 이름 규칙을 통해 최소한의 트랜잭션 어드바이스와 속성을 정의한다. 
너무 다양하게 트랜잭션 속성을 부여하면 관리만 힘들어질 뿐이다. 따라서 기준이 되는 몇 가지 트랜잭션 속성을 정의하고 그에 따라 적절한 메소드 명명 규칙을 만들어두면 하나의 어드바이스만으로 애플리케이션의 모든 서비스 빈에 트랜잭션 속성을 지정할 수 있다. 

### 프록시 방식 AOP는 같은 타깃 오브젝트 내의 메소드를 호출할 때는 적용되지 않는다.
AOP에서는 프록시를 통한 부가기능의 적용은 클라이언트로부터 호출이 일어날 때만 가능하다. 여기서 클라이언트는 인터페이스를 통해 타깃 오브젝트를 사용하는 다른 모든 오브젝트를 말한다. 

## 6.6.4 트랜잭션 속성 적용
트랜잭션 속성과 그에 따른 트랜잭션 전략을 UserService에 적용해보자.

### 트랜잭션 경계설정의 일원화 
트랜잭션 경계설정의 부가기능을 여러 계층에서 중구난방으로 적용하는 건 좋지 않다. 비즈니스 로직을 담고 있는 서비스 계층 오브젝트의 메소드가 트랜잭션 경계를 부여하기에 가장 적절하다.  
UserDao 인터페이승 정의된 메소드 중 대부분은 독자적인 트랜잭션을 가지고 사용될 가능성이 높다. 해당 메소드를 UserService 인터페이스에 추가한다.

```java
public interface UserService {
    void add(User user);
    User get(String id);
    List<User> getAll();
    void deleteAll();
    void update(User user);

    void upgradeLevels();
}
```

```java
public class UserServiceImpl implements UserService {
    UserDao userDao;

    public void deleteAll() { userDao.deleteAll(); }
    public User get(String id) { userDao.get(id); }
    public List<User> getAll() { userDao.getAll(); }
    public void update() { userDao.update(); }

}
```

### 트랜잭션 속성을 가진 트랜잭션 어드바이스 등록
readOnly 트랜잭션 속성을 사용하는 어드바이스 등록.

### 트랜잭션 속성 테스트
트랜잭션 부가기능의 적용 전략을 수정했고 새로운 메소드도 추가했다. 

```java
public List<User> getAll() {
    for (User user : super.getAll()) {
        super.update(user);
    }
    return null;
}
```
```java
@Test
public void readOnlyTransactionAttribute() {
    testUserSertive.getAll();
}
```

트랜잭션 속성이 제대로 적용됐다면 읽기전용 속성을 위반했기 때문에 예외가 발생한다. 읽기 전용 속성을 위반했을 때 발생하는 예외의 종류를 알게되었으니 아래 내용을 테스트에 반영하면 성공할 수 있다.

```java
@Test(expected=TransientDataAccessResourceException.class)
```

## 6.7.1. 트랜잭션 애노테이션
스프링 3.0은 자바 5에서 등장한 어노테이션을 많이 사용한다. 

### @Transactional
스프링은 @Transactional이 부여된 모든 오브젝트를 자동으로 타깃 오브젝트로 인식한다. 이때 사용되는 포인트컷은 TransactionAttributeSourcePointcut이다. @Transactional은 기본적으로 트랜잭션 속성을 정의하는 것이지만, 동시에 포인트 컷의 자동등록에도 사용된다. 

### 트랜잭션 속성을 이용하는 포인트컷
![](https://velog.velcdn.com/images/nunddu/post/b66dcd4e-7fca-4cc6-b8ef-e46ab819cd9f/image.png)  

이 방식을 이용하면 포인트컷과 트랜잭션 속성을 애노테이션 하나로 지정할 수 있다. 트랜잭션 속성은 타입 레벨에 일괄적으로 부여할 수도 있지만, 메소드 단위로 세분화해서 트랜잭션 속성을 다르게 지정할 수도 있기 때문에 매우 세밀한 트랜잭션 속성 제어가 가능해진다.

### 대체정책
스프링은 @Transactional을 적용할 때 4단계의 대체 정책을 이용하게 해준다. 메소드의 속성을 확인할 때 타깃 메소드, 타깃 클래스, 선언 메소드, 선언 타입의 순서에 따라서 @Transactional이 적용됐는지 차례로 확인하고, 가장 먼저 발견되는 속성정보를 사용하게 하는 방법이다. 

```java
[1]
public interface Service {
    [2]
    void method();
    [3]
    void method2();
}

[4]
public class ServiceImpl implements Service {
    [5]
    public void method1() {}
    [6]
    public void method2() {}
}
```

@Transactional을 사용하면 대체 정책을 잘 활용해서 애노테이션 자체는 최소한으로 사용하면서도 세밀한 제어가 가능하다. 기본적으로 @Transactional 적용 대상은 클라이언트가 사용하는 인터페이스가 정의한 메소드이므로 @Transactional 적용 대상은 클라이언트가 사용하는 인터페이스가 정의한 메소드이므로 @Transactional도 타깃 클래스보다는 인터페이스에 두는 게 바람직하다. 하지만 인터페이스를 사용하는 프록시 방식의 AOP가 아닌 방식으로 트랜잭션을 적용하면 인터페이에 정의한 @Transactional은 무시되기 때문에 안전하게 타깃 클래스에 @Transactional을 두는 방법을 권장한다.

## 6.8.1 선언적 트랜잭션과 트랜잭션 전파 속성

![](https://velog.velcdn.com/images/nunddu/post/0e624243-a288-46a1-b543-f778e7ad02f7/image.png)  

트랜잭션 전파 속성이 없다면 다양한 상황에서 User의 add 기능이 다양한 서비스 메소드에서 필요하고 그만큼 중복이 발생했을 것이다. AOP를 이용해 코드 외부에서 트랜잭션의 기능을 부여해주고 속성을 지정할 수 있게 하는 방법을 **선언적 트랜잭션**이라고 한다. 반대로 개별 데이터 기술의 트랜잭션 API를 사용해 직접 코드 안에서 사용하는 방법은 **프로그램에 의한 트랜잭션**이라고 한다. 

## 6.8.2 트랜잭션 동기화와 테스트

### 트랜잭션 매니저를 이용한 테스트용 트랜잭션제어
메소드를 추가하지 않고도 테스트 코드만으로 세 메소드의 트랜잭션을 통합하는 방법이 있다. 테스트 메소드에서 UserService의 메소드를 호출하기 전에 트랜잭션을 미리 시작해주기만 하면 된다.

```java
@Test
public void transactionSync() {

    DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
    TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

    userService.deleteAll();

    userService.add(users.get(0));
    userService.add(users.get(1)); // 앞에서만들어진 트랜잭션에 모두 참여한다.
    

    transactionManager.commit(txStatus); // 앞에서 시작한 트랜잭션을 커밋
}

```

### 트랜잭션 동기화 검증

```java
public void transactionSync() {
    DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
    txDefinition.setReadOnly(true);

    TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

    userService.deleteAll(); // 테스트 코드에서 시작한 트랜잭션에 참여한다면 읽기전용 속성을 위반 했으니 예외가 발생한다.
}
```
해당 테스트를 실행해보면 테스트코드 내에서 시작한 트랜잭션에 deleteAll() 메소드가 참여하고 있어 예외가 발생하는 것을 확인할 수 있고 트랜잭션 매니저를 통해 테스트환경에서도 트랜잭션을 묶을 수 있는 것을 검증할 수 있다. 

## 6.8.3 테스트를 위한 트랜잭션 애노테이션

### @Transactional
테스트에도 @Transactional을 적용할 수 있다. 테스트 클래스 또는 메소드에 @Transactional 어노테이션을 부여해주면 테스트 메소드에 트랜잭션 경계가 자동으로 설정된다. 이를 이용하면 **테스트 내에서 진행하는 모든 트랜잭션 관련 작업을 하나로 묶어줄 수 있다.** 번거로운 코드 사용 대신 간단한 어노테이션만으로 트랜잭션이 적용된 테스트를 손쉽게 만들 수 있다. 

```java
// 테스트에 적용된 @Transactional

@Test
@Transactional
public void transactionSync() {
    userService.deleteAll();
    userService.add(users.get(0));
    userService.add(users.get(1));
}
```
마찬가지로 트랜잭션 전파 여부를 확인하고 싶으면 읽기 전용으로 바꾸고 예외를 던지는지 확인해보면 된다.

```java
@Test
@Transactional(readOnly=true)
public void transactionSync() {
    userService.deleteAll(); // 트랜잭션 속성 위반으로 예외 발생
}
```

### @Rollback
테스트에 적용된 `@Transactional`은 기본적으로 트랜잭션을 강제 롤백시키도록 설정되어 있다. 테스트 메소드 안에서 진행되는 작업을 하나의 트랜잭션으로 묶고싶지만 강제 롤백을 원하지 않는 경우네는 `@Rollback`이라는 어노테이션을 사용하면 된다. 롤백은 기본값을 true로 가지고 있어 롤백을 원하지 않는다면 `@Rollback`(false)로 명시해준다.

### @TransactionConfiguration
`@Transactional`은 테스트 클래스에 넣어서 모든 테스트 메소드에 일괄 적용할 수 있지만 `@Rollback`은 메소드 레벨에서만 사용할 수 있다. `@TransactionConfiguration`을 사용하면 롤백에 대한 공통 속성을 지정하고 예외적인 메소드에는 속성을 새로 지정할 수 있다.

### NotTransactional과 Propagation.NEVER
특정 메소드에만 테스트 메소드에 의한 트랜잭션이 시작되지 않도록 만들어줄 수 있다. `@NotTransactional`을 테스트 메소드에 부여하면 클래스 레벨의 `@Transactional` 설정을 무시하고 트랜잭션을 시작하지 않은 채로 테스트를 진행한다. 물론 테스트 안에서 호출하는 메소드에서 트랜잭션을 사용하는데는 영향을 주지 않는다. 또 다른 방법은 `Transactional(propagation=Propagation.NEVER)`를 지정해주면 트랜잭션이 시작되지 않는다. 

### 효과적인 DB 테스트
테스트 내에서 트랜잭션을 제어할 수 있는 네 가지 어노테이션을 잘 활용하면 DB가 사용되는 통합 테스트를 만들 때 매우 편리하다. DB가 사용되는 통합 테스트를 별도의 클래스로 만들어둔다면 기본적으로 클래스 레벨에 @Transactional을 부여해준다. DB가 사용되는 통합 테스트는 가능한 한 롤백 테스트로 만드는게 좋다. **모든 테스트를 한꺼번에 실행하는 빌드 스크립트 등에서 테스트에서 공통적으로 이용할 수 있는 테스트 DB를 셋업해주고, 각 테스트는 자신이 필요한 테스트 데이터를 보충해서 테스트를 진행하게 만든다. 테스트는 어떤 경우에도 서로 의존하면 안된다.** 