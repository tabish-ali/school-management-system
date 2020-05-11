package admin;

import config.DateValidator;
import config.Dialogs;
import faculty.FacultyDatabases;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import models.Faculty;
import java.net.URL;
import java.util.ResourceBundle;

/*this class handles the admin functionality related to faculty
  like creating the faculty
 */

public class FacultyController implements Initializable {

    @FXML
    private TableColumn<Faculty, Integer> serialCol;

    @FXML
    private TableColumn<Faculty, String> nameCol;

    @FXML
    private TableColumn<Faculty, String> departmentCol;

    @FXML
    private TableColumn<Faculty, String> totalCoursesCol;

    @FXML
    private TableColumn<Faculty, String> officeNoCol;

    @FXML
    private TableColumn<Faculty, Double> salaryCol;

    @FXML
    private TableColumn<Faculty, String> dateJoinedCol;

    @FXML
    private TableView<Faculty> facultyTable;

    @FXML
    private Button deleteBtn;

    @FXML
    private MenuItem deleteMenuBtn;

    @FXML
    private VBox courseSelectionVbox;

    @FXML
    CoursesSelection courseSelectionController;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Button editBtn;

    @FXML
    private MenuItem editMenuBtn;

    @FXML
    private MenuItem cancelMenuBtn;

    @FXML
    private Button cancelBtn;

    ObservableList<Faculty> facultyList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setFacultyOnTable();
        tableListener();

        facultyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        facultyTable.getSelectionModel().setCellSelectionEnabled(true);

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        departmentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        officeNoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dateJoinedCol.setCellFactory(TextFieldTableCell.forTableColumn());

        salaryCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter() {
            @Override
            public Double fromString(String value) {
                try {
                    return super.fromString(value);
                } catch (NumberFormatException e) {
                    return Double.NaN; // An abnormal value
                }
            }
        }));
        onChangingFaculty();
    }

    public void tableListener() {

        // listen for table selection to enable action buttons related to table
        facultyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        deleteBtn.setDisable(false);
                        deleteMenuBtn.setDisable(false);
                        editBtn.setDisable(false);
                        editMenuBtn.setDisable(false);
                        cancelBtn.setDisable(false);
                        cancelBtn.setDisable(false);
                        cancelMenuBtn.setDisable(false);
                        Faculty faculty = facultyTable.getSelectionModel().getSelectedItem();
                        int faculty_id = faculty.getId();
                        courseSelectionController.setCoursesOnTable(faculty_id, faculty);
                    } else {
                        deleteBtn.setDisable(true);
                        deleteMenuBtn.setDisable(true);
                        editBtn.setDisable(true);
                        editMenuBtn.setDisable(true);
                        cancelBtn.setDisable(true);
                        cancelBtn.setDisable(true);
                        cancelMenuBtn.setDisable(true);
                    }
                }
        );
    }

    public void refreshTable() {
        facultyTable.refresh();
    }

    @FXML
    private void addNewFaculty() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/admin/register_faculty.fxml"));
            Parent root = loader.load();
            RegisterFaculty faculty = loader.getController();
            faculty.setRegisterWin(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteFaculty() {
        if (!facultyTable.getSelectionModel().isEmpty()) {

            int choice = new Dialogs().confirmationAlert("Confirmation", "Do you want to delete the " +
                    "selected faculties?", "Warning: All data related to this faculty will be deleted.");

            if (choice == 1) {
                ObservableList<Faculty> selected_faculty = facultyTable.getSelectionModel().getSelectedItems();

                for (Faculty faculty : selected_faculty) {
                    int faculty_id = faculty.getId();
                    new FacultyDatabases().deleteFaculty(faculty_id);
                }

                // removing faculty from UI table
                facultyList.removeAll(selected_faculty);
                facultyTable.refresh();
            }
        }
    }

    public void setFacultyOnTable() {
        facultyList = new FacultyDatabases().getFaculty();

        serialCol.setCellValueFactory(new PropertyValueFactory<>("serial"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        officeNoCol.setCellValueFactory(new PropertyValueFactory<>("officeNo"));
        dateJoinedCol.setCellValueFactory(new PropertyValueFactory<>("dateJoined"));
        totalCoursesCol.setCellValueFactory(new PropertyValueFactory<>("coursesCount"));

        facultyTable.setItems(facultyList);
    }

    public void setNewFaculty() {
        Faculty new_faculty = new FacultyDatabases().getLatestFaculty();
        facultyList.add(new_faculty);
        facultyTable.refresh();
        tableListener();
        onChangingFaculty();
    }

    @FXML
    private void closeSideMenu() {
        splitPane.getItems().remove(courseSelectionVbox);
    }

    @FXML
    private void showSideMenu() {
        if (!splitPane.getItems().contains(courseSelectionVbox)) {
            splitPane.getItems().add(courseSelectionVbox);
        }
    }

    public void refresh() {
        facultyList.clear();
        facultyTable.refresh();
        setFacultyOnTable();
        tableListener();
    }

    @FXML
    private void edit() {
        int row_no = facultyTable.getFocusModel().getFocusedCell().getRow();

        TableColumn<Faculty, ?> col = facultyTable.getFocusModel().getFocusedCell().getTableColumn();

        facultyTable.edit(row_no, col);
        onChangingFaculty();
    }

    public void onChangingFaculty() {

        nameCol.setOnEditCommit((TableColumn.CellEditEvent<Faculty, String> t) -> {
            int id = facultyTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
            new FacultyDatabases().updateFaculty(t.getNewValue(), "name", id);

            facultyTable.refresh();
        });
        dateJoinedCol.setOnEditCommit((TableColumn.CellEditEvent<Faculty, String> t) -> {
            int id = facultyTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.validateDate(t.getNewValue());

            if (validate) {
                new FacultyDatabases().updateFaculty(t.getNewValue(), "date_joined", id);

                facultyTable.getSelectionModel().getSelectedItem().setDateJoined(t.getNewValue());

            } else {
                new Dialogs().errorAlert("Invalid Date", "Please enter valid date",
                        "Date should be in this pattern dd-mm-yyyy");

            }
            facultyTable.refresh();
        });
        departmentCol.setOnEditCommit((TableColumn.CellEditEvent<Faculty, String> t) -> {
            int id = facultyTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setDepartment(t.getNewValue());
            new FacultyDatabases().updateFaculty(t.getNewValue(), "department", id);

            facultyTable.refresh();
        });

        officeNoCol.setOnEditCommit((TableColumn.CellEditEvent<Faculty, String> t) -> {
            int id = facultyTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setOfficeNo(t.getNewValue());
            new FacultyDatabases().updateFaculty(t.getNewValue(), "office_no", id);

            facultyTable.refresh();
        });


        salaryCol.setOnEditCommit((TableColumn.CellEditEvent<Faculty, Double> t) -> {
            int id = facultyTable.getSelectionModel().getSelectedItem().getId();
            if (t.getNewValue().isNaN()) {
                new Dialogs().errorAlert("Input Error", "Please enter valid input",
                        "Salary must be decimal, characters are not acceptable.");
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setSalary(t.getOldValue());
            } else {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setSalary(t.getNewValue());
                new FacultyDatabases().updateFaculty(Double.toString(t.getNewValue()), "salary", id);
            }

            facultyTable.refresh();

        });
    }

    @FXML
    private void cancelSelection() {

        facultyTable.getSelectionModel().clearSelection();
    }
}
