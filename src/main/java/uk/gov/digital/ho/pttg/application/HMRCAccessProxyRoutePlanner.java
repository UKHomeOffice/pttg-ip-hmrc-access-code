package uk.gov.digital.ho.pttg.application;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;

@Slf4j
public class HMRCAccessProxyRoutePlanner extends DefaultProxyRoutePlanner {
    private String hostToProxy;

    public HMRCAccessProxyRoutePlanner(HttpHost proxy, String hostToProxy) {
        super(proxy);
        this.hostToProxy = hostToProxy;
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context)
            throws HttpException {

        if (target.getHostName().equals(hostToProxy)) {
            return super.determineProxy(target, request, context);
        }

        return null;
    }
}
