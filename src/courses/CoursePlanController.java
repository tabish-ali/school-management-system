package courses;

import config.Dialogs;
import faculty.FacultyDashboard;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.CoursePlan;
import models.Courses;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CoursePlanController implements Initializable {

    @FXML
    private DatePicker dateField;

    @FXML
    private TextField planField;

    @FXML
    private Button addBtn;

    @FXML
    private Button delBtn, editBtn, cancelBtn;

    @FXML
    CoursesPlanTable coursesPlanTableController;

    ContextMenu context_menu = new ContextMenu();

    MenuItem edit_menu_btn = new MenuItem("Edit");
    MenuItem del_menu_btn = new MenuItem("Delete");
    MenuItem cancel_menu_btn = new MenuItem("Cancel");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        LocalDate date_value = java.time.LocalDate.now();
        dateField.setValue(date_value);

        planField.textProperty().addListener((observable, oldValue, newValue) ->
                addBtn.setDisable(newValue.isEmpty()));

        tableListener();

    }

    public void tableListener() {
        // listen for table selection to enable action buttons related to table
        coursesPlanTableController.getPlanTable().getSelectionModel().selectedItemProperty().
                addListener((obs, oldSelection, newSelection)
                        -> {
                    delBtn.setDisable(newSelection == null);
                    editBtn.setDisable(newSelection == null);
                    cancelBtn.setDisable(newSelection == null);

                    for (MenuItem menu_items : getCoursesPlanTableController().
                            getPlanTable().getContextMenu().getItems()) {
                        menu_items.setDisable(newSelection == null);
                    }
                });
    }

    public void setContextMenu() {

        tableListener();

        context_menu.getItems().addAll(edit_menu_btn, del_menu_btn, cancel_menu_btn);

        edit_menu_btn.setGraphic(new ImageView(new Image("resources/icons/edit_property_15px.png")));

        del_menu_btn.setGraphic(new ImageView(new Image("resources/icons/remove_15px.png")));

        cancel_menu_btn.setGraphic(new ImageView(new Image("resources/icons/cancel_15px.png")));

        edit_menu_btn.setOnAction(e -> edit());

        del_menu_btn.setOnAction(e -> deleteCoursePlan());

        cancel_menu_btn.setOnAction(e -> cancel());

        coursesPlanTableController.getPlanTable().setContextMenu(context_menu);
    }

    public CoursesPlanTable getCoursesPlanTableController() {
        return coursesPlanTableController;
    }

    @FXML
    private void addCoursePlan() {
        int course_id = new FacultyDashboard().getSelectedCourseId();

        if (!planField.getText().isEmpty()) {
            String date = dateField.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String plan = planField.getText();

            new CourseDatabases().setCoursePlan(course_id, date, plan);

            CoursePlan course_plan = new CourseDatabases().getLatestCoursePlan(course_id);

            coursesPlanTableController.getCoursePlansList().add(course_plan);

            coursesPlanTableController.getPlanTable().refresh();

            tableListener();
        }
    }

    @FXML
    private void deleteCoursePlan() {
        if (!coursesPlanTableController.getPlanTable().getSelectionModel().isEmpty()) {

            int choice = new Dialogs().confirmationAlert("Confirmation",
                    "Do you want to delete selected plans?",
                    "All selected plans from table will be deleted. \n Are you sure to continue?");

            if (choice == 1) {

                ObservableList<CoursePlan> course_plan_list = coursesPlanTableController.getPlanTable().getSelectionModel().
                        getSelectedItems();
                new CourseDatabases().deleteCoursePlan(coursesPlanTableController.getCoursePlansList());
                coursesPlanTableController.getCoursePlansList().removeAll(course_plan_list);
                coursesPlanTableController.getPlanTable().refresh();
                tableListener();
            }
        }
    }

    public void cancel() {

        coursesPlanTableController.getPlanTable().getSelectionModel().clearSelection();
    }

    public void edit() {

        int row_no = coursesPlanTableController.getPlanTable().getFocusModel().getFocusedCell().getRow();
        TableColumn<CoursePlan, ?> col = coursesPlanTableController.getPlanTable().
                getFocusModel().getFocusedCell().getTableColumn();
        coursesPlanTableController.getPlanTable().edit(row_no, col);
        coursesPlanTableController.onChangingPlan();
    }
}
