package courses;

import config.DateValidator;
import config.Dialogs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import models.CoursePlan;
import java.net.URL;
import java.util.ResourceBundle;

public class CoursesPlanTable implements Initializable {

    @FXML
    private TableView<CoursePlan> planTable;

    @FXML
    private TableColumn<CoursePlan, String> dateCol;

    @FXML
    private TableColumn<CoursePlan, String> planCol;

    ObservableList<CoursePlan> coursePlansList = FXCollections.observableArrayList();

    int courseId;

    static CoursesPlanTable coursesPlanTableController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        coursesPlanTableController = this;

    }

    public void removeContextMenu() {
        planTable.setContextMenu(null);
    }

    public TableView<CoursePlan> getPlanTable() {
        return planTable;
    }

    public ObservableList<CoursePlan> getCoursePlansList() {
        return coursePlansList;
    }

    public static CoursesPlanTable getCoursesPlanTableController() {
        return coursesPlanTableController;
    }

    public void setForFaculty() {

        planTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        planCol.setCellFactory(TextFieldTableCell.forTableColumn());

        planTable.getSelectionModel().setCellSelectionEnabled(true);
        planTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        onChangingPlan();
    }

    public void setCoursePlan(int course_id) {

        coursePlansList.clear();

        planTable.refresh();

        courseId = course_id;

        coursePlansList = new CourseDatabases().getCoursePlan(course_id);

        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        planCol.setCellValueFactory(new PropertyValueFactory<>("plan"));

        planTable.setItems(coursePlansList);
    }

    public void onChangingPlan() {

        dateCol.setOnEditCommit((TableColumn.CellEditEvent<CoursePlan, String> t) -> {
            int id = planTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.validateDate(t.getNewValue());

            if (validate) {

                t.getTableView().getItems().get(t.getTablePosition().getRow()).setDate(t.getNewValue());
                new CourseDatabases().changeCoursePlan(t.getNewValue(), "date", id);
            } else {
                new Dialogs().errorAlert("Invalid Date", "Please enter valid date",
                        "Date should be in this pattern dd-mm-yyyy");
            }
            planTable.refresh();
        });
        planCol.setOnEditCommit((TableColumn.CellEditEvent<CoursePlan, String> t) -> {
            int id = planTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setPlan(t.getNewValue());
            new CourseDatabases().changeCoursePlan(t.getNewValue(), "plan", id);
            planTable.refresh();
        });

    }


}
