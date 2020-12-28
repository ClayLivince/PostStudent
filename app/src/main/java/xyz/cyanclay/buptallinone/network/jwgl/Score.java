package xyz.cyanclay.buptallinone.network.jwgl;

import org.apache.log4j.LogManager;

public class Score {

    //课程名称
    //@SerializedName("kcmc")
    public String courseName;

    public String courseID;

    public String groupName;

    //考试性质名称
    //@SerializedName("ksxzmc")
    public String examType;

    //课程性质名称
    //@SerializedName("kcxzmc")
    public String courseCategory;

    //课程类别名称
    //@SerializedName("kclbmc")
    public String courseType;

    //通选课类别
    public String commonType;

    //学期名称
    //@SerializedName("xqmc")
    public String termID;

    //补考重修学期
    public String reTermID;

    //@SerializedName("kcywmc")
    //public String courseEnglish;

    //@SerializedName("cjbsmc")
    public String scoreMark;

    //备注
    //@SerializedName("bz")
    public String mark;

    //学时
    public int pointHour;
    //学分
    //@SerializedName("xf")
    public float point;
    //成绩
    //@SerializedName("zcj")
    public String score;

    public double gpa;

    //Mandatory Empty Constructor for GSON
    public Score() {
    }

    public void verifyGPA() {
        double scoreDouble;

        try {
            scoreDouble = Double.parseDouble(score);
        } catch (NumberFormatException e) {
            if (score.contains("优"))
                scoreDouble = 95d;
            else if (score.contains("良"))
                scoreDouble = 85d;
            else if (score.contains("中"))
                scoreDouble = 75d;
            else if (score.contains("不及格"))
                scoreDouble = 59d;
            else if (score.contains("及格"))
                scoreDouble = 65d;
            else {
                LogManager.getLogger(Score.class).info("Unrecognizable score:" + score);
                scoreDouble = 0d;
            }
        }

        gpa = 4d - (3d * Math.pow(100d - scoreDouble, 2d) / 1600d);

    }

}
