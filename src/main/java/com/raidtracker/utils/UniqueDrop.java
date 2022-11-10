package com.raidtracker.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UniqueDrop
{
    String username;
    String drop;
    int value;
    boolean ffa;
    int sCount;
    String uniqueID = UUID.randomUUID().toString();

    public UniqueDrop(String username, String drop, int value, boolean ffa, int sCount)
    {
        this.username = username;
        this.drop = drop;
        this.value = value;
        this.ffa = ffa;
        this.sCount = sCount;
        this.uniqueID = UUID.randomUUID().toString();
    };
    public UniqueDrop(String username, String drop)
    {
        this.username = username;
        this.drop = drop;
        this.value = -1;
        this.ffa = true;
        this.sCount = -1;
        this.uniqueID = UUID.randomUUID().toString();
    };
};
