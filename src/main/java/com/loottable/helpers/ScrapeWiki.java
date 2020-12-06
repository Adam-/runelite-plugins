package com.loottable.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapeWiki {
    private final static String baseWikiUrl = "https://oldschool.runescape.wiki/w/";
    private static Document doc;

    public final static String baseImageUrl = "https://oldschool.runescape.wiki";
    public final static String userAgent = "RuneLite";

    /**
     * Scrapes wiki's html for npcs' drop tables Tables rows structured like: / icon
     * / item name / quantity / rarity / price
     * 
     * @param monsterName monster's loot table to fetch from wiki
     */
    public static Map<String, List<String[]>> scrapeWiki(String monsterName) {
        Map<String, List<String[]>> allLootTables = new HashMap<String, List<String[]>>();
        String parsedMonsterName = parseStringForUrl(monsterName);
        try {
            doc = Jsoup.connect(baseWikiUrl + parsedMonsterName).userAgent(userAgent).get();
            Elements tableHeaders = doc.select("h3 span.mw-headline");
            int tableIndex = 0;
            for (Element tableHeader : tableHeaders) {
                String tableHeaderString = tableHeader.text();
                allLootTables.put(tableHeaderString, getTableContent(tableIndex));
                tableIndex++;
            }
        } catch (IOException error) {
            Log.info(error.toString());
        }

        return allLootTables;
    }

    /**
     * Unable to get (h3 span[id]) table selector working, just using an index for
     * now
     * 
     * @param tableHeader
     * @return
     */
    private static List<String[]> getTableContent(int tableIndex) {
        List<String[]> lootTable = new ArrayList<String[]>();
        Elements dropTables = doc.select("h3 ~ table.item-drops");
        if (dropTables.size() > tableIndex) {
            Elements dropTableRows = dropTables.get(tableIndex).select("tbody tr");
            for (Element dropTableRow : dropTableRows) {
                String[] lootRow = new String[5];
                Elements dropTableCells = dropTableRow.select("td");
                int index = 1;

                for (Element dropTableCell : dropTableCells) {
                    String cellContent = dropTableCell.text();
                    Elements images = dropTableCell.select("img");

                    if (images.size() != 0) {
                        String imageSource = images.first().attr("src");
                        if (!imageSource.isEmpty()) {
                            lootRow[0] = baseImageUrl + imageSource;
                        }
                    }

                    if (cellContent != null && !cellContent.isEmpty() && index < 5) {
                        cellContent = filterWikiTableContent(cellContent);
                        lootRow[index] = cellContent;
                        index++;
                    }
                }

                // Don't add if item name hasn't been filled in
                if (lootRow[0] != null) {
                    lootTable.add(lootRow);
                }
            }
        }

        return lootTable;
    }

    /**
     * Filters out unwanted text like footnotes i.e. [d2]
     * 
     * @param cellContent
     * @return
     */
    public static String filterWikiTableContent(String cellContent) {
        return cellContent.replaceAll("\\[.*\\]", "");
    }

    public static String parseStringForUrl(String monsterName) {
        String parsedMonsterName = monsterName.replace(' ', '_');
        parsedMonsterName = parsedMonsterName.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        return parsedMonsterName.substring(0, 1).toUpperCase() + parsedMonsterName.substring(1);
    }
}