package attendance;

import animatefx.animation.FadeIn;
import config.Dialogs;
import faculty.FacultyDashboard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Attendance;
import models.Students;
import student.StudentDatabases;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MarkAttendance implements Initializable {

    @FXML
    private TableColumn<?, ?> serialCol;

    @FXML
    private TableColumn<?, ?> studentNameCol;

    @FXML
    private TableColumn<?, ?> studentRegistrationCol;

    @FXML
    private TableColumn<?, ?> markAttendanceCol;

    @FXML
    private TableView<Attendance> attendanceTable;

    @FXML
    private DatePicker generateDateField;

    @FXML
    private DatePicker filterDateField;

    @FXML
    private Label attendancePercentageLabel;

    @FXML
    private MenuItem deleteMenuBtn;

    ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();
    ObservableList<Students> studentList = FXCollections.observableArrayList();

    AttendanceDatabase attDb;

    int course_id;

    LocalDate todayDate = java.time.LocalDate.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        attDb = new AttendanceDatabase();

        // generating attendance for current selected course
        generateDateField.setValue(todayDate);
        attendanceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filterDateField.setValue(todayDate);

        tableListener();
    }

    public void setPercentage() {
        // setting attendance percentage for selected course
        double percentage = attDb.getAttendancePercentage(course_id);
        attendancePercentageLabel.setText(percentage + " % ");
        new FadeIn(attendancePercentageLabel).play();
    }

    public ObservableList<Attendance> filterAttendance(String date) {

        int serial = 1;
        ObservableList<Attendance> filter_attendance = FXCollections.observableArrayList();

        if (course_id != 0) {
            for (Attendance attendance : attendanceList) {
                if (attendance.getDate().equals(date)) {
                    attendance.setSerial(serial++);
                    new FadeIn(attendance.getAttendanceNode()).play();
                    filter_attendance.add(attendance);
                }
            }
        } else {
            new Dialogs().warningAlert("Warning", "No course selected",
                    "Please select the course from side menu");
        }
        return filter_attendance;
    }

    public void setAttendanceTable() {

        course_id = new FacultyDashboard().getSelectedCourseId();
        studentList = new StudentDatabases().getStudentsByCourseID(course_id);
        attendanceList = attDb.getAttendanceByCourseID(course_id);
        String date = filterDateField.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        serialCol.setCellValueFactory(new PropertyValueFactory<>("serial"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentRegistrationCol.setCellValueFactory(new PropertyValueFactory<>("studentRegistration"));
        markAttendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendanceNode"));

        attendanceTable.setItems(filterAttendance(date));

        setPercentage();
        setStatus();
        tableListener();
    }

    @FXML
    private void generateAttendance() {

        if (course_id != 0) {
            String date = generateDateField.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            attDb.generateAttendance(course_id, date);

            // list of students in this course
            ObservableList<Students> studentList = new StudentDatabases().getStudentsByCourseID(course_id);

            for (Students student : studentList) {
                if (!attDb.checkIfAttendanceExits(course_id, student.getId(), date)) {
                    attDb.setAttendance(course_id, student.getId(), "N", date);
                }
            }
            setAttendanceTable();
            tableListener();
        } else {
            new Dialogs().warningAlert("Warning", "No course selected",
                    "Please select the course from side menu");
        }
    }

    // listen for status buttons in mark attendance column and set attendance

    public void setStatus() {

        for (Attendance attendance_entry : attendanceList) {

            HBox att_node = attendance_entry.getAttendanceNode();

            Button present_btn = (Button) att_node.getChildren().get(0);
            Button leave_btn = (Button) att_node.getChildren().get(1);
            Button absent_btn = (Button) att_node.getChildren().get(2);

            present_btn.setOnAction(e -> {
                attDb.updateAttendance(attendance_entry.getId(), "P");

                absent_btn.getStyleClass().remove("selected-btn-absent");
                leave_btn.getStyleClass().remove("selected-btn-leave");

                present_btn.getStyleClass().add("selected-btn-present");

                setPercentage();
            });
            leave_btn.setOnAction(e -> {
                attDb.updateAttendance(attendance_entry.getId(), "L");

                present_btn.getStyleClass().remove("selected-btn-present");
                absent_btn.getStyleClass().remove("selected-btn-absent");

                leave_btn.getStyleClass().add("selected-btn-leave");

                setPercentage();
            });
            absent_btn.setOnAction(e -> {
                attDb.updateAttendance(attendance_entry.getId(), "A");

                present_btn.getStyleClass().remove("selected-btn-present");
                leave_btn.getStyleClass().remove("selected-btn-leave");

                absent_btn.getStyleClass().add("selected-btn-absent");

                setPercentage();
            });
        }
    }

    public void tableListener() {

        // listen for table selection to enable action buttons related to table
        attendanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        deleteMenuBtn.setDisable(false);
                    }
                }
        );
    }

    @FXML
    private void deleteAttendance() {

        int choice = new Dialogs().confirmationAlert("Confirmation", "Do you want to delete selected " +
                " attendance entries?", "Once delete you will not be able to recover it.");
        if (choice == 1) {
            ObservableList<Attendance> selected_attendance = attendanceTable.getSelectionModel().getSelectedItems();
            new AttendanceDatabase().deleteAttendance(selected_attendance);
            attendanceList.removeAll(selected_attendance);
            attendanceTable.refresh();
            setAttendanceTable();
        }

    }
}
