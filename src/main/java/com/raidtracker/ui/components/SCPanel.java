package com.raidtracker.ui.components;

import com.raidtracker.utils.UniqueDrop;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.swing.*;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
public class SCPanel extends JLabel
{
    String RaidID;
    ArrayList<UniqueDrop> Uniques;
}
