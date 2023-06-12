package com.raidtracker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RaidTrackerItem {

    public String name;
    public int id;
    public int quantity;
    public int price;
};
