package com.fluxtream.connectors.zeo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.domain.metadata.DayMetadataFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;
import com.fluxtream.services.MetadataService;

@Component
public class ZeoSleepStatsFacetExtractor extends AbstractFacetExtractor {

	@Autowired
	MetadataService metadataService;

	public List<AbstractFacet> extractFacets(ApiData apiData,
			ObjectType objectType) {
		List<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		JSONObject sleepStatsResponse = JSONObject.fromObject(apiData.json);

		JSONObject response = sleepStatsResponse.getJSONObject("response");

		if (response.has("sleepRecords")) {

			JSONObject sleepRecordsWrapper = response
					.getJSONObject("sleepRecords");

			JSONArray sleepStatsArray = sleepRecordsWrapper
					.getJSONArray("sleepRecord");
			for (int i = 0; i < sleepStatsArray.size(); i++) {
				extractStatsData(facets, apiData,
						sleepStatsArray.getJSONObject(i));
			}

		} else if (response.has("sleepStats")) {
			extractStatsData(facets, apiData,
					response.getJSONObject("sleepStats"));
		}

		return facets;
	}

	private void extractStatsData(List<AbstractFacet> facets, ApiData apiData,
			JSONObject sleepStats) {
		ZeoSleepStatsFacet facet = new ZeoSleepStatsFacet();
		super.extractCommonFacetData(facet, apiData);
		facet.zq = sleepStats.getInt("zq");
		facet.bedTime = getZeoTime(sleepStats, "bedTime", apiData, facet);
		facet.riseTime = getZeoTime(sleepStats, "riseTime",
				apiData, facet);
		facet.start = facet.bedTime.getTime();
		facet.end = facet.riseTime.getTime();
		facet.morningFeel = sleepStats.getInt("morningFeel");
		facet.totalZ = sleepStats.getInt("totalZ");
		facet.timeInDeepPercentage = sleepStats.getInt("timeInDeepPercentage");
		facet.timeInLightPercentage = sleepStats
				.getInt("timeInLightPercentage");
		facet.timeInRemPercentage = sleepStats.getInt("timeInRemPercentage");
		facet.timeInWakePercentage = sleepStats.getInt("timeInWakePercentage");
		facet.awakenings = sleepStats.getInt("awakenings");
		facet.timeToZ = sleepStats.getInt("timeToZ");
		facet.sleepGraph = getSleepGraph(sleepStats);
		facets.add(facet);
	}
	
	private enum Phase {
		UNDEFINED, WAKE, REM, LIGHT, DEEP;
	}

	private String getSleepGraph(JSONObject sleepStats) {
		JSONArray sleepGraphArray = sleepStats.getJSONArray("sleepGraph");
		StringBuffer bf = new StringBuffer();
		for (int i=0; i<sleepGraphArray.size(); i++) {
			String phaseString = sleepGraphArray.getString(i);
			Phase phase = Enum.valueOf(Phase.class, phaseString);
			bf.append(String.valueOf(phase.ordinal()));
		}
		return bf.toString();
	}

	private Date getZeoTime(JSONObject stats, String key, ApiData apiData, ZeoSleepStatsFacet facet) {
		JSONObject o = stats.getJSONObject(key);
		
		int day = o.getInt("day");
		int month = o.getInt("month");
		int year = o.getInt("year");
		int hours = o.getInt("hour");
		int minutes = o.getInt("minute");
		int seconds = o.getInt("second");
		
		StringBuffer date = new StringBuffer(String.valueOf(year)).append("-")
				.append(String.valueOf(month)).append("-")
				.append(String.valueOf(day));
		TimeZone guestTimeZoneForDate = getGuestTimeZoneForDate(apiData,
				date.toString());
		Calendar c = Calendar.getInstance(guestTimeZoneForDate);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.SECOND, seconds);
		c.set(Calendar.YEAR, year);
		
		if (key.equals("bedTime"))
			facet.endTimeStorage = toTimeStorage(year, month, day, hours, minutes, seconds);
		else
			facet.startTimeStorage = toTimeStorage(year, month, day, hours, minutes, seconds);
		
		return c.getTime();
	}

	private String toTimeStorage(int year, int month, int day, int hours,
			int minutes, int seconds) {
		//yyyy-MM-dd'T'HH:mm:ss.SSS
		StringBuffer sb = new StringBuffer();
		sb.append(year).append("-")
			.append(year).append("-")
			.append(month).append("-")
			.append(day).append("T")
			.append(hours).append(":")
			.append(minutes).append(":")
			.append(seconds).append(".")
			.append("000");
		return null;
	}
	
	private TimeZone getGuestTimeZoneForDate(ApiData apiData, String date) {
		DayMetadataFacet dailyContextualInfo = metadataService
				.getDayMetadata(apiData.updateInfo.getGuestId(),
						date, true);
		return TimeZone.getTimeZone(dailyContextualInfo.timeZone);
	}

}