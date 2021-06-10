package xyz.cyanclay.poststudent.entity.jwgl;

import org.apache.log4j.LogManager;

import java.util.List;

public class AverageScore {

    public double gpaBUPT = 0;
    public float gpaPKU = 0;

    public double weightedAverage = 0;
    public double postGraduateAverage = 0;

    private double sumScore = 0;
    private double sumPoint = 0;
    private double sumGPA = 0;


    public static AverageScore calcAvg(List<Score> scores) {
        AverageScore averageScore = new AverageScore();

        calcSum(scores, averageScore);
        averageScore.weightedAverage = averageScore.sumScore / averageScore.sumPoint;
        averageScore.gpaBUPT = averageScore.sumGPA / averageScore.sumPoint;

        return averageScore;
    }

    private static void calcSum(List<Score> scores, AverageScore averageScore) {
        for (Score score : scores) {
            if (score.courseType.contains("必修") || score.courseType.contains("选修")) {
                averageScore.sumPoint += score.point;

                double scoreDouble;

                try {
                    scoreDouble = Double.parseDouble(score.score);
                } catch (NumberFormatException e) {
                    if (score.score.contains("优"))
                        scoreDouble = 95d;
                    else if (score.score.contains("良"))
                        scoreDouble = 85d;
                    else if (score.score.contains("中"))
                        scoreDouble = 75d;
                    else if (score.score.contains("不及格"))
                        scoreDouble = 59d;
                    else if (score.score.contains("及格"))
                        scoreDouble = 65d;
                    else {
                        LogManager.getLogger(AverageScore.class).info("Unrecognizable score:" + score.score);
                        scoreDouble = 0d;
                    }
                }

                averageScore.sumScore += scoreDouble * score.point;
                averageScore.sumGPA += score.gpa * score.point;
            }
        }
    }


}