# toby-spring
토비의 스프링 스터디

# 진행 방식
- 스터디에 참여하는 인원은 자신의 `영어 이니셜(Eng Name)` 혹은 `Github Id` 를 이름으로 **폴더를 생성** 하고 각자 학습한 내용을 관리한다.
- 학습 시작하기 전, [Convention > Branch](#branch) 를 참고하여 **브랜치를 생성한다.**
- 학습 시작, 생성한 브랜치에서 학습한 내용을 **커밋([Convention > Commit](#commit))하여 기록한다.**
- 학습 완료, 학습이 끝난 브랜치는 main 브랜치로 **PR([Convention > Pull Request](#pull-request)) 요청**한다.
  - 스터디원들의 확인(Approve)이 된 후에 머지(Merge) 할 수 있다.
- 스터디원들은 요청된 PR에 대해 **“잘못된 내용”**, **“보완했으면 하는 내용”**, **“추가 학습이 필요한 내용”** 등을 **적극적으로 리뷰**해준다.
- 개개인의 상황을 고려하여 **선행 학습을 제한하지 않는다.(오히려 권장)**

### 규칙
- 스터디 참여시 보증금 20000원 입금
- 학습 챕터 별 마감 기한은 2주
- 마감 기한 안에 PR 요청을 못한 경우 벌금(특별한 사유는 제외)
  - 벌금: 미요청 일수 x 5000원

# Convention
### Branch
```
{Folder Name}/chapter-{chapter-no}

ex)
memberA/chapter-1
```

### Commit
```
[{Folder Name}] yyyy-MM-dd {chapter-info} {message}

ex) 
[memberA] 2000-12-31 1.1 초난감 DAO
```

### Pull Request
```
[{Folder Name}] {chapter title}

ex) 
[memberA] 1. 오브젝트와 의존관계
```

### File
* markdown(기본)
* 한글, 영어(소문자만 허용), -(dash)

```
// 학습 문서 파일 형식
{chapter number}. {chapter title}.md

ex) 
1. 오브젝트와 의존관계.md
```

# 저장소 구조
```bash
│
├ member1
│  ├ images // 이미지 파일(Optional)
│  ├ README.md // README(Optional)
│  ├ 1. 오브젝트와 의존관계.md // 학습 내용
│  └ ...
│
├ member2
│  ├ images // 이미지 파일(Optional)
.  ├ README.md // README(Optional)
.  ├ 1. 오브젝트와 의존관계.md // 학습 내용
.  └ ...
```
