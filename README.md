# CE Demo App

This project can be used to show how simple you can integrate the Collaboration Engine into an existing application with
data binding.

To integrate the collaboration engine you need to do the following steps:

1. add necessary for listing messages and message input to view class 
  ```java
CollaborationMessageList messageList = new CollaborationMessageList(localUser, null);
CollaborationMessageInput messageInput = new CollaborationMessageInput(messageList);
  ```
2. set Topic ID for appropriate bean when available
```java
messageList.setTopic(topicId);
```
3. do some styling for beauti reasons
```java
chatDialog = new Dialog();
VerticalLayout rootLayout = new VerticalLayout();
rootLayout.setSizeFull();
rootLayout.setPadding(false);
rootLayout.setMargin(false);
rootLayout.setSpacing(false);

messageList = new CollaborationMessageList(localUser, null);
messageList.setSizeFull();

CollaborationMessageInput messageInput = new CollaborationMessageInput(messageList);
messageInput.setWidthFull();
rootLayout.add(messageList, messageInput);
rootLayout.expand(messageList);

chatDialog.setWidth(400, Unit.PIXELS);
chatDialog.setHeight(600, Unit.PIXELS);
chatDialog.add(rootLayout);
chatDialog.setModal(true);
```

See results and full implementation in [chat branch](https://github.com/SebastianKuehnau/ce-demo-app/tree/chat).
