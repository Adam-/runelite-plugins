package com.raidtracker.filereadwriter;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.raidtracker.RaidTracker;
import com.raidtracker.RaidTrackerItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static net.runelite.client.RuneLite.RUNELITE_DIR;


@Slf4j
public class FileReadWriter {

    @Getter
    private String username;
    private String dir;

    private boolean startMigrate;

    public void writeToFile(RaidTracker raidTracker)
    {
        try
        {
            //use json format so serializing and deserializing is easy
            Gson gson = new GsonBuilder().create();

            JsonParser parser = new JsonParser();

            String fileName = this.dir + "\\raid_tracker_data.log";

            FileWriter fw = new FileWriter(fileName,true); //the true will append the new data

            gson.toJson(parser.parse(getJSONString(raidTracker, gson, parser)), fw);

            fw.append("\n");

            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    public String getJSONString(RaidTracker raidTracker, Gson gson, JsonParser parser)
    {
        JsonObject RTJson =  parser.parse(gson.toJson(raidTracker)).getAsJsonObject();


        List<RaidTrackerItem> lootList = raidTracker.getLootList();

        //------------------ temporary fix until i can get gson.tojson to work for arraylist<RaidTrackerItem> ---------
        JsonArray lootListToString = new JsonArray();


        for (RaidTrackerItem item : lootList) {
            lootListToString.add(parser.parse(gson.toJson(item, new TypeToken<RaidTrackerItem>() {
            }.getType())));
        }

        RTJson.addProperty("lootList", lootListToString.toString());

        //-------------------------------------------------------------------------------------------------------------

//		System.out.println(
//				gson.toJson(lootList, new TypeToken<List<RaidTrackerItem>>(){}.getType())); //[null], raidtrackerplugin is added to the list of types, which is automatically set to skipserialize true -> null return;



        //massive bodge, works for now
        return RTJson.toString().replace("\\\"", "\"").replace("\"[", "[").replace("]\"", "]");
    }

    public ArrayList<RaidTracker> readFromFile(String alternateFile)
    {
        String fileName;
        if (alternateFile.length() != 0) {
            fileName = alternateFile;
        }
        else {
            fileName = this.dir + "\\raid_tracker_data.log";
        }

        try {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();

            BufferedReader bufferedreader = new BufferedReader(new FileReader(fileName));
            String line;

            boolean updateDates = false;

            ArrayList<RaidTracker> RTList = new ArrayList<>();
            while ((line = bufferedreader.readLine()) != null && line.length() > 0) {
                if (!line.contains("date\":")) {
                    updateDates = true;
                }
                RTList.add(gson.fromJson(parser.parse(line), RaidTracker.class));
            }

            bufferedreader.close();

            if (updateDates) {
                updateRTList(RTList);
            }

            return RTList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<RaidTracker> readFromFile() {
        return readFromFile("");
    }

    public String createFolders()
    {
        startMigrate = false;
        //old root folder: dir/loots/username/cox, adding migrate option.
        File dir_deprecated = new File(RUNELITE_DIR, "loots");

        if (dir_deprecated.exists()) {
            dir_deprecated = new File(dir_deprecated, username);
            if (dir_deprecated.exists()) {
                dir_deprecated = new File(dir_deprecated, "cox");
                if (dir_deprecated.exists()) {
                    startMigrate = true;
                }
            }
        }

        File dir = new File(RUNELITE_DIR, "raid-data tracker");
        IGNORE_RESULT(dir.mkdir());
        dir = new File(dir, username);
        IGNORE_RESULT(dir.mkdir());
        dir = new File(dir, "cox");
        IGNORE_RESULT(dir.mkdir());
        File newFile = new File(dir + "\\raid_tracker_data.log");

        try {
            IGNORE_RESULT(newFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dir.getAbsolutePath();
    }

    public void updateUsername(final String username) {
        this.username = username;
        this.dir = createFolders();

        if (startMigrate) {
            migrate();
        }
    }

    public void updateRTList(ArrayList<RaidTracker> RTList) {
        log.info("update RTList");
        log.info(RTList.toString());
        try {
            Gson gson = new GsonBuilder().create();

            JsonParser parser = new JsonParser();

            String fileName = this.dir + "\\raid_tracker_data.log";


            FileWriter fw = new FileWriter(fileName, false); //the true will append the new data

            for (RaidTracker RT : RTList) {
                gson.toJson(parser.parse(getJSONString(RT, gson, parser)), fw);

                fw.append("\n");
            }

            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {
        File newFile = new File(dir + "\\raid_tracker_data.log");

        boolean isDeleted = newFile.delete();

        try {
            IGNORE_RESULT(newFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isDeleted;
    }


    @SuppressWarnings("ConstantConditions")
    public void migrate() {
        File dir_deprecated_l1 = new File(RUNELITE_DIR, "loots");
        File dir_deprecated_l2 = new File(dir_deprecated_l1, username);
        File dir_deprecated_l3 = new File(dir_deprecated_l2, "cox");

        File logFile_deprecated = new File(dir_deprecated_l3 + "\\raid_tracker_data.log");

        if (logFile_deprecated.exists()) {
            ArrayList<RaidTracker> temp = readFromFile(dir_deprecated_l3 + "\\raid_tracker_data.log");

            for (RaidTracker RT : temp) {
                writeToFile(RT);
            }

            IGNORE_RESULT(logFile_deprecated.delete());
            IGNORE_RESULT(dir_deprecated_l3.delete());

            //making sure to not delete any lootlogger directories if present
            if (dir_deprecated_l2.listFiles().length - 1 == 0) {
                IGNORE_RESULT(dir_deprecated_l2.delete());
            }
            if (dir_deprecated_l1.listFiles().length == 0) {
                IGNORE_RESULT(dir_deprecated_l1.delete());
            }
        }
    }

    @SuppressWarnings("unused")
    public void IGNORE_RESULT(boolean b) {}
}
