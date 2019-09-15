package backend.clockin.pojo.generalFormManage;

import java.util.Date;

public class FormInfo {


    private int id;
    private String name;
    private String description;
    private String columns;
    private String columnTypeOne;
    private String columnTypeTwo;
    private String columnTypeThree;
    private Date creatTime;
    private Date modifyTime;
    private Date modifyContentTime;


    public String getColumnTypeOne() {
        return columnTypeOne;
    }

    public void setColumnTypeOne(String columnTypeOne) {
        this.columnTypeOne = columnTypeOne;
    }

    public String getColumnTypeTwo() {
        return columnTypeTwo;
    }

    public void setColumnTypeTwo(String columnTypeTwo) {
        this.columnTypeTwo = columnTypeTwo;
    }

    public String getColumnTypeThree() {
        return columnTypeThree;
    }

    public void setColumnTypeThree(String columnTypeThree) {
        this.columnTypeThree = columnTypeThree;
    }




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public Date getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Date getModifyContentTime() {
        return modifyContentTime;
    }

    public void setModifyContentTime(Date modifyContentTime) {
        this.modifyContentTime = modifyContentTime;
    }







}
