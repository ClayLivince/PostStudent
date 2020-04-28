package xyz.cyanclay.buptallinone.network.jwgl;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

class JwglData {

    private JwglManager jwgl;

    String currentTerm;
    int currentWeek;

    Date today;
    int weekday;

    File jwglDir;

    List<Course> courseWeek = new LinkedList<>();

    JwglData(JwglManager jwgl) {
        this.jwgl = jwgl;
    }

    List<Course> loadCourse(int week) throws IOException {
        File fileDir = jwgl.context.getFilesDir();
        verifyDir(fileDir);
        File classFile = new File(jwglDir, "Week" + week + ".json");
        List<Course> courses = new LinkedList<>();
        if (classFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(classFile));

            String line;
            StringBuilder sb = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                JSONArray array = new JSONArray(sb.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    courses.add(new Course(object));
                }
                courseWeek = courses;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return courses;
    }

    void saveCourse(int week) throws IOException {
        File fileDir = jwgl.context.getFilesDir();
        verifyDir(fileDir);
        File classFile = new File(jwglDir, "Week" + week + ".json");
        if (classFile.exists()) {
            if (!classFile.delete()) throw new IOException();
        }
        FileOutputStream fos = new FileOutputStream(classFile);
        Gson gson = new Gson();
        fos.write(gson.toJson(courseWeek).getBytes());
        fos.flush();
        fos.close();
    }

    private boolean verifyDir(File fileDir) throws IOException {
        jwglDir = new File(fileDir, "jwgl");
        if (jwglDir.exists()) {
            if (jwglDir.isDirectory()) {
                return true;
            } else {
                if (!jwglDir.delete()) throw new IOException("Failed to delete file jwgl.");
            }
        }
        return jwglDir.mkdir();
    }
}
