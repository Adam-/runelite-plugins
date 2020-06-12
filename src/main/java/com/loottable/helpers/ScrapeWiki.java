package com.loottable.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapeWiki {
    private static String baseWikiUrl = "https://oldschool.runescape.wiki/w/";
    private static String userAgent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36";

    /**
     * Scrapes wiki's html for npcs' drop tables
     * Tables rows structured like:
     *  / icon (skipped) / item name / quantity / rarity / price
     * @param monsterName monster's loot table to fetch from wiki
     */
    public static List<String[]> scrapeWiki(String monsterName) {
        List<String[]> lootTable = new ArrayList<String[]>();
        String parsedMonsterName = parseStringForUrl(monsterName);
        Document doc;
        try {
            doc = Jsoup.connect(baseWikiUrl + parsedMonsterName).userAgent(userAgent).get();
            Elements dropTableRows = doc.select("table.item-drops tbody tr");

            for (Element dropTableRow : dropTableRows) {
                String[] lootRow = new String[4];
                Elements dropTableCells = dropTableRow.select("td");
                int index = 0;

                for (Element dropTableCell : dropTableCells) {
                    String cellContent = dropTableCell.text();

                    if (cellContent != null && !cellContent.isEmpty() && index < 4) {
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
        } catch (IOException error ) {
            Log.info(error.toString());
        }

        return lootTable;
    }

    /**
     * Filters out unwanted text like footnotes i.e. [d2]
     * @param cellContent
     * @return
     */
    public static String filterWikiTableContent(String cellContent) {
        return cellContent.replaceAll("\\[.*\\]", "");
    }

    public static String parseStringForUrl(String monsterName) {
        String parsedMonsterName = monsterName.replace(' ', '_');
        return parsedMonsterName.substring(0, 1).toUpperCase() + parsedMonsterName.substring(1);
    }
}