package com.raidtracker;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;


@Slf4j
public class FileReadWriter {

    @Getter
    private String username;
    private String dir;

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

    public ArrayList<RaidTracker> readFromFile()
    {
        String fileName = this.dir + "\\raid_tracker_data.log";

        try {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();

            BufferedReader bufferedreader = new BufferedReader(new FileReader(fileName));
            String line;

            ArrayList<RaidTracker> RTList = new ArrayList<>();
            while ((line = bufferedreader.readLine()) != null && line.length() > 0) {
                RTList.add(gson.fromJson(parser.parse(line), RaidTracker.class));
            }

            return RTList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }


    }

    public String createFolders()
    {
        File dir = new File(RUNELITE_DIR, "loots");
        IGNORE_RESULT(dir.mkdir());
        dir = new File(dir, username);
        IGNORE_RESULT(dir.mkdir());
        dir = new File(dir, "cox");
        IGNORE_RESULT(dir.mkdir());

        return dir.getAbsolutePath();
    }

    public void updateUsername(final String username) {
        this.username = username;
        this.dir = createFolders();
    }

    @SuppressWarnings("unused")
    public void IGNORE_RESULT(boolean b) {}
}
