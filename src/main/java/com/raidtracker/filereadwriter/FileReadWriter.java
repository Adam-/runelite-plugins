package com.raidtracker.filereadwriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.raidtracker.RaidTracker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class FileReadWriter {

    @Getter
    private String username = "Canvasba";
    private String coxDir;
    private String tobDir;
    private String toaDir;


    public void writeToFile(RaidTracker raidTracker)
    {
        if (coxDir == null)
        {
            createFolders();
        };
        log.info("writer started");
        ArrayList<RaidTracker> saved = readFromFile(raidTracker.getInRaidType());
        final boolean[] newrt = {true};
        saved.forEach(srt -> {
            if (srt.getKillCountID() == raidTracker.getKillCountID())
            {
                newrt[0] = false;
                saved.set(saved.indexOf(srt), raidTracker);
            };
        });
        if (newrt[0])
        {
            saved.add(raidTracker);
        };
        updateRTList(saved, raidTracker.getInRaidType());
    }

    public String getJSONString(RaidTracker raidTracker, Gson gson, JsonParser parser)
    {
        return gson.toJson(raidTracker);
    };

    public ArrayList<RaidTracker> readFromFile(String alternateFile, int raidType)
    {
        String dir;
        switch (raidType)
        {
            case 0 : // chambers;
                dir = coxDir;
                break;
            case 1: // Tob
                dir = tobDir;
                break;
            case 2 :// toa
                dir = toaDir;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + raidType);
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

    public ArrayList<RaidTracker> readFromFile(int type) {
        return readFromFile("", type);

    }

    public void createFolders()
    {
        System.out.println("creating folders with name "+ username);
        File dir = new File(RUNELITE_DIR, "raid-data-tracker");
        IGNORE_RESULT(dir.mkdir());
        dir = new File(dir, username);
        IGNORE_RESULT(dir.mkdir());
        File dir_cox = new File(dir, "cox");
        File dir_tob = new File(dir, "tob");
        File dir_toa   = new File(dir, "toa");
        IGNORE_RESULT(dir_cox.mkdir());
        IGNORE_RESULT(dir_tob.mkdir());
        IGNORE_RESULT(dir_toa.mkdir());
        File newCoxFile = new File(dir_cox + "\\raid_tracker_data.log");
        File newTobFile = new File(dir_tob + "\\raid_tracker_data.log");
        File newToaFile = new File(dir_toa + "\\raid_tracker_data.log");

        try {
            IGNORE_RESULT(newCoxFile.createNewFile());
            IGNORE_RESULT(newTobFile.createNewFile());
            IGNORE_RESULT(newToaFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.coxDir = dir_cox.getAbsolutePath();
        this.tobDir = dir_tob.getAbsolutePath();
        this.toaDir = dir_toa.getAbsolutePath();
    }

    public void updateUsername(final String username) {
        this.username = username;
        createFolders();
    }

    public void updateRTList(ArrayList<RaidTracker> RTList, int type) {
        String dir;
        if (coxDir == null)
        {
            createFolders();
        };
        switch (type)
        {
            case 0 : // chambers;
                dir = coxDir;
                break;
            case 1: // Tob
                dir = tobDir;
                break;
            case 2 :// toa
                dir = toaDir;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        };

        try {
            Gson gson = new GsonBuilder().create();

            JsonParser parser = new JsonParser();
            String fileName = dir + "\\raid_tracker_data.log";


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


    public boolean delete(int type) {
        String dir;
        if (coxDir == null)
        {
            createFolders();
        };
        switch (type)
        {
            case 0 : // chambers;
                dir = coxDir;
                break;
            case 1: // Tob
                dir = tobDir;
                break;
            case 2 :// toa
                dir = toaDir;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        };

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
