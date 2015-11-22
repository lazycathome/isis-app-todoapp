package todoapp.app.viewmodels.todoitem;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.dto.Dto;

import todoapp.app.viewmodels.todoitem.v1_1.ToDoItemDto;
import todoapp.dom.todoitem.ToDoItem;

@Mixin
public class ToDoItem_asDtoV1_1 implements Dto {

    private final ToDoItem toDoItem;

    public ToDoItem_asDtoV1_1(final ToDoItem toDoItem) {
        this.toDoItem = toDoItem;
    }

    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-external-link",
            named = "As DTO v1.1"
    )
    @MemberOrder(sequence = "2")
    public ToDoItemDto $$() {
        return contentMappingServiceForToDoItem.toDtoV2(toDoItem);
    }

    @javax.inject.Inject
    ContentMappingServiceForToDoItem contentMappingServiceForToDoItem;


}
