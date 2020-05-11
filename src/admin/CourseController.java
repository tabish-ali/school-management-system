package admin;

import config.DateValidator;
import config.Dialogs;
import courses.CourseDatabases;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import models.Courses;
import models.TimeTable;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CourseController implements Initializable {
    @FXML
    private TableView<Courses> coursesTable;

    @FXML
    private TableColumn<Courses, String> serialCol;

    @FXML
    private TableColumn<Courses, String> courseCodeCol;

    @FXML
    private TableColumn<Courses, String> courseNameCol;

    @FXML
    private TableColumn<Courses, String> departmentCol;

    @FXML
    private TableColumn<Courses, Double> feeCol;

    @FXML
    private TableColumn<Courses, String> startDateCol;

    @FXML
    private TableColumn<Courses, String> endDateCol;

    @FXML
    private TableView<TimeTable> timeTable;

    @FXML
    private TableColumn<TimeTable, String> dayCol;

    @FXML
    private TableColumn<TimeTable, String> timeCol;

    @FXML
    private TableColumn<TimeTable, String> venueCol;

    @FXML
    private Button deleteBtn;

    @FXML
    private MenuItem editMenuBtn;

    @FXML
    private MenuItem deleteMenuBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private MenuItem cancelMenuBtn;

    @FXML
    private MenuItem editTimeTableBtn;

    @FXML
    private MenuItem cancelTimeTableBtn;

    static AdminDashboard adminController;

    ObservableList<Courses> coursesData = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // on initializing populating the course table
        setCoursesOnTable();
        tableListener();

        //enabling multiple selection on table
        coursesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        coursesTable.getSelectionModel().setCellSelectionEnabled(true);

        timeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        venueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        courseNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        courseCodeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        departmentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startDateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        endDateCol.setCellFactory(TextFieldTableCell.forTableColumn());

        feeCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter() {
            @Override
            public Double fromString(String value) {
                try {
                    return super.fromString(value);
                } catch (NumberFormatException e) {
                    return Double.NaN; // An abnormal value
                }
            }
        }));

        timeTable.getSelectionModel().setCellSelectionEnabled(true);

        onChangingTimeTable();

        onChangingCourses();
    }

    public void tableListener() {

        // listen for table selection to enable action buttons related to table
        coursesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteBtn.setDisable(false);
                deleteMenuBtn.setDisable(false);
                editMenuBtn.setDisable(false);
                cancelBtn.setDisable(false);
                cancelMenuBtn.setDisable(false);
                setTimeTable();
            } else {
                deleteBtn.setDisable(true);
                deleteMenuBtn.setDisable(true);
                editMenuBtn.setDisable(true);
                cancelBtn.setDisable(true);
                cancelMenuBtn.setDisable(true);
            }
        });
        timeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            editTimeTableBtn.setDisable(newSelection == null);
            cancelTimeTableBtn.setDisable(newSelection == null);

        });

        onChangingCourses();
    }

    public AdminDashboard getAdmin() {
        return adminController;
    }

    @FXML
    private void addNewCourse() {
        // on press of add button, load the add new course window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/admin/add_course.fxml"));
            Parent root = loader.load();
            AddNewCourse course = loader.getController();
            course.setNewCourseWin(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNewCourse() {
        // here we will get newly added course and add it to table

        Courses course = new CourseDatabases().getLatestCourse();
        coursesData.add(course);
        coursesTable.refresh();

        tableListener();
        onChangingCourses();
    }

    public void deleteCourse() {
        // on deletion of course all data related to deleted course will be deleted
        // like assignments exams etc

        //check whether course is selected from table before pressing button
        if (!coursesTable.getSelectionModel().isEmpty()) {

            //getting user confirmation
            int choice = new Dialogs().confirmationAlert("Confirmation", "Do you want to delete the" +
                    "selected courses?", "All data related to this course will be deleted \n" +
                    "e:g Assignments and Exams etc");

            // getting course id from table object
            if (choice == 1) {
                ObservableList<Courses> selected_courses = coursesTable.getSelectionModel().getSelectedItems();

                for (Courses course : selected_courses) {
                    int id = course.getId();
                    new CourseDatabases().deleteCourse(id);
                }
                // removing the course from list and refreshing table
                coursesData.removeAll(selected_courses);
                coursesTable.refresh();

                //removing timetable from UI

                timeTable.getItems().clear();

                AdminDashboard.admin.facultyController.refresh();

            }
        }
    }

    public void setCoursesOnTable() {

        // getting our list from CourseDatabase class, which will return observable list
        coursesData = new CourseDatabases().getCourses();

        // now populating the courses table

        // string values in quotes must be same as the function defined in Courses model class after get or set,
        // e:g if function : setSerial , then here in serial col we will write serial, which is value after set or get

        serialCol.setCellValueFactory(new PropertyValueFactory<>("serial"));
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCodeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("courseDepartment"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("courseStartDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("courseEndDate"));
        feeCol.setCellValueFactory(new PropertyValueFactory<>("fee"));

        /*
          ObservableList: A list that enables listeners to track changes when they occur.
          ListChangeListener: An interface that receives notifications of changes to an
          ObservableList. ObservableMap: A map that enables observers to track changes when they occur
         */
        coursesTable.setItems(coursesData);
    }

    public void setTimeTable() {

        timeTable.getItems().clear();
        timeTable.refresh();

        if (!coursesTable.getSelectionModel().isEmpty()) {

            int selected_course_id = coursesTable.getSelectionModel().getSelectedItem().getId();

            ObservableList<TimeTable> time_table_list = new CourseDatabases().getTimeTable(selected_course_id);

            dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));
            timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
            venueCol.setCellValueFactory(new PropertyValueFactory<>("venue"));

            timeTable.setItems(time_table_list);
        }
    }


    public void onChangingTimeTable() {

        timeCol.setOnEditCommit((TableColumn.CellEditEvent<TimeTable, String> t) -> {
            int id = timeTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setTime(t.getNewValue());
            new CourseDatabases().changeTimeTable(t.getNewValue(), "time", id);
            timeTable.refresh();
        });
        venueCol.setOnEditCommit((TableColumn.CellEditEvent<TimeTable, String> t) -> {
            int id = timeTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setVenue(t.getNewValue());
            new CourseDatabases().changeTimeTable(t.getNewValue(), "class_venue", id);
            timeTable.refresh();
        });
    }

    public void onChangingCourses() {

        courseNameCol.setOnEditCommit((TableColumn.CellEditEvent<Courses, String> t) -> {
            int id = coursesTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setCourseName(t.getNewValue());
            new CourseDatabases().updateCourse(t.getNewValue(), "course_name", id);
            coursesTable.refresh();
        });
        courseCodeCol.setOnEditCommit((TableColumn.CellEditEvent<Courses, String> t) -> {
            int id = coursesTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setCourseCode(t.getNewValue());
            new CourseDatabases().updateCourse(t.getNewValue(), "course_code", id);
            coursesTable.refresh();
        });
        departmentCol.setOnEditCommit((TableColumn.CellEditEvent<Courses, String> t) -> {
            int id = coursesTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setCourseName(t.getNewValue());
            new CourseDatabases().updateCourse(t.getNewValue(), "department", id);
        });
        feeCol.setOnEditCommit((TableColumn.CellEditEvent<Courses, Double> t) -> {
            int id = coursesTable.getSelectionModel().getSelectedItem().getId();
            System.out.println(t.getNewValue());
            if(t.getNewValue().isNaN()) {
                new Dialogs().errorAlert("Input Error", "Please enter valid input",
                        "Fee must be numeric, characters are not acceptable.");
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setFee(t.getOldValue());

            }else{
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setFee(t.getNewValue());
                new CourseDatabases().updateCourse(Double.toString(t.getNewValue()), "fee", id);
            }
            coursesTable.refresh();
        });
        startDateCol.setOnEditCommit((TableColumn.CellEditEvent<Courses, String> t) -> {
            int id = coursesTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.validateDate(t.getNewValue());

            if (validate) {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setCourseStartDate(t.getNewValue());
                new CourseDatabases().updateCourse(t.getNewValue(), "start_date", id);
            } else {
                new Dialogs().errorAlert("Invalid Date", "Please enter valid date",
                        "Date should be in this pattern dd-mm-yyyy");
            }
            coursesTable.refresh();

        });
        endDateCol.setOnEditCommit((TableColumn.CellEditEvent<Courses, String> t) -> {
            int id = coursesTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.validateDate(t.getNewValue());

            if (validate) {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setCourseEndDate(t.getNewValue());
                new CourseDatabases().updateCourse(t.getNewValue(), "end_date", id);
            } else {
                new Dialogs().errorAlert("Invalid Date", "Please enter valid date",
                        "Date should be in this pattern dd-mm-yyyy");
            }
            coursesTable.refresh();
        });
    }

    @FXML
    private void edit() {
        int row_no = coursesTable.getFocusModel().getFocusedCell().getRow();
        TableColumn<Courses, ?> col = coursesTable.getFocusModel().getFocusedCell().getTableColumn();
        coursesTable.edit(row_no, col);
        onChangingCourses();
    }

    @FXML
    private void cancelSelection() {

        coursesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void cancelTimeTableSelection(){
        timeTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void editTimeTable() {
        int row_no = timeTable.getFocusModel().getFocusedCell().getRow();
        TableColumn<TimeTable, ?> col = timeTable.getFocusModel().getFocusedCell().getTableColumn();
        timeTable.edit(row_no, col);
        onChangingTimeTable();
    }
}
