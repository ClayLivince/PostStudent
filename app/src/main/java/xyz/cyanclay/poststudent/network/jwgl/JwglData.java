package xyz.cyanclay.poststudent.network.jwgl;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import xyz.cyanclay.poststudent.entity.jwgl.Courses;
import xyz.cyanclay.poststudent.entity.jwgl.trainmode.TrainModeCourseGroup;
import xyz.cyanclay.poststudent.network.login.LoginException;

public class JwglData {
    private final JwglManager jwgl;

    String currentTerm;
    Map<String, String> terms = new HashMap<>();
    int currentWeek;
    int totalWeeks;
    String profession = "";
    String school = "";
    String classID = "";

    Calendar today;
    int year;
    int weekday;

    File jwglDir;

    Courses courses = new Courses();

    JwglData(JwglManager jwgl) {
        this.jwgl = jwgl;
    }

    Courses getCourses(boolean refresh) throws Exception {
        if (courses.isEmpty() && !refresh) {
            courses = loadCourse();
        }
        if (courses.isEmpty() || refresh) {
            courses = jwgl.getCourses();
            saveCourse();
        }
        return courses;
    }

    public Map<String, String> getTerms() throws Exception {
        if (terms.isEmpty()) {
            terms = jwgl.getTerms();
        }
        return terms;
    }

    public String getCurrentTerm() throws Exception {
        if (currentTerm == null) {
            getTerms();
        }
        return currentTerm;
    }

    List<TrainModeCourseGroup> loadTrainMode() throws IOException, LoginException {
        File fileDir = jwgl.context.getFilesDir();
        verifyDir(fileDir);

        String fileName = "TrainMode" + ".json";
        Log.i("JwglData", "Loading TrainMode from " + fileName);
        File trainFile = new File(jwglDir, fileName);

        LinkedList<TrainModeCourseGroup> list = new LinkedList<>();
        if (trainFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(trainFile));

            String line;
            StringBuilder sb = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                Gson gson = new Gson();
                list.addAll((List<TrainModeCourseGroup>) gson.fromJson(sb.toString(), new TypeToken<List<TrainModeCourseGroup>>() {
                }.getType()));
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    void saveTrainMode(List<TrainModeCourseGroup> list) throws IOException, LoginException {
        File fileDir = jwgl.context.getFilesDir();
        verifyDir(fileDir);
        String fileName = "TrainMode" + ".json";
        Log.i("JwglData", "Saving TrainMode to " + fileName);
        File trainFile = new File(jwglDir, fileName);
        if (trainFile.exists()) {
            if (!trainFile.delete()) throw new IOException();
        }
        FileOutputStream fos = new FileOutputStream(trainFile);
        Gson gson = new Gson();
        fos.write(gson.toJson(list).getBytes());
        fos.flush();
        fos.close();
    }

    Courses loadCourse() throws IOException {
        File fileDir = jwgl.context.getFilesDir();
        verifyDir(fileDir);

        String fileName = "Course" + ".json";
        Log.i("JwglData", "Loading Course from " + fileName);
        File classFile = new File(jwglDir, fileName);
        Courses courses = new Courses();
        if (classFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(classFile));

            String line;
            StringBuilder sb = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                Gson gson = new Gson();
                courses.addAll((Courses) gson.fromJson(sb.toString(), new TypeToken<Courses>() {
                }.getType()));
                this.courses = courses;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return courses;
    }

    void saveCourse() throws IOException {
        File fileDir = jwgl.context.getFilesDir();
        verifyDir(fileDir);
        String fileName = "Course" + ".json";
        Log.i("JwglData", "Saving Course to " + fileName);
        File classFile = new File(jwglDir, fileName);
        if (classFile.exists()) {
            if (!classFile.delete()) throw new IOException();
        }
        FileOutputStream fos = new FileOutputStream(classFile);
        Gson gson = new Gson();
        fos.write(gson.toJson(this.courses).getBytes());
        fos.flush();
        fos.close();
    }

    private void verifyDir(File fileDir) throws IOException {
        jwglDir = new File(fileDir, "jwgl");
        if (jwglDir.exists()) {
            if (jwglDir.isDirectory()) {
                return;
            } else {
                if (!jwglDir.delete()) throw new IOException("Failed to delete file jwgl.");
            }
        }
        if (!jwglDir.mkdir())
            throw new IOException("Failed to create jwglDir.");
    }
}
