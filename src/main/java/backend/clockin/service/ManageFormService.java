package backend.clockin.service;

import java.util.ArrayList;

public interface ManageFormService {

    // 管理表自检函数
    String checkProcessOne();

    void checkProcessTwo(int type);

    void checkProcessThree();

    String checkProcessFour();

    // 辅助自检函数
    String showDatabase();



}
