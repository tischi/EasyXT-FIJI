/**
 * Copyright (c) 2020 Ecole Polytechnique Fédérale de Lausanne. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.epfl.biop.imaris;

import Imaris.Error;
import Imaris.IDataItemPrx;
import Imaris.cStatisticValues;
import ij.measure.ResultsTable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is based on a Builder model to obtain selected statistics from an Imaris {@link cStatisticValues} object
 * It allows for selecting specific item IDs, stat names, channels and timepoints. Statistics are returned in the form
 * of a {@link ResultsTable} Multiple images are not directly supported, as we have never had a use for it.
 * See the example uses in the source code of {@link ch.epfl.biop.imaris.demo.GetStatisticsDemo}
 *
 * @author Olivier Burri
 * @version 0.1
 */
public class StatsQuery {
    private final String itemName;
    private List<Long> ids = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private List<String> timepoints = new ArrayList<>();
    private List<String> channels = new ArrayList<>();

    private ResultsTable results = new ResultsTable();
    private final cStatisticValues stats;
    private final int channelIdx, timeIdx, catIdx;
    private final List<String> firstColumns = Arrays.asList("Label", "Name", "ID", "Timepoint", "Category");

    /**
     * Constructor for getting selected statistics
     *
     * @param item the Imaris object from which we want statistics (Spots, Surfaces, ...)
     * @throws Error an Imaris Error Object
     */
    public StatsQuery(IDataItemPrx item) throws Error {

        // Heavy lifting here by Imaris to get all the statistics
        this.stats = item.GetStatistics();

        // Identify the position of factors we want to use
        this.channelIdx = Arrays.asList(this.stats.mFactorNames).indexOf("Channel");
        this.timeIdx = Arrays.asList(this.stats.mFactorNames).indexOf("Time");
        this.catIdx = Arrays.asList(this.stats.mFactorNames).indexOf("Category");
        this.itemName = item.GetName();
    }

    /**
     * Allows for the selection of a specific ID. Note that IDs are not necesarily continuous nor necesarily start at 0.
     * These are the IDs as per Imaris's ID value in the GUI
     *
     * @param id the ID to recover statistics from
     * @return
     */
    public StatsQuery selectId(final Integer id) {
        this.ids.add(id.longValue());
        return this;
    }

    /**
     * Will force StatsQuery to use the given ResultsTable and append to it
     * @param rt
     * @return
     */
    public StatsQuery resultsTable(ResultsTable rt) {
        this.results = rt;
        return this;
    }

    /**
     * Allows to set a list of IDs from which to get statistics from
     *
     * @param ids the list of spot or surface IDs to recover statistics from
     * @return
     */
    public StatsQuery selectIds(final List<Integer> ids) {
        this.ids.addAll(ids.stream().map(id -> id.longValue()).collect(Collectors.toList()));
        return this;
    }

    /**
     * Allows to select the name of the statistic to export. These are the same names as in the Imaris GUI **Minus** the
     * channel or image (eg. do not enter "Intensity Sum" Ch1=1 Img=1, just "Intensity Sum") Use {@link
     * StatsQuery#selectChannels(List)} and {@link StatsQuery#selectChannel(Integer)} to specify channels
     *
     * @param name the name of the statistic to recover as it appears in the Imaris GUI.
     * @return
     */
    public StatsQuery selectStatistic(final String name) {
        this.names.add(name);
        return this;
    }

    /**
     * Allows to set a list of IDs from which to get statistics from
     *
     * @param names the list of statistic names to recover as they appear in the Imaris GUI
     * @return
     */
    public StatsQuery selectStatistics(final List<String> names) {
        this.names.addAll(names);
        return this;
    }

    /**
     * Allows to select the timepoint of the statistics to export. 0-based
     * Careful. Imaris results are one-based for timepoints
     * @param timepoint the timepoint
     * @return
     */
    public StatsQuery selectTime(final Integer timepoint) {
        this.timepoints.add(timepoint.toString());
        return this;
    }

    /**
     * Allows to set a list of timepoints from which to get statistics from
     * Careful. Imaris results are one-based for timepoints
     * @param timepoints
     * @return
     */
    public StatsQuery selectTimes(final List<Integer> timepoints) {
        this.timepoints = timepoints.stream().map(t -> t.toString()).collect(Collectors.toList());
        return this;
    }

    /**
     * Allows to select the channel from which to get statistics from
     * Careful. Imaris results are one-based for channels
     * @param channel
     * @return
     */
    public StatsQuery selectChannel(Integer channel) {
        if (channel > 0) this.channels.add(channel.toString());
        return this;
    }

    /**
     * Allows to set a list of channels from which to get statistics from
     * Careful. Imaris results are one-based for channels
     * @param channels
     * @return
     */
    public StatsQuery selectChannels(final List<Integer> channels) {
        this.channels = channels.stream().map(c -> c.toString()).collect(Collectors.toList());
        return this;
    }

    /**
     * Allows appending results from a previous run
     *
     * @param results a results table from ImageJ or from a finished StatsQuery
     * @return
     */
    //TODO Ignore this
    public StatsQuery appendTo(ResultsTable results) {
        for (int i = 0; i < results.size(); i++) {
            this.results.incrementCounter();
            for (String c : results.getHeadings()) {
                this.results.addValue(c, results.getValue(c, i));
                // TODO allow for String results
            }
        }
        return this;
    }

