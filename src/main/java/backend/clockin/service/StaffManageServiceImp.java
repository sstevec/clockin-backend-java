package backend.clockin.service;

import backend.clockin.mapper.DataManageMapper;
import backend.clockin.mapper.StaffMapper;
import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class StaffManageServiceImp implements StaffManageService {

    @Autowired
    GeneralService generalService;

    @Autowired
    StaffMapper staffMapper;

    @Autowired
    DataManageMapper dataManageMapper;

    @Override
    public SysResult addStaffRelevantForm(String formName) {
        // 这里的formName 指的是用户起的名字，为了防止乱码，这里将这个名字和系统里真正的名字做关联
        try {
            ArrayList<String> existFormNames = staffMapper.getStaffRelevantFormNames();
            if (existFormNames.contains(formName)) {
                // 表格名称重复
                return SysResult.build(201, "表格名称重复");
            }

            int formNum = staffMapper.getSysNameByColumnName("uid").size() + 1;

            // 创建表格, 系统内表格名称统一为staff_#n
            String sysFormName = "staff_" + formNum;
            generalService.createForm(sysFormName, formName);
            // 增加关联记录
            staffMapper.addStaffFormCorrespond(formName, sysFormName, "id");
            staffMapper.addStaffFormCorrespond(formName, sysFormName, "uid");
            staffMapper.addStaffFormCorrespond(formName, sysFormName, "created");
            staffMapper.addStaffFormCorrespond(formName, sysFormName, "updated");

            // 修改表格的默认字段
            generalService.changeColumn(sysFormName, "column1", "id",
                    "int,10,YES,unsigned primary key auto_increment");
            generalService.addColumn(sysFormName, "uid", "varchar,255,NO", "id");
            generalService.addColumn(sysFormName, "created",
                    "timestamp,0,YES, null default current_timestamp", "uid");
            generalService.addColumn(sysFormName, "updated",
                    "timestamp,0,YES, null default current_timestamp", "created");

            return SysResult.build(200, "Staff relevant form creat success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult deleteStaffRelevantForm(String formName) {
        try {
            if (staffMapper.getSysNameByFormName(formName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统表名
            String sysName = staffMapper.getSysNameByFormName(formName).get(0);

            // 删除系统记录
            generalService.dropForm(sysName);

            // 删除系统关联记录
            staffMapper.deleteRecordBySysName(sysName);
            return SysResult.build(200, "Delete success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult changStaffRelevantForm(String oldFormName, String newFormName) {
        try {
            // 用户改表名，只能修改关联名称，系统内部的名称是不会变的
            if (staffMapper.getSysNameByFormName(oldFormName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统内部名称
            String sysName = staffMapper.getSysNameByFormName(oldFormName).get(0);

            // 修改关联名称
            staffMapper.changeStaffRelevantFormName(sysName, newFormName);
            return SysResult.build(200, "Change Success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult getStaffRelevantFormNames() {
        try {
            // 取得的表名
            ArrayList<String> existFormNames = staffMapper.getNameByColumnName("id");

            return SysResult.build(200, "Get form names success", existFormNames);
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult addStaffRelevantFormColumn(String formName, String columnName, String type, String place) {
        try {
            ArrayList<String> allColumnNames = staffMapper.getStaffRelevantAllColumnNames();
            if (allColumnNames.contains(columnName)) {
                // 字段重复，不允许添加
                return SysResult.build(201, "字段重复");
            }
            if (staffMapper.getSysNameByFormName(formName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统名称
            String sysName = staffMapper.getSysNameByFormName(formName).get(0);
            if (sysName.equals("workers")) {
                return SysResult.build(201, "不能修改基础表");
            }
            // 增加关联项
            staffMapper.addStaffFormCorrespond(formName, sysName, columnName);

            // 增加系统的表格字段
            generalService.addColumn(sysName, columnName, type, place);
            return SysResult.build(200, "Add column Success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult deleteStaffRelevantFormColumn(String columnName) {
        try {
            if (columnName.equals("id") || columnName.equals("uid") || columnName.equals("created") || columnName.equals("updated")) {
                // 不允许删除基础字段
                return SysResult.build(201, "不允许删除基础字段");
            }
            if (staffMapper.getSysNameByColumnName(columnName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统表名
            String sysName = staffMapper.getSysNameByColumnName(columnName).get(0);
            if (sysName.equals("workers")) {
                return SysResult.build(201, "不能修改基础表");
            }
            // 删除关联
            staffMapper.deleteColumnByColumnName(columnName);

            // 删除系统表格中的字段
            generalService.delColumn(sysName, columnName);
            return SysResult.build(200, "Delete column success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult changeStaffRelevantFormColumn(String formName, String oldColumn, String newColumn, String type) {
        try {
            if (oldColumn.equals("id") || oldColumn.equals("uid") || oldColumn.equals("created") || oldColumn.equals("updated")) {
                // 不允许修改基础字段
                return SysResult.build(201, "不允许修改基础字段");
            }
            if (newColumn.equals("id") || newColumn.equals("uid") || newColumn.equals("created") || newColumn.equals("updated")) {
                // 不允许修改基础字段
                return SysResult.build(201, "不允许修改其他字段为基础字段");
            }
            if (staffMapper.getSysNameByFormName(formName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统表名
            String sysName = staffMapper.getSysNameByFormName(formName).get(0);
            if (sysName.equals("workers")) {
                return SysResult.build(201, "不能修改基础表");
            }
            // 修改关联字段名
            staffMapper.changeStaffRelevantColumnName(oldColumn, newColumn);

            // 修改系统字段名
            generalService.changeColumn(sysName, oldColumn, newColumn, type);
            return SysResult.build(200, "Change Column Success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult getStaffRelevantFormAllColumns() {
        try {
            // 取得的字段带有大量的基础字段，需要去除
            ArrayList<String> allColumns = staffMapper.getAllColumns();
            int allColumnSize = allColumns.size();
            ArrayList<String> allShowColumns = new ArrayList<>();

            for (int i = 0; i < allColumnSize; i++) {
                if (allColumns.get(i).equals("id") || allColumns.get(i).equals("uid") ||
                        allColumns.get(i).equals("created") || allColumns.get(i).equals("updated")) {
                    continue;
                }
                allShowColumns.add(allColumns.get(i));
            }
            return SysResult.build(200, "Get Columns Success", allShowColumns);
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult addStaffRelevantFormRow(String formName, HashMap<String, String> info) {
        try {
            if (staffMapper.getSysNameByFormName(formName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统表名
            String sysName = staffMapper.getSysNameByFormName(formName).get(0);

            // 检查uid的 唯一性
            ArrayList<Object> uidList = generalService.quickGet("uid", sysName, "uid", info.get("uid"));
            if (uidList.size() != 0) {
                // uid已经使用过了
                return SysResult.build(201, "uid已经被添加过了");
            }
            // 若uid 没有使用过，则在系统内添加
            return generalService.addRow(sysName, info);
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult deleteStaffRelevantFormRow(String formName, String uid) {
        try {
            if (staffMapper.getSysNameByFormName(formName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统表名
            String sysName = staffMapper.getSysNameByFormName(formName).get(0);

            // 删除系统表中的对应行
            staffMapper.deleteStaffRelevantFormRow(sysName, uid);
            return SysResult.build(200, "Delete Success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult changeStaffRelevantFormRow(String formName, HashMap<String, String> info, String uid) {
        try {
            if (staffMapper.getSysNameByFormName(formName).size() == 0) {
                return SysResult.build(201, "系统内部错误，文件丢失");
            }
            // 取得系统表名
            String sysName = staffMapper.getSysNameByFormName(formName).get(0);

            // 改系统表内的行的时候不允许修改uid，防止uid重复
            info.remove("uid");
            // 改变系统表内的行
            dataManageMapper.changeRow(sysName, info, "uid", uid);
            return SysResult.build(200, "Change Row Success");
        } catch (Exception e) {
            return SysResult.build(201, "" + e);
        }
    }

    @Override
    public SysResult staffRelevantFormSearch(String targetColumn, String conditions, String searchType, String separate,
                                             String pageNum, String pageMax) {

        // 创建一个临时的表格，承载搜索的结果
        String formName = "staffTempForm";
        generalService.createForm(formName, formName);

        // 给创建好的表格增加需要呈现的字段
        // 获取所有需要查询的字段
        String[] columns = targetColumn.split(",");
        int columnNum = columns.length;
        for (int i = 0; i < columnNum; i++) {
            if (i == 0) {
                // 添加默认字段uid
                generalService.changeColumn(formName, "column1", "uid", "varchar,255,NO");
            }
            // 新增字段
            if (i == 1) {
                generalService.addColumn(formName, columns[i - 1], "varchar,255,YES", "uid");
            }
            generalService.addColumn(formName, columns[i - 1], "varchar,255,YES", columns[i - 2]);
        }

        // 根据条件，筛选符合条件的uid

        // 拆分获取到的条件
        String[] splitInput = conditions.split(",");
        int length = splitInput.length;
        // 0 是条件，1是操作，2是值
        String[][] inputCond = new String[length / 3][3];
        for (int i = 0; i < length; i = i + 3) {
            // 条件
            inputCond[i / 3][0] = splitInput[i];

            // 操作
            inputCond[i / 3][1] = splitInput[i + 1];

            // 值
            if (Integer.valueOf(searchType) == 2) {
                String tempCond = splitInput[i + 2].trim();
                inputCond[i / 3][2] = "\"" + tempCond + "\"";
            } else {
                inputCond[i / 3][2] = splitInput[i + 2];
            }
        }

        // 根据条件获取uid， 然后根据规则进行筛选
        ArrayList<String> uidList = new ArrayList<>();
        if (separate.equals("AND")) {
            for (int i = 0; i < length; i = i + 3) {
                ArrayList<String> temp = staffMapper.getSysNameByColumnName(inputCond[i / 3][0]);
                String sysName;
                String[] tempCond = new String[2];
                if (temp.size() == 1) {
                    // 获取系统表名正常
                    sysName = temp.get(0);
                } else {
                    // 获取系统表名出错
                    return SysResult.build(201, "系统内部错误，拼接条件时, 获取表名异常");
                }

                // 拆分重组条件
                tempCond[0] = inputCond[i / 3][0] + inputCond[i / 3][1];
                tempCond[1] = inputCond[i / 3][2];

                // 初始化uidList
                if (i == 0) {
                    uidList = generalService.quickSearch("uid", sysName, tempCond, searchType);
                } else {
                    ArrayList<String> tempUidResult = generalService.quickSearch("uid", sysName, tempCond, searchType);
                    // 只取与uidList 的共同部分
                    ArrayList<String> newUidList = new ArrayList<>();
                    int tempSize = tempUidResult.size();
                    for (int j = 0; j < tempSize; j++) {
                        if (uidList.contains(tempUidResult.get(j))) {
                            newUidList.add(tempUidResult.get(j));
                        }
                    }
                    uidList = newUidList;
                }
            }
        } else if (separate.equals("OR")) {
            for (int i = 0; i < length; i = i + 3) {
                ArrayList<String> temp = staffMapper.getSysNameByColumnName(inputCond[i / 3][0]);
                String sysName;
                String[] tempCond = new String[2];
                if (temp.size() == 1) {
                    // 获取系统表名正常
                    sysName = temp.get(0);
                } else {
                    // 获取系统表名出错
                    return SysResult.build(201, "系统内部错误，拼接条件时, 获取表名异常");
                }

                // 拆分重组条件
                tempCond[0] = inputCond[i / 3][0] + inputCond[i / 3][1];
                tempCond[1] = inputCond[i / 3][2];

                // 初始化uidList
                if (i == 0) {
                    uidList = generalService.quickSearch("uid", sysName, tempCond, searchType);
                } else {
                    ArrayList<String> tempUidResult = generalService.quickSearch("uid", sysName, tempCond, searchType);
                    // 只取与uidList 的共同部分
                    int tempSize = tempUidResult.size();
                    for (int j = 0; j < tempSize; j++) {
                        if (!uidList.contains(tempUidResult.get(j))) {
                            uidList.add(tempUidResult.get(j));
                        }
                    }
                }
            }

        } else {
            return SysResult.build(201, "Not a valid separator");
        }

        // 根据获取到的uid取值
        ArrayList<LinkedHashMap<String, String>> data = new ArrayList<>();
        int dataSize = uidList.size();
        for (int i = 0; i < dataSize; i++) {
            LinkedHashMap<String, String> tempData = new LinkedHashMap<>();
            tempData.put("uid", uidList.get(i));
            for (int j = 0; j < columnNum; j++) {
                ArrayList<String> temp = staffMapper.getSysNameByColumnName(columns[i]);
                String sysName;
                if (temp.size() == 1) {
                    // 获取系统表名正常
                    sysName = temp.get(0);
                } else {
                    // 获取系统表名出错
                    return SysResult.build(201, "系统内部错误，获取表名异常");
                }
                tempData.put(columns[j], generalService.quickGet(columns[j], sysName, "uid", uidList.get(i)).get(0).toString());
            }
            data.add(tempData);
        }

        // 处理分页
        int start = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageMax);
        int end = start + Integer.valueOf(pageMax);
        if (start > dataSize) {
            start = dataSize - 1;
        }
        if (end > dataSize) {
            end = dataSize;
        }
        return SysResult.build(200, "Search Success", data.subList(start, end));
    }


    public SysResult staffRelevantFormSearch2(String targetColumn, String conditions, String searchType, String separate,
                                              String start, String pageMax) {
        // 获取并拼接所有的搜索字段
        String[] columns = targetColumn.split(",");
        int columnNum = columns.length;
        String[] searchColumns = new String[columnNum + 1];
        searchColumns[0] = "workers.uid";
        ArrayList<String> sysNameForms = new ArrayList<>();
        for (int i = 1; i < columnNum + 1; i++) {
            if(columns[i-1].equals("count(0)")){
                searchColumns[0] = "count(0)";
                continue;
            }
            ArrayList<String> temp = staffMapper.getSysNameByColumnName(columns[i - 1]);
            String sysName;
            if (temp.size() == 1) {
                // 获取正常
                sysName = temp.get(0);
                // 判断表格名称是否重复
                if (!sysNameForms.contains(sysName) && !"workers".equals(sysName)) {
                    sysNameForms.add(sysName);
                }
                // 拼接字段名称
                searchColumns[i] = sysName + "." + columns[i - 1];
            } else {
                return SysResult.build(201, "系统错误，无法获取表格的系统名称");
            }
        }

        // 将条件拆分，同时将条件所在的表格名称也添加进表格的list中
        String[] allCond = conditions.split(",");
        int condLength = allCond.length;
        String[][] realCond = new String[condLength / 3][2];
        for (int i = 0; i < condLength; i = i + 3) {
            // 获取条件的表名并添加进表名list中
            if (allCond[i].equals("uid")) {
                allCond[i] = "workers" + "." + allCond[i];
                realCond[i / 3][0] = allCond[i] + allCond[i + 1];
                realCond[i / 3][1] = allCond[i + 2];
            } else {
                ArrayList<String> temp = staffMapper.getSysNameByColumnName(allCond[i]);
                String sysName;
                if (temp.size() == 1) {
                    // 获取正常
                    sysName = temp.get(0);
                    // 判断表格名称是否重复
                    if (!sysNameForms.contains(sysName) && !"workers".equals(sysName)) {
                        sysNameForms.add(sysName);
                    }
                    allCond[i] = sysName + "." + allCond[i];
                    if (searchType.equals("2")) {
                        realCond[i / 3][0] = allCond[i] + allCond[i + 1];
                        realCond[i / 3][1] = "\'%" + allCond[i + 2] + "%\'";
                    } else {
                        realCond[i / 3][0] = allCond[i] + allCond[i + 1];
                        realCond[i / 3][1] = allCond[i + 2];
                    }
                } else {
                    return SysResult.build(201, "系统错误，无法获取条件表格的系统名称");
                }
            }
        }

        String[][] leftJoinList = new String[sysNameForms.size()][2];
        int finalFormSize = sysNameForms.size();
        for (int i = 0; i < finalFormSize; i++) {
            leftJoinList[i][0] = sysNameForms.get(i);
            leftJoinList[i][1] = "workers.uid=" + sysNameForms.get(i) + ".uid";
        }

        int num = Integer.valueOf(pageMax);

        ArrayList<LinkedHashMap<String, String>> result = staffMapper.staffSearch(searchColumns, leftJoinList,
                realCond, searchType, separate, "AND", "OR", Integer.valueOf(start), num);


        return SysResult.build(200, "Success", result);
    }

    @Override
    public SysResult staffRelevantFormInFile(String filePath, String formName) {
        ArrayList<HashMap<String, Object>> newData;
        try {
            // 获取写入文件的信息
            SysResult tempResult = generalService.inFile(filePath);
            if (tempResult.isOk()) {
                // 获取成功
                newData = (ArrayList<HashMap<String, Object>>) tempResult.getData();
            } else {
                return SysResult.build(201, "获取写入信息出错： " + tempResult.getMsg());
            }
        } catch (Exception e) {
            return SysResult.build(201, "加载读取系统出错   " + e);
        }

        int dataSize = newData.size();
        // 获取所有字段名称
        Set<String> col_name_set = newData.get(0).keySet();
        ArrayList<String> allColumnNames = new ArrayList<>(col_name_set);
        allColumnNames.remove("uid");

        // 获取指定表格系统名称
        ArrayList<String> targetFormName = staffMapper.getSysNameByFormName(formName);
        String sysFormName;
        if (targetFormName.size() == 0) {
            return SysResult.build(201, "指定表格不存在");
        } else {
            sysFormName = targetFormName.get(0);
            if (sysFormName.equals("workers")) {
                return SysResult.build(201, "不能指定基础表作为扩展目标");
            }
        }

        try {
            // 开始写入数据
            int allColumnNum = allColumnNames.size();
            for (int i = 0; i < allColumnNum; i++) {
                // 找出字段对应的系统表格
                ArrayList<String> sysNames = staffMapper.getSysNameByColumnName(allColumnNames.get(i));
                String sysName;
                if (sysNames.size() == 0) {
                    // 没有找到对应的表格，在指定表格内创建新的字段
                    addStaffRelevantFormColumn(formName, allColumnNames.get(i), "varchar,255,YES", "uid");
                    sysName = sysFormName;
                } else {
                    // 找到了字段对应的表格
                    sysName = sysNames.get(0);
                }

                ArrayList<String> sysFormUidList = staffMapper.getUidListBySysName(sysName);

                // 将字段对应信息写入对应表格
                for (int j = 0; j < dataSize; j++) {
                    String value = newData.get(j).get(allColumnNames.get(i)).toString();
                    String corUid = newData.get(j).get("uid").toString();
                    HashMap<String, String> info = new HashMap<>();
                    // 判定uid是否已经存在，若存在则为update，若不存在则为insert
                    if (sysFormUidList.contains(corUid)) {
                        // update
                        info.put(allColumnNames.get(i), value);
                        changeStaffRelevantFormRow(staffMapper.getNameByColumnName(allColumnNames.get(i)).get(0), info, corUid);
                    } else {
                        // insert
                        // 获取插入表格的其他字段
                        String targetFormColumns = generalService.getFormColumns(sysName).getData().toString();
                        List<String> temp = Arrays.asList(targetFormColumns.split(","));
                        ArrayList<String> needToFillColumns = new ArrayList<>(temp);
                        int needToFillSize = needToFillColumns.size();
                        ArrayList<Object> columnTypeThree = generalService.quickGet("columnTypeThree",
                                "managementform", "name", sysName);
                        if (columnTypeThree.size() != 1) {
                            return SysResult.build(201, "插入新数据时遇到错误，获取数据第三类型失败");
                        }
                        List<String> tempTypeThree = Arrays.asList(columnTypeThree.get(0).toString().split(","));
                        ArrayList<String> needToFillColumnsTypeThree = new ArrayList<>(tempTypeThree);

                        for (int k = 0; k < needToFillSize; k++) {
                            if (needToFillColumnsTypeThree.get(k).equals("NO")) {
                                info.put(needToFillColumns.get(k), "unknown");
                            }
                        }
                        info.remove("id");
                        info.remove("uid");
                        info.remove(allColumnNames.get(i));
                        info.put("uid", corUid);
                        info.put(allColumnNames.get(i), value);
                        generalService.addRow(sysName, info);
                        System.out.println(info);
                    }
                }
            }
            return SysResult.build(200, "导入成功");
        } catch (Exception e) {
            return SysResult.build(201, "储存导入数据出错    " + e);
        }
    }

    @Override
    public SysResult editStaffRelevantForm(HashMap<String, String> info, String uid) {
        // 获取字段名
        int numberColumn = info.size();
        String[] column_name = new String[numberColumn];
        Set<String> col_name_set = info.keySet();
        col_name_set.toArray(column_name);

        for (int i = 0; i < numberColumn; i++) {
            // 根据字段名取得表名
            ArrayList<String> sysNames = staffMapper.getSysNameByColumnName(column_name[i]);
            String sysName;
            if (sysNames.size() == 0) {
                // 没有找到对应的表格
                return SysResult.build(201, "修改员工信息错误, 没有找到对应的表格");
            } else {
                // 找到了字段对应的表格
                sysName = sysNames.get(0);
            }

            // 取得系统表中所有uid
            ArrayList<String> sysFormUidList = staffMapper.getUidListBySysName(sysName);
            HashMap<String, String> tempInfo = new HashMap<>();
            // 判定uid是否已经存在，若存在则为update，若不存在则为insert
            if (sysFormUidList.contains(uid)) {
                // update
                tempInfo.put(column_name[i], info.get(column_name[i]));
                changeStaffRelevantFormRow(staffMapper.getNameByColumnName(column_name[i]).get(0), tempInfo, uid);
            } else {
                // insert
                // 获取插入表格的其他字段
                String targetFormColumns = generalService.getFormColumns(sysName).getData().toString();
                List<String> temp = Arrays.asList(targetFormColumns.split(","));
                ArrayList<String> needToFillColumns = new ArrayList<>(temp);
                int needToFillSize = needToFillColumns.size();
                ArrayList<Object> columnTypeThree = generalService.quickGet("columnTypeThree",
                        "managementform", "name", sysName);
                if (columnTypeThree.size() != 1) {
                    return SysResult.build(201, "插入新数据时遇到错误，获取数据第三类型失败");
                }
                List<String> tempTypeThree = Arrays.asList(columnTypeThree.get(0).toString().split(","));
                ArrayList<String> needToFillColumnsTypeThree = new ArrayList<>(tempTypeThree);

                for (int k = 0; k < needToFillSize; k++) {
                    if (needToFillColumnsTypeThree.get(k).equals("NO")) {
                        info.put(needToFillColumns.get(k), "unknown");
                    }
                }
                tempInfo.remove("id");
                tempInfo.remove("uid");
                tempInfo.remove(column_name[i]);
                tempInfo.put("uid", uid);
                tempInfo.put(column_name[i], info.get(column_name[i]));
                generalService.addRow(sysName, tempInfo);
            }
        }
        return SysResult.build(200, "数据更新成功");
    }

    @Override
    public SysResult softDeleteStaff(String uid){
        try{
            staffMapper.softDeleteStaff(uid);
            return SysResult.build(200,"软删除员工成功");
        }catch (Exception e)
        {
            return SysResult.build(201,"软删除异常   "+e);
        }
    }
}
