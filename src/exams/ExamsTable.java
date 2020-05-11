package exams;

import config.DateValidator;
import config.Dialogs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import main_menu.LoginController;
import models.Exams;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class ExamsTable implements Initializable {

    @FXML
    private TableColumn<Exams, Integer> serialCol;

    @FXML
    private TableColumn<Exams, String> examCodeCol;

    @FXML
    private TableColumn<Exams, String> startDateCol;

    @FXML
    private TableColumn<Exams, Integer> timeAllottedCol;

    @FXML
    private TableColumn<Exams, String> detailsCol;

    @FXML
    private TableColumn<Exams, String> takeExamCol;

    @FXML
    private TableColumn<Exams, String> startTimeCol;

    @FXML
    private TableView<Exams> examsTable;

    ObservableList<Exams> examsList = FXCollections.observableArrayList();

    int courseId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        listenForTakeExam();
    }

    public void initializeForFaculty() {
        examsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        examsTable.getSelectionModel().setCellSelectionEnabled(true);
        examsTable.setEditable(true);
        examsTable.getColumns().remove(takeExamCol);

        examCodeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startDateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        detailsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startTimeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        timeAllottedCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter() {
            @Override
            public Integer fromString(String value) {
                try {
                    return super.fromString(value);
                } catch (NumberFormatException e) {
                    return -1; // An abnormal value
                }
            }
        }));

        listenForExamSelection();
    }

    public void setExamsTable(int course_id) {

        courseId = course_id;

        examsList = new ExamsDatabases().getExamsByCourseID(course_id);

        serialCol.setCellValueFactory(new PropertyValueFactory<>("serial"));
        examCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        timeAllottedCol.setCellValueFactory(new PropertyValueFactory<>("timeAllotted"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        takeExamCol.setCellValueFactory(new PropertyValueFactory<>("takeExamBtn"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        examsTable.setItems(examsList);

    }

    public void deleteExams(ObservableList<Exams> selected_exams) {
        new ExamsDatabases().deleteExams(selected_exams);
        examsList.removeAll(selected_exams);
        examsTable.refresh();
    }

    public void setNewExam() {
        Exams exam = new ExamsDatabases().getLatestExam();
        exam.setSerial(examsList.size() + 1);
        examsList.add(exam);
        examsTable.refresh();
        listenForExamSelection();
        ExamsController.examsController.tableListener();
        onChangingExam();
    }

    public void listenForExamSelection() {

        examsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Exams selected_exam = examsTable.getSelectionModel().getSelectedItem();
            }
        });
    }

    public ObservableList<Exams> getExamsList() {
        return examsList;
    }

    public TableView<Exams> getExamsTable() {
        return examsTable;
    }

    public void clearTable() {
        examsList.clear();
        examsTable.refresh();
    }

    public void listenForTakeExam() {
        for (Exams exam : examsList) {

            Button take_exam_btn = exam.getTakeExamBtn();

            take_exam_btn.setOnAction(e -> {
                boolean check = new ExamsDatabases().checkTakenExams(exam.getCourseId(), exam.getId(), LoginController.loginID);
                if (!check) {
                    setTakeExamWin(exam);
                } else {
                    new Dialogs().warningAlert("Warning", "You have already taken this exam",
                            "Contact your faculty for re take of this exam.");
                }
            });
        }
    }

    public void setTakeExamWin(Exams exam) {
        try {
            // loading student dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/exams/take_exam.fxml"));
            Parent root = loader.load();
            TakeExam take_exam = loader.getController();
            take_exam.setTakeExamWin(root, exam);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onChangingExam() {

        examCodeCol.setOnEditCommit((TableColumn.CellEditEvent<Exams, String> t) -> {
            int id = examsTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setCode(t.getNewValue());
            new ExamsDatabases().updateExam(t.getNewValue(), "code", id);

            examsTable.refresh();
        });

        startTimeCol.setOnEditCommit((TableColumn.CellEditEvent<Exams, String> t) -> {
            int id = examsTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.timeValidator(t.getNewValue());

            if(validate) {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setStartTime(t.getNewValue());
                new ExamsDatabases().updateExam(t.getNewValue(), "start_time", id);

            }else{
                new Dialogs().errorAlert("Invalid Time", "Please enter valid time",
                        "Time should be in this pattern hh:mm:ss");

            }
            examsTable.refresh();
        });

        startDateCol.setOnEditCommit((TableColumn.CellEditEvent<Exams, String> t) -> {
            int id = examsTable.getSelectionModel().getSelectedItem().getId();

            boolean validate = DateValidator.validateDate(t.getNewValue());

            if (validate) {
                new ExamsDatabases().updateExam(t.getNewValue(), "start_date", id);

                examsTable.getSelectionModel().getSelectedItem().setStartDate(t.getNewValue());

            } else {
                new Dialogs().errorAlert("Invalid Date", "Please enter valid date",
                        "Date should be in this pattern dd-mm-yyyy");

            }
            examsTable.refresh();
        });
        timeAllottedCol.setOnEditCommit((TableColumn.CellEditEvent<Exams, Integer> t) -> {
            int id = examsTable.getSelectionModel().getSelectedItem().getId();
            if (t.getNewValue() == -1) {
                new Dialogs().errorAlert("Input Error", "Please enter valid input",
                        "Time must be Integer, characters are not acceptable.");
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setTimeAllotted(t.getOldValue());
            } else {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setTimeAllotted(t.getNewValue());
                new ExamsDatabases().updateExam(Double.toString(t.getNewValue()), "time_allotted", id);
            }
        });

        detailsCol.setOnEditCommit((TableColumn.CellEditEvent<Exams, String> t) -> {
            int id = examsTable.getSelectionModel().getSelectedItem().getId();
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setDetails(t.getNewValue());
            new ExamsDatabases().updateExam(t.getNewValue(), "details", id);
            examsTable.refresh();
        });

    }
}
