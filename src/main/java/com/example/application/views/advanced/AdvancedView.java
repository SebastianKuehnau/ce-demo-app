package com.example.application.views.advanced;

import com.example.application.Application;
import com.example.application.data.entity.SamplePerson;
import com.example.application.data.service.SamplePersonService;
import com.example.application.util.BrowserUtil;
import com.example.application.views.MainLayout;
import com.vaadin.collaborationengine.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Advanced")
@Route(value = "advanced/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class AdvancedView extends HorizontalLayout implements BeforeEnterObserver {

    public static final String TOPIC_ID = "personform_advanced";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "advanced/%d/edit";
    private final UserInfo localUser;

    private final Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final CollaborationBinder<SamplePerson> binder;

    private SamplePerson samplePerson;

    private final SamplePersonService samplePersonService;
    private CollaborationAvatarGroup avatarGroup;
    private CollaborationMessageList messageList;
    private Dialog chatDialog;
    private HorizontalLayout chatLayout;
    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private TextField occupation;
    private Checkbox important;
    private VerticalLayout editorLayoutDiv;
    private PresenceManager presenceManager;
    private List<UserInfo> userList;
    private Span readonlyNotice;

    public AdvancedView(@Autowired SamplePersonService samplePersonService) {
        this.samplePersonService = samplePersonService;

        // NOTE: In a real application, use the user id of the logged in user
        // instead
        String userId = System.identityHashCode(UI.getCurrent()) + "";
        localUser = new UserInfo(userId, "User " + userId);

        addClassNames("master-detail-view", "flex", "flex-col", "h-full");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("dateOfBirth").setAutoWidth(true);
        grid.addColumn("occupation").setAutoWidth(true);
        LitRenderer<SamplePerson> importantRenderer = LitRenderer.<SamplePerson>of(
                        "<vaadin-icon ?hidden=${!item.important} icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></vaadin-icon>" +
                        "<vaadin-icon ?hidden=${item.important} icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></vaadin-icon>")
                .withProperty("important", SamplePerson::isImportant);
        grid.addColumn(importantRenderer).setHeader("Important").setAutoWidth(true);

        grid.setItems(query -> samplePersonService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AdvancedView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(SamplePerson.class, localUser);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.samplePerson == null) {
                    this.samplePerson = new SamplePerson();
                }
                binder.writeBean(this.samplePerson);

                samplePersonService.update(this.samplePerson);
                clearForm();
                refreshGrid();
                Notification.show("SamplePerson details stored.");
                UI.getCurrent().navigate(AdvancedView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the samplePerson details.");
            }
        });

        createDialog();

        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String SAMPLEPERSON_ID = "samplePersonID";
        Optional<Integer> samplePersonId = event.getRouteParameters().getInteger(SAMPLEPERSON_ID);
        if (samplePersonId.isPresent()) {
            Optional<SamplePerson> samplePersonFromBackend = samplePersonService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %d", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AdvancedView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        editorLayoutDiv = new VerticalLayout();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");
        editorLayoutDiv.setPadding(false);

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        avatarGroup = new CollaborationAvatarGroup(
                localUser, TOPIC_ID);

        Button chatButton = new Button(VaadinIcon.CHAT.create(), buttonClickEvent -> openDialog());
        chatButton.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_TERTIARY);
        chatButton.setWidth(50, Unit.PIXELS);

        chatLayout = new HorizontalLayout(avatarGroup, chatButton);
        chatLayout.expand(avatarGroup);
        editorDiv.addComponentAsFirst(chatLayout);
        readonlyNotice = new Span("");
        readonlyNotice.setClassName("readonly-notice");
        readonlyNotice.setWidth("100%");
        readonlyNotice.setVisible(false);

        editorDiv.add(readonlyNotice);
        chatLayout.setVisible(false);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateOfBirth = new DatePicker("Date Of Birth");
        occupation = new TextField("Occupation");
        important = new Checkbox("Important");
        important.getStyle().set("padding-top", "var(--lumo-space-m)");
        Component[] fields = new Component[]{firstName, lastName, email, phone, dateOfBirth, occupation, important};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);

        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void openDialog() {
        chatDialog.open();
    }

    private void createDialog() {
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
    }

    private void createButtonLayout(VerticalLayout editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }


    private void populateForm(SamplePerson value) {
        this.samplePerson = value;

        String topicId = this.samplePerson != null ? String.format(TOPIC_ID + "/%d", this.samplePerson.getId()) : null ;

        userList = new ArrayList<>();

        if (presenceManager != null)
            presenceManager.close();

        if (value != null) {
            presenceManager = new PresenceManager(editorLayoutDiv, localUser, topicId);
            presenceManager.markAsPresent(true);
            presenceManager.setPresenceHandler(presenceContext -> {
                userList.add(presenceContext.getUser());

                makeFormReadonly();

                return () -> {
                    userList.remove(presenceContext.getUser());
                    makeFormReadonly();
                };
            });
        }

        chatLayout.setVisible(value != null);

        avatarGroup.setTopic(topicId);
        messageList.setTopic(topicId);
        binder.setTopic(topicId, () -> this.samplePerson);
    }

    private void makeFormReadonly() {
        boolean readonly = userList.size() > 1 && !userList.get(0).equals(localUser);

        readonlyNotice.setVisible(readonly);

        if (userList.size() > 0) {
            String readonlyNoticeString = String.format("Readonly, %s is editing", userList.get(0).getName());
            readonlyNotice.setText(readonlyNoticeString);
        }

        firstName.setReadOnly(readonly);
        lastName.setReadOnly(readonly);
        email.setReadOnly(readonly);
        phone.setReadOnly(readonly);
        dateOfBirth.setReadOnly(readonly);
        occupation.setReadOnly(readonly);
        important.setReadOnly(readonly);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        String browserName = BrowserUtil.getBrowserName(attachEvent.getUI());
        localUser.setName(browserName + " " + Application.userCounter.incrementAndGet());

        String iconURL = BrowserUtil.getBrowserIconURL(attachEvent.getUI());
        localUser.setImage(iconURL);
    }
}

