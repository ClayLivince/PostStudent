package xyz.cyanclay.poststudent.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;

import xyz.cyanclay.poststudent.entity.info.Bus;

public class ClayManager {
    static Gson gson = new Gson();
    static final String baseURL = "https://bupt.cyanclay.xyz/";

    /*
    public static Map<TrainModeCourseSubGroup, NetCourseGroup> doUpload(List<TrainModeCourseGroup> list) throws IOException {
        JsonObject object = new JsonObject();
        object.addProperty("packet", "course");

        JsonArray groupArray = new JsonArray();
        for (TrainModeCourseGroup group : list) {
            for (TrainModeCourseSubGroup subGroup : group.getSubGroups()) {
                JsonObject groupObject = new JsonObject();
                groupObject.addProperty("id", subGroup.getGroupID());
                groupObject.addProperty("name", subGroup.getGroupName());
                groupObject.addProperty("minPoint", subGroup.getMinimumPoint());

                groupArray.add(groupObject);
            }
        }

        object.add("body", groupArray);

        JsonObject response = post("course/upload", object).getAsJsonObject();

        Map<TrainModeCourseSubGroup, NetCourseGroup> result = new HashMap<>();

        switch (response.get("status").getAsInt()) {
            case StatusCode.OK: {
                break;
            }
            case StatusCode.CONFLICT: {
                JsonArray conflictGroups = response.get("groups").getAsJsonArray();
                HashMap<String, NetCourseGroup> conflictSet = new HashMap<>();

                for (JsonElement conflictGroupElem : conflictGroups) {
                    JsonObject conflictGroup = conflictGroupElem.getAsJsonObject();

                    conflictSet.put(conflictGroup.get("groupID").getAsString(),
                            new NetCourseGroup(conflictGroup.get("groupID").getAsString(),
                                    conflictGroup.get("groupName").getAsString(),
                                    conflictGroup.get("minimumPoint").getAsFloat()));
                }

                for (TrainModeCourseGroup group : list) {
                    for (TrainModeCourseSubGroup subGroup : group.getSubGroups()) {
                        NetCourseGroup targetGroup = conflictSet.get(subGroup.getGroupID());
                        if (targetGroup != null) {
                            result.put(subGroup, targetGroup);
                        }
                    }
                }
                break;
            }
        }

        return result;
    }

    public static List<TrainModeCourseSubGroup> getMinimumPoint(List<TrainModeCourseGroup> list) throws IOException {
        List<String> ids = new ArrayList<>();
        List<TrainModeCourseSubGroup> notFoundGroups = new LinkedList<>();

        for (TrainModeCourseGroup group : list) {
            for (TrainModeCourseSubGroup subGroup : group.getSubGroups()) {
                ids.add(subGroup.getGroupID());
            }
        }

        JsonObject object = new JsonObject();
        object.addProperty("packet", "group");
        object.add("body", gson.toJsonTree(ids));

        JsonObject response = post("course/group/get", object).getAsJsonObject();

        JsonArray foundGroups = response.get("groups").getAsJsonArray();
        //JsonArray notFounds = response.get("notFound").getAsJsonArray();

        HashMap<String, NetCourseGroup> foundSet = new HashMap<>();

        for (JsonElement foundGroupElem : foundGroups) {
            JsonObject foundGroup = foundGroupElem.getAsJsonObject();

            foundSet.put(foundGroup.get("groupID").getAsString(), new NetCourseGroup(foundGroup.get("groupID").getAsString(),
                    foundGroup.get("groupName").getAsString(),
                    foundGroup.get("minimumPoint").getAsFloat()));
        }

        for (TrainModeCourseGroup group : list) {
            for (TrainModeCourseSubGroup subGroup : group.getSubGroups()) {
                NetCourseGroup targetGroup = foundSet.get(subGroup.getGroupID());
                if (targetGroup != null) {
                    subGroup.setMinimumPoint(targetGroup.minimumPoint);
                } else {
                    notFoundGroups.add(subGroup);
                }
            }
        }

        return notFoundGroups;
    }

     */

    public static List<Bus> getBus(NetworkManager nm) throws Exception {
        Connection.Response res = nm.getNoVPN(Jsoup.connect(baseURL + "bus")
                .ignoreContentType(true)).bufferUp();
        //Document dom = res.parse();
        return gson.fromJson(res.body(), new TypeToken<List<Bus>>() {
        }.getType());
    }

    public static class NetCourseGroup {
        String groupID;
        String groupName;
        float minimumPoint;

        NetCourseGroup(String groupID, String groupName, float minimumPoint) {
            this.groupID = groupID;
            this.groupName = groupName;
            this.minimumPoint = minimumPoint;
        }

        public String getGroupID() {
            return groupID == null ? "" : groupID;
        }

        public float getMinimumPoint() {
            return minimumPoint;
        }

        public String getGroupName() {
            return groupName == null ? "" : groupName;
        }
    }

    static JsonElement post(String url, JsonElement data) throws IOException {
        Connection.Response response = Jsoup.connect(baseURL + url)
                .ignoreContentType(true)
                .method(Connection.Method.POST)
                .timeout(30000)
                .requestBody(data.toString())
                .header("Content-Type", "application/json")
                .execute();

        return JsonParser.parseString(response.body());
    }


}
