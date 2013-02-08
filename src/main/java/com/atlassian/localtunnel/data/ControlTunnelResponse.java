package com.atlassian.localtunnel.data;

public class ControlTunnelResponse
{
    private TunnelInfo control;

    public String getHost()
    {
        return control.getHost();
    }

    public String getBanner()
    {
        return control.getBanner();
    }

    public Integer getConcurrency()
    {
        return control.getConcurrency();
    }
    
    public static class TunnelInfo
    {
        private String host;
        private String banner;
        private Integer concurrency;

        public String getHost()
        {
            return host;
        }

        public String getBanner()
        {
            return banner;
        }

        public Integer getConcurrency()
        {
            return concurrency;
        }
    }
}
