# CE Demo App

This project can be used to show how simple you can integrate the Collaboration Engine into an existing application with
data binding.

To integrate the collaboration engine you need to do the following steps:

1. add @Push annotation to Application class
  ```java
@Push
public class Application ...
  ```
2. change Binder to CollaborationBinder
```java
private CollaborationBinder<SamplePerson> binder;
```
3. create a user-id for collaboration engine
```java
String userId = System.identityHashCode(UI.getCurrent()) + "";
UserInfo localUser = new UserInfo(userId, "User " + userId);
```
4. add user info instance to CE-Binder
```java
binder = new CollaborationBinder<>(SamplePerson.class, localUser);
```
5. create avatar component to show user details to other collaborators
```java
AvatarGroup avatarGroup = new CollaborationAvatarGroup(localUser, "personform");
editorDiv.addComponentAsFirst(avatarGroup);
avatarGroup.setVisible(false);
```
6. set bean and bean specific topic id to CE binder
```java
String topicId = "personform/" + person.getId() ;
avatarGroup.setTopic(topicId);
binder.setTopic(topicId, () -> this.samplePerson);
```

See results and full implementation in [collaborative branch](https://github.com/SebastianKuehnau/ce-demo-app/tree/collaborative).
