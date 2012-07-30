package com.fluxtream.connectors.quantifiedmind;

import java.util.Date;
import com.fluxtream.TimeInterval;
import com.fluxtream.connectors.picasa.PicasaPhotoFacet;
import com.fluxtream.connectors.vos.AbstractFacetVO;
import com.fluxtream.connectors.vos.AbstractInstantFacetVO;
import com.fluxtream.connectors.vos.AbstractPhotoFacetVO;
import com.fluxtream.domain.GuestSettings;

/**
 *
 * @author Candide Kemmler (candide@fluxtream.com)
 */
public class QuantifiedMindTestFacetVO extends AbstractInstantFacetVO<QuantifiedMindTestFacet> {

    public String test_name;
    public String result_name;
    public long session_timestamp;
    public double result_value;

    @Override
    protected void fromFacet(final QuantifiedMindTestFacet facet, final TimeInterval timeInterval, final GuestSettings settings) {
        startMinute = toMinuteOfDay(new Date(facet.start), timeInterval.timeZone);
        this.start = facet.start;
        this.test_name = facet.test_name;
        this.result_name = facet.result_name;
        this.session_timestamp = facet.session_timestamp;
        this.result_value = facet.result_value;
    }

}
