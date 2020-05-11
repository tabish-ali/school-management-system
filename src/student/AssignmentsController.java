package student;

/* this class will allow student to manage assignment
   like upload answer to the assignment given by faculty
 */

import assignments.AssignmentsDatabases;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Assignments;

import java.io.IOException;

public class AssignmentsController {

    @FXML
    private TableView<Assignments> assignmentsTable;

    @FXML
    private TableColumn<?, ?> assignSerialCol;

    @FXML
    private TableColumn<?, ?> assignTopicCol;

    @FXML
    private TableColumn<?, ?> assignDeadlineCol;

    @FXML
    private TableColumn<?, ?> remainingDaysCol;

    @FXML
    private Button uploadBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private MenuItem uploadMenuBtn;

    @FXML
    private MenuItem cancelMenuBtn;

    CoursesController coursesController;

    ObservableList<Assignments> assignmentsList = FXCollections.observableArrayList();

    public void injectCourseController(CoursesController courses_controller) {

        coursesController = courses_controller;

    }

    public void setAssignmentsTable(int course_id) {

        assignmentsList = new AssignmentsDatabases().getAssignmentsByCourseID(course_id);

        assignSerialCol.setCellValueFactory(new PropertyValueFactory<>("serial"));
        assignTopicCol.setCellValueFactory(new PropertyValueFactory<>("topic"));
        assignDeadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        remainingDaysCol.setCellValueFactory(new PropertyValueFactory<>("daysRemaining"));

        assignmentsTable.setItems(assignmentsList);

        tableListener();
    }

    public void tableListener() {

        // listen for table selection to enable action buttons related to table
        assignmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    uploadBtn.setDisable(newSelection == null);
                    uploadMenuBtn.setDisable(newSelection == null);
                    cancelMenuBtn.setDisable(newSelection == null);
                    cancelBtn.setDisable(newSelection == null);
                }
        );
    }

    @FXML
    private void uploadAssignment() {
        if (!assignmentsTable.getSelectionModel().isEmpty()) {

            Assignments assignment = assignmentsTable.getSelectionModel().getSelectedItem();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/student/upload_assignment.fxml"));
                Parent root = loader.load();
                UploadAssignment upload = loader.getController();
                upload.setUploadWin(root, assignment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearTable() {
        assignmentsList.clear();
        assignmentsTable.getItems().clear();
        assignmentsTable.refresh();
    }

    @FXML
    private void cancel() {

        assignmentsTable.getSelectionModel().clearSelection();
    }
}

