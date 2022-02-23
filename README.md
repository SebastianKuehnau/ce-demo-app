# CE Demo App

This branch shows the simple integration of the PresenceManager part of the Collaboration Engine. With the Presence Menanger you can respond when a user joins and leaves to a specific topic context. 

To integrate the presence manager you need to do the following steps:

1. create instance of presence manager when bean is populated to a form
  ```java
PresenceManager presenceManager = new PresenceManager(component, localUser, topicId);
  ```
2. make user presence visible in presence manager
```java
presenceManager.markAsPresent(true);
```
3. implement listener invoke when user state is changing
```java
presenceManager.setPresenceHandler(presenceContext -> {
        userList.add(presenceContext.getUser());

        //do something when user joins

        return () -> {
            userList.remove(presenceContext.getUser());
            //do something when user leaves
        };
});
```

See results and full implementation in [presence manger branch](https://github.com/SebastianKuehnau/ce-demo-app/tree/presence-manager).
