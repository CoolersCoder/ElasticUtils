// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package de.bytefish.elasticutils.elasticsearch2.client;



import de.bytefish.elasticutils.elasticsearch2.client.ElasticSearchClient;
import de.bytefish.elasticutils.elasticsearch2.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.elasticsearch2.client.bulk.options.BulkProcessingOptionsBuilder;
import de.bytefish.elasticutils.elasticsearch2.model.LocalWeatherData;
import de.bytefish.elasticutils.elasticsearch2.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.elasticsearch2.mapping.LocalWeatherDataMapper;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ElasticSearchClientTest {

    @Mock
    private Client client;

    @Mock
    private BulkProcessor.Listener bulkProcessorListener;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void one_bulk_insert_on_transport_client_when_bulk_action_threshold_is_reached() {

        // Configure the BulkProcessor to use:
        BulkProcessorConfiguration configuration = new BulkProcessorConfiguration(new BulkProcessingOptionsBuilder().build(), bulkProcessorListener);

        // And create a fake index builder:
        IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client, IndexAction.INSTANCE);

        // The mapping to use:
        IElasticSearchMapping localWeatherDataMapper = new LocalWeatherDataMapper();

        // Index to insert to:
        String indexName = "weather_data";

        // Initialize it with the default settings:
        when(client.settings())
                .thenReturn(Settings.builder().build());

        when(client.prepareIndex())
                .thenReturn(indexRequestBuilder);

        // Create the Test subject:
        ElasticSearchClient<LocalWeatherData> elasticSearchClient = new ElasticSearchClient<>(client, indexName, localWeatherDataMapper, configuration);

        // Create more entities, than Bulk insertion threshold:
        Stream<LocalWeatherData> entitiesStream = getData(configuration.getBulkProcessingOptions().getBulkActions() + 1).stream();

        // Index the Data:
        elasticSearchClient.index(entitiesStream);

        // Verify, that the TransportClient bulk insert has been called:
        verify(client, times(1)).bulk(anyObject(), anyObject());
        verify(bulkProcessorListener, times(1)).beforeBulk(anyLong(), anyObject());
    }

    @Test
    public void no_value_inserted_when_not_enough_requests() {

        // Create Mocks:
        Client mockedTransportClient = mock(Client.class);
        BulkProcessor.Listener mockedBulkProcessorListener = mock(BulkProcessor.Listener.class);

        // Configure the BulkProcessor to use:
        BulkProcessorConfiguration configuration = new BulkProcessorConfiguration(new BulkProcessingOptionsBuilder().build(), mockedBulkProcessorListener);

        // And create a fake index builder:
        IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(mockedTransportClient, IndexAction.INSTANCE);

        // The mapping to use:
        IElasticSearchMapping localWeatherDataMapper = new LocalWeatherDataMapper();

        // Index to insert to:
        String indexName = "weather_data";

        // Initialize it with the default settings:
        when(mockedTransportClient.settings())
                .thenReturn(Settings.builder().build());

        when(mockedTransportClient.prepareIndex())
                .thenReturn(indexRequestBuilder);

        // Create the Test subject:
        ElasticSearchClient<LocalWeatherData> elasticSearchClient = new ElasticSearchClient<>(mockedTransportClient, indexName, localWeatherDataMapper, configuration);

        // Create more entities, than Bulk insertion threshold:
        Stream<LocalWeatherData> entitiesStream = getData(configuration.getBulkProcessingOptions().getBulkActions() - 1).stream();

        // Index the Data:
        elasticSearchClient.index(entitiesStream);

        // Verify, that the TransportClient bulk insert has been called:
        verify(mockedTransportClient, times(0)).bulk(anyObject(), anyObject());
        verify(mockedBulkProcessorListener, times(0)).beforeBulk(anyLong(), anyObject());
    }

    @Test
    public void values_inserted_when_single_entity_is_written() {

        // Configure the BulkProcessor to use:
        BulkProcessorConfiguration configuration = new BulkProcessorConfiguration(new BulkProcessingOptionsBuilder().build(), bulkProcessorListener);

        // And create a fake index builder:
        IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client, IndexAction.INSTANCE);

        // The mapping to use:
        IElasticSearchMapping localWeatherDataMapper = new LocalWeatherDataMapper();

        // Index to insert to:
        String indexName = "weather_data";

        // Initialize it with the default settings:
        when(client.settings())
                .thenReturn(Settings.builder().build());

        when(client.prepareIndex())
                .thenReturn(indexRequestBuilder);

        // Create the Test subject:
        ElasticSearchClient<LocalWeatherData> elasticSearchClient = new ElasticSearchClient<>(client, indexName, localWeatherDataMapper, configuration);

        // Create more entities, than Bulk insertion threshold:
        LocalWeatherData entityToInsert = new LocalWeatherData();

        // Index the Data:
        elasticSearchClient.index(entityToInsert);

        // Flush the Thing:
        elasticSearchClient.flush();

        // Verify, that the TransportClient bulk insert has been called:
        verify(client, times(1)).bulk(anyObject(), anyObject());
        verify(bulkProcessorListener, times(1)).beforeBulk(anyLong(), anyObject());
    }

    private List<LocalWeatherData> getData(int numberOfEntities) {
        List<LocalWeatherData> entitiesToInsert = new ArrayList<>();

        for (int i = 0; i < numberOfEntities; i++) {
            entitiesToInsert.add(new LocalWeatherData());
        }

        return entitiesToInsert;
    }
}
