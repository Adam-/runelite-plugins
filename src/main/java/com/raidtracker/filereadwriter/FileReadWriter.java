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
    private String coxDir;
    private String tobDir;

    public void writeToFile(RaidTracker raidTracker)
    {
        String dir;

        if (raidTracker.isInTheatreOfBlood()) {
            dir = tobDir;
        }
        else {
            dir = coxDir;
        }
        try
        {
            log.info("writer started");

            //use json format so serializing and deserializing is easy
            Gson gson = new GsonBuilder().create();

            JsonParser parser = new JsonParser();

            String fileName = dir + "\\raid_tracker_data.log";

            FileWriter fw = new FileWriter(fileName,true); //the true will append the new data

            gson.toJson(parser.parse(getJSONString(raidTracker, gson, parser)), fw);

            fw.append("\n");

            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage() + " in writeToFile");
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

    public ArrayList<RaidTracker> readFromFile(String alternateFile, boolean isTob)
    {
        String dir;

        if (isTob) {
            dir = tobDir;
        }
        else {
            dir = coxDir;
        }

        String fileName;
        if (alternateFile.length() != 0) {
            fileName = alternateFile;
        }
        else {
            fileName = dir + "\\raid_tracker_data.log";
        }

        try {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();

            BufferedReader bufferedreader = new BufferedReader(new FileReader(fileName));
            String line;

            ArrayList<RaidTracker> RTList = new ArrayList<>();
            while ((line = bufferedreader.readLine()) != null && line.length() > 0) {
                try {
                    RaidTracker parsed = gson.fromJson(parser.parse(line), RaidTracker.class);
                    RTList.add(parsed);
                }
                catch (JsonSyntaxException e) {
                    System.out.println("Bad line: " + line);
                }

            }

            bufferedreader.close();
            return RTList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<RaidTracker> readFromFile() {
        return readFromFile("", false);
    }

    public ArrayList<RaidTracker> readFromFile(boolean isTob) {
        return readFromFile("", isTob);
    }

    public void createFolders()
    {
        File dir = new File(RUNELITE_DIR, "raid-data tracker");
        IGNORE_RESULT(dir.mkdir());
        dir = new File(dir, username);
        IGNORE_RESULT(dir.mkdir());
        File dir_cox = new File(dir, "cox");
        File dir_tob = new File(dir, "tob");
        IGNORE_RESULT(dir_cox.mkdir());
        IGNORE_RESULT(dir_tob.mkdir());
        File newCoxFile = new File(dir_cox + "\\raid_tracker_data.log");
        File newTobFile = new File(dir_tob + "\\raid_tracker_data.log");

        try {
            IGNORE_RESULT(newCoxFile.createNewFile());
            IGNORE_RESULT(newTobFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.coxDir = dir_cox.getAbsolutePath();
        this.tobDir = dir_tob.getAbsolutePath();
    }

    public void updateUsername(final String username) {
        this.username = username;
        createFolders();
    }

    public void updateRTList(ArrayList<RaidTracker> RTList, boolean isTob) {
        String dir;

        if (isTob) {
            dir = tobDir;
        }
        else {
            dir = coxDir;
        }
        try {
            Gson gson = new GsonBuilder().create();

            JsonParser parser = new JsonParser();

            String fileName = dir + "\\raid_tracker_data.log";


            FileWriter fw = new FileWriter(fileName, false); //the true will append the new data

            for (RaidTracker RT : RTList) {
                if (RT.getLootSplitPaid() > 0) {
                    RT.setSpecialLootInOwnName(true);
                }
                else {
                    //bit of a wonky check, so try to avoid with lootsplitpaid if possible
                    RT.setSpecialLootInOwnName(RT.getLootList().size() > 0 && RT.getLootList().get(0).getName().toLowerCase().equals(RT.getSpecialLoot().toLowerCase()));
                }

                gson.toJson(parser.parse(getJSONString(RT, gson, parser)), fw);

                fw.append("\n");
            }

            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateRTList(ArrayList<RaidTracker> RTList) {
        updateRTList(RTList, false);
    }

    public boolean delete(boolean isTob) {
        String dir;

        if (isTob) {
            dir = tobDir;
        }
        else {
            dir = coxDir;
        }

        File newFile = new File(dir + "\\raid_tracker_data.log");

        boolean isDeleted = newFile.delete();

        try {
            IGNORE_RESULT(newFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isDeleted;
    }

    public void IGNORE_RESULT(boolean b) {}
}
