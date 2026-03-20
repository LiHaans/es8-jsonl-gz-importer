package com.github.lihaans.esimporter.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.github.lihaans.esimporter.config.JobConfig;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ElasticsearchClientFactory {
    public ElasticsearchClient create(JobConfig config) {
        List<HttpHost> hosts = new ArrayList<HttpHost>();
        for (String host : config.getEsHosts()) {
            hosts.add(HttpHost.create(host));
        }

        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]));

        if (config.getApiKey() != null && !config.getApiKey().trim().isEmpty()) {
            Header[] headers = new Header[]{
                    new BasicHeader("Authorization", "ApiKey " + config.getApiKey().trim())
            };
            builder.setDefaultHeaders(headers);
        } else if (config.getUsername() != null && config.getPassword() != null) {
            String token = Base64.getEncoder().encodeToString((config.getUsername() + ":" + config.getPassword()).getBytes(StandardCharsets.UTF_8));
            Header[] headers = new Header[]{
                    new BasicHeader("Authorization", "Basic " + token)
            };
            builder.setDefaultHeaders(headers);
        }

        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
