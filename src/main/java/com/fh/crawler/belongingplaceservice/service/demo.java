package com.fh.crawler.belongingplaceservice.service;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.ArrayList;
import java.util.List;

public class demo {
    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        list.add("123");

        System.out.println(list.toString());

        // Solr服务器URL
        String solrUrl = "http://127.0.0.1:8999/solr/SPX_DATA_PERSONINFO_shard5_replica1";

        // 使用HttpSolrClient创建连接
        try (SolrClient solrClient = new HttpSolrClient.Builder(solrUrl).build()) {
            // 创建一个SolrQuery对象
            SolrQuery query = new SolrQuery();

            // 设置查询条件
            query.setQuery("*:*"); // 查询所有文档，实际使用时应替换为具体的查询条件

            // 可以添加更多查询设置，例如排序、分页等
             query.setSort("APPTYPE", SolrQuery.ORDER.asc);
             query.setStart(0);
             query.setRows(20);

            // 执行查询并获取响应
            QueryResponse response = solrClient.query(query);

            // 获取查询结果
            SolrDocumentList documents = response.getResults();

            // 打印结果数量
            System.out.println("Found " + documents.getNumFound() + " documents");

            // 遍历结果并打印
            for (SolrDocument document : documents) {
                System.out.println(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