    /**
     * Heavy lifting function that performs the requested operation and returns a table.
     * It is rather naive. It will go through each row of the raw Imaris statistics and see if that row matches
     * the names, channels and timepoints that were requested. If they all match, then we add them as a Map
     * We return a sorted results table by ID (Rows) and Column names, minus selected columns
     * NOTE: We ignore statistics without IDs (average values in Imaris) as we assume we can get them outside of Imaris
     * @return the resultsTable with all requested statistics
     * @throws Error
     */
    public ResultsTable get() throws Error {
        // identify what we need
        List<Integer> selectedIndexes = new ArrayList<>();
        // Stats are all about having a unique ID per row.
        // This means that channels should be appended to the ID
        // IDs change per timepoint, so no need for that
        Map<Long, Map<String, String>> statsById = new HashMap<>();

        // Name of object we are getting the statistics from
        String imageName = new File(EasyXT.getOpenImageName()).getName();
        for (int i = 0; i < this.stats.mIds.length; i++) {

            boolean matchesName, matchesChannel, matchesTime, matchesID;

            if (this.names.size() > 0) { // We have requested specific statistic names
                matchesName = false;
                for (String name : this.names) {
                    matchesName = this.stats.mNames[i].matches(name);
                    if (matchesName) break;
                }
            } else matchesName = true; // No specific names selected, make true for all

            if (this.channels.size() > 0) { // We have requested specific channels
                matchesChannel = false;
                for (String channel : this.channels) {
                    matchesChannel = this.stats.mFactors[channelIdx][i].matches(channel);
                    if (matchesChannel) break;
                }
            } else matchesChannel = true;

            if (this.timepoints.size() > 0) { // We have requested specific timepoints
                matchesTime = false;
                for (String time : this.timepoints) {
                    matchesTime = this.stats.mFactors[timeIdx][i].matches(time);
                    if (matchesTime) break;
                }
            } else matchesTime = true;

            if (this.ids.size() > 0) { // We have requested specific object IDs
                matchesID = false;
                for (Long id : this.ids) {
                    matchesID = this.stats.mIds[i] == id;
                    if (matchesID) break;
                }
            } else matchesID = true;

            // If we get a true for all these, then we can keep the statistic
            // Any "global" statistic has an ID of -1. We ignore these in favor of computing these outside Imaris
            if (matchesName && matchesChannel && matchesID && matchesTime && this.stats.mIds[i] != -1) {
                String name = stats.mNames[i];
                Float value = stats.mValues[i];
                String cat = this.stats.mFactors[catIdx][i];
                String channel = this.stats.mFactors[channelIdx][i];
                String time = this.stats.mFactors[timeIdx][i];
                long id = this.stats.mIds[i];

                // If it exists, use it and append more stats
                Map<String, String> statElements = (statsById.containsKey(id)) ? statsById.get(id) : new HashMap<>();

                // List all stats we want to add
                statElements.put("Label", imageName);
                statElements.put("ID", String.valueOf(id));
                statElements.put("Category", cat);
                statElements.put("Timepoint", time);
                statElements.put("Name", this.itemName);

                if (!channel.equals("")) name += " C" + channel;
                statElements.put(name, value.toString());

                // TODO : Check if this can be rewritten in a neater way as it is not necessary to 'put' again if it is already in statsByID
                // TODO: But because it checks if the ID is unique, the overhead is not much. Still ugly though.
                statsById.put(id, statElements);
            }
        }

        // Sort the Ids to have them in order
        Map<Long, Map<String, String>> statsByIdSorted = new ImarisIDComparator().sort(statsById);

        // Add all the results to the results table
        statsByIdSorted.forEach((uid, columns) -> {

            results.incrementCounter();

            // We want some order in the columns. Label, Name, ID, Timepoint, Category
            firstColumns.forEach(name -> {
                results.addValue(name, columns.get(name));
                columns.remove(name);
            });

            // Sort the remaining columns
            Map<String, String> columnsSorted = new ImarisColumnComparator().sort(columns);

            // Add all the columns
            columnsSorted.forEach((name, value) -> {
                if (isNumber(value))
                    results.addValue(name, Double.valueOf(value));
                else
                    results.addValue(name, value);
            });
        });
        return results;
    }

    /**
     * Convenience to check if we can parse the number or not
     * @param test the string to test
     * @return
     */
    private boolean isNumber(String test) {
        try {
            Double.valueOf(test);
            return true;
        } catch (NumberFormatException | NullPointerException ne) {
            return false;
        }
    }

    /**
     * Comparators to help sort the results. The first one compares by ID, the second one by column name
     */
    class ImarisIDComparator implements Comparator<Map.Entry<Long, Map<String, String>>> {

        @Override
        public int compare(Map.Entry<Long, Map<String, String>> o1, Map.Entry<Long, Map<String, String>> o2) {
            // Compare the keys
            return o1.getKey().compareTo(o2.getKey());
        }

        private Map<Long, Map<String, String>> sort(Map<Long, Map<String, String>> idMap) {
            // First create the List
            ArrayList<Map.Entry<Long, Map<String, String>>> idList = new ArrayList<Map.Entry<Long, Map<String, String>>>(idMap.entrySet());

            // Sort the list
            Collections.sort(idList, this);

            // Copying entries from List to Map
            Map<Long, Map<String, String>> sortedMap = new LinkedHashMap<>();
            for (Map.Entry<Long, Map<String, String>> entry : idList) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            // Finally return map
            return sortedMap;
        }
    }

    class ImarisColumnComparator implements Comparator<Map.Entry<String, String>> {

        @Override
        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
            // Compare the keys
            return o1.getKey().compareTo(o2.getKey());
        }

        private Map<String, String> sort(Map<String, String> columnMap) {
            // First create the List
            ArrayList<Map.Entry<String, String>> columnList = new ArrayList<Map.Entry<String, String>>(columnMap.entrySet());

            // Sort the list
            Collections.sort(columnList, this);

            // Copying entries from List to Map
            Map<String, String> sortedColumn = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : columnList) {
                sortedColumn.put(entry.getKey(), entry.getValue());
            }
            // Finally return map
            return sortedColumn;
        }
    }

}
