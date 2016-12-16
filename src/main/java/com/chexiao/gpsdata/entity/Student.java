package com.chexiao.gpsdata.entity;

import com.chexiao.base.dao.annotation.Column;
import com.chexiao.base.dao.annotation.Id;
import com.chexiao.base.dao.annotation.NotDBColumn;
import com.chexiao.base.dao.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fulei on 2016-12-16.
 */
@Table(name = "t_student")
public class Student implements Serializable {
    @NotDBColumn
    private static final long serialVersionUID = -3706501669575864312L;

    @Id(insertable = true)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "createdate")
    private Date createDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
