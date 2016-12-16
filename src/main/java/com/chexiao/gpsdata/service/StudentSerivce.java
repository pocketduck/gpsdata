package com.chexiao.gpsdata.service;

import com.chexiao.base.util.UniqueIDUtils;
import com.chexiao.gpsdata.dao.helper.DBHelper;
import com.chexiao.gpsdata.entity.Student;

import java.util.List;

/**
 * Created by fulei on 2016-12-16.
 */
public class StudentSerivce {

    public List<Student> getAllStudet() throws Exception {
        return (List<Student>)DBHelper.getDaoOperator().getListByCustom(Student.class, "*",
                "", "");
    }

    public long addStudet(Student s) throws Exception{
        long id = UniqueIDUtils.getUniqueID();
        s.setId(id);
        DBHelper.getDaoOperator().insert(s);
        return id;

    }
}
