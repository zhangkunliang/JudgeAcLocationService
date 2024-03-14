package com.fh.crawler.belongingplaceservice.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;

@Configuration
public class SolrConfig {
    @Value("${solr.overs.fields}")
    private String oversFields;

    @Value("${overs.host}")
    private String oversSolrHost;

    public String getOversFields() {
        return oversFields;
    }


    public String getOversSolrHost() {
        return oversSolrHost;
    }

    @Bean
    public SolrClient oversSolrClient() {
        return new HttpSolrClient.Builder(oversSolrHost).build();
    }

    @Bean
    public SolrTemplate oversSolrTemplate(SolrClient oversSolrClient) {
        return new SolrTemplate(oversSolrClient);
    }


    @Value("${solr.weibo.fields}")
    private String weiboFields;

    @Value("${weibo.host}")
    private String weiboSolrHost;

    public String getWeiboFields() {
        return weiboFields;
    }

    public String getWeiboSolrHost() {
        return weiboSolrHost;
    }

    @Bean
    public SolrClient weiboSolrClient() {
        return new HttpSolrClient.Builder(weiboSolrHost).build();
    }

    @Bean
    public SolrTemplate weiboSolrTemplate(SolrClient weiboSolrClient) {
        return new SolrTemplate(weiboSolrClient);
    }

    @Value("${solr.media.fields}")
    private String mediaFields;
    @Value("${media.host}")
    private String mediaSolrHost;

    public String getMediaFields() {
        return mediaFields;
    }

    public String getMediaSolrHost() {
        return mediaSolrHost;
    }

    @Bean
    public SolrClient mediaSolrClient() {
        return new HttpSolrClient.Builder(mediaSolrHost).build();
    }

    @Bean
    public SolrTemplate mediaSolrTemplate(SolrClient mediaSolrClient) {
        return new SolrTemplate(mediaSolrClient);
    }

    @Value("${source.solr.url}")
    private String sourceSolrUrl;

    @Bean
    public SolrClient sourceSolrClient() {
        return new HttpSolrClient.Builder(sourceSolrUrl).build();
    }

    @Bean
    public SolrTemplate sourceSolrTemplate(SolrClient sourceSolrClient) {
        return new SolrTemplate(sourceSolrClient);
    }


    @Value("${destination.solr.url}")
    private String destinationSolrUrl;

    @Bean
    public SolrClient destinationSolrClient() {
        return new HttpSolrClient.Builder(destinationSolrUrl).build();
    }

    @Bean
    public SolrTemplate destinationSolrTemplate(SolrClient destinationSolrClient) {
        return new SolrTemplate(destinationSolrClient);
    }


}
