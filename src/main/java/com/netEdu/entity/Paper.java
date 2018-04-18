package com.netEdu.entity;

import com.netEdu.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Description: 试卷表
 * User: Lei
 * Date: 2018-04-03
 * Time: 14:36
 */
@Data
@Entity
public class Paper extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paper_id;

    private String paper_name;

    private int teacher_id;

    private String questions;

    private String create_date;

    private String correct_answers;

    private String remarks;

    @Transient
    private String teacher_name;

}