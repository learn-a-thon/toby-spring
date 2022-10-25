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

