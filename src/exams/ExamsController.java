package exams;

/*
In this class we will handle and show the uploaded exams by the faculty
 */

import config.Dialogs;
import faculty.FacultyDashboard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Exams;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ExamsController implements Initializable {

    @FXML
    private Button editBtn, delBtn, cancelBtn;

    @FXML
    ExamsTable examsTableController;

    @FXML
    UploadedExams uploadedExamsController;

    TableView<Exams> examsTable;

    ObservableList<Exams> examsList = FXCollections.observableArrayList();

    static ExamsController examsController;

    ContextMenu context_menu = new ContextMenu();

    MenuItem edit_menu_btn = new MenuItem("Edit");
    MenuItem del_menu_btn = new MenuItem("Delete");
    MenuItem cancel_menu_btn = new MenuItem("Cancel");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        examsController = this;
        examsList = examsTableController.getExamsList();
        examsTable = examsTableController.getExamsTable();

    }

    public void setContextMenu() {
        tableListener();

        context_menu.getItems().addAll(edit_menu_btn, del_menu_btn, cancel_menu_btn);

        edit_menu_btn.setGraphic(new ImageView(new Image("resources/icons/edit_property_15px.png")));

        del_menu_btn.setGraphic(new ImageView(new Image("resources/icons/remove_15px.png")));

        cancel_menu_btn.setGraphic(new ImageView(new Image("resources/icons/cancel_15px.png")));

        edit_menu_btn.setOnAction(e -> edit());

        del_menu_btn.setOnAction(e -> deleteExams());

        cancel_menu_btn.setOnAction(e -> cancel());

        examsTable.setContextMenu(context_menu);
    }

    public void tableListener() {

        // listen for table selection to enable action buttons related to table
        examsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            del_menu_btn.setDisable(newSelection == null);
            edit_menu_btn.setDisable(newSelection == null);
            cancel_menu_btn.setDisable(newSelection == null);
            editBtn.setDisable(newSelection == null);
            delBtn.setDisable(newSelection == null);
            cancelBtn.setDisable(newSelection == null);
        });
    }

    public ExamsTable getExamsTableController() {
        return examsTableController;
    }

    public UploadedExams getUploadedExamsController() {
        return uploadedExamsController;
    }

    public ExamsController getExamsController() {
        return examsController;
    }

    @FXML
    private void addExam() {
        if (new FacultyDashboard().getSelectedCourseId() != 0) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/exams/add_exam.fxml"));
                Parent root = loader.load();
                AddExam add_exam = loader.getController();
                add_exam.setAddExamWin(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new Dialogs().warningAlert("Warning", "No course selected",
                    "Please select the course from side menu.");
        }
    }

    @FXML
    private void deleteExams() {

        if (!examsTable.getSelectionModel().isEmpty()) {

            int choice = new Dialogs().confirmationAlert("Confirmation", "Do you want to delete selected exams",
                    "Once deleted you will not be able to recover the deleted data");

            if (choice == 1) {
                ObservableList<Exams> selected_exams = examsTable.getSelectionModel().getSelectedItems();
                examsTableController.deleteExams(selected_exams);
            }
        }
    }

    @FXML
    private void edit() {

        int row_no = examsTable.getFocusModel().getFocusedCell().getRow();

        TableColumn col = examsTable.getFocusModel().getFocusedCell().getTableColumn();

        examsTable.edit(row_no, col);
        examsTableController.onChangingExam();

    }

    @FXML
    private void cancel() {
        examsTable.getSelectionModel().clearSelection();
    }


}
