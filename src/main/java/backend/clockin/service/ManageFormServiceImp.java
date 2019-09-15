package backend.clockin.service;


import backend.clockin.mapper.DataManageMapper;
import backend.clockin.pojo.generalFormManage.FormInfo;
import backend.clockin.pojo.generalFormManage.ManageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ManageFormServiceImp implements ManageFormService {

    @Autowired
    DataManageMapper dataManageMapper;

    private final String formManagement = "managementform";
    private ManageForm manageForm = new ManageForm();
    private String[] formManagementColumns = manageForm.getFormManagementColumns();
    private String[] formManagementColumnsType = manageForm.getFormManagementColumnType();
    private String[] formManagementColumnsDetailOne = manageForm.getFormManagementFormDetailTypeOne();
    private String[] formManagementColumnsDetailTwo = manageForm.getFormManagementFormDetailTypeTwo();
    private String[] formManagementColumnsDetailThree = manageForm.getFormManagementFormDetailTypeThree();


    @Override
    public String checkProcessOne() {
        Random r = new Random();
        String[] newFormList = showDatabase().split(",");
        int newFormListNumber = newFormList.length;
        boolean formExist = false;
        //  eliminate all the spaces
        for (int i = 0; i < newFormListNumber; i++) {
            newFormList[i] = newFormList[i].replace("[", "");
            newFormList[i] = newFormList[i].replace("]", "");
            newFormList[i] = newFormList[i].trim();
            if (newFormList[i].equals(formManagement)) {
                formExist = true;
            }
        }

        if (formExist) {
            // make sure the exist form is the form we want

            // means something goes wrong recreate the form
            if (dataManageMapper.getFirstRow(formManagement) == null) {
                //DataManageMapper.changeFormName(formManagement,r.nextInt(999999)+"_Duplicated");
                dataManageMapper.dropForm(formManagement);
                dataManageMapper.createForm(formManagement);
                System.out.println("Process1: Checked form management form");
                checkProcessTwo(1);
                return "check complete";
            }
            Set formManagementColumnSet = dataManageMapper.getFirstRow(formManagement).keySet();
            Object[] columnArray = formManagementColumnSet.toArray();
            int setSize = formManagementColumnSet.size();
            boolean isThatForm = false;

            if (setSize != formManagementColumns.length) {
                //DataManageMapper.changeFormName(formManagement,r.nextInt(999999)+"_old_"+"_Duplicated");
                dataManageMapper.dropForm(formManagement);
                dataManageMapper.createForm(formManagement);
                System.out.println("Process1: Checked form management form");
                checkProcessTwo(1);
                return "check complete";
            }
            // further check
            else {
                for (int k = 0; k < setSize; k++) {
                    if (!columnArray[k].toString().equals(formManagementColumns[k])) {
                        break;
                    } else if (k == setSize - 1) {
                        isThatForm = true;
                    }
                }
            }
            if (isThatForm) {
                System.out.println("Process1: Checked form management form");
                checkProcessTwo(2);
                return "check complete";
            } else {
                //DataManageMapper.changeFormName(formManagement,r.nextInt(999999)+"_old_"+"_Duplicated");
                dataManageMapper.dropForm(formManagement);
                dataManageMapper.createForm(formManagement);
                System.out.println("Process1: Checked form management form");
                checkProcessTwo(1);
                return "check complete";
            }
        } else {
            dataManageMapper.createForm(formManagement);
            System.out.println("Process1: Checked form management form");
            checkProcessTwo(1);
            return "check complete";
        }


    }

    // check if form management is in form management, delete extra rows
    @Override
    public void checkProcessTwo(int type) {
        // check if form management has the normal columns
        if (type == 1)// add all the columns, type 1 means new created
        {
            // use loop to add managementform columns
            System.out.println("Process2: Adding columns to form");
            dataManageMapper.changeColumn(formManagement, "column1", formManagementColumns[0], formManagementColumnsType[0]);
            int managementformlength = formManagementColumns.length;
            for (int i = 1; i < managementformlength; i++) {
                dataManageMapper.addColumn(formManagement, formManagementColumns[i], formManagementColumnsType[i], formManagementColumns[i - 1]);
            }
        }


        ArrayList<FormInfo> oldFormList = dataManageMapper.showExistForm();
        int oldFromListNumber = oldFormList.size();
        boolean formIsInForm = false;

        for (int i = 0; i < oldFromListNumber; i++) {
            if (oldFormList.get(i).getName().equals(formManagement)) {
                formIsInForm = true;
            }
        }

        if (formIsInForm) {
            System.out.println("Process2: Form management form has already been registered");
            System.out.println("Process2: Start to delete extra recordings");
            checkProcessThree();
        } else {
            System.out.println("Process2: Start to register form management form");
            String columns = "";
            int columnsNumber = formManagementColumns.length;
            for (int i = 0; i < columnsNumber; i++) {
                columns = columns + formManagementColumns[i];
                if (i != columnsNumber - 1) {
                    columns = columns + ",";
                }
            }
            FormInfo manageFormInfo = new FormInfo();
            manageFormInfo.setId(0);
            manageFormInfo.setName(formManagement);
            manageFormInfo.setDescription("this is management form");
            manageFormInfo.setColumns(columns);

            int columnDetailsLength = formManagementColumnsDetailOne.length;
            String columnDetailOne = "";
            String columnDetailTwo = "";
            String columnDetailThree = "";
            for (int j = 0; j < columnDetailsLength; j++) {
                if (j != columnDetailsLength - 1) {
                    columnDetailOne = columnDetailOne + formManagementColumnsDetailOne[j] + ",";
                    columnDetailTwo = columnDetailTwo + formManagementColumnsDetailTwo[j] + ",";
                    columnDetailThree = columnDetailThree + formManagementColumnsDetailThree[j] + ",";
                } else {
                    columnDetailOne = columnDetailOne + formManagementColumnsDetailOne[j];
                    columnDetailTwo = columnDetailTwo + formManagementColumnsDetailTwo[j];
                    columnDetailThree = columnDetailThree + formManagementColumnsDetailThree[j];
                }
            }
            manageFormInfo.setColumnTypeOne(columnDetailOne);
            manageFormInfo.setColumnTypeTwo(columnDetailTwo);
            manageFormInfo.setColumnTypeThree(columnDetailThree);
            dataManageMapper.addExistForm(manageFormInfo);
            System.out.println("Process2: Start to delete extra recording");
            checkProcessThree();
        }

    }


    // delete extra recordings
    @Override
    public void checkProcessThree() {
        String[] databaseFormList = showDatabase().split(",");
        ArrayList<FormInfo> formManageFormList = dataManageMapper.showExistForm();

        // form in database
        int databaseFormListNumber = databaseFormList.length;

        // form in recording
        int formManageFormListNumber = formManageFormList.size();


        //  eliminate all the spaces
        for (int i = 0; i < databaseFormListNumber; i++) {
            databaseFormList[i] = databaseFormList[i].replace("[", "");
            databaseFormList[i] = databaseFormList[i].replace("]", "");
            databaseFormList[i] = databaseFormList[i].trim();
        }

        for (int i = 0; i < formManageFormListNumber; i++) {
            formManageFormList.get(i).setName(formManageFormList.get(i).getName().trim());
        }

        for (int i = 0; i < formManageFormListNumber; i++) {
            HashMap<String, Integer> duplicateCheck = new HashMap<>();
            if (duplicateCheck.put(formManageFormList.get(i).getName(), 1) != null) {
                dataManageMapper.delRow(formManagement, "name", formManageFormList.get(i).getName());
                System.out.println("Process3: Delete row " + formManageFormList.get(i).getName() + ", Reason: Duplicated name");
            }
            for (int j = 0; j < databaseFormListNumber; j++) {
                if (databaseFormList[j].equals(formManageFormList.get(i).getName())) {
                    break;
                } else if (j == databaseFormListNumber - 1) {
                    dataManageMapper.delRow(formManagement, "name", formManageFormList.get(i).getName());
                    System.out.println("Process3: delete extra recording: " + formManageFormList.get(i).getName());
                }
            }
        }

        System.out.println("Process3: Deleted all the duplication and unused recordings");
        checkProcessFour();
    }


    // put unregistered form into form management
    @Override
    public String checkProcessFour() {
        String[] newFormList = showDatabase().split(",");
        ArrayList<FormInfo> oldFormList = dataManageMapper.showExistForm();

        int newFormListNumber = newFormList.length;
        int oldFromListNumber = oldFormList.size();

        //  eliminate all the spaces
        for (int i = 0; i < newFormListNumber; i++) {
            newFormList[i] = newFormList[i].replace("[", "");
            newFormList[i] = newFormList[i].replace("]", "");
            newFormList[i] = newFormList[i].trim();
        }

        for (int i = 0; i < oldFromListNumber; i++) {
            oldFormList.get(i).setName(oldFormList.get(i).getName().trim());
        }


        for (int i = 0; i < newFormListNumber; i++) {
            Map<String, String> tempForm = dataManageMapper.getFirstRow(newFormList[i]);
            Set columnSet;
            Object[] columnArray;
            if (tempForm != null) {
                columnSet = tempForm.keySet();
                columnArray = columnSet.toArray();
            }else{
                System.out.println("Error: Form: " + newFormList[i] + " is empty or has an invalid row, plz check");

                columnArray = dataManageMapper.getColumnNamesFromSys(newFormList[i]).toArray();
            }

            int setSize = columnArray.length;
            String columns = "";
            for (int k = 0; k < setSize; k++) {
                if (k != setSize - 1) {
                    columns = columns + columnArray[k].toString().trim() + ",";
                } else {
                    columns = columns + columnArray[k].toString().trim();
                }
            }

            ArrayList<HashMap<String, Object>> columnDetails = dataManageMapper.getColumnDetails(newFormList[i]);
            int columnDetailsLength = columnDetails.size();
            String columnDetailOne = "";
            String columnDetailTwo = "";
            String columnDetailThree = "";
            for (int j = 0; j < columnDetailsLength; j++) {
                if (j != columnDetailsLength - 1) {
                    columnDetailOne = columnDetailOne + columnDetails.get(j).get("DATA_TYPE") + ",";
                    columnDetailTwo = columnDetailTwo + columnDetails.get(j).get("CHARACTER_MAXIMUM_LENGTH") + ",";
                    columnDetailThree = columnDetailThree + columnDetails.get(j).get("IS_NULLABLE") + ",";
                } else {
                    columnDetailOne = columnDetailOne + columnDetails.get(j).get("DATA_TYPE");
                    columnDetailTwo = columnDetailTwo + columnDetails.get(j).get("CHARACTER_MAXIMUM_LENGTH");
                    columnDetailThree = columnDetailThree + columnDetails.get(j).get("IS_NULLABLE");
                }
            }

            for (int j = 0; j < oldFromListNumber; j++) {
                if (newFormList[i].equals(oldFormList.get(j).getName())) {
                    System.out.println("Process4: name match");
                    HashMap<String, String> updateTemp = new HashMap<>();
                    // check description and columns
                    if (oldFormList.get(j).getDescription() == null || oldFormList.get(j).getDescription().equals("")) {
                        updateTemp.put(formManagementColumns[2], oldFormList.get(i).getName());
                    }
                    if (oldFormList.get(j).getColumns() == null || oldFormList.get(j).getColumns().equals("")
                            || !oldFormList.get(j).getColumns().equals(columns)) {
                        updateTemp.put(formManagementColumns[3], columns);
                        System.out.println(columns);
                    }
                    // check type one, column type
                    if (oldFormList.get(j).getColumnTypeOne() == null || oldFormList.get(j).getColumnTypeOne().equals("")
                            || !oldFormList.get(j).getColumnTypeOne().equals(columnDetailOne)) {
                        updateTemp.put(formManagementColumns[4], columnDetailOne);
                    }
                    // check type one, column type
                    if (oldFormList.get(j).getColumnTypeTwo() == null || oldFormList.get(j).getColumnTypeTwo().equals("")
                            || !oldFormList.get(j).getColumnTypeTwo().equals(columnDetailTwo)) {
                        updateTemp.put(formManagementColumns[5], columnDetailTwo);
                    }
                    // check type one, column type
                    if (oldFormList.get(j).getColumnTypeThree() == null || oldFormList.get(j).getColumnTypeThree().equals("")
                            || !oldFormList.get(j).getColumnTypeThree().equals(columnDetailThree)) {
                        updateTemp.put(formManagementColumns[6], columnDetailThree);
                    }
                    if(updateTemp.size() != 0) {
                        dataManageMapper.updateExistForm(updateTemp, oldFormList.get(j).getId());
                    }
                    break;
                } else if (j == oldFromListNumber - 1) // checked all the exist forms but none of them match
                {
                    FormInfo manageFormInfo = new FormInfo();
                    manageFormInfo.setId(0);
                    manageFormInfo.setName(newFormList[i]);
                    manageFormInfo.setDescription(newFormList[i]);
                    manageFormInfo.setColumns(columns);
                    manageFormInfo.setColumnTypeOne(columnDetailOne);
                    manageFormInfo.setColumnTypeTwo(columnDetailTwo);
                    manageFormInfo.setColumnTypeThree(columnDetailThree);
                    dataManageMapper.addExistForm(manageFormInfo);
                }
            }
        }

        return "check and update success";
    }

    @Override
    public String showDatabase() {
        return dataManageMapper.showDatabase().toString();
    }

}
