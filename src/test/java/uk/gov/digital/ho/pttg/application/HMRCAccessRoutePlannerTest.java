package uk.gov.digital.ho.pttg.application;


import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HMRCAccessRoutePlannerTest {

    @Test
    public void shouldProxyHMRCAccessRequests() throws HttpException {

        HttpHost proxy = new HttpHost("test.proxy");

        HMRCAccessProxyRoutePlanner planner = new HMRCAccessProxyRoutePlanner(
                proxy, "test.hmrc.gov.uk");

        HttpHost result = planner.determineProxy(new HttpHost("test.hmrc.gov.uk"),
                new BasicHttpRequest("POST", "/api"),
                new BasicHttpContext());

        assertThat(result).isEqualTo(proxy);
    }

    @Test
    public void shouldNotProxyAnyOtherRequests() throws HttpException {

        HttpHost proxy = new HttpHost("test.proxy");

        HMRCAccessProxyRoutePlanner planner = new HMRCAccessProxyRoutePlanner(
                proxy, "not.a.hmrc.gov");

        HttpHost result = planner.determineProxy(new HttpHost("test.hmrc.gov.uk"),
                new BasicHttpRequest("POST", "/api"),
                new BasicHttpContext());

        assertThat(result).isNull();

    }
}
