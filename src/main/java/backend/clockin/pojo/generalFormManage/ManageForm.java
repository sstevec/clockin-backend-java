package backend.clockin.pojo.generalFormManage;

public class ManageForm {

    private String[] formManagementColumns = new String[]{
            "id", "name", "description", "columns", "columnTypeOne",
            "columnTypeTwo", "columnTypeThree", "creatTime", "modifyTime",
            "modifyContentTime"};

    private String[] formManagementColumnType = new String[]{
            "int unsigned auto_increment primary key",
            "varchar(255) not null",
            "varchar(255) null default 'No description'",
            "varchar(7000) null default 'column1'",
            "varchar(5000) not null",
            "varchar(2000) not null",
            "varchar(4000) not null",
            "timestamp(0) null default current_timestamp",
            "timestamp(0) null default current_timestamp",
            "timestamp(0) null default current_timestamp",};

    private String[] formManagementFormDetailTypeOne = new String[]{
            "int",
            "varchar",
            "varchar",
            "varchar",
            "varchar",
            "varchar",
            "varchar",
            "timestamp",
            "timestamp",
            "timestamp",
    };

    private String[] formManagementFormDetailTypeTwo = new String[]{
            "null",
            "255",
            "255",
            "7000",
            "5000",
            "2000",
            "4000",
            "0",
            "0",
            "0",
    };

    private String[] formManagementFormDetailTypeThree = new String[]{
            "YES",
            "NO",
            "YES",
            "NO",
            "NO",
            "NO",
            "NO",
            "YES",
            "YES",
            "YES",
    };

    public String[] getFormManagementColumns() {
        return formManagementColumns;
    }

    public String[] getFormManagementColumnType() {
        return formManagementColumnType;
    }

    public String[] getFormManagementFormDetailTypeOne() {
        return formManagementFormDetailTypeOne;
    }

    public String[] getFormManagementFormDetailTypeTwo() {
        return formManagementFormDetailTypeTwo;
    }

    public String[] getFormManagementFormDetailTypeThree() {
        return formManagementFormDetailTypeThree;
    }
}
