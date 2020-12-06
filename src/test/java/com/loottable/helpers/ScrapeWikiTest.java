package com.loottable.helpers;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ScrapeWikiTest {
    @Test
    public void testScrapeWikiFetchesWikiPage() {
        Map<String, List<String[]>> allLootTables = ScrapeWiki.scrapeWiki("Fire giant");
        List<String[]> lootTable = allLootTables.get("100%");
        // Assuming bones still at top of page
        String[] bonesRow = lootTable.get(0);
        assertEquals(bonesRow[0], ScrapeWiki.baseImageUrl + "/images/1/11/Big_bones.png?bc2e9");
        assertEquals(bonesRow[1], "Big bones");
        assertEquals(bonesRow[2], "1");
        assertEquals(bonesRow[3], "Always");

        // Weapons and armour
        lootTable = allLootTables.get("Weapons and armour");
        String[] steelAxeRow = lootTable.get(0);
        assertEquals(steelAxeRow[0], ScrapeWiki.baseImageUrl + "/images/1/10/Steel_axe.png?c724c");
        assertEquals(steelAxeRow[1], "Steel axe");
        assertEquals(steelAxeRow[2], "1");
        assertEquals(steelAxeRow[3], "3/128");
    }

    @Test
    public void testFilterWikiTableContent() {
        String testContent = "1/428;1/76[d2]";
        String expected = "1/428;1/76";
        // Expect [d2] to be removed
        String result = ScrapeWiki.filterWikiTableContent(testContent);
        assertEquals(expected, result);
    }

    @Test
    public void testParseStringForUrlLesserDemon() {
        String urlString = ScrapeWiki.parseStringForUrl("Lesser Demon");
        assertEquals("Lesser_demon", urlString);
    }
}