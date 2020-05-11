package faculty;

import assignments.AddAssignment;
import assignments.AssignmentsDatabases;
import config.CalculateDays;
import config.DateValidator;
import config.Dialogs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import main_menu.LoginController;
import models.Assignments;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


/* this class provides privileges to faculty to manage assignments
    like deleting, adding, extending deadline
 */
public class AssignmentsController implements Initializable {

    @FXML
    UploadedAssignments uploadedAssignmentsController;

    @FXML
    private Button delBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private MenuItem cancelMenuBtn;

    @FXML
    private MenuItem delMenuBtn;

    @FXML
    private Button editBtn;

    @FXML
    private MenuItem editMenuBtn;


    @FXML
    private TableColumn<Assignments, String> serialCol;

    @FXML
    private TableColumn<Assignments, String> topicCol;

    @FXML
    private TableColumn<Assignments, String> contentCol;

    @FXML
    private TableColumn<Assignments, String> deadlineCol;

    @FXML
    private TableColumn<Assignments, Integer> remainingDaysCol;

    @FXML
    private TableView<Assignments> assignmentTable;

    static int facultyId;

    ObservableList<Assignments> assignmentsList = FXCollections.observableArrayList();

    AssignmentsDatabases assignmentsDatabases;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        assignmentsDatabases = new AssignmentsDatabases();
        facultyId = LoginController.loginID;
        assignmentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        assignmentTable.getSelectionModel().setCellSelectionEnabled(true);

        contentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        topicCol.setCellFactory(TextFieldTableCell.forTableColumn());
        deadlineCol.setCellFactory(TextFieldTableCell.forTableColumn());

        tableListener();

        onChangingAssignment();
    }

    public void tableListener() {

        // listen for table selection to enable action buttons related to table
        assignmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection)
                -> {
            delBtn.setDisable(newSelection == null);
            delMenuBtn.setDisable(newSelection == null);
            editBtn.setDisable(newSelection == null);
            editMenuBtn.setDisable(newSelection == null);
            cancelBtn.setDisable(newSelection == null);
            cancelMenuBtn.setDisable(newSelection == null);
        });

    }

    public UploadedAssignments getUploadedAssignmentsController() {

        return uploadedAssignmentsController;
    }

    public void setUploadedAssignmentStatus() {
        uploadedAssignmentsController.refreshTable();
        tableListener();
    }

    // this method prompt faculty to add new assignment
    @FXML
    private void addNewAssignment() {
        if (FacultyDashboard.selectedCourseId != 0) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/faculty/add_assignment.fxml"));
                Parent root = loader.load();
                AddAssignment assignment = loader.getController();
                assignment.setNewAssignmentWin(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new Dialogs().warningAlert("Warning", "No course selected",
                    "Please select the course from side menu.");
        }
    }


    public void setNewlyAddedAssignment(Assignments assignment) {

        assignment.setSerial(assignmentsList.size() + 1);
        assignmentsList.add(assignment);
        assignmentTable.refresh();
        tableListener();
        onChangingAssignment();
    }

    // this method set assignment for selected courses on table
    public void setAssignmentOnTable(int course_id) {

        assignmentsList = assignmentsDatabases.getAssignmentsByCourseID(course_id);

        serialCol.setCellValueFactory(new PropertyValueFactory<>("serial"));
        topicCol.setCellValueFactory(new PropertyValueFactory<>("topic"));
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        remainingDaysCol.setCellValueFactory(new PropertyValueFactory<>("daysRemaining"));
        assignmentTable.setItems(assignmentsList);
        tableListener();
    }

    @FXML
    private void delete() {
        if (!assignmentTable.getSelectionModel().isEmpty()) {
            int choice = new Dialogs().confirmationAlert("Confirmation",
                    "Do you want to delete the selected assignments?", "" +
                            "Warning: All the uploaded assignments by students related to this will be deleted.");

            if (choice == 1) {
                ObservableList<Assignments> selected_assignments = assignmentTable.getSelectionModel().getSelectedItems();
                assignmentsDatabases.deleteAssignments(selected_assignments);
                assignmentsList.removeAll(selected_assignments);
                assignmentTable.refresh();
            }
        }
    }

    @FXML
    private void edit() {

        int row_no = assignmentTable.getFocusModel().getFocusedCell().getRow();

        TableColumn col = assignmentTable.getFocusModel().getFocusedCell().getTableColumn();

        assignmentTable.edit(row_no, col);
        onChangingAssignment();

    }

    public void onChangingAssignment() {

        topicCol.setOnEditCommit((TableColumn.CellEditEvent<Assignments, String> t) -> {
            int id = assignmentTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setTopic(t.getNewValue());
            new AssignmentsDatabases().updateAssignment(t.getNewValue(), "topic", id);
        });
        deadlineCol.setOnEditCommit((TableColumn.CellEditEvent<Assignments, String> t) -> {
            int id = assignmentTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.validateDate(t.getNewValue());

            if (validate) {
                new AssignmentsDatabases().updateAssignment(t.getNewValue(), "deadline", id);

                // updating remaining days
                assignmentTable.getSelectionModel().getSelectedItem().
                        setDaysRemaining(CalculateDays.calculateDaysUntil(t.getNewValue()));
                assignmentTable.getSelectionModel().getSelectedItem().setDeadline(t.getNewValue());

                assignmentTable.refresh();

            } else {
                new Dialogs().errorAlert("Invalid Date", "Please enter valid date",
                        "Date should be in this pattern dd-mm-yyyy");

                assignmentTable.refresh();
            }
        });
        contentCol.setOnEditCommit((TableColumn.CellEditEvent<Assignments, String> t) -> {
            int id = assignmentTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setContent(t.getNewValue());
            new AssignmentsDatabases().updateAssignment(t.getNewValue(), "content", id);
        });
    }

    @FXML
    private void cancel() {
        assignmentTable.getSelectionModel().clearSelection();
    }
}
