package com.example;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> getContentItems(String content) {
        String[] splitArray = content.split("\\n");
        List<String> itemList = new ArrayList<>();

        for (String item : splitArray) {
            String trimmedItem = item.trim();
            if (!trimmedItem.isEmpty() && !trimmedItem.equals("N/A")) {
                itemList.add(trimmedItem);
            }
        }

        return itemList;
    }

}
